package sample.atriasoft.ege.mapFactory.tools;

import java.util.List;
import java.util.function.Consumer;

import org.atriasoft.ege.Entity;
import org.atriasoft.ege.components.ComponentMesh;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentPostProcess;
import org.atriasoft.ege.geometry.Ray;
import org.atriasoft.ege.postprocess.AdditiveOverlayEffect;
import org.atriasoft.ege.postprocess.OutlineEffect;
import org.atriasoft.etk.Color;
import org.atriasoft.loader3d.resources.ResourceMesh;
import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.widget.Label;
import org.atriasoft.ewol.widget.Sizer;
import org.atriasoft.ewol.widget.Slider;
import org.atriasoft.ewol.widget.Widget;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sample.atriasoft.ege.mapFactory.ApplScene;
import sample.atriasoft.ege.mapFactory.DeleteEntityAction;
import sample.atriasoft.ege.mapFactory.EgeScene;
import sample.atriasoft.ege.mapFactory.TransformEntityAction;
import sample.atriasoft.ege.mapFactory.model.Map;

public class ToolObjectSelector implements MapToolInterface {
	final static private Logger LOGGER = LoggerFactory.getLogger(ToolObjectSelector.class);

	// Highlight configuration (in pixels, screen-space)
	private static final float SELECTION_OUTLINE_WIDTH = 6.0f;
	private static final float HOVER_OUTLINE_WIDTH = 3.0f;
	private static final Color SELECTION_BORDER_COLOR = new Color(0.0f, 0.0f, 0.0f, 1.0f);
	private static final Color HOVER_BORDER_COLOR = new Color(1.0f, 0.5f, 0.0f, 1.0f);
	private static final Color SELECTION_ADD = new Color(1.0f, 1.0f, 1.0f, 0.25f);
	private static final Color HOVER_ADD = new Color(1.0f, 1.0f, 1.0f, 0.15f);

	private EgeScene sceneRef = null;
	private Consumer<Entity> onSelectionChanged = null;
	private Entity selectedEntity = null;
	private ComponentPosition selectedPosition = null;
	private Entity hoveredEntity = null;
	private float objectRotation = 0.0f;
	private float objectScale = 1.0f;
	private Slider scaleSlider = null;

	// Drag state
	private boolean isDragging = false;
	private Transform3D dragStartTransform = null;

	// Undo tracking for rotation/scale edits
	private Transform3D transformBeforeEdit = null;

	private static void onRotationChanged(final ToolObjectSelector self, final Float value) {
		self.objectRotation = value;
		LOGGER.debug("Object rotation: {}", value);
		if (self.selectedEntity != null && self.selectedPosition != null) {
			if (self.transformBeforeEdit == null) {
				self.transformBeforeEdit = self.selectedPosition.getTransform();
			}
			final Transform3D current = self.selectedPosition.getTransform();
			final Quaternion rot = Quaternion.fromEulerAngles(
					new Vector3f(0, 0, value * (float) Math.PI / 180.0f));
			self.selectedPosition.setTransform(new Transform3D(current.position(), rot, current.scale()));
		}
	}

	private static void onScaleChanged(final ToolObjectSelector self, final Float value) {
		self.objectScale = value;
		LOGGER.debug("Object scale: {}", value);
		if (self.selectedEntity != null && self.selectedPosition != null) {
			if (self.transformBeforeEdit == null) {
				self.transformBeforeEdit = self.selectedPosition.getTransform();
			}
			final Transform3D current = self.selectedPosition.getTransform();
			self.selectedPosition.setTransform(
					current.withScale(new Vector3f(value, value, value)));
		}
	}

	private void flushPendingTransformAction() {
		if (this.transformBeforeEdit != null && this.selectedPosition != null && this.sceneRef instanceof final ApplScene applScene) {
			final Transform3D after = this.selectedPosition.getTransform();
			if (!this.transformBeforeEdit.equals(after)) {
				final TransformEntityAction action = new TransformEntityAction(this.selectedPosition, this.transformBeforeEdit, after);
				applScene.getUndoManager().execute(action);
			}
			this.transformBeforeEdit = null;
		}
	}

	@Override
	public Widget getWidget() {
		final Sizer mainSizer = Sizer.vertical().expand(true, true).fill(true, true);

		// Title
		mainSizer.subWidgetAdd(Label.create("Object Selection").expand(true, false).fill(true, false));

		// Rotation slider
		mainSizer.subWidgetAdd(Label.create("Rotation:").expand(true, false).fill(true, false));

		final Slider rotationSlider = Slider.create().min(0.0f).max(360.0f).value(this.objectRotation)
				.expand(true, false).fill(true, false);
		rotationSlider.signalValue.connectAuto(this, ToolObjectSelector::onRotationChanged);
		mainSizer.subWidgetAdd(rotationSlider);

		// Scale slider
		mainSizer.subWidgetAdd(Label.create("Scale:").expand(true, false).fill(true, false));

		this.scaleSlider = Slider.create().min(0.1f).max(5.0f).value(this.objectScale)
				.expand(true, false).fill(true, false);
		this.scaleSlider.signalValue.connectAuto(this, ToolObjectSelector::onScaleChanged);
		mainSizer.subWidgetAdd(this.scaleSlider);

		return mainSizer;
	}

