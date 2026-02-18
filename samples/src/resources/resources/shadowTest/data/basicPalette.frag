#version 400 core

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

struct Light {
    vec3 color;
    vec3 position;
    vec3 attenuation;
};

const int MAX_LIGHT_NUMBER = 8;
const int MAX_SHADOW_MAPS = 12; // MAX_CASTERS(3) * MAX_CASCADES(4)
const int MAX_CASCADES = 4;
// Fraction of cascade range used for blending between adjacent cascades
const float CASCADE_BLEND_BAND = 0.1;

in vec2 io_textureCoords;
in vec3 io_surfaceNormal;
in vec3 io_toCameraVector;
in vec3 io_toLightVector[MAX_LIGHT_NUMBER];
// FOW: Fog Of War result calculation
in float io_fowVisibility;
// Shadow mapping
in vec4 io_fragPosLightSpace[MAX_SHADOW_MAPS];
in float io_distanceFromCamera;

// texture properties
uniform sampler2D in_textureBase;
// 2 light for suns and other for locals ...
uniform Light in_lights[MAX_LIGHT_NUMBER];
// global color of the sky
const vec3 in_sky_color = vec3(1.0, 1.0, 1.0);

// CSM shadow uniforms
uniform int in_shadowCasterCount;
uniform int in_cascadeCount;
uniform sampler2DShadow in_shadowMap[MAX_SHADOW_MAPS];
uniform float in_cascadeSplits[MAX_CASCADES];
// PCF kernel half-size: 0 = hard (1x1), 1 = medium (3x3), 2 = soft (5x5)
uniform int in_pcfHalfKernel;

// output:
out vec4 out_Color;

// Adaptive slope-scaled bias to reduce shadow acne on angled surfaces.
// Keep values very small — polygon offset handles most of the bias.
float computeBias(vec3 normal, vec3 lightDir) {
	float cosTheta = abs(dot(normal, lightDir));
	return max(0.001 * (1.0 - cosTheta), 0.0002);
}

// PCF shadow calculation using hardware shadow comparison (sampler2DShadow).
// Each texture() call performs a bilinear-interpolated depth comparison,
// yielding a smooth 0.0-1.0 value per sample instead of hard 0/1.
float calculateShadow(vec4 fragPosLightSpace, sampler2DShadow shadowTex, float bias) {
	vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
	projCoords = projCoords * 0.5 + 0.5;
	if (projCoords.z > 1.0) {
		return 0.0;
	}
	float refDepth = projCoords.z - bias;
	float shadow = 0.0;
	vec2 texelSize = 1.0 / textureSize(shadowTex, 0);
	int halfK = in_pcfHalfKernel;
	int sampleCount = 0;
	for (int x = -halfK; x <= halfK; x++) {
		for (int y = -halfK; y <= halfK; y++) {
			shadow += texture(shadowTex, vec3(projCoords.xy + vec2(x, y) * texelSize, refDepth));
			sampleCount++;
		}
	}
	// Invert: texture() returns 1.0 when lit, we want 1.0 when shadowed
	return 1.0 - shadow / float(sampleCount);
}

// Select the cascade index and compute blend factor with the next cascade.
int selectCascade(out float blendFactor) {
	blendFactor = 0.0;
	for (int i = 0; i < in_cascadeCount - 1; i++) {
		if (io_distanceFromCamera < in_cascadeSplits[i]) {
			float cascadeNear = (i == 0) ? 0.0 : in_cascadeSplits[i - 1];
			float cascadeRange = in_cascadeSplits[i] - cascadeNear;
			float blendStart = in_cascadeSplits[i] - cascadeRange * CASCADE_BLEND_BAND;
			if (io_distanceFromCamera > blendStart) {
				blendFactor = (io_distanceFromCamera - blendStart) / (cascadeRange * CASCADE_BLEND_BAND);
			}
			return i;
		}
	}
	return in_cascadeCount - 1;
}

void main(void) {
	// keep material:
	vec3 tex_ambientFactor = texture(in_textureBase, vec2(io_textureCoords.x, 4.5/8.0)).xyz;
	vec4 textureColour = texture(in_textureBase, io_textureCoords);
	vec3 tex_specularFactor = texture(in_textureBase, vec2(io_textureCoords.x, 2.5/8.0)).xyz;
	float tex_shininess = texture(in_textureBase, vec2(io_textureCoords.x, 6.5/8.0)).x;

	vec3 unitNormal = normalize(io_surfaceNormal);
	vec3 unitVectorToCamera = normalize(io_toCameraVector);
	vec3 totalDiffuse = vec3(0.0);
	vec3 totalSpecular = vec3(0.0);
	for (int iii = 0; iii < MAX_LIGHT_NUMBER; iii++) {
		float distance = length(io_toLightVector[iii]);
		float attenuationFactor = in_lights[iii].attenuation.x + (in_lights[iii].attenuation.y * distance) + (in_lights[iii].attenuation.z * distance * distance);
		vec3 unitLightVector = normalize(io_toLightVector[iii]);
		float nDot1 = dot(unitNormal, unitLightVector);
		float brightness = max(nDot1, 0.0);
		vec3 lightDirection = -unitLightVector;
		vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
		float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);
		specularFactor = max(specularFactor, 0.0);
		float damperFactor = pow(specularFactor, tex_shininess);
		vec3 diffuse = (brightness * in_lights[iii].color) / attenuationFactor;
		vec3 finalSpecular = (damperFactor * tex_specularFactor.x * in_lights[iii].color) / attenuationFactor;
		totalDiffuse = totalDiffuse + diffuse;
		totalSpecular = totalSpecular + finalSpecular;
	}

	// Apply CSM shadows
	if (in_shadowCasterCount > 0 && in_cascadeCount > 0) {
		vec3 lightDir = normalize(io_toLightVector[0]);
		float bias = computeBias(unitNormal, lightDir);

		float blendFactor;
		int cascade = selectCascade(blendFactor);
		float maxShadow = 0.0;
		for (int caster = 0; caster < in_shadowCasterCount; caster++) {
			int mapIndex = caster * in_cascadeCount + cascade;
			if (mapIndex < MAX_SHADOW_MAPS) {
				float shadowVal = calculateShadow(io_fragPosLightSpace[mapIndex], in_shadowMap[mapIndex], bias);
				if (blendFactor > 0.0 && cascade + 1 < in_cascadeCount) {
					int nextMapIndex = caster * in_cascadeCount + cascade + 1;
					if (nextMapIndex < MAX_SHADOW_MAPS) {
						float nextShadowVal = calculateShadow(io_fragPosLightSpace[nextMapIndex], in_shadowMap[nextMapIndex], bias);
						shadowVal = mix(shadowVal, nextShadowVal, blendFactor);
					}
				}
				maxShadow = max(maxShadow, shadowVal);
			}
		}
		totalDiffuse *= (1.0 - maxShadow * 0.7);
		totalSpecular *= (1.0 - maxShadow);
	}

	// Ambient floor so unlit faces remain slightly visible (reveals polygon edges)
	totalDiffuse = max(totalDiffuse, 0.2);

	out_Color = vec4(totalDiffuse, 1.0) * textureColour + vec4(totalSpecular, 1.0);
}
