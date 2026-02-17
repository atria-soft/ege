#version 400 core

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

layout (location = 0) in vec3 in_position;

uniform mat4 in_matrixTransformation;
uniform mat4 in_lightSpaceMatrix;

void main(void) {
	gl_Position = in_lightSpaceMatrix * in_matrixTransformation * vec4(in_position, 1.0);
}
