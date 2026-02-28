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
import org.atriasoft.ege.map.MapVoxel;
import org.atriasoft.ephysics.collision.shapes.BoxShape;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoxelApplicationPerso extends GaleApplication {
	final static private Logger LOGGER = LoggerFactory.getLogger(LoxelApplicationPerso.class);
	public static Vector3f box1HalfSize;
	public static Vector3f box2HalfSize;
	public static List<Vector3f> testPoints = new ArrayList<>();
	public static List<Vector3f> testPointsBox = new ArrayList<>();
	public static List<Boolean> testPointsCollide = new ArrayList<>();
	public static Quaternion testQTransfert;
	public static Vector3f testRpos;
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
		Gale.getContext().grabPointerEvents(true, new Vector2f(0, 0));
		this.env = new Environement();
		setSize(new Vector2f(1500, 1500));
		setTitle("Loxel sample");
		this.map = new MapVoxel(this.env);

		// simple sun to have a global light ...
		final Entity globalGravity = new Entity(this.env);
		globalGravity.addComponent(new ComponentGravityStatic(new Vector3f(0, -1, 0)));
		this.env.addEntity(globalGravity);

		// simple sun to have a global light ...
		final Entity sun = new Entity(this.env);
		sun.addComponent(new ComponentPosition(new Transform3D(new Vector3f(1000, 1000, 1000))));
		sun.addComponent(new ComponentLightSun(
				new Light(new Color(0.4f, 0.4f, 0.4f), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0, 0))));
		this.env.addEntity(sun);

		this.lightPosition = new ComponentPosition(new Transform3D(new Vector3f(-10, 17, -10)));

		{
			final Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
			localBox.addComponent(new ComponentLight(
					new Light(new Color(0.0f, 1.0f, 0.0f), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0.03f, 0.002f))));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"),
					new Uri("DATA", "basic.frag", "loxelEngine")));
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 3, 0))));
			final ComponentPhysics physics2 = new ComponentPhysics();
			physics2.setBodyType(PhysicBodyType.BODY_STATIC);
			physics2.addShape(new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f), 0.0f), 1.0f);
			localBox.addComponent(physics2);
			this.env.addEntity(localBox);
		}
		{
			final Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
			localBox.addComponent(new ComponentLight(
					new Light(new Color(0.0f, 1.0f, 0.0f), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0.03f, 0.002f))));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"),
					new Uri("DATA", "basic.frag", "loxelEngine")));
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(1.1f, 1.0f, 5.1f),
					Quaternion.fromEulerAngles(new Vector3f(0.15f, 0.95f, 0.3f)))));
			final ComponentPhysics physics2 = new ComponentPhysics();
			physics2.setBodyType(PhysicBodyType.BODY_STATIC);
			physics2.addShape(new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f), 0.0f), 1.0f);
			localBox.addComponent(physics2);
			this.env.addEntity(localBox);
		}
		{
			final Entity localBox = new Entity(this.env);
			localBox.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
			localBox.addComponent(new ComponentTexture(new Uri("DATA", "blocks/clay.png", "loxelEngine")));
			localBox.addComponent(new ComponentLight(
					new Light(new Color(0.0f, 1.0f, 0.0f), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0.03f, 0.002f))));
			localBox.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"),
					new Uri("DATA", "basic.frag", "loxelEngine")));
			localBox.addComponent(new ComponentPosition(new Transform3D(new Vector3f(1.8f, 4, 5.8f),
					Quaternion.fromEulerAngles(new Vector3f(0.15f, 0.95f, 0.3f)))));
			final ComponentPhysics physics2 = new ComponentPhysics();
			physics2.setBodyType(PhysicBodyType.BODY_DYNAMIC);
			physics2.addShape(new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f), 0.0f), 5.0f);
			localBox.addComponent(physics2);
			this.env.addEntity(localBox);
		}

		final Entity gird = new Entity(this.env);
		gird.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 0, 0))));
		gird.addComponent(new ComponentStaticMesh(MeshGenerator.createGrid(5)));
		gird.addComponent(new ComponentRenderColoredStaticMesh(new Uri("DATA", "wireColor.vert", "ege"),
				new Uri("DATA", "wireColor.frag", "ege")));
		this.env.addEntity(gird);

		final Entity player = new Entity(this.env);
		{
			final Transform3D playerTransform = new Transform3D(new Vector3f(0, 0, -5));
			this.objectPosition = new ComponentPositionPlayer();
			player.addComponent(this.objectPosition);
			this.objectPlayer = new ComponentPlayer();
			player.addComponent(this.objectPlayer);
			player.addComponent(new ComponentMaterial(new Material()));
			player.addComponent(new ComponentStaticMesh(new Uri("RES", "person.obj")));
			player.addComponent(new ComponentTexture(new Uri("RES", "playerTexture.png")));
			player.addComponent(
					new ComponentRenderTexturedMaterialsStaticMesh(new Uri("DATA", "basicMaterial.vert", "loxelEngine"),
							new Uri("DATA", "basicMaterial.frag", "loxelEngine")));
			final ComponentPhysics physics = new ComponentPhysics();
			physics.setBodyType(PhysicBodyType.BODY_DYNAMIC);
			player.addComponent(physics);
			this.env.addEntity(player);
		}
		final Camera mainView = new Camera();
		this.env.addCamera("default", mainView);
		mainView.setPitch((float) Math.PI * 0.25f);
		mainView.setPosition(new Vector3f(0, 5, 5));

		this.simpleControl = new ControlCameraPlayer(mainView, player);
		this.env.addControlInterface(this.simpleControl);

		// start the engine.
		this.env.setPropertyStatus(GameStatus.gameStart);

		this.basicRotation = Quaternion.fromEulerAngles(new Vector3f(0.005f, 0.005f, 0.01f));
		this.basicRotation2 = Quaternion.fromEulerAngles(new Vector3f(0.003f, 0.01f, 0.001f));

		// ready to let Gale & Ege manage the display
		LOGGER.info("==> Init APPL (END)");
	}

	@Override
	public void onDraw(final GaleContext context) {
		final Vector2f size = getSize();
		OpenGL.push();
		final Matrix4f tmpProjection = Matrix4f.createMatrixPerspective(3.14f * 0.5f, getAspectRatio(), 0.1f, 50000);
		OpenGL.setMatrix(tmpProjection);
		OpenGL.setViewPort(new Vector2f(0, 0), size);

		final Color bgColor = new Color(0.18f, 0.43f, 0.95f, 1.0f);
		OpenGL.clearColor(bgColor);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_colorBuffer);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.enable(Flag.flag_depthTest);

		this.env.render(20, "default");
		if (this.debugDrawProperty == null) {
			this.debugDrawProperty = ResourceColored3DObject.create();
		}
		for (int iii = 0; iii < LoxelApplicationPerso.testPoints.size(); iii++) {
			final Vector3f elem = LoxelApplicationPerso.testPoints.get(iii);
			final boolean collide = LoxelApplicationPerso.testPointsCollide.get(iii);
			if (collide) {
				this.debugDrawProperty.drawSquare(new Vector3f(0.1f, 0.1f, 0.1f),
						Matrix4f.IDENTITY.multiply(
								Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y() + 14, elem.z()))),
						new Color(1, 0, 0, 1));
			} else if (iii == 0) {
				this.debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f),
						Matrix4f.IDENTITY.multiply(
								Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y() + 14, elem.z()))),
						new Color(0, 1, 0, 1));
			} else if (iii == 7) {
				this.debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f),
						Matrix4f.IDENTITY.multiply(
								Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y() + 14, elem.z()))),
						new Color(1, 1, 0, 1));
			} else {
				this.debugDrawProperty.drawSquare(new Vector3f(0.1f, 0.1f, 0.1f),
						Matrix4f.IDENTITY.multiply(
								Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y() + 14, elem.z()))),
						new Color(1, 1, 1, 1));
			}
		}
		for (int iii = 0; iii < LoxelApplicationPerso.testPointsBox.size(); iii++) {
			final Vector3f elem = LoxelApplicationPerso.testPointsBox.get(iii);
			if (iii == 0) {
				this.debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f),
						Matrix4f.IDENTITY.multiply(
								Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y() + 14, elem.z()))),
						new Color(0, 1, 0, 1));
			} else if (iii == 7) {
				this.debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f),
						Matrix4f.IDENTITY.multiply(
								Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y() + 14, elem.z()))),
						new Color(1, 1, 0, 1));
			} else {
				this.debugDrawProperty.drawSquare(new Vector3f(0.1f, 0.1f, 0.1f),
						Matrix4f.IDENTITY.multiply(
								Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y() + 14, elem.z()))),
						new Color(0, 0, 1, 1));
			}
		}

		if (LoxelApplicationPerso.testRpos != null) {
			final Matrix4f transformation = Matrix4f
					.createMatrixTranslate(new Vector3f(LoxelApplicationPerso.testRpos.x(),
							LoxelApplicationPerso.testRpos.y(), LoxelApplicationPerso.testRpos.z()))
					.multiply(Matrix4f.createMatrixTranslate(new Vector3f(0, 14, 0)))
					.multiply(LoxelApplicationPerso.testQTransfert.getMatrix4());
			this.debugDrawProperty.drawSquare(LoxelApplicationPerso.box2HalfSize, transformation,
					new Color(0, 1, 0, 0.5f));
			this.debugDrawProperty.drawSquare(LoxelApplicationPerso.box1HalfSize,
					Matrix4f.createMatrixTranslate(new Vector3f(0, 14, 0)), new Color(0, 0, 1, 0.5f));
		}

		OpenGL.pop();
	}

	@Override
	public void onKeyboard(
			final KeySpecial special,
			final KeyKeyboard type,
			final Character value,
			final KeyStatus state) {
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
	public void onPointer(
			final KeySpecial special,
			final KeyType type,
			final int pointerID,
			final Vector2f pos,
			final KeyStatus state) {
		this.env.onPointer(special, type, pointerID, pos, state);
	}

	@Override
	public void onRegenerateDisplay(final GaleContext context) {
		this.angleLight += 0.01;
		this.lightPosition.setTransform(this.lightPosition.getTransform()
				.withPosition(new Vector3f(5 + (float) Math.cos(this.angleLight) * 7.0f,
						this.lightPosition.getTransform().getPosition().y(),
						5 + (float) Math.sin(this.angleLight) * 7.0f)));
		this.env.periodicCall();
		markDrawingIsNeeded();
	}
}
