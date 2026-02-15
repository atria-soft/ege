package sample.atriasoft.ege.mapFactory;

import org.atriasoft.ege.ControlCameraSimple;
import org.atriasoft.ege.ControlInterface;
import org.atriasoft.ege.Entity;
import org.atriasoft.ege.components.ComponentMesh;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderMeshPalette;
import org.atriasoft.ege.components.ComponentTexturePalette;
import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sample.atriasoft.ege.mapFactory.model.Map;
import sample.atriasoft.ege.mapFactory.tools.MapToolInterface;

public class ApplScene extends EgeScene {
	final static private Logger LOGGER = LoggerFactory.getLogger(ApplScene.class);
	//Ground ground = new Ground();
	Map map = new Map();
	private ControlInterface simpleControl;
	private MapToolInterface currentTool = null;
	
	/**
	 * Constructor
	 */
	public ApplScene() {
		addGenericGird();
		// test entity
		Entity groundEntity = new Entity(this.env);
		ComponentPosition objectPosition = new ComponentPosition(new Transform3D(new Vector3f(0, 0, 0)));
		groundEntity.addComponent(objectPosition);
		//this.materialCube = new Material();
		//basicTree.addComponent(new ComponentMaterial(this.materialCube));
		//groundEntity.addComponent(new ComponentMesh(new Uri("DATA", "tree1.emf", "plop")));
		groundEntity.addComponent(new ComponentMesh(this.map.ground.createMesh()));
		groundEntity.addComponent(new ComponentTexturePalette(new Uri("DATA", "palette_1.json")));
		//basicTree.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
		groundEntity.addComponent(new ComponentRenderMeshPalette(new Uri("DATA", "basicPalette.vert"),
				new Uri("DATA", "basicPalette.frag"), (EngineLight) this.env.getEngine(EngineLight.ENGINE_NAME)));
		this.env.addEntity(groundEntity);
		
		this.map.updateMesh();
		this.simpleControl = new ControlCameraSimple(this.mainView);
		this.env.addControlInterface(this.simpleControl);
		
		// add a cube to test collision ...
		//		final Entity localBox = new Entity(this.env);
		//		localBox.addComponent(new ComponentStaticMesh(new Uri("DATA", "cube-one.obj")));
		//		localBox.addComponent(new ComponentTexture(new Uri("DATA", "clay.png")));
		//		//localBox.addComponent(new ComponentLight(new Light(new Color(0.0f, 1.0f, 0.0f), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0.03f, 0.002f))));
		//		localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert"), new Uri("DATA", "basic.frag")));
		//		this.posRay = new ComponentPosition(new Transform3D(new Vector3f(0, 4, 2.5f)));
		//
		//		localBox.addComponent(this.posRay);
		//		//final ComponentPhysics physics2 = new ComponentPhysics(this.env);
		//		//final PhysicBox box2 = new PhysicBox();
		//		//box2.setSize(new Vector3f(2.0f, 2.0f, 2.0f));
		//		//box2.setOrigin(new Vector3f(0, 0, 0));
		//		//box2.setMass(1);
		//		//physics2.addShape(box2);
		//		//localBox.addComponent(physics2);
		//		this.env.addEntity(localBox);
		
	}
	
	public MapToolInterface getCurrentTool() {
		return this.currentTool;
	}
	
	@Override
	protected void onDrawScene() {
		if (this.currentTool != null) {
			this.currentTool.onDraw(this.map);
		}
	}

	@Override
	public boolean onEventEntry(final EventEntry event) {
		if (this.currentTool != null) {
			if (this.currentTool.onEventEntry(event, this.map, this)) {
				return true;
			}
		}
		return super.onEventEntry(event);
	}

	@Override
	public boolean onEventInput(final EventInput event) {
		if (this.currentTool != null) {
			if (this.currentTool.onEventInput(event, this.map, this)) {
				return true;
			}
		}
		return super.onEventInput(event);
	}
	
	public void setCurrentTool(final MapToolInterface currentTool) {
		if (this.currentTool != null) {
			this.currentTool.onDeactivate(this);
		}
		this.currentTool = currentTool;
	}
}