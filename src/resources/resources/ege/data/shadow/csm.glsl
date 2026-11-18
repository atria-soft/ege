// Cascaded shadow maps, the part every receiving shader shares (ege ShadowRender's uniforms).
// Pulled in by the include directive of ShaderAssembler (file shadow/csm.glsl), after the #version line.
// The shader keeps what differs: how a fragment is projected into a shadow map, the sampling and the use
// of the result. A change of the cascade selection, the bias or the uniforms is made here, once.
const int MAX_SHADOW_MAPS = 12; // MAX_CASTERS(3) * MAX_CASCADES(4)
const int MAX_CASCADES = 4;
// Fraction of a cascade range blended with the next one
const float CASCADE_BLEND_BAND = 0.1;

// CSM shadow uniforms (ege ShadowRender)
uniform int in_shadowCasterCount;
uniform int in_cascadeCount;
uniform sampler2DShadow in_shadowMap[MAX_SHADOW_MAPS];
uniform float in_cascadeSplits[MAX_CASCADES];
// PCF kernel half-size: 0 = hard (1x1), 1 = medium (3x3), 2 = soft (5x5)
uniform int in_pcfHalfKernel;

// Adaptive slope-scaled bias to reduce shadow acne on angled surfaces.
// Keep values very small: polygon offset handles most of the bias.
float computeBias(vec3 normal, vec3 lightDir) {
	float cosTheta = abs(dot(normal, lightDir));
	return max(0.001 * (1.0 - cosTheta), 0.0002);
}

// Select the cascade index of a fragment at view distance `depth` and compute the blend factor with the next
// cascade: 0.0 means fully in the selected cascade, above 0.0 blending towards the next one (the last
// CASCADE_BLEND_BAND fraction of the range of the cascade).
int selectCascade(float depth, out float blendFactor) {
	blendFactor = 0.0;
	for (int i = 0; i < in_cascadeCount - 1; i++) {
		if (depth < in_cascadeSplits[i]) {
			float cascadeNear = (i == 0) ? 0.0 : in_cascadeSplits[i - 1];
			float cascadeRange = in_cascadeSplits[i] - cascadeNear;
			float blendStart = in_cascadeSplits[i] - cascadeRange * CASCADE_BLEND_BAND;
			if (depth > blendStart) {
				blendFactor = (depth - blendStart) / (cascadeRange * CASCADE_BLEND_BAND);
			}
			return i;
		}
	}
	return in_cascadeCount - 1;
}
