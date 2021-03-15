package org.atriasoft.ege.samples.s1_texturedCube;

import org.atriasoft.ege.ControlCameraSimple;
import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.GameStatus;
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
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderColoredStaticMesh;
import org.atriasoft.ege.components.ComponentRenderTexturedStaticMesh;
import org.atriasoft.ege.components.ComponentStaticMesh;
import org.atriasoft.ege.components.ComponentTexture;
import org.atriasoft.ege.tools.MeshGenerator;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeySpecial;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.gale.key.KeyType;

public class S1Application extends Application {
	private Environement env;
	private ComponentPosition objectPosition;
	private Quaternion basicRotation = Quaternion.identity();
	private Quaternion basicRotation2 = Quaternion.identity();
	private boolean creationDone;
	private ControlCameraSimple simpleControl;
	public S1Application(){
		creationDone = false;
	}
	@Override
	public void onCreate(Context context) {
		env = new Environement();
		this.canDraw = true;
		setSize(new Vector2f(800, 600));
		setTitle("Low Poly sample");

		Entity gird = new Entity(this.env);
		gird.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0,0,0))));
		gird.addComponent(new ComponentStaticMesh(MeshGenerator.createGrid(5)));
		gird.addComponent(new ComponentRenderColoredStaticMesh(new Uri("DATA_EGE", "wireColor.vert"), new Uri("DATA_EGE", "wireColor.frag")));
		env.addEntity(gird);
		
		Entity basicTree = new Entity(this.env);
		objectPosition = new ComponentPosition(new Transform3D(new Vector3f(0,0,0)));
		basicTree.addComponent(objectPosition);
		basicTree.addComponent(new ComponentStaticMesh(new Uri("RES", "cube.obj")));
		basicTree.addComponent(new ComponentTexture(new Uri("RES", "dirt.png")));
		basicTree.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert"), new Uri("DATA", "basic.frag")));
		env.addEntity(basicTree);
		
		
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
		// apply a litthe rotation to show the element move
		objectPosition.getTransform().applyRotation(basicRotation);
		objectPosition.getTransform().applyRotation(basicRotation2);
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
