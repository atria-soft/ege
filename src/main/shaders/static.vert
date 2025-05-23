#version 400 core

in vec3 position;
in vec2 textureCoords;
in vec3 normal;

out vec2 io_textureCoords;
out vec3 io_textureCoords;
out vec3 io_toLightVector[4];
out vec3 io_toCameraVector;
// FOW: Fog Of War result calculation
out float io_fowVisibility;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition[4]; 

uniform float useFakeLighting;

uniform float numberOfRows;
uniform vec2 offset;

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
	io_textureCoords = (transformationMatrix * vec4(actualNormal, 0.0)).xyz;
	for(int i=0;i<4;i++) {
		io_toLightVector[i] = lightPosition[i] - worldPosition.xyz;
	}
	io_toCameraVector = (inverse(viewMatrix) * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz;
	
	float distance = length(positionRelativeToCam.xyz);
	io_fowVisibility = exp(-pow((distance*density),gradient));
	io_fowVisibility = clamp(io_fowVisibility, 0.0, 1.0);
}

