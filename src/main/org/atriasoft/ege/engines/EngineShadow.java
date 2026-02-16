package org.atriasoft.ege.engines;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.celestial.CelestialBody;
import org.atriasoft.ege.celestial.CelestialSystem;
import org.atriasoft.ege.components.ComponentStaticMesh;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.shadow.ShadowMapResources;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Engine that renders depth passes for shadow mapping.
 * <p>
 * This engine runs during the render phase, <b>before</b> {@link EngineRender}.
 * For each active shadow-casting celestial body, it:
 * <ol>
 *   <li>Computes the light-space matrix (orthographic projection from the light's perspective)</li>
 *   <li>Renders all meshes into a depth-only FBO</li>
 *   <li>Stores the shadow map texture and light-space matrix for use by the main render pass</li>
 * </ol>
 * <p>
 * The shadow data is accessed by {@link org.atriasoft.ege.components.part.ShadowRender}
 * during the main render pass.
 */
public class EngineShadow extends Engine {
	private static final Logger LOGGER = LoggerFactory.getLogger(EngineShadow.class);
	public static final String ENGINE_NAME = "shadow";

	private final CelestialSystem celestialSystem;
	private final List<ShadowMapResources> shadowMaps = new ArrayList<>();
	private boolean initialized = false;

	// Shadow configuration
	private int shadowMapResolution = 1024;
	private float shadowDistance = 100.0f;

	// Active shadow data (read by ShadowRender during main pass)
	private int activeShadowCount = 0;
	private final Matrix4f[] activeLightSpaceMatrices = new Matrix4f[3];
	private final int[] activeShadowTextureIds = new int[3];

	public EngineShadow(final Environement env, final CelestialSystem celestialSystem) {
		super(env);
		this.celestialSystem = celestialSystem;
	}

	@Override
	public String getType() {
		return ENGINE_NAME;
	}

	@Override
	public void componentAdd(final Component ref) {
		// Shadow engine does not use component routing — it collects
		// mesh+position pairs from entities at render time.
	}

	@Override
	public void componentRemove(final Component ref) {
		// Nothing to do — no component tracking.
	}

	@Override
	public void update(final long deltaMili) {
		// Update celestial body orbital positions
		this.celestialSystem.update(deltaMili);
	}

	@Override
	public void render(final long deltaMili, final Camera camera) {
		final List<CelestialBody> casters = this.celestialSystem.getActiveShadowCasters();
		if (casters.isEmpty()) {
			this.activeShadowCount = 0;
			return;
		}

		// Lazy init
		if (!this.initialized) {
			initResources(casters.size());
			this.initialized = true;
		}

		// Ensure we have enough shadow map resources
		ensureShadowMapCount(casters.size());

		// Collect mesh+position pairs from all entities
		final List<Entity> entities = this.env.getEntity();
		final List<MeshPositionPair> meshPairs = new ArrayList<>();
		for (final Entity entity : entities) {
			ComponentStaticMesh mesh = null;
			PositionningInterface position = null;
			for (final Component comp : entity.getComponents()) {
				if (comp instanceof final ComponentStaticMesh typedMesh) {
					mesh = typedMesh;
				}
				if (comp instanceof final PositionningInterface typedPos) {
					position = typedPos;
				}
			}
			if (mesh != null && position != null) {
				meshPairs.add(new MeshPositionPair(mesh, position));
			}
		}

		final Vector2f viewportSize = OpenGL.getViewportSize();
		this.activeShadowCount = casters.size();

		for (int i = 0; i < casters.size(); i++) {
			final CelestialBody caster = casters.get(i);
			final ShadowMapResources resources = this.shadowMaps.get(i);

			// Compute light-space matrix
			final Matrix4f lightSpaceMatrix = computeLightSpaceMatrix(caster, camera);

			// Begin depth pass
			resources.beginDepthPass(this.shadowMapResolution, lightSpaceMatrix);

			// Render all meshes into the shadow map
			for (final MeshPositionPair pair : meshPairs) {
				resources.renderMeshDepth(pair.mesh, pair.position);
			}

			// End depth pass
			resources.endDepthPass(viewportSize);

			// Store results for the main render pass
			this.activeLightSpaceMatrices[i] = lightSpaceMatrix;
			this.activeShadowTextureIds[i] = resources.getDepthTextureId();
		}
	}

	@Override
	public void renderDebug(final long deltaMili, final Camera camera) {
		// No debug rendering
	}

	/**
	 * Compute an orthographic light-space matrix for a directional celestial body.
	 * The projection is centered on the camera position and covers the shadow distance.
	 */
	private Matrix4f computeLightSpaceMatrix(final CelestialBody caster, final Camera camera) {
		final Vector3f lightDir = caster.getDirection();
		final Vector3f cameraPos = camera.getPosition();

		// Light position: far along the light direction from the camera
		final float lightDistance = this.shadowDistance * 2.0f;
		final Vector3f lightPos = new Vector3f(
				cameraPos.x() + lightDir.x() * lightDistance,
				cameraPos.y() + lightDir.y() * lightDistance,
				cameraPos.z() + lightDir.z() * lightDistance);

		// Look-at target is the camera position
		final Vector3f target = cameraPos;

		// Up vector (Z-up world): avoid degenerate case when light is nearly vertical (aligned with Z)
		Vector3f up = new Vector3f(0.0f, 0.0f, 1.0f);
		if (Math.abs(lightDir.x()) < 0.001f && Math.abs(lightDir.y()) < 0.001f) {
			up = new Vector3f(1.0f, 0.0f, 0.0f);
		}

		final Matrix4f lightView = Matrix4f.createMatrixLookAt(lightPos, target, up);
		final float halfSize = this.shadowDistance;
		final Matrix4f lightProjection = Matrix4f.createMatrixOrtho(
				-halfSize, halfSize,
				-halfSize, halfSize,
				0.1f, lightDistance * 2.0f);

		return lightProjection.multiply(lightView);
	}

	private void initResources(final int count) {
		for (int i = 0; i < count; i++) {
			final ShadowMapResources resources = new ShadowMapResources();
			resources.init();
			this.shadowMaps.add(resources);
		}
	}

	private void ensureShadowMapCount(final int count) {
		while (this.shadowMaps.size() < count) {
			final ShadowMapResources resources = new ShadowMapResources();
			resources.init();
			this.shadowMaps.add(resources);
		}
	}

	// --- Accessors for ShadowRender ---

	/** @return Number of active shadow maps this frame */
	public int getActiveShadowCount() {
		return this.activeShadowCount;
	}

	/** @return Light-space matrix for shadow caster at given index */
	public Matrix4f getLightSpaceMatrix(final int index) {
		return this.activeLightSpaceMatrices[index];
	}

	/** @return Depth texture ID for shadow caster at given index */
	public int getShadowTextureId(final int index) {
		return this.activeShadowTextureIds[index];
	}

	/** @return The celestial system managed by this engine */
	public CelestialSystem getCelestialSystem() {
		return this.celestialSystem;
	}

	public int getShadowMapResolution() {
		return this.shadowMapResolution;
	}

	public void setShadowMapResolution(final int shadowMapResolution) {
		this.shadowMapResolution = shadowMapResolution;
	}

	public float getShadowDistance() {
		return this.shadowDistance;
	}

	public void setShadowDistance(final float shadowDistance) {
		this.shadowDistance = shadowDistance;
	}

	/**
	 * Temporary pair linking a mesh to its position, collected at render time.
	 */
	private static class MeshPositionPair {
		final ComponentStaticMesh mesh;
		final PositionningInterface position;

		MeshPositionPair(final ComponentStaticMesh mesh, final PositionningInterface position) {
			this.mesh = mesh;
			this.position = position;
		}
	}
}
