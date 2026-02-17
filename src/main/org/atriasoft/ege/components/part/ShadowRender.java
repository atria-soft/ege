package org.atriasoft.ege.components.part;

import org.atriasoft.ege.engines.EngineShadow;
import org.atriasoft.gale.resource.ResourceProgram;

/**
 * Render part that binds Cascaded Shadow Map (CSM) data to shader uniforms
 * during the main render pass.
 * <p>
 * For each active shadow caster, binds N cascade shadow maps and their
 * light-space matrices. Also binds cascade split distances so the fragment
 * shader can select the appropriate cascade based on fragment distance.
 * <p>
 * When no shadows are active ({@code in_shadowCasterCount == 0}), the shader
 * produces the same output as if shadows were not compiled in.
 * <p>
 * Shadow map textures are bound to texture units 2+ (units 0-1 are reserved
 * for the diffuse texture and any other existing textures).
 *
 * @see EngineShadow
 */
public class ShadowRender {
	/** Max total shadow maps = MAX_SHADOW_CASTERS * MAX_CASCADES */
	private static final int MAX_TOTAL_SHADOW_MAPS = EngineShadow.MAX_SHADOW_CASTERS * EngineShadow.MAX_CASCADES;
	/** First texture unit used for shadow maps (0 = diffuse, 1 = reserved) */
	private static final int SHADOW_TEXTURE_UNIT_BASE = 2;

	// Uniform locations
	private int glShadowCasterCount = -1;
	private int glCascadeCount = -1;
	private final int[] glLightSpaceMatrix = new int[MAX_TOTAL_SHADOW_MAPS];
	private final int[] glShadowMap = new int[MAX_TOTAL_SHADOW_MAPS];
	private final int[] glCascadeSplits = new int[EngineShadow.MAX_CASCADES];

	public void init(final ResourceProgram program) {
		this.glShadowCasterCount = program.getUniform("in_shadowCasterCount");
		this.glCascadeCount = program.getUniform("in_cascadeCount");
		for (int i = 0; i < MAX_TOTAL_SHADOW_MAPS; i++) {
			this.glLightSpaceMatrix[i] = program.getUniform("in_lightSpaceMatrix[" + i + "]");
			this.glShadowMap[i] = program.getUniform("in_shadowMap[" + i + "]");
		}
		for (int i = 0; i < EngineShadow.MAX_CASCADES; i++) {
			this.glCascadeSplits[i] = program.getUniform("in_cascadeSplits[" + i + "]");
		}
	}

	public void bindForRendering(final ResourceProgram program, final EngineShadow shadowEngine) {
		if (shadowEngine == null) {
			return;
		}
		final int casterCount = shadowEngine.getActiveShadowCasterCount();
		final int cascadeCount = shadowEngine.getCascadeCount();
		final int totalMaps = casterCount * cascadeCount;

		program.uniformInt(this.glShadowCasterCount, casterCount);
		program.uniformInt(this.glCascadeCount, cascadeCount);

		// Bind all shadow maps and light-space matrices (flattened: caster * cascade + cascade)
		for (int i = 0; i < totalMaps && i < MAX_TOTAL_SHADOW_MAPS; i++) {
			program.uniformMatrix(this.glLightSpaceMatrix[i], shadowEngine.getLightSpaceMatrix(i));
			program.setTexture(this.glShadowMap[i], shadowEngine.getShadowTextureId(i),
					SHADOW_TEXTURE_UNIT_BASE + i);
		}

		// Bind cascade split distances
		final float[] splits = shadowEngine.getCascadeSplitDistances();
		for (int i = 0; i < splits.length && i < EngineShadow.MAX_CASCADES; i++) {
			program.uniformFloat(this.glCascadeSplits[i], splits[i]);
		}
	}

	public void unBindForRendering() {
		// Shadow textures are unbound when the next frame rebinds them.
	}
}