	@Override
	public void onDeactivate(final EgeScene widget) {
		flushPendingTransformAction();
		// Remove selection highlight
		if (this.selectedEntity != null) {
			this.selectedEntity.removeComponent(ComponentPostProcess.COMPONENT_NAME);
			this.selectedEntity = null;
			this.selectedPosition = null;
		}
		// Remove hover highlight
		if (this.hoveredEntity != null) {
			this.hoveredEntity.removeComponent(ComponentPostProcess.COMPONENT_NAME);
			this.hoveredEntity = null;
		}
		this.isDragging = false;
		this.dragStartTransform = null;
		this.sceneRef = null;
	}

	@Override
	public void onDraw(final Map map) {
		// Post-process effects are now handled by EnginePostProcess automatically.
		// No manual drawing needed.
	}

	@Override
	public boolean onEventEntry(final EventEntry event, final Map map, final EgeScene widget) {
		// Delete selected entity with backspace or delete key
		if (event.type() == KeyKeyboard.CHARACTER && event.status() == KeyStatus.down) {
			final Character ch = event.getChar();
			if (ch != null && (ch == '\b' || ch == '\u007f')) {
				if (this.selectedEntity != null && this.sceneRef != null) {
					flushPendingTransformAction();
					if (this.sceneRef instanceof final ApplScene applScene) {
						final String meshPath = map.entityMeshPaths.get(this.selectedEntity);
						final Transform3D transform = this.selectedPosition.getTransform();
						final DeleteEntityAction action = new DeleteEntityAction(
								this.selectedEntity, meshPath, transform, map, applScene);
						// Remove highlight before executing
						this.selectedEntity.removeComponent(ComponentPostProcess.COMPONENT_NAME);
						applScene.getUndoManager().execute(action);
					}
					this.selectedEntity = null;
					this.selectedPosition = null;
					this.hoveredEntity = null;
					this.isDragging = false;
					this.dragStartTransform = null;
					LOGGER.info("Deleted selected entity");
					if (this.onSelectionChanged != null) {
						this.onSelectionChanged.accept(null);
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEventInput(final EventInput event, final Map map, final EgeScene widget) {
		this.sceneRef = widget;

		final Vector2f globalPos = event.pos();
		final Vector2f relPos = widget.relativePosition(globalPos);

		// Ray-cast
		final Ray mouseRay = widget.mainView.getRayFromScreen(widget.projection, widget.getSize(), relPos);

		// Drag move while button held
		if (event.inputId() == 1 && event.status() == KeyStatus.move && this.isDragging) {
			if (this.selectedPosition != null) {
				final Vector3f newPos = map.ground.intersectRay(mouseRay);
				if (newPos != null) {
					final Transform3D current = this.selectedPosition.getTransform();
					this.selectedPosition.setTransform(
							new Transform3D(newPos, current.orientation(), current.scale()));
				}
			}
			return true;
		}

		// End drag on mouse up
		if (event.inputId() == 1 && (event.status() == KeyStatus.up || event.status() == KeyStatus.upAfter)) {
			if (this.isDragging && this.dragStartTransform != null && this.selectedPosition != null) {
				final Transform3D after = this.selectedPosition.getTransform();
				if (!this.dragStartTransform.position().equals(after.position())) {
					if (widget instanceof final ApplScene applScene) {
						final TransformEntityAction action = new TransformEntityAction(
								this.selectedPosition, this.dragStartTransform, after);
						applScene.getUndoManager().execute(action);
					}
				}
			}
			this.isDragging = false;
			this.dragStartTransform = null;
			return true;
		}

		// Update hover on every mouse event
		if (!this.isDragging) {
			final Entity newHovered = findEntityAtRay(mouseRay, map);
			updateHover(newHovered);
		}

		// Select object on left click + start drag if entity under cursor
		if (event.inputId() == 1 && event.status() == KeyStatus.pressSingle) {
			final Entity newHovered = findEntityAtRay(mouseRay, map);
			selectEntity(newHovered);
			if (this.selectedEntity != null && this.selectedPosition != null) {
				this.isDragging = true;
				this.dragStartTransform = this.selectedPosition.getTransform();
			}
			return true;
		}

		// Rotate with scroll wheel while holding Ctrl
		if ((event.inputId() == 4 || event.inputId() == 5) && event.status() == KeyStatus.down
				&& event.specialKey() != null && event.specialKey().getCtrlLeft()) {
			if (this.selectedEntity != null && this.selectedPosition != null) {
				if (this.transformBeforeEdit == null) {
					this.transformBeforeEdit = this.selectedPosition.getTransform();
				}
				final float delta = (event.inputId() == 4) ? 15.0f : -15.0f;
				this.objectRotation = (this.objectRotation + delta + 360.0f) % 360.0f;
				final Transform3D current = this.selectedPosition.getTransform();
				final Quaternion rot = Quaternion.fromEulerAngles(
						new Vector3f(0, 0, this.objectRotation * (float) Math.PI / 180.0f));
				this.selectedPosition.setTransform(new Transform3D(current.position(), rot, current.scale()));
			}
			return true;
		}

		// Scale with scroll wheel while holding Shift
		if ((event.inputId() == 4 || event.inputId() == 5) && event.status() == KeyStatus.down
				&& event.specialKey() != null && event.specialKey().getShiftLeft()) {
			if (this.selectedEntity != null && this.selectedPosition != null) {
				if (this.transformBeforeEdit == null) {
					this.transformBeforeEdit = this.selectedPosition.getTransform();
				}
				final float delta = (event.inputId() == 4) ? 0.1f : -0.1f;
				this.objectScale = Math.max(0.1f, Math.min(5.0f, this.objectScale + delta));
				final Transform3D current = this.selectedPosition.getTransform();
				this.selectedPosition.setTransform(
						current.withScale(new Vector3f(this.objectScale, this.objectScale, this.objectScale)));
				if (this.scaleSlider != null) {
					this.scaleSlider.value(this.objectScale);
				}
			}
			return true;
		}

		return false;
	}

	private void updateHover(final Entity newHovered) {
		if (newHovered == this.hoveredEntity) {
			return; // no change
		}
		// Remove hover highlight from old entity (if it's not selected)
		if (this.hoveredEntity != null && this.hoveredEntity != this.selectedEntity) {
			this.hoveredEntity.removeComponent(ComponentPostProcess.COMPONENT_NAME);
		}
		this.hoveredEntity = newHovered;
		// Add hover highlight to new entity (if it's not selected)
		if (this.hoveredEntity != null && this.hoveredEntity != this.selectedEntity) {
			this.hoveredEntity.addComponent(new ComponentPostProcess()
					.addEffect(new OutlineEffect(HOVER_OUTLINE_WIDTH, HOVER_BORDER_COLOR))
					.addEffect(new AdditiveOverlayEffect(HOVER_ADD)));
		}
	}

	public void setOnSelectionChanged(final Consumer<Entity> callback) {
		this.onSelectionChanged = callback;
	}

	public void selectEntity(final Entity entity) {
		// Flush any pending rotation/scale edits as undo action
		flushPendingTransformAction();

		// Remove old selection highlight
		if (this.selectedEntity != null) {
			this.selectedEntity.removeComponent(ComponentPostProcess.COMPONENT_NAME);
		}
		// Remove hover highlight from the entity we're about to select
		if (entity != null && entity == this.hoveredEntity && entity != this.selectedEntity) {
			entity.removeComponent(ComponentPostProcess.COMPONENT_NAME);
		}

		if (entity != null) {
			this.selectedEntity = entity;
			this.selectedPosition = (ComponentPosition) entity.getComponent("position");
			LOGGER.info("Selected entity at {}", this.selectedPosition.getTransform().position());
			// Add selection highlight
			this.selectedEntity.addComponent(new ComponentPostProcess()
					.addEffect(new OutlineEffect(SELECTION_OUTLINE_WIDTH, SELECTION_BORDER_COLOR))
					.addEffect(new AdditiveOverlayEffect(SELECTION_ADD)));
			// Read the entity's current scale and update the slider
			final Vector3f currentScale = this.selectedPosition.getTransform().scale();
			this.objectScale = currentScale.x();
			if (this.scaleSlider != null) {
				this.scaleSlider.value(this.objectScale);
			}
		} else {
			this.selectedEntity = null;
			this.selectedPosition = null;
		}
		if (this.onSelectionChanged != null) {
			this.onSelectionChanged.accept(this.selectedEntity);
		}
	}

	private Entity findEntityAtRay(final Ray ray, final Map map) {
		Entity nearest = null;
		float nearestDist = Float.MAX_VALUE;

		for (final Entity entity : map.placedEntities) {
			final ComponentPosition posComp = (ComponentPosition) entity.getComponent("position");
			final ComponentMesh meshComp = (ComponentMesh) entity.getComponent("mesh");
			if (posComp == null || meshComp == null) {
				continue;
			}
			if (!(meshComp.getMesh() instanceof final ResourceMesh resourceMesh)) {
				continue;
			}
			final Transform3D transform = posComp.getTransform();
			final List<Vector3f> vertices = resourceMesh.getGeneratedPosition();
			if (vertices == null || vertices.size() < 3) {
				continue;
			}
			// Test ray against each triangle (vertices are triplets)
			for (int i = 0; i + 2 < vertices.size(); i += 3) {
				final Vector3f v0 = transform.multiply(vertices.get(i));
				final Vector3f v1 = transform.multiply(vertices.get(i + 1));
				final Vector3f v2 = transform.multiply(vertices.get(i + 2));
				final Vector3f hit = ray.intersectTriangle(v0, v1, v2);
				if (hit != null) {
					final float dist = ray.origin().less(hit).length();
					if (dist < nearestDist) {
						nearestDist = dist;
						nearest = entity;
					}
				}
			}
		}
		return nearest;
	}
}
