#version 400 core

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

out vec4 out_Color;

void main(void) {
	out_Color = vec4(1.0, 1.0, 1.0, 1.0);
}
