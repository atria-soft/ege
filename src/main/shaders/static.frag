#version 400 core

const int MAX_LIGHT_NUMBER = 4;
const int MAX_SHADOW_MAPS = 12; // MAX_CASTERS(3) * MAX_CASCADES(4)
const int MAX_CASCADES = 4;

in vec2 io_textureCoords;
in vec3 io_surfaceNormal;
in vec3 io_toLightVector[MAX_LIGHT_NUMBER];
in vec3 io_toCameraVector;
// FOW: Fog Of War result calculation
in float io_fowVisibility;
// Shadow mapping
in vec4 io_fragPosLightSpace[MAX_SHADOW_MAPS];
// Distance from camera (for cascade selection)
in float io_distanceFromCamera;

out vec4 out_Color;

uniform sampler2D textureSampler;
uniform vec3 lightColour[MAX_LIGHT_NUMBER];
uniform vec3 lightAttenuation[MAX_LIGHT_NUMBER];
uniform float reflectivity;
uniform float shineDamper;
uniform vec3 skyColor;

// CSM shadow uniforms
uniform int in_shadowCasterCount;
uniform int in_cascadeCount;
uniform sampler2D in_shadowMap[MAX_SHADOW_MAPS];
uniform float in_cascadeSplits[MAX_CASCADES];

// PCF shadow calculation with 3x3 kernel
float calculateShadow(vec4 fragPosLightSpace, sampler2D shadowTex) {
	vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
	projCoords = projCoords * 0.5 + 0.5;
	// Fragment outside light frustum = not in shadow
	if (projCoords.z > 1.0) {
		return 0.0;
	}
	float currentDepth = projCoords.z;
	float bias = 0.005;
	// PCF: sample 3x3 neighborhood for soft edges
	float shadow = 0.0;
	vec2 texelSize = 1.0 / textureSize(shadowTex, 0);
	for (int x = -1; x <= 1; x++) {
		for (int y = -1; y <= 1; y++) {
			float closestDepth = texture(shadowTex, projCoords.xy + vec2(x, y) * texelSize).r;
			shadow += (currentDepth - bias > closestDepth) ? 1.0 : 0.0;
		}
	}
	return shadow / 9.0;
}

// Select the cascade index based on fragment distance from camera
int selectCascade() {
	for (int i = 0; i < in_cascadeCount - 1; i++) {
		if (io_distanceFromCamera < in_cascadeSplits[i]) {
			return i;
		}
	}
	return in_cascadeCount - 1;
}

void main(void) {

	vec3 unitNormal = normalize(io_surfaceNormal);
	vec3 unitVectorToCamera = normalize(io_toCameraVector);
	vec3 totalDiffuse = vec3(0.0);
	vec3 totalSpecular = vec3(0.0);
	for(int i=0;i<MAX_LIGHT_NUMBER;i++) {
		float distance = length(io_toLightVector[i]);
		float attenuationFactor = lightAttenuation[i].x + (lightAttenuation[i].y * distance) + (lightAttenuation[i].z * distance * distance);
		vec3 unitLightVector = normalize(io_toLightVector[i]);
		float nDot1 = dot(unitNormal, unitLightVector);
		float brightness = max(nDot1, 0.0);
		vec3 lightDirection = -unitLightVector;
		vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
		float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);
		specularFactor = max(specularFactor, 0.0);
		float damperFactor = pow(specularFactor, shineDamper);
		vec3 diffuse = (brightness * lightColour[i]) / attenuationFactor;
		vec3 finalSpecular = (damperFactor * reflectivity * lightColour[i]) / attenuationFactor;
		totalDiffuse = totalDiffuse + diffuse;
		totalSpecular = totalSpecular + finalSpecular;
	}
	// the 0.2 represent the ambiant lightning ==> maybe set an uniform for this
	totalDiffuse = max(totalDiffuse, 0.2);

	// Apply CSM shadows
	if (in_shadowCasterCount > 0 && in_cascadeCount > 0) {
		int cascade = selectCascade();
		float maxShadow = 0.0;
		for (int caster = 0; caster < in_shadowCasterCount; caster++) {
			int mapIndex = caster * in_cascadeCount + cascade;
			if (mapIndex < MAX_SHADOW_MAPS) {
				maxShadow = max(maxShadow, calculateShadow(io_fragPosLightSpace[mapIndex], in_shadowMap[mapIndex]));
			}
		}
		// Shadows attenuate diffuse and specular (shadows are not fully black)
		totalDiffuse *= (1.0 - maxShadow * 0.7);
		totalSpecular *= (1.0 - maxShadow);
	}

	// disable transparency elements in the texture ...
	// Can be set at the start of the shader ...
	vec4 textureColour = texture(textureSampler, io_textureCoords);
	if (textureColour.a < 0.5) {
		discard;
	}

	out_Color = vec4(totalDiffuse, 1.0) * textureColour + vec4(totalSpecular, 1.0);
	out_Color = mix(vec4(skyColor, 1.0), out_Color, io_fowVisibility);
}
