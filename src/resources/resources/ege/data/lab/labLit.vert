#version 400 core

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// Lit triangles of the lab kit (org.atriasoft.ege.lab.LabRenderer): world positions, one normal per face
// (flat shading), one colour per vertex (its alpha: the opacity of a translucent overlay). Lit per pixel in
// labLit.frag.
layout (location = 0) in vec3 in_position;
layout (location = 2) in vec3 in_normal;
layout (location = 3) in vec4 in_colors;

uniform mat4 in_matrixProjection;
uniform mat4 in_matrixView;

out vec4 io_color;
out vec3 io_normal;
out vec3 io_worldPosition;
// Depth in front of the camera: picks the shadow cascade (ege fits them along the view depth).
out float io_viewDepth;

void main(void) {
	vec4 relative = in_matrixView * vec4(in_position, 1.0);
	gl_Position = in_matrixProjection * relative;
	io_color = in_colors;
	io_normal = in_normal;
	io_worldPosition = in_position;
	io_viewDepth = -relative.z;
}
