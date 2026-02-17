package org.atriasoft.ege.engines;

import java.util.Vector;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentRender;
import org.atriasoft.ege.components.part.RenderContext;

public class EngineRender extends Engine {
	public static final String ENGINE_NAME = "render";
	private static float TIME_STEP = 5.0f;
	private float accumulator = 0;
	private final Vector<ComponentRender> components = new Vector<ComponentRender>();
	private final Vector<ResultNearestElement> displayElementOrdered = new Vector<ResultNearestElement>();
	private RenderContext renderContext;

	public EngineRender(final Environement env) {
		super(env);
	}

	@Override
	public void componentAdd(final Component ref) {
		if (ref instanceof ComponentRender == false) {
			return;
		}
		this.components.add((ComponentRender) ref);
	}

	@Override
	public void componentRemove(final Component ref) {
		this.components.remove(ref);
	}

	@Override
	public String getType() {
		return ENGINE_NAME;
	}

	@Override
	public void render(final long deltaMili, final Camera camera) {
		// Lazily build the render context on first call (engines are stable after init)
		if (this.renderContext == null) {
			final EngineLight engineLight = (EngineLight) this.env.getEngine(EngineLight.ENGINE_NAME);
			final EngineShadow engineShadow = (EngineShadow) this.env.getEngine(EngineShadow.ENGINE_NAME);
			this.renderContext = new RenderContext(engineLight, engineShadow);
		}
		for (final ComponentRender it : this.components) {
			it.render(this.renderContext);
		}
	}

	@Override
	public void renderDebug(final long deltaMili, final Camera camera) {

	}

	@Override
	public void update(final long deltaMili) {
		// Add the time difference in the accumulator
		this.accumulator += deltaMili * 0.0001f;
		// While there is enough accumulated time to take one or several physics steps
		while (this.accumulator >= TIME_STEP) {
			for (final ComponentRender it : this.components) {
				it.update(TIME_STEP);
			}
			// Decrease the accumulated time
			this.accumulator -= TIME_STEP;
		}
	}
}

class ResultNearestElement {
	public ComponentRender element;
	public float dist;
}
