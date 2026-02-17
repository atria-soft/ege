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
import org.atriasoft.ege.components.ComponentMesh;
import org.atriasoft.ege.components.ComponentStaticMesh;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.shadow.ShadowCascade;
import org.atriasoft.ege.shadow.ShadowConfig;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Engine that renders depth passes for Cascaded Shadow Mapping (CSM).
 * <p>
 * This engine runs during the render phase, <b>before</b> {@link EngineRender}.
 * For each active shadow-casting celestial body, it renders N cascade depth passes
 * (one per cascade level), each covering a progressively larger slice of the
 * camera frustum.
 * <p>
 * The shadow data (per-cascade depth textures, light-space matrices, split distances)
 * is accessed by {@link org.atriasoft.ege.components.part.ShadowRender}
 * during the main render pass.
 */
public class EngineShadow extends Engine {
	private static final Logger LOGGER = LoggerFactory.getLogger(EngineShadow.class);
	public static final String ENGINE_NAME = "shadow";
	
	/** Maximum number of simultaneous shadow casters */
	public static final int MAX_SHADOW_CASTERS = 3;
	/** Maximum number of cascades per shadow caster */
	public static final int MAX_CASCADES = 4;
	
	private final CelestialSystem celestialSystem;
	private final ShadowConfig config;
	private boolean initialized = false;
	
	// Cascades: [casterIndex][cascadeIndex]
	private final ShadowCascade[][] cascades = new ShadowCascade[MAX_SHADOW_CASTERS][MAX_CASCADES];
	
	// Active shadow data (read by ShadowRender during main pass)
	private int activeShadowCasterCount = 0;
	// Flattened arrays: index = casterIndex * cascadeCount + cascadeIndex
	private final Matrix4f[] activeLightSpaceMatrices = new Matrix4f[MAX_SHADOW_CASTERS * MAX_CASCADES];
	private final int[] activeShadowTextureIds = new int[MAX_SHADOW_CASTERS * MAX_CASCADES];
	private float[] activeCascadeSplitDistances = new float[0];
	
	// Camera FOV — set by the application (default PI/2 = 90 degrees)
	private float cameraFovY = (float) (Math.PI * 0.5);
	private float cameraAspectRatio = 1.333f;
	
	// Debug thumbnail rendering
	private boolean debugThumbnailEnabled = false;
	private ResourceProgram debugProgram;
	private int debugDepthTexUniform = -1;
	private ResourceVirtualArrayObject debugQuadVao;
	
	public EngineShadow(final Environement env, final CelestialSystem celestialSystem) {
		this(env, celestialSystem, new ShadowConfig());
	}
	
	public EngineShadow(final Environement env, final CelestialSystem celestialSystem, final ShadowConfig config) {
		super(env);
		this.celestialSystem = celestialSystem;
		this.config = config;
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
			this.activeShadowCasterCount = 0;
			return;
		}
		
		// Lazy init
		if (!this.initialized) {
			initAllCascades();
			this.initialized = true;
		}
		
		final int cascadeCount = this.config.getCascadeCount();
		final int casterCount = Math.min(casters.size(), MAX_SHADOW_CASTERS);
		
		// Collect mesh+position pairs from all entities
		final List<MeshPositionPair> meshPairs = collectMeshPairs();
		
		final Vector2f viewportSize = OpenGL.getViewportSize();
		this.activeShadowCasterCount = casterCount;
		this.activeCascadeSplitDistances = this.config.getCascadeSplitDistances();
		
