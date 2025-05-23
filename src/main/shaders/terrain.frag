#version 400 core

in vec2 pass_textureCoordinates;
in vec3 surfaceNormal;
in vec3 toLightVector[4];
in vec3 toCameraVector;
// FOW: Fog Of War result calculation
in float visibility;

out vec4 out_Color;

uniform sampler2D backgroundTexture;
uniform sampler2D rTexture;
uniform sampler2D gTexture;
uniform sampler2D bTexture;
uniform sampler2D blendMap;

uniform vec3 lightColour[4];
uniform vec3 lightAttenuation[4];
uniform float reflectivity;
uniform float shineDamper;
uniform vec3 skyColor;


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
	for(int i=0;i<4;i++) {
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
	totalDiffuse = max (totalDiffuse, 0.2);
	
	out_Color = vec4(totalDiffuse,1.0) * totalColour + vec4(totalSpecular, 1.0); 
	out_Color = mix(vec4(skyColor,1.0), out_Color, visibility);
}

