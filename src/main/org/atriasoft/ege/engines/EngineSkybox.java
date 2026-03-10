package org.atriasoft.ege.engines;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.skybox.SkyboxConfig;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;
import org.atriasoft.gale.resource.ResourceTextureCubeMap;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Engine that renders a cubemap skybox behind all scene geometry.
 * <p>
 * Uses a unit cube (-1..+1) centered at the origin. The vertex shader strips
 * translation from the view matrix so the skybox stays at infinity, and writes
 * depth = 1.0 via the xyww trick so all scene geometry renders in front.
 * <p>
 * Must be inserted in the engine list AFTER {@link EngineShadow} (so shadow
 * maps are ready) and BEFORE {@link EngineRender} (so the skybox is drawn
 * first and scene geometry overwrites it).
 */
public class EngineSkybox extends Engine {
	public static final String ENGINE_NAME = "skybox";
	private static final Logger LOGGER = LoggerFactory.getLogger(EngineSkybox.class);

	// Unit cube: 8 vertices, 36 indices (viewed from inside, CCW winding)
	//@formatter:off
	private static final float[] CUBE_VERTICES = {
		-1,  1, -1,  // 0: left  top    back
		 1,  1, -1,  // 1: right top    back
		 1, -1, -1,  // 2: right bottom back
		-1, -1, -1,  // 3: left  bottom back
		-1,  1,  1,  // 4: left  top    front
		 1,  1,  1,  // 5: right top    front
		 1, -1,  1,  // 6: right bottom front
		-1, -1,  1,  // 7: left  bottom front
	};

	private static final int[] CUBE_INDICES = {
		// -Z face (back, viewed from inside → CCW)
		0, 2, 1,
		0, 3, 2,
		// +Z face (front, viewed from inside → CCW)
		4, 5, 6,
		4, 6, 7,
		// -X face (left, viewed from inside → CCW)
		0, 4, 7,
		0, 7, 3,
		// +X face (right, viewed from inside → CCW)
		1, 2, 6,
		1, 6, 5,
		// +Y face (top, viewed from inside → CCW)
		0, 1, 5,
		0, 5, 4,
		// -Y face (bottom, viewed from inside → CCW)
		3, 7, 6,
		3, 6, 2,
	};
	//@formatter:on

	private SkyboxConfig config;
	private ResourceTextureCubeMap cubeMap;
	private ResourceVirtualArrayObject cubeVao;
	private ResourceProgram program;
	private int uniformProjection;
	private int uniformView;
	private int uniformCubeMap;
	private boolean initialized = false;

	public EngineSkybox(final Environement env) {
		super(env);
	}

	/**
	 * Configure the skybox with the given configuration.
	 * Can be called at any time; the GPU resources are created lazily.
	 * @param config Skybox configuration (null to disable)
	 */
	public void setConfig(final SkyboxConfig config) {
		this.config = config;
		this.initialized = false;
		this.cubeMap = null;
		this.program = null;
		this.cubeVao = null;
	}

	/**
	 * Get the current skybox configuration.
	 * @return Current config, or null if no skybox is set
	 */
	public SkyboxConfig getConfig() {
		return this.config;
	}

	private void initResources() {
		if (this.config == null) {
			return;
		}
		LOGGER.debug("Initializing skybox resources");
		// Create cubemap texture (face order: +X, -X, +Y, -Y, +Z, -Z)
		this.cubeMap = ResourceTextureCubeMap.create(
				this.config.getRight(),
				this.config.getLeft(),
				this.config.getTop(),
				this.config.getBottom(),
				this.config.getFront(),
				this.config.getBack());
		// Create cube VAO with indexed geometry
		this.cubeVao = ResourceVirtualArrayObject.create(CUBE_VERTICES, null, null, null, CUBE_INDICES);
		this.cubeVao.updateContext();
		// Create shader program
		final Uri vertexUri = new Uri("DATA", "skybox.vert", "ege");
		final Uri fragmentUri = new Uri("DATA", "skybox.frag", "ege");
		this.program = ResourceProgram.create(vertexUri, fragmentUri);
		if (this.program != null) {
			this.uniformProjection = this.program.getUniform("in_matrixProjection");
			this.uniformView = this.program.getUniform("in_matrixView");
			this.uniformCubeMap = this.program.getUniform("cubeMap");
		}
		this.initialized = true;
	}

	@Override
	public void render(final long deltaMili, final Camera camera) {
		if (this.config == null) {
			return;
		}
		if (!this.initialized) {
			initResources();
		}
		if (this.program == null || this.cubeMap == null || this.cubeVao == null) {
			return;
		}
		// Use GL_LEQUAL so fragments at depth 1.0 (from xyww trick) pass
		GL11.glDepthFunc(GL11.GL_LEQUAL);

		this.program.use();

		final Matrix4f projectionMatrix = OpenGL.getMatrix();
		final Matrix4f viewMatrix = OpenGL.getCameraMatrix();
		this.program.uniformMatrix(this.uniformProjection, projectionMatrix);
		this.program.uniformMatrix(this.uniformView, viewMatrix);

		// Bind cubemap texture to unit 0
		this.cubeMap.bindForRendering(0);
		this.program.uniformInt(this.uniformCubeMap, 0);

		// Draw cube
		this.cubeVao.bindForRendering();
		OpenGL.updateAllFlags();
		this.cubeVao.render(OpenGL.RenderMode.TRIANGLE);
		this.cubeVao.unBindForRendering();

		this.cubeMap.unBindForRendering();
		this.program.unUse();

		// Restore default depth function
		GL11.glDepthFunc(GL11.GL_LESS);
	}

	@Override
	public String getType() {
		return ENGINE_NAME;
	}

	@Override
	public void componentAdd(final Component ref) {
		// Skybox is not component-based
	}

	@Override
	public void componentRemove(final Component ref) {
		// Skybox is not component-based
	}

	@Override
	public void update(final long deltaMili) {
		// No per-frame updates needed
	}

	@Override
	public void renderDebug(final long deltaMili, final Camera camera) {
		// No debug rendering
	}
}
