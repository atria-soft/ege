package org.atriasoft.ege.postprocess;

import org.atriasoft.ege.components.ComponentMesh;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.ResourceProgram;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared GPU resources for the post-processing pipeline.
 * <p>
 * Manages the silhouette FBO (off-screen render target), shader programs,
 * and the fullscreen quad VAO used by all post-process effects.
 * <p>
 * Created once by {@link org.atriasoft.ege.engines.EnginePostProcess} and
 * passed to each effect during rendering. Effects access shader programs
 * and uniform locations through getters to configure their draws.
 * <p>
 * The FBO is lazily initialized and automatically resized when the viewport
 * dimensions change. It uses a single-channel R8 texture for the silhouette
 * mask and a 24-bit depth renderbuffer for correct self-occlusion.
 *
 * @see PostProcessEffect
 * @see org.atriasoft.ege.engines.EnginePostProcess
 */
public class PostProcessResources {
	private static final Logger LOGGER = LoggerFactory.getLogger(PostProcessResources.class);

	// --- Silhouette shader (renders mesh white-on-black into FBO) ---
	private ResourceProgram silhouetteProgram;
	private int silhouetteMatTransform = -1;
	private int silhouetteMatProj = -1;
	private int silhouetteMatView = -1;

	// --- Outline shader (fullscreen quad, circular neighbor sampling) ---
	private ResourceProgram outlineProgram;
	private int outlineSilhouetteTex = -1;
	private int outlineColorUniform = -1;
	private int outlineSizeUniform = -1;
	private int outlineTexelSizeUniform = -1;

	// --- Mask overlay shader (fullscreen quad, colors inside silhouette) ---
	private ResourceProgram maskOverlayProgram;
	private int maskOverlaySilhouetteTex = -1;
	private int maskOverlayColorUniform = -1;

	// --- FBO (lazy init, resized when viewport changes) ---
	private int fboId = -1;
	private int fboTextureId = -1;
	private int fboDepthRboId = -1;
	private int fboWidth = 0;
	private int fboHeight = 0;

	// --- Fullscreen quad VAO ---
	private ResourceVirtualArrayObject quadVao = null;

	/**
	 * Initialize shader programs and the fullscreen quad VAO.
	 * Must be called once before any rendering.
	 */
	public void init() {
		// --- Silhouette shader ---
		this.silhouetteProgram = ResourceProgram.create(
				new Uri("DATA", "postprocess/silhouette.vert", "ege"),
				new Uri("DATA", "postprocess/silhouette.frag", "ege"));
		if (this.silhouetteProgram != null) {
			this.silhouetteMatTransform = this.silhouetteProgram.getUniform("in_matrixTransformation");
			this.silhouetteMatProj = this.silhouetteProgram.getUniform("in_matrixProjection");
			this.silhouetteMatView = this.silhouetteProgram.getUniform("in_matrixView");
		} else {
			LOGGER.error("Failed to create silhouette shader program");
		}

		// --- Outline shader ---
		this.outlineProgram = ResourceProgram.create(
				new Uri("DATA", "postprocess/fullscreenQuad.vert", "ege"),
				new Uri("DATA", "postprocess/outline.frag", "ege"));
		if (this.outlineProgram != null) {
			this.outlineSilhouetteTex = this.outlineProgram.getUniform("in_silhouetteTexture");
			this.outlineColorUniform = this.outlineProgram.getUniform("in_outlineColor");
			this.outlineSizeUniform = this.outlineProgram.getUniform("in_outlineSize");
			this.outlineTexelSizeUniform = this.outlineProgram.getUniform("in_texelSize");
		} else {
			LOGGER.error("Failed to create outline shader program");
		}

		// --- Mask overlay shader ---
		this.maskOverlayProgram = ResourceProgram.create(
				new Uri("DATA", "postprocess/fullscreenQuad.vert", "ege"),
				new Uri("DATA", "postprocess/maskOverlay.frag", "ege"));
		if (this.maskOverlayProgram != null) {
			this.maskOverlaySilhouetteTex = this.maskOverlayProgram.getUniform("in_silhouetteTexture");
			this.maskOverlayColorUniform = this.maskOverlayProgram.getUniform("in_overlayColor");
		} else {
			LOGGER.error("Failed to create mask overlay shader program");
		}

		// --- Fullscreen quad ---
		createFullscreenQuad();
	}

	private void createFullscreenQuad() {
		final float[] positions = {
				-1.0f, -1.0f, 0.0f,
				 1.0f, -1.0f, 0.0f,
				 1.0f,  1.0f, 0.0f,
				-1.0f,  1.0f, 0.0f
		};
		final float[] texCoords = {
				0.0f, 0.0f,
				1.0f, 0.0f,
				1.0f, 1.0f,
				0.0f, 1.0f
		};
		final int[] indices = { 0, 1, 2, 0, 2, 3 };

		this.quadVao = ResourceVirtualArrayObject.create(positions, texCoords, null, indices);
		// Upload immediately to GPU since we're already in a render pass
		// (flush() would defer to next ResourceManager.updateContext() which already ran this frame)
		this.quadVao.updateContext();
	}

