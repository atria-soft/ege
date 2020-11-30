package org.atriasoft.gameengine.components;

import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;
import org.atriasoft.gameengine.Component;

public class ComponentRenderTexturedStaticMesh extends ComponentRender {
	ComponentStaticMesh mesh = null;
	ComponentTexture texture = null;
	ComponentPosition position = null;
	ResourceProgram program = null;
	private int GLMatrixTransformation;
	private int GLMatrixProjection;
	private int GLMatrixView;
	
	public ComponentRenderTexturedStaticMesh(Uri vertexShader, Uri fragmentShader) {
		this.program = ResourceProgram.create(vertexShader, fragmentShader);
		if (this.program != null) {
			this.GLMatrixTransformation = this.program.getUniform("in_matrixTransformation");
			this.GLMatrixProjection     = this.program.getUniform("in_matrixProjection");
			this.GLMatrixView           = this.program.getUniform("in_matrixView");
		}
		
	}
	@Override
	public void addFriendComponent(Component component) {
		if (component.getType().contentEquals("static-mesh")) {
			mesh = (ComponentStaticMesh)component;
		}
		if (component.getType().contentEquals("texture")) {
			texture = (ComponentTexture)component;
		}
		if (component.getType().contentEquals("position")) {
			position = (ComponentPosition)component;
		}
	}
	@Override
	public void removeFriendComponent(Component component) {
		// nothing to do.
	}
	@Override
	public void render() {
		this.program.use();
		Matrix4f projectionMatrix = OpenGL.getMatrix();
		Matrix4f viewMatrix = OpenGL.getCameraMatrix();
		Matrix4f transformationMatrix = position.getTransform().getOpenGLMatrix();
		this.mesh.bindForRendering();
		this.texture.bindForRendering();
		this.program.uniformMatrix(this.GLMatrixView, viewMatrix);
		this.program.uniformMatrix(this.GLMatrixProjection, projectionMatrix);
		// Change the position for each element with the same pipeline you need to render ...
		this.program.uniformMatrix(this.GLMatrixTransformation, transformationMatrix);
		// update of flags is done asynchronously ==> need update before drawing...
		OpenGL.updateAllFlags();
		// Request the draw all the elements:
		this.mesh.render();
		this.texture.unBindForRendering();
		this.mesh.unBindForRendering();
		this.program.unUse();
	}
}

