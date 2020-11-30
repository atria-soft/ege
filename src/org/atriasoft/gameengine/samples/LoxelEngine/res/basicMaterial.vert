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

// Input:
in vec3 in_position;
in vec3 in_normal;
in vec2 in_textureCoords;
// 2 light for suns and other for locals ...
uniform Light in_lights[MAX_LIGHT_NUMBER];

uniform mat4 in_matrixTransformation;
uniform mat4 in_matrixProjection;
uniform mat4 in_matrixView;

//uniform float in_numberOfRows;
//uniform vec2 in_offset;
const float in_numberOfRows = 1;
const vec2 in_offset = vec2(0.0,0.0);

// Configuration of the FOV ==> TODO: Set it in parameter uniform ...
const float c_density = 0.007;
const float c_gradient = 1.5;

// output:
out vec2 io_textureCoords;
out vec3 io_surfaceNormal;
out vec3 io_toCameraVector;
out vec3 io_toLightVector[MAX_LIGHT_NUMBER];
// FOW: Fog Of War result calculation
out float io_fowVisibility;

void main(void) {
	vec4 worldPosition = in_matrixTransformation * vec4(in_position, 1.0);
	vec4 positionRelativeToCam = in_matrixView * worldPosition;
	gl_Position = in_matrixProjection * positionRelativeToCam;
	io_textureCoords = (in_textureCoords/in_numberOfRows) + in_offset;

	io_surfaceNormal = (in_matrixTransformation * vec4(in_normal, 0.0)).xyz;
	for(int iii=0;iii<MAX_LIGHT_NUMBER;iii++) {
		io_toLightVector[iii] = in_lights[iii].position - worldPosition.xyz;
	}
	io_toCameraVector = (inverse(in_matrixView) * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz;

	float distance = length(positionRelativeToCam.xyz);

	io_fowVisibility = exp(-pow((distance*c_density),c_gradient));
	io_fowVisibility = clamp(io_fowVisibility, 0.0, 1.0);
}
