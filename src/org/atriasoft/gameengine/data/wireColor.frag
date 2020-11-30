#version 400 core

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

in vec4 io_color;

// output:
out vec4 out_Color;

void main(void) {
	out_Color = vec4(1.0,0.0,0.0,1.0);
	// io_color;
}
