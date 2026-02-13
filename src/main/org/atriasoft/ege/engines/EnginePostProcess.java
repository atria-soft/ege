package org.atriasoft.ege.engines;

import java.util.Vector;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentMesh;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentPostProcess;
import org.atriasoft.ege.postprocess.PostProcessEffect;
import org.atriasoft.ege.postprocess.PostProcessResources;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;

/**
 * Engine that renders post-process effects on entities.
 * <p>
 * This engine runs after {@link EngineRender} in the rendering pipeline.
 * For each entity with a {@link ComponentPostProcess}, it:
 * <ol>
 *   <li>Renders the entity's mesh silhouette (white on black) into a shared FBO</li>
 *   <li>Iterates through the component's effects in order</li>
 *   <li>Each effect draws a fullscreen quad using the silhouette as a pixel mask</li>
 * </ol>
 * <p>
 * The FBO and shader programs are shared across all entities to minimize GPU
 * resource usage. The FBO is lazily initialized and resized when the viewport changes.
 *
 * @see ComponentPostProcess
 * @see PostProcessEffect
 * @see PostProcessResources
 */
public class EnginePostProcess extends Engine {
	/** Engine type identifier, must match {@link ComponentPostProcess#COMPONENT_NAME}. */
	public static final String ENGINE_NAME = "postprocess";

	private final Vector<ComponentPostProcess> components = new Vector<>();
	private PostProcessResources resources = null;

	public EnginePostProcess(final Environement env) {
		super(env);
	}

	@Override
	public String getType() {
		return ENGINE_NAME;
	}

	@Override
	public void componentAdd(final Component ref) {
		if (ref instanceof ComponentPostProcess pp) {
			this.components.add(pp);
		}
	}

	@Override
	public void componentRemove(final Component ref) {
		this.components.remove(ref);
	}

	@Override
	public void render(final long deltaMili, final Camera camera) {
		if (this.components.isEmpty()) {
			return;
		}
		// Lazy init resources on first render (needs OpenGL context)
		if (this.resources == null) {
			this.resources = new PostProcessResources();
			this.resources.init();
		}

		for (final ComponentPostProcess pp : this.components) {
			if (!pp.hasEffects()) {
				continue;
			}
			final ComponentMesh mesh = pp.getMesh();
			final ComponentPosition position = pp.getPosition();
			if (mesh == null || position == null) {
				continue;
			}

			final Vector2f viewportSize = OpenGL.getViewportSize();

			// Phase A: Render silhouette into FBO
			this.resources.renderSilhouette(mesh, position, viewportSize);

			// Disable depth test/write for fullscreen quad passes
			OpenGL.disable(Flag.flag_depthTest);
			OpenGL.setDeathMask(false);

			// Apply each effect in order
			for (final PostProcessEffect effect : pp.getEffects()) {
				effect.render(this.resources, viewportSize);
			}

			// Restore state
			OpenGL.setDeathMask(true);
			OpenGL.enable(Flag.flag_depthTest);
			OpenGL.updateAllFlags();
		}
	}

	@Override
	public void update(final long deltaMili) {
		// No update logic needed for post-process effects
	}

	@Override
	public void renderDebug(final long deltaMili, final Camera camera) {
		// No debug rendering for post-process
	}
}
