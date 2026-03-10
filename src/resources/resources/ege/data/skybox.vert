#version 400 core

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

layout (location = 0) in vec3 in_position;

out vec3 textureCoords;

uniform mat4 in_matrixProjection;
uniform mat4 in_matrixView;

void main() {
	textureCoords = in_position;
	// Strip translation from view matrix (skybox stays at infinity)
	// and write depth = 1.0 via xyww trick so all geometry renders in front
	vec4 pos = in_matrixProjection * mat4(mat3(in_matrixView)) * vec4(in_position, 1.0);
	gl_Position = pos.xyww;
}
