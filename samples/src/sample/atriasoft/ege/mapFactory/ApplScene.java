package sample.atriasoft.ege.mapFactory;

import org.atriasoft.ege.Entity;
import org.atriasoft.ege.components.ComponentMesh;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderMeshPalette;
import org.atriasoft.ege.components.ComponentTexturePalette;
import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;

public class ApplScene extends EgeScene {
	Ground ground = new Ground();
	
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
		groundEntity.addComponent(new ComponentMesh(this.ground.createMesh()));
		groundEntity.addComponent(new ComponentTexturePalette(new Uri("DATA", "palette_1.json")));
		//basicTree.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
		groundEntity
				.addComponent(new ComponentRenderMeshPalette(new Uri("DATA", "basicPalette.vert"), new Uri("DATA", "basicPalette.frag"), (EngineLight) this.env.getEngine(EngineLight.ENGINE_NAME)));
		this.env.addEntity(groundEntity);
		this.ground.updateMesh();
	}
	
}
/*
private float angleLight = 0;
private Quaternion basicRotation = Quaternion.IDENTITY;
private Quaternion basicRotation2 = Quaternion.IDENTITY;
private ComponentPosition lightPosition;
private Material materialCube;
private ComponentPosition objectPosition;
private ControlCameraSimple simpleControl;

public LowPolyApplication() {}

@Override
public void onCreate(final GaleContext context) {
	
	// simple sun to have a global light ...
	final Entity sun = new Entity(this.env);
	sun.addComponent(new ComponentPosition(new Transform3D(new Vector3f(1000, 1000, 1000))));
	sun.addComponent(new ComponentLightSun(new Light(new Color(1.0f, 1.0f, 1.0f), new Vector3f(0, 0, 0), new Vector3f(1.0f, 0, 0))));
	this.env.addEntity(sun);
	
	// add a cube to show where in the light ...
	final Entity localLight = new Entity(this.env);
	this.lightPosition = new ComponentPosition(new Transform3D(new Vector3f(-10, -10, 1)));
	localLight.addComponent(this.lightPosition);
	localLight.addComponent(new ComponentStaticMesh(new Uri("DATA", "cube-one.obj")));
	localLight.addComponent(new ComponentTexture(new Uri("DATA", "grass.png")));
	localLight.addComponent(new ComponentLight(new Light(new Color(0.0f, 0.0f, 2.0f), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0.01f, 0.002f))));
	localLight.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
	this.env.addEntity(localLight);
	
	// Simple Gird
	final Entity gird = new Entity(this.env);
	gird.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 0, 0))));
	gird.addComponent(new ComponentStaticMesh(MeshGenerator.createGrid(5)));
	gird.addComponent(new ComponentRenderColoredStaticMesh(new Uri("DATA", "wireColor.vert", "ege"), new Uri("DATA", "wireColor.frag", "ege")));
	this.env.addEntity(gird);
	
	// test entity
	Entity basicTree = new Entity(this.env);
	this.objectPosition = new ComponentPosition(new Transform3D(new Vector3f(0, 0, 0)));
	basicTree.addComponent(this.objectPosition);
	//this.materialCube = new Material();
	//basicTree.addComponent(new ComponentMaterial(this.materialCube));
	basicTree.addComponent(new ComponentMesh(new Uri("DATA", "tree1.emf")));
	basicTree.addComponent(new ComponentTexturePalette(new Uri("DATA", "palette_1.json")));
	//basicTree.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
	basicTree.addComponent(new ComponentRenderMeshPalette(new Uri("DATA", "basicPalette.vert"), new Uri("DATA", "basicPalette.frag"),
				(EngineLight) this.env.getEngine(EngineLight.ENGINE_NAME)));
	this.env.addEntity(basicTree);

	
	basicTree = new Entity(this.env);
	this.objectPosition = new ComponentPosition(new Transform3D(new Vector3f(3, 2, 0)));
	basicTree.addComponent(this.objectPosition);
	//this.materialCube = new Material();
	//basicTree.addComponent(new ComponentMaterial(this.materialCube));
	basicTree.addComponent(new ComponentMesh(new Uri("DATA", "tree2.emf")));
	basicTree.addComponent(new ComponentTexturePalette(new Uri("DATA", "palette_1.json")));
	//basicTree.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
	basicTree.addComponent(new ComponentRenderMeshPalette(new Uri("DATA", "basicPalette.vert"), new Uri("DATA", "basicPalette.frag"),
				(EngineLight) this.env.getEngine(EngineLight.ENGINE_NAME)));
	this.env.addEntity(basicTree);
	
	this.simpleControl = new ControlCameraSimple(mainView);
	this.env.addControlInterface(this.simpleControl);
	
	// start the engine.
	this.env.setPropertyStatus(GameStatus.gameStart);
	
	this.basicRotation = Quaternion.fromEulerAngles(new Vector3f(0.005f, 0.005f, 0.01f));
	this.basicRotation2 = Quaternion.fromEulerAngles(new Vector3f(0.003f, 0.01f, 0.001f));
	// ready to let Gale & Ege manage the display
	Log.info("==> Init APPL (END)");
}

@Override
public void onKeyboard(final KeySpecial special, final KeyKeyboard type, final Character value, final KeyStatus state) {
	this.env.onKeyboard(special, type, value, state);
}

@Override
public void onPointer(final KeySpecial special, final KeyType type, final int pointerID, final Vector2f pos, final KeyStatus state) {
	this.env.onPointer(special, type, pointerID, pos, state);
}
*/
