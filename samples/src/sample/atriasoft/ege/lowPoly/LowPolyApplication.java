package sample.atriasoft.ege.lowPoly;

import org.atriasoft.ege.ControlCameraSimple;
import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.GameStatus;
import org.atriasoft.ege.Light;
import org.atriasoft.ege.Material;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentLight;
import org.atriasoft.ege.components.ComponentLightSun;
import org.atriasoft.ege.components.ComponentMaterial;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderColoredStaticMesh;
import org.atriasoft.ege.components.ComponentRenderTexturedMaterialsStaticMesh;
import org.atriasoft.ege.components.ComponentRenderTexturedStaticMesh;
import org.atriasoft.ege.components.ComponentStaticMesh;
import org.atriasoft.ege.components.ComponentTexture;
import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.ege.tools.MeshGenerator;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.GaleApplication;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;
import org.atriasoft.gale.context.Context;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeySpecial;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.gale.key.KeyType;

public class LowPolyApplication extends GaleApplication {
	private float angleLight = 0;
	private Quaternion basicRotation = Quaternion.IDENTITY;
	private Quaternion basicRotation2 = Quaternion.IDENTITY;
	private Environement env;
	private ComponentPosition lightPosition;
	private Material materialCube;
	private ComponentPosition objectPosition;
	private ControlCameraSimple simpleControl;
	
	public LowPolyApplication() {}
	
	@Override
	public void onCreate(final Context context) {
		this.env = new Environement();
		setSize(new Vector2f(800, 600));
		setTitle("Low Poly sample");
		
		// simple sun to have a global light ...
		final Entity sun = new Entity(this.env);
		sun.addComponent(new ComponentPosition(new Transform3D(new Vector3f(1000, 1000, 1000))));
		sun.addComponent(new ComponentLightSun(new Light(new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0, 0))));
		this.env.addEntity(sun);
		
		// add a cube to show where in the light ...
		final Entity localLight = new Entity(this.env);
		this.lightPosition = new ComponentPosition(new Transform3D(new Vector3f(-10, -10, 0)));
		localLight.addComponent(this.lightPosition);
		localLight.addComponent(new ComponentStaticMesh(new Uri("RES", "cube.obj")));
		localLight.addComponent(new ComponentTexture(new Uri("RES", "grass.png")));
		localLight.addComponent(new ComponentLight(new Light(new Vector3f(0, 2, 0), new Vector3f(0, 0, 0), new Vector3f(0.8f, 0.01f, 0.002f))));
		localLight.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "loxelEngine"), new Uri("DATA", "basic.frag", "loxelEngine")));
		this.env.addEntity(localLight);
		
		final Entity gird = new Entity(this.env);
		gird.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 0, 0))));
		gird.addComponent(new ComponentStaticMesh(MeshGenerator.createGrid(5)));
		gird.addComponent(new ComponentRenderColoredStaticMesh(new Uri("DATA", "wireColor.vert", "ege"), new Uri("DATA", "wireColor.frag", "ege")));
		this.env.addEntity(gird);
		
		final Entity basicTree = new Entity(this.env);
		this.objectPosition = new ComponentPosition(new Transform3D(new Vector3f(0, 0, 0)));
		basicTree.addComponent(this.objectPosition);
		this.materialCube = new Material();
		basicTree.addComponent(new ComponentMaterial(this.materialCube));
		basicTree.addComponent(new ComponentStaticMesh(new Uri("RES", "cube.obj")));
		basicTree.addComponent(new ComponentTexture(new Uri("RES", "grass.png")));
		basicTree.addComponent(new ComponentRenderTexturedMaterialsStaticMesh(new Uri("DATA", "basicMaterial.vert", "loxelEngine"), new Uri("DATA", "basicMaterial.frag", "loxelEngine"),
				(EngineLight) this.env.getEngine(EngineLight.ENGINE_NAME)));
		this.env.addEntity(basicTree);
		
		for (int xxx = -10; xxx < 10; xxx++) {
			for (int yyy = -10; yyy < 10; yyy++) {
				final Entity superGrass = new Entity(this.env);
				superGrass.addComponent(new ComponentPosition(new Transform3D(new Vector3f(xxx, yyy, -1))));
				superGrass.addComponent(new ComponentMaterial(new Material()));
				superGrass.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
				superGrass.addComponent(new ComponentTexture(new Uri("RES", "dirt.png")));
				superGrass.addComponent(new ComponentRenderTexturedMaterialsStaticMesh(new Uri("DATA", "basicMaterial.vert", "loxelEngine"), new Uri("DATA", "basicMaterial.frag", "loxelEngine"),
						(EngineLight) this.env.getEngine(EngineLight.ENGINE_NAME)));
				this.env.addEntity(superGrass);
			}
		}
		
		final Camera mainView = new Camera();
		this.env.addCamera("default", mainView);
		mainView.setPitch((float) Math.PI * -0.25f);
		mainView.setPosition(new Vector3f(0, -5, 5));
		
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
	public void onDraw(final Context context) {
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
		final Color bgColor = new Color(0.0f, 1.0f, 0.0f, 1.0f);
		OpenGL.clearColor(bgColor);
		// real clear request:
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_colorBuffer);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.enable(Flag.flag_depthTest);
		
		this.env.render(20, "default");
		
		// Restore context of matrix
		OpenGL.pop();
	}
	
	@Override
	public void onKeyboard(final KeySpecial special, final KeyKeyboard type, final Character value, final KeyStatus state) {
		this.env.onKeyboard(special, type, value, state);
	}
	
	@Override
	public void onPointer(final KeySpecial special, final KeyType type, final int pointerID, final Vector2f pos, final KeyStatus state) {
		this.env.onPointer(special, type, pointerID, pos, state);
	}
	
	@Override
	public void onRegenerateDisplay(final Context context) {
		//Log.verbose("Regenerate Gale Application");
		//materialCube.setAmbientFactor(new Vector3f(1.0f,1.0f,1.0f));
		// apply a little rotation to show the element move
		//objectPosition.getTransform().applyRotation(basicRotation);
		//objectPosition.getTransform().applyRotation(basicRotation2);
		this.angleLight += 0.01;
		this.lightPosition.setTransform(this.lightPosition.getTransform()
				.withPosition(new Vector3f((float) Math.cos(this.angleLight) * 7.0f, (float) Math.sin(this.angleLight) * 7.0f, this.lightPosition.getTransform().getPosition().z())));
		this.env.periodicCall();
		markDrawingIsNeeded();
	}
}
