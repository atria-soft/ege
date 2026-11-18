package org.atriasoft.ege.widget;

import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.camera.ProjectionInterface;
import org.atriasoft.ege.camera.ProjectionPerspective;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderColoredStaticMesh;
import org.atriasoft.ege.components.ComponentStaticMesh;
import org.atriasoft.ege.tools.MeshGenerator;
import org.atriasoft.esignal.Connection;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.event.EventTime;
import org.atriasoft.ewol.object.EwolObject;
import org.atriasoft.ewol.widget.Widget;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;

/**
 * Base ewol widget showing an ege {@link Environement} through a perspective camera.
 * It owns the environment, a default camera, redraws itself on every periodic call and
 * forwards keyboard and pointer events to the environment. Subclasses draw their own
 * overlay in {@link #onDrawScene()}.
 */
public class Ege3DSceneWidget extends Widget {

	/**
	 * Periodic call to update graphic display
	 * @param self the scene instance
	 * @param event Time generic event
	 */
	protected static void periodicCall(final Ege3DSceneWidget self, final EventTime event) {
		self.markToRedraw();
	}

	// Widget display camera
	public Camera mainView;
	// Widget view mode
	public ProjectionInterface projection;
	// Environment model system.
	protected Environement env;

	// Periodic call handle to remove it when needed
	protected Connection periodicConnectionHandle = new Connection();

	/**
	 * Constructor
	 */
	public Ege3DSceneWidget() {
		this.propertyCanFocus = true;
		markToRedraw();
		// can not support multiple click...
		setMouseLimit(2);
		this.env = new Environement();

		// default camera....
		this.mainView = new Camera();
		this.env.addCamera("default", this.mainView);
		this.mainView.setPitch((float) Math.PI * 0.25f);
		this.mainView.setPosition(new Vector3f(4, 5, 5));

		this.projection = new ProjectionPerspective();

		// Connect to periodic callback to ensure the 3D scene is refreshed
		// even when no other widget triggers a redraw
		this.periodicConnectionHandle = EwolObject.getObjectManager().periodicCall
				.connect(this, Ege3DSceneWidget::periodicCall);
	}

	@Override
	public void destroy() {
		// Stop the periodic redraw: the connection is not tied to the widget's life otherwise.
		this.periodicConnectionHandle.close();
		super.destroy();
	}

	public Environement getEnvironement() {
		return this.env;
	}

	public Entity addGenericGird() {
		// Simple Grid
		final Entity gird = new Entity(this.env);
		gird.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 0, 0))));
		gird.addComponent(new ComponentStaticMesh(MeshGenerator.createGrid(5)));
		gird.addComponent(new ComponentRenderColoredStaticMesh(new Uri("DATA", "wireColor.vert", "ege"),
				new Uri("DATA", "wireColor.frag", "ege")));
		this.env.addEntity(gird);
		return gird;
	}

	@Override
	public void calculateMinMaxSize() {
		// call main class
		super.calculateMinMaxSize();
		this.minSize = Vector2f.VALUE_128;
		// verify the min max of the min size ...
		checkMinSize();
	}

	protected float getAspectRatio() {
		if (this.size.y() == 0.0f) {
			return 1.0f;
		}
		return this.size.x() / this.size.y();
	}

	@Override
	public void onChangeSize() {
		super.onChangeSize();
		// update the projection matrix on the view size;
		this.projection.updateMatrix(getSize());
	}

	@Override
	protected void onDraw() {
		// Store openGl context.
		OpenGL.push();
		// set projection matrix:
		OpenGL.setMatrix(this.projection.getMatrix());

		// set the basic openGL view port: (Draw in all the windows...)
		OpenGL.setViewPort(new Vector2f(0, 0), new Vector2f(getSize().x(), getSize().y()));

		// real clear request:
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.enable(Flag.flag_depthTest);
		this.env.render(20, "default");
		onDrawScene();
		OpenGL.disable(Flag.flag_depthTest);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		// Restore context of matrix
		OpenGL.pop();
	}

	protected void onDrawScene() {
		// nothing to do...
	}

	@Override
	public boolean onEventEntry(final EventEntry event) {
		this.env.onKeyboard(event.specialKey(), event.type(), event.getChar(), event.status());
		return true;
	}

	@Override
	public boolean onEventInput(final EventInput event) {
		keepFocus();
		final Vector2f relPos = relativePosition(event.pos());
		this.env.onPointer(event.specialKey(), event.type(), event.inputId(), relPos, event.status());
		return true;
	}

	@Override
	public void onRegenerateDisplay() {
		// IMPORTANT: needRedraw() resets the internal needRegenerateDisplay flag.
		// Without this call, markToRedraw() short-circuits (the flag is already true)
		// and never notifies the WidgetManager that a redraw is needed, causing the
		// render loop to skip drawing this widget entirely.
		needRedraw();
		this.env.periodicCall();
		markToRedraw();
	}
}