		for (int casterIdx = 0; casterIdx < casterCount; casterIdx++) {
			final CelestialBody caster = casters.get(casterIdx);
			final Vector3f lightDir = caster.getDirection();
			
			for (int cascadeIdx = 0; cascadeIdx < cascadeCount; cascadeIdx++) {
				final ShadowCascade cascade = ensureCascade(casterIdx, cascadeIdx);
				
				// Set cascade split range
				final float near = this.config.getCascadeNear(cascadeIdx);
				final float far = this.config.getCascadeFar(cascadeIdx);
				cascade.setSplitRange(near, far);
				
				// Compute light-space matrix fitted to this frustum slice
				final Matrix4f lightSpaceMatrix = cascade.computeLightSpaceMatrix(lightDir, camera, this.cameraFovY,
						this.cameraAspectRatio);
				
				// Render depth pass
				cascade.getResources().beginDepthPass(this.config.getShadowMapResolution(), lightSpaceMatrix);
				for (final MeshPositionPair pair : meshPairs) {
					cascade.getResources().renderMeshDepth(pair);
				}
				cascade.getResources().endDepthPass(viewportSize);
				
				// Store results for the main render pass
				final int flatIndex = casterIdx * cascadeCount + cascadeIdx;
				this.activeLightSpaceMatrices[flatIndex] = lightSpaceMatrix;
				this.activeShadowTextureIds[flatIndex] = cascade.getResources().getDepthTextureId();
			}
		}
	}
	
	@Override
	public void renderDebug(final long deltaMili, final Camera camera) {
		if (this.debugThumbnailEnabled) {
			renderDebugThumbnails();
		}
	}
	
	/**
	 * Render shadow map depth textures as small thumbnails in the bottom-right
	 * corner of the screen. Each cascade is shown as a separate thumbnail.
	 * Call from the application's onDraw after the main render pass.
	 */
	public void renderDebugThumbnails() {
		if (this.activeShadowCasterCount == 0) {
			return;
		}
		initDebugResources();
		if (this.debugProgram == null || this.debugQuadVao == null) {
			return;
		}
		
		final int cascadeCount = this.config.getCascadeCount();
		final int totalMaps = this.activeShadowCasterCount * cascadeCount;
		final int thumbnailSize = 600;
		final int padding = 10;
		
		final Vector2f viewportSize = OpenGL.getViewportSize();
		final int viewW = (int) viewportSize.x();
		final int viewH = (int) viewportSize.y();
		
		// Disable depth test for overlay
		OpenGL.disable(OpenGL.Flag.flag_depthTest);
		OpenGL.updateAllFlags();
		
		this.debugProgram.use();
		
		for (int i = 0; i < totalMaps && i < MAX_SHADOW_CASTERS * MAX_CASCADES; i++) {
			final int texId = this.activeShadowTextureIds[i];
			if (texId <= 0) {
				continue;
			}
			
			// Position: bottom-right corner, stacked vertically
			final int x = viewW - thumbnailSize - padding;
			final int y = padding + i * (thumbnailSize + padding);
			
			OpenGL.setViewPort(new Vector2f(x, y), new Vector2f(thumbnailSize, thumbnailSize));
			
			// Bind depth texture to unit 0
			this.debugProgram.setTexture(this.debugDepthTexUniform, texId, 0);
			
			this.debugQuadVao.bindForRendering();
			OpenGL.updateAllFlags();
			this.debugQuadVao.render(OpenGL.RenderMode.TRIANGLE);
			this.debugQuadVao.unBindForRendering();
		}
		
		this.debugProgram.unUse();
		
		// Restore viewport and depth test
		OpenGL.setViewPort(new Vector2f(0, 0), viewportSize);
		OpenGL.enable(OpenGL.Flag.flag_depthTest);
		OpenGL.updateAllFlags();
	}
	
	private void initDebugResources() {
		if (this.debugProgram != null) {
			return;
		}
		this.debugProgram = ResourceProgram.create(new Uri("DATA", "postprocess/fullscreenQuad.vert", "ege"),
				new Uri("DATA", "shadow/debugDepth.frag", "ege"));
		if (this.debugProgram != null) {
			this.debugDepthTexUniform = this.debugProgram.getUniform("in_depthTexture");
		} else {
			LOGGER.error("Failed to create shadow debug shader program");
		}
		
		// Create a fullscreen quad
		final float[] positions = { -1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f };
		final float[] texCoords = { 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f };
		final int[] indices = { 0, 1, 2, 0, 2, 3 };
		this.debugQuadVao = ResourceVirtualArrayObject.create(positions, texCoords, null, indices);
		this.debugQuadVao.updateContext();
	}
	
	private List<MeshPositionPair> collectMeshPairs() {
		final List<Entity> entities = this.env.getEntity();
		final List<MeshPositionPair> meshPairs = new ArrayList<>();
		for (final Entity entity : entities) {
			ComponentStaticMesh staticMesh = null;
			ComponentMesh dynamicMesh = null;
			PositionningInterface position = null;
			for (final Component comp : entity.getComponents()) {
				if (comp instanceof final ComponentStaticMesh typedMesh) {
					staticMesh = typedMesh;
				}
				if (comp instanceof final ComponentMesh typedMesh) {
					dynamicMesh = typedMesh;
				}
				if (comp instanceof final PositionningInterface typedPos) {
					position = typedPos;
				}
			}
			if (staticMesh != null && position != null) {
				final ComponentStaticMesh meshRef = staticMesh;
				meshPairs.add(new MeshPositionPair(position, meshRef::bindForRendering, meshRef::render,
						meshRef::unBindForRendering));
			} else if (dynamicMesh != null && position != null) {
				final ComponentMesh meshRef = dynamicMesh;
				meshPairs.add(new MeshPositionPair(position, meshRef::bindForRendering, meshRef::render,
						meshRef::unBindForRendering));
			}
		}
		return meshPairs;
	}
	
	private void initAllCascades() {
		for (int c = 0; c < MAX_SHADOW_CASTERS; c++) {
			for (int k = 0; k < MAX_CASCADES; k++) {
				this.cascades[c][k] = new ShadowCascade();
				this.cascades[c][k].init();
			}
		}
	}
	
	private ShadowCascade ensureCascade(final int casterIndex, final int cascadeIndex) {
		if (this.cascades[casterIndex][cascadeIndex] == null) {
			this.cascades[casterIndex][cascadeIndex] = new ShadowCascade();
			this.cascades[casterIndex][cascadeIndex].init();
		}
		return this.cascades[casterIndex][cascadeIndex];
	}
	
	// --- Accessors for ShadowRender ---
	
	/** @return Number of active shadow casters this frame */
	public int getActiveShadowCasterCount() {
		return this.activeShadowCasterCount;
	}
	
	/** @return Number of cascades per shadow caster */
	public int getCascadeCount() {
		return this.config.getCascadeCount();
	}
	
	/**
	 * @return Light-space matrix for the given caster and cascade.
	 *         Flat index = casterIndex * cascadeCount + cascadeIndex.
	 */
	public Matrix4f getLightSpaceMatrix(final int flatIndex) {
		return this.activeLightSpaceMatrices[flatIndex];
	}
	
	/**
	 * @return Depth texture ID for the given caster and cascade.
	 *         Flat index = casterIndex * cascadeCount + cascadeIndex.
	 */
	public int getShadowTextureId(final int flatIndex) {
		return this.activeShadowTextureIds[flatIndex];
	}
	
	/**
	 * @return Cascade split distances in world units (length = cascadeCount - 1).
	 *         Used by shaders to select which cascade to sample.
	 */
	public float[] getCascadeSplitDistances() {
		return this.activeCascadeSplitDistances;
	}
	
	// --- Legacy API compatibility (Phase 1 single-cascade) ---
	
	/** @return Total number of active shadow maps (casters * cascades) */
	public int getActiveShadowCount() {
		return this.activeShadowCasterCount * this.config.getCascadeCount();
	}
	
	/** @return The celestial system managed by this engine */
	public CelestialSystem getCelestialSystem() {
		return this.celestialSystem;
	}
	
	public ShadowConfig getConfig() {
		return this.config;
	}
	
	public int getShadowMapResolution() {
		return this.config.getShadowMapResolution();
	}
	
	public void setShadowMapResolution(final int shadowMapResolution) {
		this.config.setShadowMapResolution(shadowMapResolution);
	}
	
	public float getShadowDistance() {
		return this.config.getShadowDistance();
	}
	
	public void setShadowDistance(final float shadowDistance) {
		this.config.setShadowDistance(shadowDistance);
	}
	
	public float getCameraFovY() {
		return this.cameraFovY;
	}
	
	public void setCameraFovY(final float cameraFovY) {
		this.cameraFovY = cameraFovY;
	}
	
	public float getCameraAspectRatio() {
		return this.cameraAspectRatio;
	}
	
	public void setCameraAspectRatio(final float cameraAspectRatio) {
		this.cameraAspectRatio = cameraAspectRatio;
	}
	
	public boolean isDebugThumbnailEnabled() {
		return this.debugThumbnailEnabled;
	}
	
	public void setDebugThumbnailEnabled(final boolean debugThumbnailEnabled) {
		this.debugThumbnailEnabled = debugThumbnailEnabled;
	}
	
	/**
	 * Pairs a positioned entity with its mesh rendering callbacks for the depth pass.
	 * Supports both ComponentStaticMesh (OBJ) and ComponentMesh (EMF) via method references.
	 */
	public static class MeshPositionPair {
		public final PositionningInterface position;
		public final Runnable bind;
		public final Runnable render;
		public final Runnable unbind;
		
		MeshPositionPair(final PositionningInterface position, final Runnable bind, final Runnable render,
				final Runnable unbind) {
			this.position = position;
			this.bind = bind;
			this.render = render;
			this.unbind = unbind;
		}
	}
}
