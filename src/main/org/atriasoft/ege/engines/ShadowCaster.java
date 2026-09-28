package org.atriasoft.ege.engines;

import org.atriasoft.etk.math.Matrix4f;

/**
 * Geometry drawn into the shadow maps of {@link EngineShadow} that is not
 * the mesh component of an ege entity (a batch the application draws
 * itself, for instance): registered with
 * {@link EngineShadow#addShadowCaster}, drawn by every depth pass after the
 * meshes of the entities.
 * <p>
 * When {@link #renderShadowDepth()} is called, the depth pass has bound its
 * framebuffer, its viewport and its program, whose
 * {@code in_matrixTransformation} is {@link #getShadowTransform()} and whose
 * vertex positions are read from attribute 0: the caster only binds its
 * vertices, draws its triangles and unbinds them. Called on the rendering
 * thread.
 */
public interface ShadowCaster {

	/** Model matrix of the vertices: the identity for vertices already in world space. */
	default Matrix4f getShadowTransform() {
		return Matrix4f.IDENTITY;
	}

	/** Bind the vertices, draw the triangles into the bound depth pass, unbind them. */
	void renderShadowDepth();
}
