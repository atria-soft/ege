package org.atriasoft.ege.components.part;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;

public class TransformRender implements PartRenderInterface {

	private int GLMatrixProjection;
	private int GLMatrixTransformation;
	private int GLMatrixView;
	private PositionningInterface position = null;
	
	public TransformRender() {
		
	}

	@Override
	public void init(final ResourceProgram program) {
		this.GLMatrixTransformation = program.getUniform("in_matrixTransformation");
		this.GLMatrixProjection = program.getUniform("in_matrixProjection");
		this.GLMatrixView = program.getUniform("in_matrixView");
	}

	@Override
	public void bindForRendering(final ResourceProgram program) {
		// preparing stage
		final Matrix4f projectionMatrix = OpenGL.getMatrix();
		final Matrix4f viewMatrix = OpenGL.getCameraMatrix();
		Matrix4f transformationMatrix = this.position.getTransform().getOpenGLMatrix();
		// injection stage
		program.uniformMatrix(this.GLMatrixView, viewMatrix);
		program.uniformMatrix(this.GLMatrixProjection, projectionMatrix);
		// Change the position for each element with the same pipeline you need to render ...
		program.uniformMatrix(this.GLMatrixTransformation, transformationMatrix);
	}

	
	@Override
	public void unBindForRendering() {
	
	}
	
	public PositionningInterface getPositionning() {
		return this.position;
	}

	public void setPositionning(final PositionningInterface component) {
		this.position = component;
	}
}
