package sample.atriasoft.ege.loxelEngine;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.ege.ControlCameraPlayer;
import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.GameStatus;
import org.atriasoft.ege.Light;
import org.atriasoft.ege.Material;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentGravityStatic;
import org.atriasoft.ege.components.ComponentLight;
import org.atriasoft.ege.components.ComponentLightSun;
import org.atriasoft.ege.components.ComponentMaterial;
import org.atriasoft.ege.components.ComponentPhysics;
import org.atriasoft.ege.components.ComponentPlayer;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentPositionPlayer;
import org.atriasoft.ege.components.ComponentRenderColoredStaticMesh;
import org.atriasoft.ege.components.ComponentRenderTexturedMaterialsStaticMesh;
import org.atriasoft.ege.components.ComponentRenderTexturedStaticMesh;
import org.atriasoft.ege.components.ComponentStaticMesh;
import org.atriasoft.ege.components.ComponentTexture;
import org.atriasoft.ege.components.PhysicBodyType;
import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.ege.map.MapVoxel;
import org.atriasoft.ege.tools.MeshGenerator;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.Gale;
import org.atriasoft.gale.GaleApplication;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;
import org.atriasoft.gale.context.GaleContext;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeySpecial;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.gale.key.KeyType;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.phyligram.PhysicBox;
import org.atriasoft.phyligram.PhysicSphere;
import org.atriasoft.phyligram.PhysicTriangle;

public class LoxelApplicationPerso extends GaleApplication {
	public static Vector3f box1HalfSize;
	public static Vector3f box2HalfSize;
	//	public static ComponentPosition relativeTestPos;
	//	public static Box boxTest;
	public static List<Vector3f> testPoints = new ArrayList<>();
	public static List<Vector3f> testPointsBox = new ArrayList<>();
	public static List<Boolean> testPointsCollide = new ArrayList<>();
	public static Quaternion testQTransfert;
	public static Vector3f testRpos;
	public boolean disable = false;
	private float angleLight = 0;
	private Quaternion basicRotation = Quaternion.IDENTITY;
	private Quaternion basicRotation2 = Quaternion.IDENTITY;
	private ResourceColored3DObject debugDrawProperty;
	private Environement env;
	private ComponentPosition lightPosition;
	private MapVoxel map;
	private ComponentPlayer objectPlayer;
	private ComponentPosition objectPosition;
	private ControlCameraPlayer simpleControl;
	
	public LoxelApplicationPerso() {
		
	}
	
	@Override
	public void onCreate(final GaleContext context) {
		// set the system global max speed
		//ComponentPhysicsPerso.globalMaxSpeed = 3;
		Gale.getContext().grabPointerEvents(true, new Vector2f(0, 0));
		this.env = new Environement();
		setSize(new Vector2f(1500, 1500));
		setTitle("Loxel sample");
		this.map = new MapVoxel(this.env);
		//		this.env.addEngine(map);
		//		map.init();
		
		// simple sun to have a global light ...
		final Entity globalGravity = new Entity(this.env);
		globalGravity.addComponent(new ComponentGravityStatic(new Vector3f(0, 0, -1)));
		this.env.addEntity(globalGravity);
		
		// simple sun to have a global light ...
		final Entity sun = new Entity(this.env);
		sun.addComponent(new ComponentPosition(new Transform3D(new Vector3f(1000, 1000, 1000))));
		sun.addComponent(new ComponentLightSun(new Light(new Color(0.4f, 0.4f, 0.4f), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0, 0))));
		this.env.addEntity(sun);
		
