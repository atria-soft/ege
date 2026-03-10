#version 400 core

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

in vec3 textureCoords;

out vec4 out_Color;

uniform samplerCube cubeMap;

void main() {
	out_Color = texture(cubeMap, textureCoords);
}
