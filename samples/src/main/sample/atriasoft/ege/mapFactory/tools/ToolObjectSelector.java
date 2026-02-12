package sample.atriasoft.ege.mapFactory.tools;

import org.atriasoft.ege.Entity;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.geometry.Ray;
import org.atriasoft.etk.Color;
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
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sample.atriasoft.ege.mapFactory.EgeScene;
import sample.atriasoft.ege.mapFactory.model.Map;

public class ToolObjectSelector implements MapToolInterface {
	final static private Logger LOGGER = LoggerFactory.getLogger(ToolObjectSelector.class);

	private final ResourceColored3DObject dynamicElement;

	private EgeScene sceneRef = null;
	private Entity selectedEntity = null;
	private ComponentPosition selectedPosition = null;
	private float objectRotation = 0.0f;

	public ToolObjectSelector() {
		this.dynamicElement = ResourceColored3DObject.create();
	}

	private static void onRotationChanged(final ToolObjectSelector self, final Float value) {
		self.objectRotation = value;
		LOGGER.debug("Object rotation: {}", value);
		if (self.selectedEntity != null && self.selectedPosition != null) {
			final Transform3D current = self.selectedPosition.getTransform();
			final Quaternion rot = Quaternion.fromEulerAngles(
					new Vector3f(0, 0, value * (float) Math.PI / 180.0f));
			self.selectedPosition.setTransform(new Transform3D(current.position(), rot));
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

		return mainSizer;
	}

	@Override
	public void onDraw(final Map map) {
		// Draw selection indicator around selected entity
		if (this.selectedEntity != null && this.selectedPosition != null) {
			final Vector3f pos = this.selectedPosition.getTransform().position();
			final Transform3D selTransform = new Transform3D(pos);
			this.dynamicElement.drawSphere(1.2f, 12, 12,
					selTransform.getOpenGLMatrix(), Color.YELLOW.withA(0.3f));
		}
	}

	@Override
	public boolean onEventEntry(final EventEntry event, final Map map, final EgeScene widget) {
		// Delete selected entity with backspace or delete key
		if (event.type() == KeyKeyboard.CHARACTER && event.status() == KeyStatus.down) {
			final Character ch = event.getChar();
			if (ch != null && (ch == '\b' || ch == '\u007f')) {
				if (this.selectedEntity != null && this.sceneRef != null) {
					this.sceneRef.getEnvironement().rmEntity(this.selectedEntity);
					map.placedEntities.remove(this.selectedEntity);
					this.selectedEntity = null;
					this.selectedPosition = null;
					LOGGER.info("Deleted selected entity");
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

		// Select object on left click
		if (event.inputId() == 1 && event.status() == KeyStatus.pressSingle) {
			final Entity found = findEntityAtRay(mouseRay, map);
			if (found != null) {
				this.selectedEntity = found;
				this.selectedPosition = (ComponentPosition) found.getComponent("position");
				LOGGER.info("Selected entity at {}", this.selectedPosition.getTransform().position());
			} else {
				this.selectedEntity = null;
				this.selectedPosition = null;
			}
			return true;
		}

		// Rotate with scroll wheel while holding Ctrl
		if ((event.inputId() == 4 || event.inputId() == 5) && event.status() == KeyStatus.down
				&& event.specialKey() != null && event.specialKey().getCtrlLeft()) {
			if (this.selectedEntity != null && this.selectedPosition != null) {
				final float delta = (event.inputId() == 4) ? 15.0f : -15.0f;
				this.objectRotation = (this.objectRotation + delta + 360.0f) % 360.0f;
				final Transform3D current = this.selectedPosition.getTransform();
				final Quaternion rot = Quaternion.fromEulerAngles(
						new Vector3f(0, 0, this.objectRotation * (float) Math.PI / 180.0f));
				this.selectedPosition.setTransform(new Transform3D(current.position(), rot));
			}
			return true;
		}

		return false;
	}

	private Entity findEntityAtRay(final Ray ray, final Map map) {
		Entity nearest = null;
		float nearestDist = Float.MAX_VALUE;

		for (final Entity entity : map.placedEntities) {
			final ComponentPosition pos = (ComponentPosition) entity.getComponent("position");
			if (pos == null) {
				continue;
			}
			final Vector3f entityPos = pos.getTransform().position();
			if (ray.intersectSphere(entityPos, 1.0f)) {
				final float dist = ray.origin().less(entityPos).length();
				if (dist < nearestDist) {
					nearestDist = dist;
					nearest = entity;
				}
			}
		}
		return nearest;
	}
}