		// add a cube to show where in the light ...
		final Entity localLight = new Entity(this.env);
		this.lightPosition = new ComponentPosition(new Transform3D(new Vector3f(-10, -10, 17)));
		//		localLight.addComponent(lightPosition);
		//		localLight.addComponent(new ComponentStaticMesh(new Uri("RES", "cube.obj")));
		//		localLight.addComponent(new ComponentTexture(new Uri("RES", "grass.png")));
		//		localLight.addComponent(new ComponentLight(new Light(new Vector3f(0,1,0), new Vector3f(0,0,0), new Vector3f(0.8f,0.03f,0.002f))));
		//		localLight.addComponent(new ComponentRenderTexturedStaticMesh(
		//				new Uri("DATA", "basic.vert"),
		//				new Uri("DATA", "basic.frag")));
		//		env.addEntity(localLight);
		if (this.disable) {
			// add a cube to test collision ...
			final Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
			localBox.addComponent(new ComponentLight(new Light(new Color(0.0f, 1.0f, 0.0f), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0.03f, 0.002f))));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 0, 5))));
			final ComponentPhysics physics2 = new ComponentPhysics(this.env);
			final PhysicBox box2 = new PhysicBox();
			box2.setSize(new Vector3f(1.001f, 1.001f, 1.001f));
			box2.setOrigin(new Vector3f(0, 0, 0));
			box2.setMass(1);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			this.env.addEntity(localBox);
		}
		if (this.disable) {
			// add a cube to test collision ...
			final Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
			localBox.addComponent(new ComponentLight(new Light(new Color(0.0f, 1.0f, 0.0f), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0.03f, 0.002f))));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 4, 2.5f))));
			final ComponentPhysics physics2 = new ComponentPhysics(this.env);
			final PhysicBox box2 = new PhysicBox();
			box2.setSize(new Vector3f(2.0f, 2.0f, 2.0f));
			box2.setOrigin(new Vector3f(0, 0, 0));
			box2.setMass(1);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			this.env.addEntity(localBox);
		}
		if (this.disable) {
			// add a cube to test collision ...
			final Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(-2, 2, 1.5f))));
			final ComponentPhysics physics2 = new ComponentPhysics(this.env);
			final PhysicBox box2 = new PhysicBox();
			box2.setSize(new Vector3f(3.0f, 3.0f, 3.0f));
			box2.setOrigin(new Vector3f(0, 0, 0));
			box2.setMass(1);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			this.env.addEntity(localBox);
		}
		
		if (this.disable) {
			// add a cube to test collision ...
			final Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(-5, -5, 0))));
			final ComponentPhysics physics2 = new ComponentPhysics(this.env);
			final PhysicBox box2 = new PhysicBox();
			box2.setSize(new Vector3f(2, 2, 2));
			box2.setOrigin(new Vector3f(0, 0, 0));
			box2.setMass(1);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			this.env.addEntity(localBox);
		}
		if (this.disable) {
			// add a cube to test collision ...
			final Entity localBox = new Entity(this.env);
			Quaternion orientation = new Quaternion(0.5f, 0.2f, 0.4f, 1);
			orientation = orientation.normalize();
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(15, 15, 0), orientation)));
			final ComponentPhysics physics2 = new ComponentPhysics(this.env);
			final PhysicBox box2 = new PhysicBox();
			box2.setSize(new Vector3f(4, 4, 4));
			box2.setOrigin(new Vector3f(0, 0, 0));
			box2.setMass(1);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			this.env.addEntity(localBox);
		}
		if (this.disable) {
			// add a cube to test collision ...
			final Entity localBox = new Entity(this.env);
			final Quaternion orientation = new Quaternion(0.3f, 1.3f, 0.4f, 1);
			//orientation.normalize();
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(2, -2, 0.2f), orientation)));
			final ComponentPhysics physics2 = new ComponentPhysics(this.env);
			// TODO: physics2.setAngularReactionEnable(false);
			final PhysicBox box2 = new PhysicBox();
			box2.setSize(new Vector3f(0.5f, 0.5f, 0.5f));
			box2.setOrigin(new Vector3f(0, 0, 0));
			box2.setMass(1);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			this.env.addEntity(localBox);
		}
		if (this.disable) {
			// this is the floor
			final Entity localBox = new Entity(this.env);
			Quaternion orientation = new Quaternion(0, 0, 0, 1);
			orientation = orientation.normalize();
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/dirt.png", "loxelEngine")));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 0, 0.0f), orientation)));
			final ComponentPhysics physics2 = new ComponentPhysics(this.env);
			physics2.setBodyType(PhysicBodyType.BODY_STATIC);
			final PhysicBox box2 = new PhysicBox();
			box2.setSize(new Vector3f(20.0f, 20.0f, 0.5f));
			box2.setOrigin(new Vector3f(0, 0, 0));
			box2.setMass(0);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			this.env.addEntity(localBox);
		}
		//		{
		//			// add a cube to test collision ...
		//			Entity localBox = new Entity(this.env);
		//			relativeTestPos = new ComponentPosition(new Transform3D(new Vector3f(0,0,14),new Quaternion(0.5f,0.2f,0.4f,1)));
		//			localBox.addComponent(relativeTestPos);
		////			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
		////			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
		////			localBox.addComponent(new ComponentLight(new Light(new Vector3f(0,1,0), new Vector3f(0,0,0), new Vector3f(0.8f,0.03f,0.002f))));
		////			localBox.addComponent(new ComponentRenderTexturedStaticMesh(
		////					new Uri("DATA", "basic.vert"),
		////					new Uri("DATA", "basic.frag")));
		//			ComponentPhysicsPerso physics2 = new ComponentPhysicsPerso(true);
		//			boxTest = new Box();
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
		//			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
		//			localBox.addComponent(new ComponentRenderTexturedStaticMesh(
		//					new Uri("DATA", "basic.vert"),
		//					new Uri("DATA", "basic.frag")));
		//			env.addEntity(localBox);
		//		}
		boolean selectCase1 = true;
		if (!this.disable) {
			// add a cube to test collision ...
			final Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
			localBox.addComponent(new ComponentLight(new Light(new Color(0.0f, 1.0f, 0.0f), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0.03f, 0.002f))));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 3, 0))));
			final ComponentPhysics physics2 = new ComponentPhysics(this.env);
			physics2.setBodyType(PhysicBodyType.BODY_STATIC);
			PhysicTriangle box2 = new PhysicTriangle();
			if (selectCase1) {
				box2.setPoints(new Vector3f(4, 0, 0), new Vector3f(0, 4, 0), new Vector3f(-2, -2, 1));
				box2.setOrigin(new Vector3f(0, 0, 0));
				box2.setMass(0);
				physics2.addShape(box2);
				box2 = new PhysicTriangle();
				box2.setPoints(new Vector3f(4, 0, 0), new Vector3f(0, 4, 0), new Vector3f(600, 600, -100));
				box2.setOrigin(new Vector3f(0, 0, 0));
				box2.setMass(0);
				box2.setFrictionCoefficient(1.0f);
				box2.setBouncingCoefficient(0.5f);
			} else {
				box2.setPoints(new Vector3f(4, 0, 0), new Vector3f(0, 4, 0), new Vector3f(-2, -2, 1));
				box2.setOrigin(new Vector3f(0, 0, 0));
				box2.setMass(0);
				box2.setBouncingCoefficient(0.5f);
			}
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			this.env.addEntity(localBox);
		}
		if (selectCase1) {
			// add a cube to test collision ...
			final Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
			localBox.addComponent(new ComponentLight(new Light(new Color(0.0f, 1.0f, 0.0f), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0.03f, 0.002f))));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(1.1f, 5.1f, 1.0f), Quaternion.fromEulerAngles(new Vector3f(0.15f, 0.95f, 0.3f)))));
			final ComponentPhysics physics2 = new ComponentPhysics(this.env);
			final PhysicSphere box2 = new PhysicSphere();
			physics2.setBodyType(PhysicBodyType.BODY_STATIC);
			box2.setSize(1.0f);
			box2.setOrigin(new Vector3f(0, 0, 0));
			box2.setMass(0);
			box2.setBouncingCoefficient(0.5f);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			this.env.addEntity(localBox);
		}
		if (true) {
			// add a cube to test collision ...
			final Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
			localBox.addComponent(new ComponentLight(new Light(new Color(0.0f, 1.0f, 0.0f), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0.03f, 0.002f))));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(1.8f, 5.8f, 4), Quaternion.fromEulerAngles(new Vector3f(0.15f, 0.95f, 0.3f)))));
			final ComponentPhysics physics2 = new ComponentPhysics(this.env);
			physics2.setBodyType(PhysicBodyType.BODY_DYNAMIC);
			final PhysicSphere box2 = new PhysicSphere();
			box2.setSize(1.0f);
			box2.setOrigin(new Vector3f(0, 0, 0));
			box2.setMass(1);
			physics2.addShape(box2);
			localBox.addComponent(physics2);
			this.env.addEntity(localBox);
		}
		
		final Entity gird = new Entity(this.env);
		gird.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 0, 0))));
		gird.addComponent(new ComponentStaticMesh(MeshGenerator.createGrid(5)));
		gird.addComponent(new ComponentRenderColoredStaticMesh(new Uri("DATA", "wireColor.vert", "ege"), new Uri("DATA", "wireColor.frag", "ege")));
		this.env.addEntity(gird);
		
		final Entity player = new Entity(this.env);
		if (false) {
			final Transform3D playerTransform = new Transform3D(new Vector3f(0, -5, 1));
			//this.objectPosition = new ComponentPositionPlayer();
			//player.addComponent(this.objectPosition);
			this.objectPlayer = new ComponentPlayer();
			player.addComponent(this.objectPlayer);
			player.addComponent(new ComponentMaterial(new Material()));
			//player.addComponent(new ComponentStaticMesh(new Uri("RES", "person.obj")));
			player.addComponent(new ComponentStaticMesh(new Uri("RES", "person_-yfw_zup.obj")));
			player.addComponent(new ComponentTexture(new Uri("RES", "playerTexture.png")));
			player.addComponent(new ComponentRenderTexturedMaterialsStaticMesh(new Uri("DATA", "basicMaterial.vert", "loxelEngine"), new Uri("DATA", "basicMaterial.frag", "loxelEngine"),
					(EngineLight) this.env.getEngine(EngineLight.ENGINE_NAME)));
			player.addComponent(new ComponentPosition(playerTransform));
			final ComponentPhysics physics = new ComponentPhysics(this.env);
			physics.setBodyType(PhysicBodyType.BODY_DYNAMIC);
			//physics.setAngularReactionEnable(false);
			//physics.setSleepingEnable(false);
			final PhysicBox box = new PhysicBox();
			box.setSize(new Vector3f(0.3f, 0.3f, 0.9f));
			box.setOrigin(new Vector3f(0, 0, 0.9f));
			box.setMass(1);
			physics.addShape(box);
			player.addComponent(physics);
			this.env.addEntity(player);
		} else {
			final Transform3D playerTransform = new Transform3D(new Vector3f(0, -5, 0));
			this.objectPosition = new ComponentPositionPlayer();
			player.addComponent(this.objectPosition);
			this.objectPlayer = new ComponentPlayer();
			player.addComponent(this.objectPlayer);
			player.addComponent(new ComponentMaterial(new Material()));
			//player.addComponent(new ComponentStaticMesh(new Uri("RES", "person.obj")));
			player.addComponent(new ComponentStaticMesh(new Uri("RES", "person_-yfw_zup.obj")));
			player.addComponent(new ComponentTexture(new Uri("RES", "playerTexture.png")));
			player.addComponent(new ComponentRenderTexturedMaterialsStaticMesh(new Uri("DATA", "basicMaterial.vert", "loxelEngine"), new Uri("DATA", "basicMaterial.frag", "loxelEngine"),
					(EngineLight) this.env.getEngine(EngineLight.ENGINE_NAME)));
			final ComponentPhysics physics = new ComponentPhysics(this.env);
			physics.setBodyType(PhysicBodyType.BODY_DYNAMIC);
			final PhysicBox box = new PhysicBox();
			box.setSize(new Vector3f(0.6f, 0.6f, 1.8f));
			box.setOrigin(new Vector3f(0, 0, 0.9f));
			box.setMass(0);
			physics.addShape(box);
			player.addComponent(physics);
			this.env.addEntity(player);
		}
		final Camera mainView = new Camera();
		this.env.addCamera("default", mainView);
		mainView.setPitch((float) Math.PI * -0.25f);
		mainView.setPosition(new Vector3f(0, -5, 5));
		
		this.simpleControl = new ControlCameraPlayer(mainView, player);
		this.env.addControlInterface(this.simpleControl);
		
		// start the engine.
		this.env.setPropertyStatus(GameStatus.gameStart);
		
		this.basicRotation = Quaternion.fromEulerAngles(new Vector3f(0.005f, 0.005f, 0.01f));
		this.basicRotation2 = Quaternion.fromEulerAngles(new Vector3f(0.003f, 0.01f, 0.001f));
		
		// ready to let Gale & Ege manage the display
		Log.info("==> Init APPL (END)");
	}
	
	@Override
	public void onDraw(final GaleContext context) {
		//Log.info("==> appl Draw ...");
		final Vector2f size = getSize();
		// Store openGl context.
		OpenGL.push();
		// set projection matrix:
		final Matrix4f tmpProjection = Matrix4f.createMatrixPerspective(3.14f * 0.5f, getAspectRatio(), 0.1f, 50000);
		OpenGL.setMatrix(tmpProjection);
		
		// set the basic openGL view port: (Draw in all the windows...)
		OpenGL.setViewPort(new Vector2f(0, 0), size);
		
		// clear background
		final Color bgColor = new Color(0.18f, 0.43f, 0.95f, 1.0f);
		OpenGL.clearColor(bgColor);
		// real clear request:
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_colorBuffer);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.enable(Flag.flag_depthTest);
		
		//Log.info("==> appl Draw ...");
		this.env.render(20, "default");
		if (this.debugDrawProperty == null) {
			this.debugDrawProperty = ResourceColored3DObject.create();
		}
		// now render the point test collision ...
		for (int iii = 0; iii < LoxelApplicationPerso.testPoints.size(); iii++) {
			final Vector3f elem = LoxelApplicationPerso.testPoints.get(iii);
			final boolean collide = LoxelApplicationPerso.testPointsCollide.get(iii);
			if (collide) {
				this.debugDrawProperty.drawSquare(new Vector3f(0.1f, 0.1f, 0.1f), Matrix4f.IDENTITY.multiply(Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14))),
						new Color(1, 0, 0, 1));
			} else if (iii == 0) {
				this.debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), Matrix4f.IDENTITY.multiply(Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14))),
						new Color(0, 1, 0, 1));
			} else if (iii == 7) {
				this.debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), Matrix4f.IDENTITY.multiply(Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14))),
						new Color(1, 1, 0, 1));
			} else {
				this.debugDrawProperty.drawSquare(new Vector3f(0.1f, 0.1f, 0.1f), Matrix4f.IDENTITY.multiply(Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14))),
						new Color(1, 1, 1, 1));
			}
		}
		for (int iii = 0; iii < LoxelApplicationPerso.testPointsBox.size(); iii++) {
			final Vector3f elem = LoxelApplicationPerso.testPointsBox.get(iii);
			if (iii == 0) {
				this.debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), Matrix4f.IDENTITY.multiply(Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14))),
						new Color(0, 1, 0, 1));
			} else if (iii == 7) {
				this.debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), Matrix4f.IDENTITY.multiply(Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14))),
						new Color(1, 1, 0, 1));
			} else {
				this.debugDrawProperty.drawSquare(new Vector3f(0.1f, 0.1f, 0.1f), Matrix4f.IDENTITY.multiply(Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14))),
						new Color(0, 0, 1, 1));
			}
		}
		
		if (LoxelApplicationPerso.testRpos != null) {
			//debugDrawProperty.drawSquare(box2HalfSize, testQTransfert.getMatrix4().multiplyNew(Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z+14))), new Color(0,1,0,0.5f));
			//Matrix4f transformation = Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z)).multiply(testQTransfert.getMatrix4()).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			//Matrix4f transformation = testQTransfert.getMatrix4().multiply(Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z))).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			//Matrix4f transformation = testQTransfert.getMatrix4().multiply(Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z))).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			final Matrix4f transformation = Matrix4f.createMatrixTranslate(new Vector3f(LoxelApplicationPerso.testRpos.x(), LoxelApplicationPerso.testRpos.y(), LoxelApplicationPerso.testRpos.z()))
					.multiply(Matrix4f.createMatrixTranslate(new Vector3f(0, 0, 14))).multiply(LoxelApplicationPerso.testQTransfert.getMatrix4());
			// OK sans la box1 orientation ...
			//Matrix4f transformation = Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z)).multiply(testQTransfert.getMatrix4()).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			//Matrix4f transformation = Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z)).multiply(testQTransfert.getMatrix4()).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			this.debugDrawProperty.drawSquare(LoxelApplicationPerso.box2HalfSize, transformation, new Color(0, 1, 0, 0.5f));
			this.debugDrawProperty.drawSquare(LoxelApplicationPerso.box1HalfSize, Matrix4f.createMatrixTranslate(new Vector3f(0, 0, 14)), new Color(0, 0, 1, 0.5f));
		}
		
		// Restore context of matrix
		OpenGL.pop();
	}
	
	@Override
	public void onKeyboard(final KeySpecial special, final KeyKeyboard type, final Character value, final KeyStatus state) {
		if (type == KeyKeyboard.F1) {
			Gale.getContext().grabPointerEvents(false, new Vector2f(0, 0));
		}
		if (type == KeyKeyboard.F2) {
			Gale.getContext().grabPointerEvents(true, new Vector2f(0, 0));
		}
		if (type == KeyKeyboard.F12) {
			Gale.getContext().setFullScreen(!Gale.getContext().getFullScreen());
		}
		this.env.onKeyboard(special, type, value, state);
	}
	
	@Override
	public void onPointer(final KeySpecial special, final KeyType type, final int pointerID, final Vector2f pos, final KeyStatus state) {
		this.env.onPointer(special, type, pointerID, pos, state);
	}
	
	@Override
	public void onRegenerateDisplay(final GaleContext context) {
		//Log.verbose("Regenerate Gale Application");
		this.angleLight += 0.01;
		this.lightPosition.setTransform(this.lightPosition.getTransform()
				.withPosition(new Vector3f(5 + (float) Math.cos(this.angleLight) * 7.0f, 5 + (float) Math.sin(this.angleLight) * 7.0f, this.lightPosition.getTransform().getPosition().z())));
		this.env.periodicCall();
		markDrawingIsNeeded();
	}
}
