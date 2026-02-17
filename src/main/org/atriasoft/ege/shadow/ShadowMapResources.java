package org.atriasoft.ege.shadow;

import org.atriasoft.ege.engines.EngineShadow.MeshPositionPair;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;
import org.atriasoft.gale.resource.ResourceProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GPU resources for a single shadow map: depth-only FBO, depth texture,
 * and the depth-pass shader program.
 * <p>
 * One instance is created per active shadow caster. The depth texture
 * can be sampled by the main rendering shaders to compute shadow factors.
 *
 * @see org.atriasoft.ege.engines.EngineShadow
 */
public class ShadowMapResources {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShadowMapResources.class);

	// Depth-pass shader
	private ResourceProgram depthProgram;
	private int depthMatTransform = -1;
	private int depthLightSpaceMatrix = -1;

	// FBO (depth-only)
	private int fboId = -1;
	private int depthTextureId = -1;
	private int fboWidth = 0;
	private int fboHeight = 0;

	// The light-space matrix computed for this shadow map
	private Matrix4f lightSpaceMatrix = Matrix4f.IDENTITY;

	public ShadowMapResources() {
	}

	/**
	 * Initialize the depth-pass shader program.
	 * Must be called once before rendering (requires OpenGL context).
	 */
	public void init() {
		this.depthProgram = ResourceProgram.create(
				new Uri("DATA", "shadow/depthPass.vert", "ege"),
				new Uri("DATA", "shadow/depthPass.frag", "ege"));
		if (this.depthProgram != null) {
			this.depthMatTransform = this.depthProgram.getUniform("in_matrixTransformation");
			this.depthLightSpaceMatrix = this.depthProgram.getUniform("in_lightSpaceMatrix");
		} else {
			LOGGER.error("Failed to create depth-pass shader program");
		}
	}

	/**
	 * Ensure the depth FBO exists and matches the requested dimensions.
	 * Creates or resizes as needed.
	 *
	 * @param width  Shadow map width in pixels
	 * @param height Shadow map height in pixels
	 */
	public void ensureFbo(final int width, final int height) {
		if (this.fboId != -1 && this.fboWidth == width && this.fboHeight == height) {
			return;
		}
		if (this.fboId != -1) {
			destroyFbo();
		}

		this.fboId = OpenGL.glGenFramebuffers();
		OpenGL.bindFramebuffer(this.fboId);

		// Depth texture (not renderbuffer — we need to sample it in shaders)
		this.depthTextureId = OpenGL.glGenTextures();
		OpenGL.bindTexture2D(this.depthTextureId);
		OpenGL.glTexImage2D(0, GL14.GL_DEPTH_COMPONENT24, width, height, 0,
				GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT);
		OpenGL.setTexture2DFilterNearest();
		OpenGL.setTexture2DWrapClampToBorder();
		// Border color = 1.0 → fragments outside the shadow map are fully lit
		OpenGL.setTexture2DBorderColor(1.0f, 1.0f, 1.0f, 1.0f);
		OpenGL.glFramebufferTexture2D(GL30.GL_DEPTH_ATTACHMENT, this.depthTextureId);

		// No color attachment for depth-only FBO
		OpenGL.glDrawBuffer(GL11.GL_NONE);
		OpenGL.glReadBuffer(GL11.GL_NONE);

		if (!OpenGL.checkFramebufferStatus()) {
			LOGGER.error("Failed to create shadow map FBO ({}x{})", width, height);
			destroyFbo();
			OpenGL.bindFramebuffer(0);
			return;
		}

		OpenGL.bindFramebuffer(0);
		this.fboWidth = width;
		this.fboHeight = height;
		LOGGER.debug("Created shadow map FBO: {}x{}", width, height);
	}

	private void destroyFbo() {
		if (this.depthTextureId != -1) {
			OpenGL.glDeleteTextures(this.depthTextureId);
			this.depthTextureId = -1;
		}
		if (this.fboId != -1) {
			OpenGL.glDeleteFramebuffers(this.fboId);
			this.fboId = -1;
		}
		this.fboWidth = 0;
		this.fboHeight = 0;
	}

	/**
	 * Render a single mesh into the shadow map depth buffer.
	 *
	 * @param pair The mesh+position pair to render
	 */
	public void renderMeshDepth(final MeshPositionPair pair) {
		if (this.depthProgram == null || pair == null) {
			return;
		}
		final Matrix4f transformMatrix = pair.position.getTransform().getOpenGLMatrix();
		this.depthProgram.uniformMatrix(this.depthMatTransform, transformMatrix);

		pair.bind.run();
		OpenGL.updateAllFlags();
		pair.render.run();
		pair.unbind.run();
	}

	/**
	 * Begin a shadow depth pass: bind FBO, set viewport, clear depth, enable depth test.
	 *
	 * @param resolution       Shadow map resolution (width = height)
	 * @param lightSpaceMatrix The light's view-projection matrix
	 */
	public void beginDepthPass(final int resolution, final Matrix4f lightSpaceMatrix) {
		this.lightSpaceMatrix = lightSpaceMatrix;
		ensureFbo(resolution, resolution);
		if (this.fboId == -1) {
			return;
		}

		OpenGL.bindFramebuffer(this.fboId);
		OpenGL.setViewPort(
				new org.atriasoft.etk.math.Vector2f(0, 0),
				new org.atriasoft.etk.math.Vector2f(resolution, resolution));
		OpenGL.clearDepth(1.0f);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.enable(Flag.flag_depthTest);
		OpenGL.updateAllFlags();

		if (this.depthProgram != null) {
			this.depthProgram.use();
			this.depthProgram.uniformMatrix(this.depthLightSpaceMatrix, lightSpaceMatrix);
		}
	}

	/**
	 * End the depth pass: unbind shader and restore default framebuffer.
	 *
	 * @param viewportSize The original viewport size to restore
	 */
	public void endDepthPass(final org.atriasoft.etk.math.Vector2f viewportSize) {
		if (this.depthProgram != null) {
			this.depthProgram.unUse();
		}
		OpenGL.bindFramebuffer(0);
		OpenGL.setViewPort(
				new org.atriasoft.etk.math.Vector2f(0, 0),
				viewportSize);
	}

	/**
	 * Release all GPU resources.
	 */
	public void destroy() {
		destroyFbo();
	}

	/** @return The OpenGL texture ID of the depth texture (for sampling in shaders) */
	public int getDepthTextureId() {
		return this.depthTextureId;
	}

	/** @return The light-space matrix used for the last depth pass */
	public Matrix4f getLightSpaceMatrix() {
		return this.lightSpaceMatrix;
	}

	/** @return true if the FBO is valid and ready */
	public boolean isReady() {
		return this.fboId != -1 && this.depthProgram != null;
	}
}
