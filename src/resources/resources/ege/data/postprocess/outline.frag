#version 400 core

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

in vec2 v_texCoord;

uniform sampler2D in_silhouetteTexture;
uniform vec4 in_outlineColor;
uniform float in_outlineSize;
uniform vec2 in_texelSize;

out vec4 out_Color;

void main(void) {
	// Check if current pixel is inside the silhouette
	float center = texture(in_silhouetteTexture, v_texCoord).r;
	if (center > 0.0) {
		discard; // inside object silhouette — keep original rendering
	}

	// Search circular region for filled neighbor pixels
	int outInt = int(ceil(in_outlineSize));
	float o2 = in_outlineSize * in_outlineSize;

	for (int y = -outInt; y <= outInt; y++) {
		for (int x = -outInt; x <= outInt; x++) {
			if (float(x * x + y * y) > o2) continue;

			vec2 offset = vec2(float(x), float(y)) * in_texelSize;
			float neighbor = texture(in_silhouetteTexture, v_texCoord + offset).r;

			if (neighbor > 0.0) {
				out_Color = in_outlineColor;
				return;
			}
		}
	}

	// No filled neighbor — not part of the outline
	discard;
}
