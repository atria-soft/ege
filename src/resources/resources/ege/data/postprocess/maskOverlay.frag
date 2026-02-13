#version 400 core

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

in vec2 v_texCoord;

uniform sampler2D in_silhouetteTexture;
uniform vec4 in_overlayColor;

out vec4 out_Color;

void main(void) {
	float mask = texture(in_silhouetteTexture, v_texCoord).r;
	if (mask <= 0.0) {
		discard; // outside the mesh silhouette
	}
	out_Color = in_overlayColor;
}
