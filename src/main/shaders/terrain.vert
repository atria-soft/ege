#version 400 core

in vec3 position;
in vec2 textureCoords;
in vec3 normal;

out vec2 pass_textureCoordinates;
out vec3 surfaceNormal;
out vec3 toLightVector[4];
out vec3 toCameraVector;
// FOW: Fog Of War result calculation
out float visibility;
// Shadow mapping
out vec4 fragPosLightSpace[3];

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition[4];

// Shadow uniforms (optional: in_shadowCount == 0 means no shadows)
uniform int in_shadowCount;
uniform mat4 in_lightSpaceMatrix[3];

const float density = 0.007;
const float gradient = 2.5;

void main(void) {
	vec4 worldPosition = transformationMatrix * vec4(position, 1.0);
	vec4 positionRelativeToCam = viewMatrix * worldPosition;
	gl_Position = projectionMatrix * positionRelativeToCam;
	pass_textureCoordinates = textureCoords;

	surfaceNormal = (transformationMatrix * vec4(normal, 0.0)).xyz;
	for(int i=0;i<4;i++) {
		toLightVector[i] = lightPosition[i] - worldPosition.xyz;
	}
	toCameraVector = (inverse(viewMatrix) * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz;

	float distance = length(positionRelativeToCam.xyz);
	visibility = exp(-pow((distance*density),gradient));
	visibility = clamp(visibility, 0.0, 1.0);

	// Compute fragment position in each shadow caster's clip space
	for (int i = 0; i < in_shadowCount; i++) {
		fragPosLightSpace[i] = in_lightSpaceMatrix[i] * worldPosition;
	}
}

