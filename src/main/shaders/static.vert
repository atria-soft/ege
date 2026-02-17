#version 400 core

in vec3 position;
in vec2 textureCoords;
in vec3 normal;

const int MAX_LIGHT_NUMBER = 4;
const int MAX_SHADOW_MAPS = 12; // MAX_CASTERS(3) * MAX_CASCADES(4)

out vec2 io_textureCoords;
out vec3 io_surfaceNormal;
out vec3 io_toLightVector[MAX_LIGHT_NUMBER];
out vec3 io_toCameraVector;
// FOW: Fog Of War result calculation
out float io_fowVisibility;
// Shadow mapping
out vec4 io_fragPosLightSpace[MAX_SHADOW_MAPS];
// Distance from camera (for cascade selection)
out float io_distanceFromCamera;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition[MAX_LIGHT_NUMBER];

uniform float useFakeLighting;

uniform float numberOfRows;
uniform vec2 offset;

// CSM shadow uniforms
uniform int in_shadowCasterCount;
uniform int in_cascadeCount;
uniform mat4 in_lightSpaceMatrix[MAX_SHADOW_MAPS];

const float density = 0.007;
const float gradient = 1.5;

void main(void) {
	vec4 worldPosition = transformationMatrix * vec4(position, 1.0);
	vec4 positionRelativeToCam = viewMatrix * worldPosition;
	gl_Position = projectionMatrix * positionRelativeToCam;
	io_textureCoords = (textureCoords/numberOfRows) + offset;

	vec3 actualNormal = normal;
	if (useFakeLighting > 0.5) {
		actualNormal = vec3(0.0, 1.0, 0.0);
	}
	io_surfaceNormal = (transformationMatrix * vec4(actualNormal, 0.0)).xyz;
	for(int i=0;i<MAX_LIGHT_NUMBER;i++) {
		io_toLightVector[i] = lightPosition[i] - worldPosition.xyz;
	}
	io_toCameraVector = (inverse(viewMatrix) * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz;

	float distance = length(positionRelativeToCam.xyz);
	io_distanceFromCamera = distance;

	io_fowVisibility = exp(-pow((distance*density),gradient));
	io_fowVisibility = clamp(io_fowVisibility, 0.0, 1.0);

	// Compute fragment position in each shadow map's clip space
	int totalMaps = in_shadowCasterCount * in_cascadeCount;
	for (int i = 0; i < totalMaps && i < MAX_SHADOW_MAPS; i++) {
		io_fragPosLightSpace[i] = in_lightSpaceMatrix[i] * worldPosition;
	}
}
