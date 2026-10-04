package org.atriasoft.ege.engines;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.skybox.SkyboxConfig;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.resource.OwnedResources;
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

	private static final Uri VERTEX_SHADER = new Uri("DATA", "skybox.vert", "ege");
	private static final Uri FRAGMENT_SHADER = new Uri("DATA", "skybox.frag", "ege");
	private static final Vector3f ROTATION_AXIS = new Vector3f(0.0f, 1.0f, 0.0f);
	private static final float FULL_TURN = (float) (2.0 * Math.PI);

	/** The OpenGL resources of the engine, released once (by gale) when the engine is collected. */
	private final OwnedResources resources = new OwnedResources(this);
	private SkyboxConfig config;
	/** Cube map of the current configuration, loaded by {@link #render} on its first use. */
	private ResourceTextureCubeMap cubeMap;
	private boolean cubeMapLoaded = false;
	/**
	 * Cube maps of the replaced configurations, released by {@link #render} after
	 * the load of the new one: a sky set again is kept by gale, with its texture.
	 */
	private final List<ResourceTextureCubeMap> cubeMapsToRelease = new ArrayList<>();
	// The cube and its shader do not depend on the configuration: created once, kept across sky changes.
	private ResourceVirtualArrayObject cubeVao;
	private ResourceProgram program;
	private int uniformProjection;
	private int uniformView;
	private int uniformCubeMap;
	private boolean cubeInitialized = false;
	/** Rotation of the sky around the Y axis, in radians, in [0, 2 PI). */
	private float rotationAngle = 0.0f;

	public EngineSkybox(final Environement env) {
		super(env);
	}

	/**
	 * Configure the skybox with the given configuration.
	 * Can be called at any time and from any thread: the GPU resources are
	 * created lazily by {@link #render}, which also releases the cube map of
	 * the previous configuration (OpenGL objects can only be deleted on the
	 * rendering thread).
	 * @param config Skybox configuration (null to disable)
	 */
	public synchronized void setConfig(final SkyboxConfig config) {
		this.config = config;
		if (this.cubeMap != null) {
			this.cubeMapsToRelease.add(this.cubeMap);
			this.cubeMap = null;
		}
		this.cubeMapLoaded = false;
		this.rotationAngle = 0.0f;
	}

	/**
	 * Get the current skybox configuration.
	 * @return Current config, or null if no skybox is set
	 */
	public synchronized SkyboxConfig getConfig() {
		return this.config;
	}

	/**
	 * Get the current rotation of the sky around the Y axis.
	 * @return Angle in radians, in [0, 2 PI)
	 */
	public synchronized float getRotationAngle() {
		return this.rotationAngle;
	}

	/**
	 * Load the cube map of the current configuration if needed and release
	 * the ones it replaced. Rendering thread only.
	 * @return The cube map to draw, or null when there is no sky to draw
	 */
	private synchronized ResourceTextureCubeMap prepareCubeMap() {
		if (this.config != null && !this.cubeMapLoaded) {
			this.cubeMap = loadCubeMap(this.config);
			this.cubeMapLoaded = true;
		}
		// After the load: a sky that is set again keeps its texture.
		releaseReplacedCubeMaps();
		return this.config == null ? null : this.cubeMap;
	}

	private ResourceTextureCubeMap loadCubeMap(final SkyboxConfig skybox) {
		LOGGER.debug("Loading skybox cube map");
		// Face order: +X, -X, +Y, -Y, +Z, -Z. A cube map still living (same faces) is kept by gale.
		return this.resources.own(ResourceTextureCubeMap.create(
				skybox.getRight(),
				skybox.getLeft(),
				skybox.getTop(),
				skybox.getBottom(),
				skybox.getFront(),
				skybox.getBack()));
	}

	/** Release the cube maps replaced: gale deletes the texture of the last user's. */
	private void releaseReplacedCubeMaps() {
		for (final ResourceTextureCubeMap replaced : this.cubeMapsToRelease) {
			this.resources.releaseOwned(replaced);
		}
		this.cubeMapsToRelease.clear();
	}

	/** Create the cube and its shader on the first frame that draws a sky. */
	private void initCube() {
		if (this.cubeInitialized) {
			return;
		}
		this.cubeInitialized = true;
		LOGGER.debug("Initializing skybox resources");
		// Create cube VAO with indexed geometry
		this.cubeVao = this.resources.own(
				ResourceVirtualArrayObject.create(CUBE_VERTICES, null, null, null, CUBE_INDICES));
		this.cubeVao.updateContext();
		// Create shader program
		this.program = this.resources.own(ResourceProgram.create(VERTEX_SHADER, FRAGMENT_SHADER));
		if (this.program != null) {
			this.uniformProjection = this.program.getUniform("in_matrixProjection");
			this.uniformView = this.program.getUniform("in_matrixView");
			this.uniformCubeMap = this.program.getUniform("cubeMap");
		}
	}

	@Override
	public void render(final long deltaMili, final Camera camera) {
		final ResourceTextureCubeMap sky = prepareCubeMap();
		if (sky == null) {
			return;
		}
		initCube();
		if (this.program == null || this.cubeVao == null) {
			return;
		}
		// Use GL_LEQUAL so fragments at depth 1.0 (from xyww trick) pass
		GL11.glDepthFunc(GL11.GL_LEQUAL);

		this.program.use();

		final Matrix4f projectionMatrix = OpenGL.getMatrix();
		Matrix4f viewMatrix = OpenGL.getCameraMatrix();
		final float angle = getRotationAngle();
		if (angle != 0.0f) {
			// Turn the cube itself: the shader only keeps the rotation part of the view.
			viewMatrix = viewMatrix.multiply(Matrix4f.createMatrixRotate(ROTATION_AXIS, angle));
		}
		this.program.uniformMatrix(this.uniformProjection, projectionMatrix);
		this.program.uniformMatrix(this.uniformView, viewMatrix);

		// Bind cubemap texture to unit 0
		sky.bindForRendering(0);
		this.program.uniformInt(this.uniformCubeMap, 0);

		// Draw cube
		this.cubeVao.bindForRendering();
		OpenGL.updateAllFlags();
		this.cubeVao.render(OpenGL.RenderMode.TRIANGLE);
		this.cubeVao.unBindForRendering();

		sky.unBindForRendering();
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
	public synchronized void update(final long deltaMili) {
		if (this.config == null) {
			return;
		}
		// Read at each update: the speed can be changed on the configuration at any time.
		final float speed = this.config.getRotationSpeed();
		if (speed == 0.0f) {
			return;
		}
		final float angle = (this.rotationAngle + speed * deltaMili / 1000.0f) % FULL_TURN;
		this.rotationAngle = angle < 0.0f ? angle + FULL_TURN : angle;
	}

	@Override
	public void renderDebug(final long deltaMili, final Camera camera) {
		// No debug rendering
	}
}
