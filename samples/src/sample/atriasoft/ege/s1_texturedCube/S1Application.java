package sample.atriasoft.ege.s1_texturedCube;

import org.atriasoft.ege.ControlCameraSimple;
import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.GameStatus;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderColoredStaticMesh;
import org.atriasoft.ege.components.ComponentRenderTexturedStaticMesh;
import org.atriasoft.ege.components.ComponentStaticMesh;
import org.atriasoft.ege.components.ComponentTexture;
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

public class S1Application extends GaleApplication {
	private Quaternion basicRotation = Quaternion.IDENTITY;
	private Quaternion basicRotation2 = Quaternion.IDENTITY;
	private Environement env;
	private Camera mainView;
	private ComponentPosition objectPosition;
	private boolean signe = false;
	private ControlCameraSimple simpleControl;
	
	public S1Application() {}
	
	@Override
	public void onCreate(final Context context) {
		Log.info("On create .... [BEGIN]");
		this.env = new Environement();
		setSize(new Vector2f(800, 600));
		setTitle("Low Poly sample");
		
		final Entity gird = new Entity(this.env);
		gird.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 0, 0))));
		gird.addComponent(new ComponentStaticMesh(MeshGenerator.createGrid(5)));
		gird.addComponent(new ComponentRenderColoredStaticMesh(new Uri("DATA", "wireColor.vert", "ege"), new Uri("DATA", "wireColor.frag", "ege")));
		this.env.addEntity(gird);
		
		final Entity basicTree = new Entity(this.env);
		this.objectPosition = new ComponentPosition(new Transform3D(new Vector3f(0, 0, -5)));
		basicTree.addComponent(this.objectPosition);
		basicTree.addComponent(new ComponentStaticMesh(new Uri("RES", "cube.obj")));
		basicTree.addComponent(new ComponentTexture(new Uri("DATA", "blocks/dirt.png", "loxelEngine")));
		basicTree.addComponent(new ComponentRenderTexturedStaticMesh(new Uri("DATA", "basic.vert", "sample"), new Uri("DATA", "basic.frag", "sample")));
		this.env.addEntity(basicTree);
		
		this.mainView = new Camera();
		this.env.addCamera("default", this.mainView);
		//mainView.setPitch((float) Math.PI * -0.25f);
		//mainView.setPosition(new Vector3f(0, 0, -5));
		
		this.simpleControl = new ControlCameraSimple(this.mainView);
		this.env.addControlInterface(this.simpleControl);
		
		// start the engine.
		this.env.setPropertyStatus(GameStatus.gameStart);
		
		this.basicRotation = Quaternion.fromEulerAngles(new Vector3f(0, 0, 0.01f));
		this.basicRotation2 = Quaternion.fromEulerAngles(new Vector3f(0.003f, 0.01f, 0.001f));
		// ready to let Gale & Ege manage the display
		Log.info("==> Init APPL (END)");
		Log.info("On create .... [ END ]");
	}
	
	@Override
	public void onDraw(final Context context) {
		Log.info("On draw .... [BEGIN]");
		//Log.info("==> appl Draw ...");
		final Vector2f size = getSize();
		// Store openGl context.
		OpenGL.push();
		// set projection matrix:
		final Matrix4f tmpProjection = Matrix4f.createMatrixPerspective(3.14f * 0.5f, getAspectRatio(), 0.1f, 50000);
		//final Matrix4f tmpProjection = Matrix4f.createMatrixOrtho(300, 300, 400, 400, -50000, 50000);
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
		Log.info("On draw .... [ END ]");
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
		Log.info("On Regenerate Display .... [BEGIN]");
		//Log.verbose("Regenerate Gale Application");
		
		//this.mainView.setPitch((float) Math.PI * -0.25f);
		this.mainView.setPitch(-0.7f);
		this.mainView.setPosition(new Vector3f(0, -10, 10));
		//this.mainView.setPosition(Vector3f.ZERO);
		
		//this.objectPosition.setTransform(this.objectPosition.getTransform().withPosition(new Vector3f(2, -1, -5)));
		if (this.signe == true) {
			this.objectPosition.setTransform(this.objectPosition.getTransform().withPosition(this.objectPosition.getTransform().getPosition().add(new Vector3f(0, 0, -0.1f))));
			if (this.objectPosition.getTransform().getPosition().z() < -5) {
				this.signe = false;
			}
		} else {
			this.objectPosition.setTransform(this.objectPosition.getTransform().withPosition(this.objectPosition.getTransform().getPosition().add(new Vector3f(0, 0, 0.1f))));
			if (this.objectPosition.getTransform().getPosition().z() > 5) {
				this.signe = true;
			}
		}
		// apply a little rotation to show the element move
		this.objectPosition.setTransform(this.objectPosition.getTransform().rotate(this.basicRotation));
		this.objectPosition.setTransform(this.objectPosition.getTransform().rotate(this.basicRotation2));
		this.env.periodicCall();
		markDrawingIsNeeded();
		Log.info("On Regenerate Display .... [ END ]");
	}
}
