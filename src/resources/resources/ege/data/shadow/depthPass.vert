#version 400 core

in vec3 position;

uniform mat4 in_matrixTransformation;
uniform mat4 in_lightSpaceMatrix;

void main(void) {
	gl_Position = in_lightSpaceMatrix * in_matrixTransformation * vec4(position, 1.0);
}
