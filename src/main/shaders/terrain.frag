#version 400 core

const int MAX_LIGHT_NUMBER = 4;
const int MAX_SHADOW_MAPS = 12; // MAX_CASTERS(3) * MAX_CASCADES(4)
const int MAX_CASCADES = 4;

in vec2 pass_textureCoordinates;
in vec3 surfaceNormal;
in vec3 toLightVector[MAX_LIGHT_NUMBER];
in vec3 toCameraVector;
// FOW: Fog Of War result calculation
in float visibility;
// Shadow mapping
in vec4 fragPosLightSpace[MAX_SHADOW_MAPS];
// Distance from camera (for cascade selection)
in float distanceFromCamera;

out vec4 out_Color;

uniform sampler2D backgroundTexture;
uniform sampler2D rTexture;
uniform sampler2D gTexture;
uniform sampler2D bTexture;
uniform sampler2D blendMap;

uniform vec3 lightColour[MAX_LIGHT_NUMBER];
uniform vec3 lightAttenuation[MAX_LIGHT_NUMBER];
uniform float reflectivity;
uniform float shineDamper;
uniform vec3 skyColor;

// CSM shadow uniforms
uniform int in_shadowCasterCount;
uniform int in_cascadeCount;
uniform sampler2DShadow in_shadowMap[MAX_SHADOW_MAPS];
uniform float in_cascadeSplits[MAX_CASCADES];

// PCF shadow calculation using hardware shadow comparison (sampler2DShadow).
float calculateShadow(vec4 fragPosLS, sampler2DShadow shadowTex) {
	vec3 projCoords = fragPosLS.xyz / fragPosLS.w;
	projCoords = projCoords * 0.5 + 0.5;
	if (projCoords.z > 1.0) {
		return 0.0;
	}
	float refDepth = projCoords.z - 0.0005;
	float shadow = 0.0;
	vec2 texelSize = 1.0 / textureSize(shadowTex, 0);
	for (int x = -1; x <= 1; x++) {
		for (int y = -1; y <= 1; y++) {
			shadow += texture(shadowTex, vec3(projCoords.xy + vec2(x, y) * texelSize, refDepth));
		}
	}
	return 1.0 - shadow / 9.0;
}

// Select the cascade index based on fragment distance from camera
int selectCascade() {
	for (int i = 0; i < in_cascadeCount - 1; i++) {
		if (distanceFromCamera < in_cascadeSplits[i]) {
			return i;
		}
	}
	return in_cascadeCount - 1;
}

void main(void) {

	vec4 blendMapColour = texture(blendMap, pass_textureCoordinates);

	float backTextureAmount = 1 - (blendMapColour.r + blendMapColour.g + blendMapColour.b);
	vec2 tiledCoords = pass_textureCoordinates * 40.0;
	vec4 backgroundTextureColour = texture(backgroundTexture, tiledCoords) * backTextureAmount;
	vec4 rTextureColour = texture(rTexture, tiledCoords) * blendMapColour.r;
	vec4 gTextureColour = texture(gTexture, tiledCoords) * blendMapColour.g;
	vec4 bTextureColour = texture(bTexture, tiledCoords) * blendMapColour.b;

	vec4 totalColour = backgroundTextureColour + rTextureColour + gTextureColour + bTextureColour;

	vec3 unitNormal = normalize(surfaceNormal);

	vec3 totalDiffuse = vec3(0.0);
	vec3 totalSpecular = vec3(0.0);
	for(int i=0;i<MAX_LIGHT_NUMBER;i++) {
		float distance = length(toLightVector[i]);
		float attenuationFactor = lightAttenuation[i].x + (lightAttenuation[i].y * distance) + (lightAttenuation[i].z * distance * distance);
		vec3 unitLightVector = normalize(toLightVector[i]);

		float nDot1 = dot(unitNormal, unitLightVector);
		float brightness = max(nDot1, 0.0);

		vec3 unitVectorToCamera = normalize(toCameraVector);
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
				maxShadow = max(maxShadow, calculateShadow(fragPosLightSpace[mapIndex], in_shadowMap[mapIndex]));
			}
		}
		totalDiffuse *= (1.0 - maxShadow * 0.7);
		totalSpecular *= (1.0 - maxShadow);
	}

	out_Color = vec4(totalDiffuse, 1.0) * totalColour + vec4(totalSpecular, 1.0);
	out_Color = mix(vec4(skyColor, 1.0), out_Color, visibility);
}
