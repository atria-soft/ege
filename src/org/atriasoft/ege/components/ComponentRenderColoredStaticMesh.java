
package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;

public class ComponentRenderColoredStaticMesh extends ComponentRender {
	ComponentStaticMesh mesh = null;
	private int oGLMatrixProjection;
	private int oGLMatrixTransformation;
	private int oGLMatrixView;
	ComponentPosition position = null;
	ResourceProgram program = null;
	
	public ComponentRenderColoredStaticMesh(Uri vertexShader, Uri fragmentShader) {
		this.program = ResourceProgram.create(vertexShader, fragmentShader);
		if (this.program != null) {
			this.oGLMatrixTransformation = this.program.getUniform("in_matrixTransformation");
			this.oGLMatrixProjection = this.program.getUniform("in_matrixProjection");
			this.oGLMatrixView = this.program.getUniform("in_matrixView");
		}
		
	}
	
	@Override
	public void addFriendComponent(Component component) {
		if (component.getType().contentEquals("static-mesh")) {
			this.mesh = (ComponentStaticMesh) component;
		}
		if (component.getType().contentEquals("position")) {
			this.position = (ComponentPosition) component;
		}
	}
	
	@Override
	public void removeFriendComponent(Component component) {
		// nothing to do.
	}
	
	@Override
	public void render() {
		this.program.use();
		
		final Matrix4f projectionMatrix = OpenGL.getMatrix();
		final Matrix4f viewMatrix = OpenGL.getCameraMatrix();
		//Log.warning("position 22  " + this.position.getTransform());
		final Matrix4f transformationMatrix = this.position.getTransform().getOpenGLMatrix();
		this.mesh.bindForRendering();
		
		this.program.uniformMatrix(this.oGLMatrixView, viewMatrix);
		this.program.uniformMatrix(this.oGLMatrixProjection, projectionMatrix);
		// Change the position for each element with the same pipeline you need to render ...
		this.program.uniformMatrix(this.oGLMatrixTransformation, transformationMatrix);
		// update of flags is done asyncronously ==> need update befor drawing...
		OpenGL.updateAllFlags();
		// Request the draw od the elements:
		this.mesh.render();
		
		this.mesh.unBindForRendering();
		
		this.program.unUse();
	}
}
