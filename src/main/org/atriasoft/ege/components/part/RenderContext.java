package org.atriasoft.ege.components.part;

import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.ege.engines.EngineShadow;

/**
 * Transverse context passed to {@link org.atriasoft.ege.components.ComponentRender#render(RenderContext)}
 * by {@link org.atriasoft.ege.engines.EngineRender}.
 * <p>
 * Provides access to engine services (lighting, shadows) without requiring
 * each component to hold a direct reference to the engines.
 */
public class RenderContext {
	private final EngineLight engineLight;
	private final EngineShadow engineShadow;

	public RenderContext(final EngineLight engineLight, final EngineShadow engineShadow) {
		this.engineLight = engineLight;
		this.engineShadow = engineShadow;
	}

	public EngineLight getEngineLight() {
		return this.engineLight;
	}

	public EngineShadow getEngineShadow() {
		return this.engineShadow;
	}
}
