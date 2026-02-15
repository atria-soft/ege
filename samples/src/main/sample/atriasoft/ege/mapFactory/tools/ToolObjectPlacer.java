package sample.atriasoft.ege.mapFactory.tools;

import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.components.ComponentMesh;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderMeshPalette;
import org.atriasoft.ege.components.ComponentTexturePalette;
import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.ege.geometry.Ray;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.widget.Label;
import org.atriasoft.ewol.widget.Sizer;
import org.atriasoft.ewol.widget.Widget;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sample.atriasoft.ege.mapFactory.EgeScene;
import sample.atriasoft.ege.mapFactory.model.Map;

public class ToolObjectPlacer implements MapToolInterface {
	final static private Logger LOGGER = LoggerFactory.getLogger(ToolObjectPlacer.class);

	private static final String MESH_DIRECTORY = System.getProperty("user.home") + "/mesh";

	private String selectedMeshFile = null;

	private Vector3f positionRay = null;
	private final ResourceColored3DObject previewElement;

	private EgeScene sceneRef = null;
	private Entity previewEntity = null;
	private ComponentPosition previewPosition = null;
	private String previewMeshFile = null;

	public ToolObjectPlacer() {
		this.previewElement = ResourceColored3DObject.create();
	}

	private static void onMeshFileValidated(final ToolObjectPlacer self, final String filePath) {
		if (filePath != null && filePath.endsWith(".emf")) {
			self.selectedMeshFile = filePath;
			LOGGER.info("Validated mesh file: {}", filePath);
			self.updatePreviewEntity();
		}
	}

	private void updatePreviewEntity() {
		if (this.sceneRef == null || this.selectedMeshFile == null) {
			return;
		}
		// Already loaded this mesh as preview
		if (this.selectedMeshFile.equals(this.previewMeshFile)) {
			return;
		}
		final Environement env = this.sceneRef.getEnvironement();
		// Remove old preview entity
		if (this.previewEntity != null) {
			env.rmEntity(this.previewEntity);
			this.previewEntity = null;
		}
		// Create new preview entity with EMF mesh
		this.previewEntity = new Entity(env);
		this.previewPosition = new ComponentPosition(Transform3D.IDENTITY);
		this.previewEntity.addComponent(this.previewPosition);
		final Uri meshUri = new Uri("FILE", this.selectedMeshFile);
		this.previewEntity.addComponent(new ComponentMesh(meshUri));
		this.previewEntity.addComponent(new ComponentTexturePalette(meshUri));
		this.previewEntity.addComponent(new ComponentRenderMeshPalette(
				new Uri("DATA", "basicPalette.vert"),
				new Uri("DATA", "basicPalette.frag"),
				(EngineLight) env.getEngine(EngineLight.ENGINE_NAME)));
		env.addEntity(this.previewEntity);
		this.previewMeshFile = this.selectedMeshFile;
	}

	@Override
	public void onDeactivate(final EgeScene widget) {
		if (this.previewEntity != null && widget != null) {
			widget.getEnvironement().rmEntity(this.previewEntity);
			this.previewEntity = null;
			this.previewPosition = null;
			this.previewMeshFile = null;
		}
		this.positionRay = null;
	}

	@Override
	public Widget getWidget() {
		final Sizer mainSizer = Sizer.vertical().expand(true, true).fill(true, true);

		// Title
		mainSizer.subWidgetAdd(Label.create("Place Objects").expand(true, false).fill(true, false));

		// Mesh file list from ~/mesh/
		mainSizer.subWidgetAdd(Label.create("Mesh files (" + MESH_DIRECTORY + "):").expand(true, false).fill(true, false));

		final MeshFileList meshList = MeshFileList.create(MESH_DIRECTORY);
		meshList.showFiles(true).showFolders(false).showHidden(false);
		meshList.expand(true, true).fill(true, true);
		meshList.maxSizePixelY(300);
		meshList.signalFileSelect.connectAuto(this, ToolObjectPlacer::onMeshFileValidated);
		meshList.signalFileValidate.connectAuto(this, ToolObjectPlacer::onMeshFileValidated);
		mainSizer.subWidgetAdd(meshList);

		return mainSizer;
	}

	@Override
	public void onDraw(final Map map) {
		if (this.positionRay != null) {
			// Draw preview of object placement
			final Transform3D transform = new Transform3D(this.positionRay);
			final float previewSize = 0.5f;

			// Draw a simple sphere as placeholder preview
			this.previewElement.drawSphere(previewSize, 12, 12, transform.getOpenGLMatrix(), Color.GREEN.withA(0.5f));
		}
	}

	@Override
	public boolean onEventEntry(final EventEntry event, final Map map, final EgeScene widget) {
		return false;
	}

	@Override
	public boolean onEventInput(final EventInput event, final Map map, final EgeScene widget) {
		this.sceneRef = widget;

		final Vector2f globalPos = event.pos();
		final Vector2f relPos = widget.relativePosition(globalPos);

		// Ray-cast on the heightmap terrain
		final Ray mouseRay = widget.mainView.getRayFromScreen(widget.projection, widget.getSize(), relPos);
		this.positionRay = map.ground.intersectRay(mouseRay);

		// Update preview entity position
		if (this.positionRay != null && this.previewPosition != null) {
			this.previewPosition.setTransform(new Transform3D(this.positionRay));
		}

		// Place object on left click
		if (event.inputId() == 1 && event.status() == KeyStatus.pressSingle) {
			if (this.positionRay != null && this.selectedMeshFile != null) {
				placeObject(map);
				return true;
			}
		}

		return false;
	}

	private void placeObject(final Map map) {
		if (this.positionRay == null || this.selectedMeshFile == null || this.sceneRef == null) {
			LOGGER.warn("Cannot place object: missing mesh file or scene reference");
			return;
		}
		final Environement env = this.sceneRef.getEnvironement();

		// Create a permanent entity with the EMF mesh
		final Entity entity = new Entity(env);
		entity.addComponent(new ComponentPosition(new Transform3D(this.positionRay)));
		final Uri meshUri = new Uri("FILE", this.selectedMeshFile);
		entity.addComponent(new ComponentMesh(meshUri));
		entity.addComponent(new ComponentTexturePalette(meshUri));
		entity.addComponent(new ComponentRenderMeshPalette(
				new Uri("DATA", "basicPalette.vert"),
				new Uri("DATA", "basicPalette.frag"),
				(EngineLight) env.getEngine(EngineLight.ENGINE_NAME)));
		env.addEntity(entity);
		map.placedEntities.add(entity);

		LOGGER.info("Placed mesh '{}' at position {}", this.selectedMeshFile, this.positionRay);
	}
}
