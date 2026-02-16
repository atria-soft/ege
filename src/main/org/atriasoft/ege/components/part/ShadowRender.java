package org.atriasoft.ege.components.part;

import org.atriasoft.ege.engines.EngineShadow;
import org.atriasoft.gale.resource.ResourceProgram;

/**
 * Render part that binds shadow map data (depth textures and light-space matrices)
 * to shader uniforms during the main render pass.
 * <p>
 * When no shadows are active ({@code in_shadowCount == 0}), the shader
 * produces the same output as if shadows were not compiled in.
 * <p>
 * Shadow map textures are bound to texture units 2+ (units 0-1 are reserved
 * for the diffuse texture and any other existing textures).
 *
 * @see EngineShadow
 */
public class ShadowRender implements PartRenderInterface {
	/** Maximum number of simultaneous shadow casters */
	private static final int MAX_SHADOW_CASTERS = 3;
	/** First texture unit used for shadow maps (0 = diffuse, 1 = reserved) */
	private static final int SHADOW_TEXTURE_UNIT_BASE = 2;

	private final EngineShadow shadowEngine;

	// Uniform locations
	private int glShadowCount = -1;
	private final int[] glLightSpaceMatrix = new int[MAX_SHADOW_CASTERS];
	private final int[] glShadowMap = new int[MAX_SHADOW_CASTERS];

	public ShadowRender(final EngineShadow shadowEngine) {
		this.shadowEngine = shadowEngine;
	}

	@Override
	public void init(final ResourceProgram program) {
		this.glShadowCount = program.getUniform("in_shadowCount");
		for (int i = 0; i < MAX_SHADOW_CASTERS; i++) {
			this.glLightSpaceMatrix[i] = program.getUniform("in_lightSpaceMatrix[" + i + "]");
			this.glShadowMap[i] = program.getUniform("in_shadowMap[" + i + "]");
		}
	}

	@Override
	public void bindForRendering(final ResourceProgram program) {
		final int count = Math.min(this.shadowEngine.getActiveShadowCount(), MAX_SHADOW_CASTERS);
		program.uniformInt(this.glShadowCount, count);

		for (int i = 0; i < count; i++) {
			program.uniformMatrix(this.glLightSpaceMatrix[i], this.shadowEngine.getLightSpaceMatrix(i));
			program.setTexture(this.glShadowMap[i], this.shadowEngine.getShadowTextureId(i),
					SHADOW_TEXTURE_UNIT_BASE + i);
		}
	}

	@Override
	public void unBindForRendering() {
		// Shadow textures are unbound when the next frame rebinds them.
		// No explicit cleanup needed.
	}
}
