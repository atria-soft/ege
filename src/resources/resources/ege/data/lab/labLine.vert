#version 400 core

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// Lines of the lab kit (org.atriasoft.ege.lab.LabRenderer): world positions, one colour per vertex, unlit.
layout (location = 0) in vec3 in_position;
layout (location = 3) in vec4 in_colors;

uniform mat4 in_matrixProjection;
uniform mat4 in_matrixView;

out vec4 io_color;

void main(void) {
	gl_Position = in_matrixProjection * in_matrixView * vec4(in_position, 1.0);
	io_color = in_colors;
}
