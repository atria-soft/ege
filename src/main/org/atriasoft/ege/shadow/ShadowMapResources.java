package org.atriasoft.ege.shadow;

import org.atriasoft.ege.engines.EngineShadow.MeshPositionPair;
import org.atriasoft.ege.engines.ShadowCaster;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2i;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;
import org.atriasoft.gale.resource.ResourceProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
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
	 * Creates or resizes as needed; the framebuffers and the 2D texture of
	 * the active unit bound by the caller are bound again afterwards.
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
		// Creating binds a framebuffer and a texture: those of the caller are given back afterwards.
		final int callerDrawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
		final int callerReadFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
		final int callerTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

		this.fboId = OpenGL.glGenFramebuffers();
		OpenGL.bindFramebuffer(this.fboId);

		// Depth texture (not renderbuffer — we need to sample it in shaders)
		this.depthTextureId = OpenGL.glGenTextures();
		OpenGL.bindTexture2D(this.depthTextureId);
		OpenGL.glTexImage2D(0, GL14.GL_DEPTH_COMPONENT24, width, height, 0,
				GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT);
		// Linear filtering enables hardware-interpolated depth comparison:
		// each texture() call on a sampler2DShadow performs bilinear interpolation
		// of 4 depth comparisons, yielding a smooth 0.0-1.0 shadow factor per texel.
		OpenGL.setTexture2DFilterLinear();
		OpenGL.setTexture2DWrapClampToBorder();
		// Border color = 1.0 → fragments outside the shadow map are fully lit
		OpenGL.setTexture2DBorderColor(1.0f, 1.0f, 1.0f, 1.0f);
		// Enable hardware shadow comparison: texture() with sampler2DShadow
		// compares the reference depth against the stored depth and returns 0.0 or 1.0
		// (with bilinear interpolation giving smooth intermediate values).
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL14.GL_COMPARE_R_TO_TEXTURE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_FUNC, GL11.GL_LEQUAL);
		OpenGL.glFramebufferTexture2D(GL30.GL_DEPTH_ATTACHMENT, this.depthTextureId);

		// No color attachment for depth-only FBO
		OpenGL.glDrawBuffer(GL11.GL_NONE);
		OpenGL.glReadBuffer(GL11.GL_NONE);

		final boolean complete = OpenGL.checkFramebufferStatus();
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, callerDrawFramebuffer);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, callerReadFramebuffer);
		OpenGL.bindTexture2D(callerTexture);
		if (!complete) {
			LOGGER.error("Failed to create shadow map FBO ({}x{})", width, height);
			destroyFbo();
			return;
		}
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
	 * Draw the depth of a registered {@link ShadowCaster} into the pass begun
	 * by a successful {@link #beginDepthPass}.
	 *
	 * @param caster the geometry to draw
	 */
	public void renderCasterDepth(final ShadowCaster caster) {
		if (this.depthProgram == null || caster == null) {
			return;
		}
		this.depthProgram.uniformMatrix(this.depthMatTransform, caster.getShadowTransform());
		OpenGL.updateAllFlags();
		caster.renderShadowDepth();
	}

	/**
	 * Begin a shadow depth pass: bind the FBO, set the viewport, clear the
	 * depth, enable the depth test, the polygon offset and the depth program.
	 * <p>
	 * The state of the caller is NOT saved here: capture it once with
	 * {@link RenderTarget#capture()} before the depth passes of a frame and
	 * restore it once after the last {@link #endDepthPass()} (in a
	 * {@code finally}, after ending a pass left open by an exception).
	 *
	 * @param resolution       Shadow map resolution (width = height)
	 * @param lightSpaceMatrix The light's view-projection matrix
	 * @return whether the pass is ready (the FBO exists): only then draw the meshes and call
	 *         {@link #endDepthPass()}
	 */
	public boolean beginDepthPass(final int resolution, final Matrix4f lightSpaceMatrix) {
		this.lightSpaceMatrix = lightSpaceMatrix;
		ensureFbo(resolution, resolution);
		if (this.fboId == -1 || this.depthProgram == null) {
			return false;
		}
		OpenGL.bindFramebuffer(this.fboId);
		OpenGL.setViewPort(new Vector2i(0, 0), new Vector2i(resolution, resolution));
		OpenGL.clearDepth(1.0f);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.enable(Flag.flag_depthTest);
		// Small polygon offset to prevent shadow acne on angled surfaces.
		// Keep values low to avoid a visible gap between objects and their shadows.
		OpenGL.enable(Flag.flag_polygonOffsetFill);
		GL11.glPolygonOffset(1.0f, 1.0f);
		OpenGL.updateAllFlags();
		this.depthProgram.use();
		this.depthProgram.uniformMatrix(this.depthLightSpaceMatrix, lightSpaceMatrix);
		return true;
	}

	/**
	 * End a depth pass begun by a successful {@link #beginDepthPass}: unbind
	 * the depth program and turn the polygon offset off. The framebuffer, the
	 * viewport and the rest of the state stay those of the pass (see
	 * {@link RenderTarget}). Calling it again changes nothing more.
	 */
	public void endDepthPass() {
		if (this.depthProgram != null) {
			this.depthProgram.unUse();
		}
		// Restore polygon offset state
		OpenGL.disable(Flag.flag_polygonOffsetFill);
		GL11.glPolygonOffset(0.0f, 0.0f);
		OpenGL.updateAllFlags();
	}

	/**
	 * Release all GPU resources: the framebuffer and the depth texture
	 * (deleted at once: call on the OpenGL thread) and the reference to the
	 * depth program. Nothing is drawn afterwards ({@link #isReady()} is false).
	 */
	public void destroy() {
		destroyFbo();
		if (this.depthProgram != null) {
			this.depthProgram.release();
			this.depthProgram = null;
		}
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

	/**
	 * The state of the caller of the depth passes that they change: draw and
	 * read framebuffers (0 = the window, or an off-screen scene target),
	 * viewport (possibly a sub-rectangle of it), depth test, depth clear
	 * value, polygon offset (fill flag, factor, units) and program in use.
	 * Captured once before the passes of a frame and given back once after,
	 * so that the shadow maps never leave the caller drawing into the window,
	 * into the wrong rectangle or with the state of a depth pass.
	 */
	public static final class RenderTarget {
		private final int drawFramebuffer;
		private final int readFramebuffer;
		private final int[] viewport;
		private final boolean depthTest;
		private final float clearDepth;
		private final boolean polygonOffsetFill;
		private final float polygonOffsetFactor;
		private final float polygonOffsetUnits;
		private final int program;

		private RenderTarget(final int drawFramebuffer, final int readFramebuffer, final int[] viewport,
				final boolean depthTest, final float clearDepth, final boolean polygonOffsetFill,
				final float polygonOffsetFactor, final float polygonOffsetUnits, final int program) {
			this.drawFramebuffer = drawFramebuffer;
			this.readFramebuffer = readFramebuffer;
			this.viewport = viewport;
			this.depthTest = depthTest;
			this.clearDepth = clearDepth;
			this.polygonOffsetFill = polygonOffsetFill;
			this.polygonOffsetFactor = polygonOffsetFactor;
			this.polygonOffsetUnits = polygonOffsetUnits;
			this.program = program;
		}

		/** The state bound now (the flags gale still had to apply are applied first). */
		public static RenderTarget capture() {
			// gale applies its flags lazily: once applied, OpenGL tells the state the caller asked for.
			OpenGL.updateAllFlags();
			final int[] viewport = new int[4];
			GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
			return new RenderTarget(GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),
					GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING), viewport, GL11.glIsEnabled(GL11.GL_DEPTH_TEST),
					GL11.glGetFloat(GL11.GL_DEPTH_CLEAR_VALUE), GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_FILL),
					GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_FACTOR), GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_UNITS),
					GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM));
		}

		/** Bind the captured framebuffers, viewport, depth and polygon offset state and program again. */
		public void restore() {
			GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.drawFramebuffer);
			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, this.readFramebuffer);
			OpenGL.setViewPort(new Vector2i(this.viewport[0], this.viewport[1]),
					new Vector2i(this.viewport[2], this.viewport[3]));
			setFlag(Flag.flag_depthTest, this.depthTest);
			setFlag(Flag.flag_polygonOffsetFill, this.polygonOffsetFill);
			OpenGL.updateAllFlags();
			GL11.glPolygonOffset(this.polygonOffsetFactor, this.polygonOffsetUnits);
			OpenGL.clearDepth(this.clearDepth);
			OpenGL.programUse(this.program);
		}

		private static void setFlag(final Flag flag, final boolean enabled) {
			if (enabled) {
				OpenGL.enable(flag);
			} else {
				OpenGL.disable(flag);
			}
		}
	}
}
