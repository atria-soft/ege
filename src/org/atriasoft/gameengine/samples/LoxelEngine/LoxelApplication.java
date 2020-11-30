package org.atriasoft.gameengine.samples.LoxelEngine;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.Application;
import org.atriasoft.gale.Gale;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;
import org.atriasoft.gale.context.Context;
import org.atriasoft.gameengine.ControlCameraPlayer;
import org.atriasoft.gameengine.Entity;
import org.atriasoft.gameengine.Environement;
import org.atriasoft.gameengine.GameStatus;
import org.atriasoft.gameengine.Light;
import org.atriasoft.gameengine.Material;
import org.atriasoft.gameengine.camera.Camera;
import org.atriasoft.gameengine.components.ComponentGravityStatic;
import org.atriasoft.gameengine.components.ComponentLight;
import org.atriasoft.gameengine.components.ComponentLightSun;
import org.atriasoft.gameengine.components.ComponentMaterial;
import org.atriasoft.gameengine.components.ComponentPhysics;
import org.atriasoft.gameengine.components.ComponentPlayer;
import org.atriasoft.gameengine.components.ComponentPosition;
import org.atriasoft.gameengine.components.ComponentPositionPlayer;
import org.atriasoft.gameengine.components.ComponentRenderColoredStaticMesh;
import org.atriasoft.gameengine.components.ComponentRenderTexturedMaterialsStaticMesh;
import org.atriasoft.gameengine.components.ComponentRenderTexturedStaticMesh;
import org.atriasoft.gameengine.components.ComponentStaticMesh;
import org.atriasoft.gameengine.components.ComponentTexture;
import org.atriasoft.gameengine.engines.EngineLight;
import org.atriasoft.gameengine.map.MapVoxel;
import org.atriasoft.gameengine.physics.PhysicBox;
import org.atriasoft.gameengine.tools.MeshGenerator;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeySpecial;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.gale.key.KeyType;
import org.atriasoft.gale.resource.ResourceColored3DObject;

