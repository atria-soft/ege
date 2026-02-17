#version 400 core

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

in vec2 v_texCoord;

uniform sampler2D in_depthTexture;

out vec4 out_Color;

void main(void) {
	float depth = texture(in_depthTexture, v_texCoord).r;
	// Ortho projection has linear depth.
	// Border color is 1.0 (white = no shadow), objects are darker.
	// Apply contrast enhancement: remap depth to make objects more visible.
	// Most depth values cluster near 1.0 in large ortho projections.
	// pow(depth, 8) pushes mid-range values darker while keeping 1.0 white.
	float enhanced = pow(depth, 8.0);
	out_Color = vec4(enhanced, enhanced, enhanced, 1.0);
}
