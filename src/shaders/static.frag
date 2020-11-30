#version 400 core

in vec2 io_textureCoords;
in vec3 io_textureCoords;
in vec3 io_toLightVector[4];
in vec3 io_toCameraVector;
// FOW: Fog Of War result calculation
in float io_fowVisibility;

out vec4 out_Color;

uniform sampler2D textureSampler;
uniform vec3 lightColour[4];
uniform vec3 lightAttenuation[4];
uniform float reflectivity;
uniform float shineDamper;
uniform vec3 skyColor;


void main(void) {
	
	vec3 unitNormal = normalize(io_textureCoords);
	vec3 unitVectorToCamera = normalize(io_toCameraVector);
	vec3 totalDiffuse = vec3(0.0);
	vec3 totalSpecular = vec3(0.0);
	for(int i=0;i<4;i++) {
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
		vec3 diffuse = (brightness * lightColour[i]) / attenuationFactor;;
		vec3 finalSpecular = (damperFactor * reflectivity * lightColour[i]) / attenuationFactor;;
		totalDiffuse = totalDiffuse + diffuse;
		totalSpecular = totalSpecular + finalSpecular;
	}
	// the 0.2 represent the ambiant lightning ==> maybe set an uniform for this
	totalDiffuse = max(totalDiffuse, 0.2);
	
	// disable transparency elements in the texture ...
	// Can be set at the start of the shader ...
	vec4 textureColour = texture(textureSampler,io_textureCoords);
	if (textureColour.a < 0.5) {
		discard;
	}
	
	out_Color = vec4(totalDiffuse,1.0) * textureColour + vec4(totalSpecular, 1.0);
	out_Color = mix(vec4(skyColor,1.0), out_Color, io_fowVisibility);
}

