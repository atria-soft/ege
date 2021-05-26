package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;

public class ComponentRenderMeshPalette extends ComponentRender {
	private int GLMatrixProjection;
	private int GLMatrixTransformation;
	private int GLMatrixView;
	ComponentMesh mesh = null;
	private ComponentPhysics playerPhysics = null;
	ComponentPosition position = null;
	ResourceProgram program = null;
	ComponentTexturePalette texture = null;
	
	public ComponentRenderMeshPalette(final Uri vertexShader, final Uri fragmentShader) {
		this.program = ResourceProgram.create(vertexShader, fragmentShader);
		if (this.program != null) {
			this.GLMatrixTransformation = this.program.getUniform("in_matrixTransformation");
			this.GLMatrixProjection = this.program.getUniform("in_matrixProjection");
			this.GLMatrixView = this.program.getUniform("in_matrixView");
		}
		
	}
	
	@Override
	public void addFriendComponent(final Component component) {
		if (component.getType().contentEquals("mesh")) {
			this.mesh = (ComponentMesh) component;
		}
		if (component.getType().contentEquals("texture")) {
			this.texture = (ComponentTexturePalette) component;
		}
		if (component.getType().contentEquals("position")) {
			this.position = (ComponentPosition) component;
		}
		if (component.getType().contentEquals("physics")) {
			this.playerPhysics = (ComponentPhysics) component;
		}
	}
	
	@Override
	public void removeFriendComponent(final Component component) {
		// nothing to do.
	}
	
	@Override
	public void render() {
		this.program.use();
		final Matrix4f projectionMatrix = OpenGL.getMatrix();
		final Matrix4f viewMatrix = OpenGL.getCameraMatrix();
		Matrix4f transformationMatrix = null;
		if (this.position != null) {
			//Log.warning("position " + this.position.getTransform());
			transformationMatrix = this.position.getTransform().getOpenGLMatrix();
		} else if (this.playerPhysics != null) {
			//Log.warning("playerPosition " + this.playerPhysics.getTransform());
			transformationMatrix = this.playerPhysics.getTransform().getOpenGLMatrix();
		}
		this.mesh.bindForRendering();
		this.texture.bindForRendering();
		this.program.uniformMatrix(this.GLMatrixView, viewMatrix);
		this.program.uniformMatrix(this.GLMatrixProjection, projectionMatrix);
		// Change the position for each element with the same pipeline you need to render ...
		this.program.uniformMatrix(this.GLMatrixTransformation, transformationMatrix);
		// update of flags is done asynchronously ==> need update before drawing...
		OpenGL.updateAllFlags();
		// Request the draw all the elements:
		this.mesh.renderArrays();
		
		this.texture.unBindForRendering();
		this.mesh.unBindForRendering();
		this.program.unUse();
	}
}