public class LoxelApplication extends Application {
	private Environement env;
	private ComponentPosition objectPosition;
	private Quaternion basicRotation = Quaternion.identity();
	private Quaternion basicRotation2 = Quaternion.identity();
	private boolean creationDone;
	private ControlCameraPlayer simpleControl;
	private ComponentPosition lightPosition;
	private float angleLight = 0;
	private MapVoxel map;
	private ComponentPlayer objectPlayer;
//	public static ComponentPosition relativeTestPos;
//	public static PhysicBox boxTest;
	public static List<Vector3f> testPoints = new ArrayList<Vector3f>();
	public static List<Vector3f> testPointsBox = new ArrayList<Vector3f>();
	public static List<Boolean> testPointsCollide = new ArrayList<Boolean>();
	public static Vector3f testRpos;
	public static Quaternion testQTransfert;
	public static Vector3f box1HalfSize;
	public static Vector3f box2HalfSize;
	private ResourceColored3DObject debugDrawProperty;
	public LoxelApplication(){
		creationDone = false;
	}
	@Override
	public void onCreate(Context context) {
		// set the system global max speed
		ComponentPhysics.globalMaxSpeed = 3;
		Gale.getContext().grabPointerEvents(true, new Vector2f(0,0));
		env = new Environement();
		this.canDraw = true;
		setSize(new Vector2f(1500, 1500));
		setTitle("Loxel sample");
		map = new MapVoxel(this.env);
//		this.env.addEngine(map);
//		map.init();

		// simple sun to have a global light ...
		Entity globalGravity = new Entity(this.env);
		globalGravity.addComponent(new ComponentGravityStatic(new Vector3f(0,0,-1)));
		env.addEntity(globalGravity);
		
		// simple sun to have a global light ...
		Entity sun = new Entity(this.env);
		sun.addComponent(new ComponentPosition(new Transform3D(new Vector3f(1000,1000,1000))));
		sun.addComponent(new ComponentLightSun(new Light(new Vector3f(0.4f,0.4f,0.4f), new Vector3f(0,0,0), new Vector3f(0.8f,0,0))));
		env.addEntity(sun);

		// add a cube to show where in the light ...
		Entity localLight = new Entity(this.env);
		lightPosition = new ComponentPosition(new Transform3D(new Vector3f(-10,-10,17)));
//		localLight.addComponent(lightPosition);
//		localLight.addComponent(new ComponentStaticMesh(new Uri("RES", "cube.obj")));
//		localLight.addComponent(new ComponentTexture(new Uri("RES", "grass.png")));
//		localLight.addComponent(new ComponentLight(new Light(new Vector3f(0,1,0), new Vector3f(0,0,0), new Vector3f(0.8f,0.03f,0.002f))));
//		localLight.addComponent(new ComponentRenderTexturedStaticMesh(
//				new Uri("DATA", "basic.vert"),
//				new Uri("DATA", "basic.frag")));
//		env.addEntity(localLight);
		{
			// add a cube to test collision ...
			Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(-2,-2,14))));
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png")));
			localBox.addComponent(new ComponentLight(new Light(new Vector3f(0,1,0), new Vector3f(0,0,0), new Vector3f(0.8f,0.03f,0.002f))));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(
					new Uri("DATA", "basic.vert"),
					new Uri("DATA", "basic.frag")));
			ComponentPhysics physics2 = new ComponentPhysics(true);
			PhysicBox box2 = new PhysicBox();
			box2.setSize(new Vector3f(1,1,1));
			box2.setOrigin(new Vector3f(0,0,0));
			box2.setMass(1);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			env.addEntity(localBox);
		}
		{
			// add a cube to test collision ...
			Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0,4,12.5f))));
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png")));
			localBox.addComponent(new ComponentLight(new Light(new Vector3f(0,1,0), new Vector3f(0,0,0), new Vector3f(0.8f,0.03f,0.002f))));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(
					new Uri("DATA", "basic.vert"),
					new Uri("DATA", "basic.frag")));
			ComponentPhysics physics2 = new ComponentPhysics(true);
			PhysicBox box2 = new PhysicBox();
			box2.setSize(new Vector3f(1,1,1));
			box2.setOrigin(new Vector3f(0,0,0));
			box2.setMass(1);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			env.addEntity(localBox);
		}
		{
			// add a cube to test collision ...
			Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(-2,2,14.5f))));
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png")));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(
					new Uri("DATA", "basic.vert"),
					new Uri("DATA", "basic.frag")));
			ComponentPhysics physics2 = new ComponentPhysics(true);
			PhysicBox box2 = new PhysicBox();
			box2.setSize(new Vector3f(1,1,1));
			box2.setOrigin(new Vector3f(0,0,0));
			box2.setMass(1);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			env.addEntity(localBox);
		}

		{
			// add a cube to test collision ...
			Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(-5,-5,14))));
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png")));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(
					new Uri("DATA", "basic.vert"),
					new Uri("DATA", "basic.frag")));
			ComponentPhysics physics2 = new ComponentPhysics(true);
			PhysicBox box2 = new PhysicBox();
			box2.setSize(new Vector3f(4,4,4));
			box2.setOrigin(new Vector3f(0,0,0));
			box2.setMass(1);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			env.addEntity(localBox);
		}
		{
			// add a cube to test collision ...
			Entity localBox = new Entity(this.env);
			Quaternion transform = new Quaternion(0.5f,0.2f,0.4f,1);
			transform.normalize();
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(2,-2,14.2f),transform)));
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png")));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(
					new Uri("DATA", "basic.vert"),
					new Uri("DATA", "basic.frag")));
			ComponentPhysics physics2 = new ComponentPhysics(true);
			PhysicBox box2 = new PhysicBox();
			box2.setSize(new Vector3f(1,1,1));
			box2.setOrigin(new Vector3f(0,0,0));
			box2.setMass(1);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			env.addEntity(localBox);
		}
		{
			// add a cube to test collision ...
			Entity localBox = new Entity(this.env);
			Quaternion transform = new Quaternion(0,0,1,1);
			transform.normalize();
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(2,2,14.2f),transform)));
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png")));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(
					new Uri("DATA", "basic.vert"),
					new Uri("DATA", "basic.frag")));
			ComponentPhysics physics2 = new ComponentPhysics(true);
			PhysicBox box2 = new PhysicBox();
			box2.setSize(new Vector3f(1,1,1));
			box2.setOrigin(new Vector3f(0,0,0));
			box2.setMass(1);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			env.addEntity(localBox);
		}
