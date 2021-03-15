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

// texture properties
uniform sampler2D in_textureBase;
// Material
uniform Material in_material;
// 2 light for suns and other for locals ...
uniform Light in_lights[MAX_LIGHT_NUMBER];
// global color of the sky ... needed to have a better color for the FOW
//uniform vec3 in_sky_color;
const vec3 in_sky_color = vec3(1.0,1.0,1.0);

// output:
out vec4 out_Color;

void main(void) {
	// disable transparency elements in the texture ...
	// Can be set at the start of the shader ...
	vec4 textureColour = texture(in_textureBase, io_textureCoords);
	if (textureColour.a < 0.5) {
		discard;
	}

	vec3 unitNormal = normalize(io_surfaceNormal);
	vec3 unitVectorToCamera = normalize(io_toCameraVector);
	vec3 totalDiffuse = vec3(0.0);
	vec3 totalSpecular = vec3(0.0);
	for(int iii=0; iii<MAX_LIGHT_NUMBER; iii++) {
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
		vec3 diffuse = (brightness * in_lights[iii].color) / attenuationFactor;;
		vec3 finalSpecular = (damperFactor * in_material.specularFactor.x * in_lights[iii].color) / attenuationFactor; // TODO: Remove the x ....
		totalDiffuse = totalDiffuse + diffuse;
		totalSpecular = totalSpecular + finalSpecular;
	}
	// the 0.2 represent the ambiant lightning ==> maybe set an uniform for this
	totalDiffuse = max(totalDiffuse, 0.2);

	out_Color = vec4(totalDiffuse,1.0) * textureColour + vec4(totalSpecular, 1.0);
	out_Color = mix(vec4(in_sky_color,1.0), out_Color, io_fowVisibility);
}




