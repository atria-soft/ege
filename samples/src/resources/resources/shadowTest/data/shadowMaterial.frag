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

struct Material {
    vec3 ambientFactor;
    vec3 diffuseFactor;
    vec3 specularFactor;
    float shininess;
};
const int MAX_LIGHT_NUMBER = 8;


in vec2 io_textureCoords;
in vec3 io_surfaceNormal;
in vec3 io_toCameraVector;
in vec3 io_toLightVector[MAX_LIGHT_NUMBER];
// FOW: Fog Of War result calculation
in float io_fowVisibility;
// Shadow mapping
in vec4 io_fragPosLightSpace[3];

// texture properties
uniform sampler2D in_textureBase;
// Material
uniform Material in_material;
// 2 light for suns and other for locals ...
uniform Light in_lights[MAX_LIGHT_NUMBER];
// global color of the sky
const vec3 in_sky_color = vec3(0.5, 0.7, 1.0);

// Shadow uniforms (optional: in_shadowCount == 0 means no shadows)
uniform int in_shadowCount;
uniform sampler2D in_shadowMap[3];

// output:
out vec4 out_Color;

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

void main(void) {
	// disable transparency elements in the texture ...
	vec4 textureColour = texture(in_textureBase, io_textureCoords);
	if (textureColour.a < 0.5) {
		discard;
	}

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
		float damperFactor = pow(specularFactor, in_material.shininess);
		vec3 diffuse = (brightness * in_lights[iii].color) / attenuationFactor;
		vec3 finalSpecular = (damperFactor * in_material.specularFactor.x * in_lights[iii].color) / attenuationFactor;
		totalDiffuse = totalDiffuse + diffuse;
		totalSpecular = totalSpecular + finalSpecular;
	}
	// the 0.2 represent the ambient lightning
	totalDiffuse = max(totalDiffuse, 0.2);

	// Apply shadows (optional: when in_shadowCount == 0, no shadow attenuation)
	if (in_shadowCount > 0) {
		float maxShadow = 0.0;
		for (int i = 0; i < in_shadowCount; i++) {
			maxShadow = max(maxShadow, calculateShadow(io_fragPosLightSpace[i], in_shadowMap[i]));
		}
		// Shadows attenuate diffuse and specular (shadows are not fully black)
		totalDiffuse *= (1.0 - maxShadow * 0.7);
		totalSpecular *= (1.0 - maxShadow);
	}

	out_Color = vec4(totalDiffuse, 1.0) * textureColour + vec4(totalSpecular, 1.0);
	out_Color = mix(vec4(in_sky_color, 1.0), out_Color, io_fowVisibility);
}