	/**
	 * Ensure the FBO exists and matches the given viewport dimensions.
	 * Creates or resizes the FBO as needed.
	 *
	 * @param width  Viewport width in pixels
	 * @param height Viewport height in pixels
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

		// Color texture (single-channel R8)
		this.fboTextureId = OpenGL.glGenTextures();
		OpenGL.bindTexture2D(this.fboTextureId);
		OpenGL.glTexImage2D(0, GL30.GL_R8, width, height, 0, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE);
		OpenGL.setTexture2DFilterNearest();
		OpenGL.setTexture2DWrapClampToEdge();
		OpenGL.glFramebufferTexture2D(GL30.GL_COLOR_ATTACHMENT0, this.fboTextureId);

		// Depth renderbuffer
		this.fboDepthRboId = OpenGL.glGenRenderbuffers();
		OpenGL.glBindRenderbuffer(this.fboDepthRboId);
		OpenGL.glRenderbufferStorage(GL30.GL_DEPTH_COMPONENT24, width, height);
		OpenGL.glFramebufferRenderbuffer(GL30.GL_DEPTH_ATTACHMENT, this.fboDepthRboId);

		if (!OpenGL.checkFramebufferStatus()) {
			LOGGER.error("Failed to create post-process FBO ({}x{})", width, height);
			destroyFbo();
			OpenGL.bindFramebuffer(0);
			return;
		}

		OpenGL.bindFramebuffer(0);
		this.fboWidth = width;
		this.fboHeight = height;
		LOGGER.debug("Created post-process FBO: {}x{}", width, height);
	}

	private void destroyFbo() {
		if (this.fboTextureId != -1) {
			OpenGL.glDeleteTextures(this.fboTextureId);
			this.fboTextureId = -1;
		}
		if (this.fboDepthRboId != -1) {
			OpenGL.glDeleteRenderbuffers(this.fboDepthRboId);
			this.fboDepthRboId = -1;
		}
		if (this.fboId != -1) {
			OpenGL.glDeleteFramebuffers(this.fboId);
			this.fboId = -1;
		}
		this.fboWidth = 0;
		this.fboHeight = 0;
	}

	/**
	 * Render the entity's mesh silhouette (white on black) into the FBO.
	 * <p>
	 * After this call, the FBO texture contains the entity's screen-space mask
	 * and the default framebuffer is re-bound.
	 *
	 * @param mesh         The entity's mesh component
	 * @param position     The entity's position component
	 * @param viewportSize Current viewport dimensions in pixels
	 */
	public void renderSilhouette(final ComponentMesh mesh, final ComponentPosition position,
			final Vector2f viewportSize) {
		final int vpWidth = (int) viewportSize.x();
		final int vpHeight = (int) viewportSize.y();
		ensureFbo(vpWidth, vpHeight);
		if (this.fboId == -1) {
			return;
		}

		final Matrix4f projMatrix = OpenGL.getMatrix();
		final Matrix4f viewMatrix = OpenGL.getCameraMatrix();
		final Matrix4f transformMatrix = position.getTransform().getOpenGLMatrix();

		// Render silhouette into FBO
		OpenGL.bindFramebuffer(this.fboId);
		OpenGL.setViewPort(Vector2f.ZERO, viewportSize);
		OpenGL.clearColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_colorBuffer);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);

		OpenGL.enable(Flag.flag_depthTest);
		OpenGL.updateAllFlags();

		if (this.silhouetteProgram != null) {
			this.silhouetteProgram.use();
			this.silhouetteProgram.uniformMatrix(this.silhouetteMatProj, projMatrix);
			this.silhouetteProgram.uniformMatrix(this.silhouetteMatView, viewMatrix);
			this.silhouetteProgram.uniformMatrix(this.silhouetteMatTransform, transformMatrix);

			mesh.bindForRendering();
			OpenGL.updateAllFlags();
			mesh.render();
			mesh.unBindForRendering();

			this.silhouetteProgram.unUse();
		}

		// Restore default framebuffer
		OpenGL.bindFramebuffer(0);
		OpenGL.setViewPort(Vector2f.ZERO, viewportSize);
	}

	/**
	 * Release all GPU resources (FBO, textures, renderbuffers).
	 */
	public void destroy() {
		destroyFbo();
	}

	// --- Getters for effect implementations ---

	/** @return The outline shader program, or null if initialization failed */
	public ResourceProgram getOutlineProgram() {
		return this.outlineProgram;
	}

	/** @return Uniform location for the silhouette texture in the outline shader */
	public int getOutlineSilhouetteTex() {
		return this.outlineSilhouetteTex;
	}

	/** @return Uniform location for the outline color */
	public int getOutlineColorUniform() {
		return this.outlineColorUniform;
	}

	/** @return Uniform location for the outline width in pixels */
	public int getOutlineSizeUniform() {
		return this.outlineSizeUniform;
	}

	/** @return Uniform location for the texel size vector */
	public int getOutlineTexelSizeUniform() {
		return this.outlineTexelSizeUniform;
	}

	/** @return The mask overlay shader program, or null if initialization failed */
	public ResourceProgram getMaskOverlayProgram() {
		return this.maskOverlayProgram;
	}

	/** @return Uniform location for the silhouette texture in the mask overlay shader */
	public int getMaskOverlaySilhouetteTex() {
		return this.maskOverlaySilhouetteTex;
	}

	/** @return Uniform location for the overlay color */
	public int getMaskOverlayColorUniform() {
		return this.maskOverlayColorUniform;
	}

	/** @return The OpenGL texture ID of the silhouette FBO texture */
	public int getFboTextureId() {
		return this.fboTextureId;
	}

	/** @return The fullscreen quad VAO used for all post-process passes */
	public ResourceVirtualArrayObject getQuadVao() {
		return this.quadVao;
	}
}
