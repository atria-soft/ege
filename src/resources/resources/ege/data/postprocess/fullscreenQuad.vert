#version 400 core

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

layout (location = 0) in vec3 in_position;
layout (location = 1) in vec2 in_texCoord;

out vec2 v_texCoord;

void main(void) {
	gl_Position = vec4(in_position, 1.0);
	v_texCoord = in_texCoord;
}
