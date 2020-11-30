package org.atriasoft.gameengine.samples.lowPoly;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.Application;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;
import org.atriasoft.gale.context.Context;
import org.atriasoft.gameengine.ControlCameraSimple;
import org.atriasoft.gameengine.Entity;
import org.atriasoft.gameengine.Environement;
import org.atriasoft.gameengine.GameStatus;
import org.atriasoft.gameengine.Light;
import org.atriasoft.gameengine.Material;
import org.atriasoft.gameengine.camera.Camera;
import org.atriasoft.gameengine.components.ComponentLight;
import org.atriasoft.gameengine.components.ComponentLightSun;
import org.atriasoft.gameengine.components.ComponentMaterial;
import org.atriasoft.gameengine.components.ComponentPosition;
import org.atriasoft.gameengine.components.ComponentRenderColoredStaticMesh;
import org.atriasoft.gameengine.components.ComponentRenderTexturedMaterialsStaticMesh;
import org.atriasoft.gameengine.components.ComponentRenderTexturedStaticMesh;
import org.atriasoft.gameengine.components.ComponentStaticMesh;
import org.atriasoft.gameengine.components.ComponentTexture;
import org.atriasoft.gameengine.engines.EngineLight;
import org.atriasoft.gameengine.tools.MeshGenerator;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeySpecial;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.gale.key.KeyType;

public class LowPolyApplication extends Application {
	private Environement env;
	private ComponentPosition objectPosition;
	private Quaternion basicRotation = Quaternion.identity();
	private Quaternion basicRotation2 = Quaternion.identity();
	private boolean creationDone;
	private ControlCameraSimple simpleControl;
	private Material materialCube;
	private ComponentPosition lightPosition;
	private float angleLight = 0;
	public LowPolyApplication(){
		creationDone = false;
	}
	@Override
	public void onCreate(Context context) {
		env = new Environement();
		this.canDraw = true;
		setSize(new Vector2f(800, 600));
		setTitle("Low Poly sample");

		// simple sun to have a global light ...
		Entity sun = new Entity(this.env);
		sun.addComponent(new ComponentPosition(new Transform3D(new Vector3f(1000,1000,1000))));
		sun.addComponent(new ComponentLightSun(new Light(new Vector3f(0.4f,0.4f,0.4f), new Vector3f(0,0,0), new Vector3f(0.8f,0,0))));
		env.addEntity(sun);
		
		// add a cube to show where in the light ...
		Entity localLight = new Entity(this.env);
		lightPosition = new ComponentPosition(new Transform3D(new Vector3f(-10,-10,0)));
		localLight.addComponent(lightPosition);
		localLight.addComponent(new ComponentStaticMesh(new Uri("RES", "cube.obj")));
		localLight.addComponent(new ComponentTexture(new Uri("RES", "grass.png")));
		localLight.addComponent(new ComponentLight(new Light(new Vector3f(0,2,0), new Vector3f(0,0,0), new Vector3f(0.8f,0.01f,0.002f))));
		localLight.addComponent(new ComponentRenderTexturedStaticMesh(
				new Uri("DATA", "basic.vert"),
				new Uri("DATA", "basic.frag")));
		env.addEntity(localLight);
		
		Entity gird = new Entity(this.env);
		gird.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0,0,0))));
		gird.addComponent(new ComponentStaticMesh(MeshGenerator.createGrid(5)));
		gird.addComponent(new ComponentRenderColoredStaticMesh(
				new Uri("DATA_EGE", "wireColor.vert"),
				new Uri("DATA_EGE", "wireColor.frag")));
		env.addEntity(gird);

		Entity basicTree = new Entity(this.env);
		objectPosition = new ComponentPosition(new Transform3D(new Vector3f(0,0,0)));
		basicTree.addComponent(objectPosition);
		materialCube = new Material();
		basicTree.addComponent(new ComponentMaterial(materialCube));
		basicTree.addComponent(new ComponentStaticMesh(new Uri("RES", "cube.obj")));
		basicTree.addComponent(new ComponentTexture(new Uri("RES", "grass.png")));
		basicTree.addComponent(new ComponentRenderTexturedMaterialsStaticMesh(
				new Uri("DATA", "basicMaterial.vert"),
				new Uri("DATA", "basicMaterial.frag"),
				(EngineLight)env.getEngine(EngineLight.ENGINE_NAME)));
		env.addEntity(basicTree);
		
		for (int xxx=-10; xxx<10; xxx++) {
			for (int yyy=-10; yyy<10; yyy++) {
				Entity superGrass = new Entity(this.env);
				superGrass.addComponent(new ComponentPosition(new Transform3D(new Vector3f(xxx,yyy,-1))));
				superGrass.addComponent(new ComponentMaterial(new Material()));
				superGrass.addComponent(new ComponentStaticMesh(new Uri("RES", "cube-one.obj")));
				superGrass.addComponent(new ComponentTexture(new Uri("RES", "dirt.png")));
				superGrass.addComponent(new ComponentRenderTexturedMaterialsStaticMesh(
						new Uri("DATA", "basicMaterial.vert"),
						new Uri("DATA", "basicMaterial.frag"),
						(EngineLight)env.getEngine(EngineLight.ENGINE_NAME)));
				env.addEntity(superGrass);
			}
		}
		
		
		Camera mainView = new Camera();
		env.addCamera("default", mainView);
		mainView.setPitch((float)Math.PI*-0.25f);
		mainView.setPosition(new Vector3f(0,-5,5));
		
		this.simpleControl = new ControlCameraSimple(mainView);
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
		//materialCube.setAmbientFactor(new Vector3f(1.0f,1.0f,1.0f));
		// apply a little rotation to show the element move
		//objectPosition.getTransform().applyRotation(basicRotation);
		//objectPosition.getTransform().applyRotation(basicRotation2);
		angleLight += 0.01;
		lightPosition.getTransform().getPosition().x = (float)Math.cos(angleLight) * 7.0f;
		lightPosition.getTransform().getPosition().y = (float)Math.sin(angleLight) * 7.0f;
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
		Color bgColor = new Color(0.0f, 1.0f, 0.0f, 1.0f);
		OpenGL.clearColor(bgColor);
		// real clear request:
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_colorBuffer);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.enable(Flag.flag_depthTest);
		
		env.render(20, "default");
		
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
		env.onKeyboard(special, type, value, state);
	}
}