//		{
//			// add a cube to test collision ...
//			Entity localBox = new Entity(this.env);
//			relativeTestPos = new ComponentPosition(new Transform3D(new Vector3f(0,0,14),new Quaternion(0.5f,0.2f,0.4f,1)));
//			localBox.addComponent(relativeTestPos);
////			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
////			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png")));
////			localBox.addComponent(new ComponentLight(new Light(new Vector3f(0,1,0), new Vector3f(0,0,0), new Vector3f(0.8f,0.03f,0.002f))));
////			localBox.addComponent(new ComponentRenderTexturedStaticMesh(
////					new Uri("DATA", "basic.vert"),
////					new Uri("DATA", "basic.frag")));
//			ComponentPhysics physics2 = new ComponentPhysics(true);
//			boxTest = new PhysicBox();
//			boxTest.setSize(new Vector3f(1,1,1));
//			boxTest.setOrigin(new Vector3f(0,0,0));
//			boxTest.setMass(1);
//			physics2.addShape(boxTest);
//			localBox.addComponent(physics2);
//			env.addEntity(localBox);
//		}
//		{
//			Entity localBox = new Entity(this.env);
//			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0,0,14))));
//			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
//			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png")));
//			localBox.addComponent(new ComponentRenderTexturedStaticMesh(
//					new Uri("DATA", "basic.vert"),
//					new Uri("DATA", "basic.frag")));
//			env.addEntity(localBox);
//		}
		
		Entity gird = new Entity(this.env);
		gird.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0,0,13))));
		gird.addComponent(new ComponentStaticMesh(MeshGenerator.createGrid(5)));
		gird.addComponent(new ComponentRenderColoredStaticMesh(
				new Uri("DATA_EGE", "wireColor.vert"),
				new Uri("DATA_EGE", "wireColor.frag")));
		env.addEntity(gird);

		Entity player = new Entity(this.env);
		objectPosition = new ComponentPositionPlayer(new Transform3D(new Vector3f(5,5,13)));
		player.addComponent(objectPosition);
		objectPlayer = new ComponentPlayer();
		player.addComponent(objectPlayer);
		player.addComponent(new ComponentMaterial(new Material()));
		//player.addComponent(new ComponentStaticMesh(new Uri("RES", "person.obj")));
		player.addComponent(new ComponentStaticMesh(new Uri("RES", "person_-yfw_zup.obj")));
		player.addComponent(new ComponentTexture(new Uri("RES", "playerTexture.png")));
		player.addComponent(new ComponentRenderTexturedMaterialsStaticMesh(
				new Uri("DATA", "basicMaterial.vert"),
				new Uri("DATA", "basicMaterial.frag"),
				(EngineLight)env.getEngine(EngineLight.ENGINE_NAME)));
		ComponentPhysics physics = new ComponentPhysics(true);
		PhysicBox box = new PhysicBox();
		box.setSize(new Vector3f(0.6f,0.6f,1.8f));
		box.setOrigin(new Vector3f(0,0,0.9f));
		box.setMass(1);
		physics.addShape(box);
		player.addComponent(physics);
		env.addEntity(player);
		
		
		Camera mainView = new Camera();
		env.addCamera("default", mainView);
		mainView.setPitch((float)Math.PI*-0.25f);
		mainView.setPosition(new Vector3f(0,-5,5));
		
		this.simpleControl = new ControlCameraPlayer(mainView, player);
		env.addControlInterface(simpleControl);
		
		// start the engine.
		env.setPropertyStatus(GameStatus.gameStart);
		
		basicRotation.setEulerAngles(new Vector3f(0.005f,0.005f,0.01f));
		basicRotation2.setEulerAngles(new Vector3f(0.003f,0.01f,0.001f));
		// ready to let Gale & Ege manage the display
		Log.info("==> Init APPL (END)");
		creationDone = true;
	}
	
	@Override
	public void onRegenerateDisplay(Context context) {
		//Log.verbose("Regenerate Gale Application");
		if (!this.creationDone) {
			return;
		}
		angleLight += 0.01;
		lightPosition.getTransform().getPosition().x = 5 + (float)Math.cos(angleLight) * 7.0f;
		lightPosition.getTransform().getPosition().y = 5 + (float)Math.sin(angleLight) * 7.0f;
		env.periodicCall();
		markDrawingIsNeeded();
	}
	
	@Override
	public void onDraw(Context context) {
		//Log.info("==> appl Draw ...");
		Vector2f size = getSize();
		if (!this.creationDone) {
			OpenGL.setViewPort(new Vector2f(0,0), size);
			Color bgColor = new Color(0.8f, 0.5f, 0.5f, 1.0f);
			OpenGL.clearColor(bgColor);
			Log.info("==> appl clear ==> not created ...");
			return;
		}
		// Store openGl context.
		OpenGL.push();
		// set projection matrix:
		Matrix4f tmpProjection = Matrix4f.createMatrixPerspective(3.14f*0.5f, getAspectRatio(), 0.1f, 50000);
		OpenGL.setMatrix(tmpProjection);
		
		// set the basic openGL view port: (Draw in all the windows...)
		OpenGL.setViewPort(new Vector2f(0,0), size);

		// clear background
		Color bgColor = new Color(0.18f, 0.43f, 0.95f, 1.0f);
		OpenGL.clearColor(bgColor);
		// real clear request:
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_colorBuffer);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.enable(Flag.flag_depthTest);

		//Log.info("==> appl Draw ...");
		env.render(20, "default");
		if (this.debugDrawProperty == null) {
			debugDrawProperty = ResourceColored3DObject.create();
		}
		// now render the point test collision ...
		for (int iii=0; iii<LoxelApplication.testPoints.size(); iii++) {
			Vector3f elem = LoxelApplication.testPoints.get(iii);
			boolean collide = LoxelApplication.testPointsCollide.get(iii);
			if (collide) {
				debugDrawProperty.drawSquare(new Vector3f(0.1f,0.1f,0.1f), Matrix4f.identity().multiplyNew(Matrix4f.createMatrixTranslate(new Vector3f(elem.x,elem.y,elem.z+14))), new Color(1,0,0,1));
			} else {
				if (iii==0) {
					debugDrawProperty.drawSquare(new Vector3f(0.05f,0.05f,0.05f), Matrix4f.identity().multiplyNew(Matrix4f.createMatrixTranslate(new Vector3f(elem.x,elem.y,elem.z+14))), new Color(0,1,0,1));
				} else if (iii==7) {
					debugDrawProperty.drawSquare(new Vector3f(0.05f,0.05f,0.05f), Matrix4f.identity().multiplyNew(Matrix4f.createMatrixTranslate(new Vector3f(elem.x,elem.y,elem.z+14))), new Color(1,1,0,1));
				} else {
					debugDrawProperty.drawSquare(new Vector3f(0.1f,0.1f,0.1f), Matrix4f.identity().multiplyNew(Matrix4f.createMatrixTranslate(new Vector3f(elem.x,elem.y,elem.z+14))), new Color(1,1,1,1));
				}
			}
		}
		for (int iii=0; iii<LoxelApplication.testPointsBox.size(); iii++) {
			Vector3f elem = LoxelApplication.testPointsBox.get(iii);
			if (iii==0) {
				debugDrawProperty.drawSquare(new Vector3f(0.05f,0.05f,0.05f), Matrix4f.identity().multiplyNew(Matrix4f.createMatrixTranslate(new Vector3f(elem.x,elem.y,elem.z+14))), new Color(0,1,0,1));
			} else if (iii==7) {
				debugDrawProperty.drawSquare(new Vector3f(0.05f,0.05f,0.05f), Matrix4f.identity().multiplyNew(Matrix4f.createMatrixTranslate(new Vector3f(elem.x,elem.y,elem.z+14))), new Color(1,1,0,1));
			} else {
				debugDrawProperty.drawSquare(new Vector3f(0.1f,0.1f,0.1f), Matrix4f.identity().multiplyNew(Matrix4f.createMatrixTranslate(new Vector3f(elem.x,elem.y,elem.z+14))), new Color(0,0,1,1));
			}
		}
		
		if (testRpos != null) {
			//debugDrawProperty.drawSquare(box2HalfSize, testQTransfert.getMatrix4().multiplyNew(Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z+14))), new Color(0,1,0,0.5f));
			//Matrix4f transformation = Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z)).multiply(testQTransfert.getMatrix4()).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			//Matrix4f transformation = testQTransfert.getMatrix4().multiply(Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z))).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			//Matrix4f transformation = testQTransfert.getMatrix4().multiply(Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z))).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			Matrix4f trensformation = Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z)).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14))).multiply(testQTransfert.getMatrix4());
			// OK sans la box1 orientation ...
			//Matrix4f transformation = Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z)).multiply(testQTransfert.getMatrix4()).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			//Matrix4f transformation = Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z)).multiply(testQTransfert.getMatrix4()).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			debugDrawProperty.drawSquare(box2HalfSize, trensformation, new Color(0,1,0,0.5f));
			debugDrawProperty.drawSquare(box1HalfSize, Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)), new Color(0,0,1,0.5f));
		}
		
		// Restore context of matrix
		OpenGL.pop();
	}
	@Override
	public void onPointer(KeySpecial special,
			KeyType type,
			int pointerID,
			Vector2f pos,
			KeyStatus state) {
		env.onPointer(special, type, pointerID, pos, state);
	}
	@Override
	public void onKeyboard(KeySpecial special,
			KeyKeyboard type,
			Character value,
			KeyStatus state) {
		if (type == KeyKeyboard.f1 ) {
			Gale.getContext().grabPointerEvents(false, new Vector2f(0,0));
		}
		if (type == KeyKeyboard.f2 ) {
			Gale.getContext().grabPointerEvents(true, new Vector2f(0,0));
		}
		if (type == KeyKeyboard.f12 ) {
			Gale.getContext().setFullScreen(!Gale.getContext().getFullScreen());
		}
		env.onKeyboard(special, type, value, state);
	}
}
