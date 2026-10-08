#version 400 core

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// Lit triangles of the lab kit (labLit.vert): a hemisphere ambient (sky above, ground below) and the sun, of
// which the cascaded shadows of ege's shadow engine take a share. Each face is lit on the side the eye sees:
// the winding of the shapes does not matter. The ground (in_grid = 1) draws its lines every 1, 5 and 25 m,
// and the axes through the origin: X (east) red, Z (south) blue.
const int MAX_SHADOW_MAPS = 12; // MAX_CASTERS(3) * MAX_CASCADES(4)
const int MAX_CASCADES = 4;
// Fraction of a cascade range blended with the next one
const float CASCADE_BLEND_BAND = 0.1;

in vec4 io_color;
in vec3 io_normal;
in vec3 io_worldPosition;
in float io_viewDepth;

uniform vec3 in_eye;
// Unit direction towards the sun, its light, the ambient light from the sky and from the ground.
uniform vec3 in_toSun;
uniform vec3 in_sunColor;
uniform vec3 in_skyAmbient;
uniform vec3 in_groundAmbient;
// Share of the sunlight a full shadow takes away.
uniform float in_shadowStrength;
// 1 while the ground is drawn.
uniform int in_grid;

// CSM shadow uniforms (ege ShadowRender)
uniform int in_shadowCasterCount;
uniform int in_cascadeCount;
uniform mat4 in_lightSpaceMatrix[MAX_SHADOW_MAPS];
uniform sampler2DShadow in_shadowMap[MAX_SHADOW_MAPS];
uniform float in_cascadeSplits[MAX_CASCADES];
// PCF kernel half-size: 0 = hard (1x1), 1 = medium (3x3), 2 = soft (5x5)
uniform int in_pcfHalfKernel;

out vec4 out_Color;

// ---- Cascaded shadow maps: COPIED CODE (dementia surface.frag, eMapRender terrain.frag) ----------
// Adaptive slope-scaled bias to reduce shadow acne on angled surfaces.
float computeBias(vec3 normal, vec3 lightDir) {
	float cosTheta = abs(dot(normal, lightDir));
	return max(0.001 * (1.0 - cosTheta), 0.0002);
}

// Hardware comparison of shadow map mapIndex at coord (xy, reference depth): 1.0 lit, 0.0 in shadow. GLSL 4.00
// only indexes an array of samplers by a dynamically uniform value: each map through a constant index.
float shadowSample(int mapIndex, vec3 coord) {
	switch (mapIndex) {
	case 0: return textureLod(in_shadowMap[0], coord, 0.0);
	case 1: return textureLod(in_shadowMap[1], coord, 0.0);
	case 2: return textureLod(in_shadowMap[2], coord, 0.0);
	case 3: return textureLod(in_shadowMap[3], coord, 0.0);
	case 4: return textureLod(in_shadowMap[4], coord, 0.0);
	case 5: return textureLod(in_shadowMap[5], coord, 0.0);
	case 6: return textureLod(in_shadowMap[6], coord, 0.0);
	case 7: return textureLod(in_shadowMap[7], coord, 0.0);
	case 8: return textureLod(in_shadowMap[8], coord, 0.0);
	case 9: return textureLod(in_shadowMap[9], coord, 0.0);
	case 10: return textureLod(in_shadowMap[10], coord, 0.0);
	case 11: return textureLod(in_shadowMap[11], coord, 0.0);
	}
	return 1.0;
}

// PCF shadow of one shadow map: 1.0 in full shadow, 0.0 lit.
float calculateShadow(int mapIndex, float bias) {
	vec4 fragPosLightSpace = in_lightSpaceMatrix[mapIndex] * vec4(io_worldPosition, 1.0);
	vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
	projCoords = projCoords * 0.5 + 0.5;
	if (projCoords.z > 1.0) {
		return 0.0;
	}
	float refDepth = projCoords.z - bias;
	float lit = 0.0;
	vec2 texelSize = 1.0 / textureSize(in_shadowMap[0], 0);
	int halfK = in_pcfHalfKernel;
	int sampleCount = 0;
	for (int x = -halfK; x <= halfK; x++) {
		for (int y = -halfK; y <= halfK; y++) {
			lit += shadowSample(mapIndex, vec3(projCoords.xy + vec2(x, y) * texelSize, refDepth));
			sampleCount++;
		}
	}
	return 1.0 - lit / float(sampleCount);
}

// Cascade of the fragment (by view depth) and the blend factor with the next one.
int selectCascade(out float blendFactor) {
	blendFactor = 0.0;
	for (int i = 0; i < in_cascadeCount - 1; i++) {
		if (io_viewDepth < in_cascadeSplits[i]) {
			float cascadeNear = (i == 0) ? 0.0 : in_cascadeSplits[i - 1];
			float cascadeRange = in_cascadeSplits[i] - cascadeNear;
			float blendStart = in_cascadeSplits[i] - cascadeRange * CASCADE_BLEND_BAND;
			if (io_viewDepth > blendStart) {
				blendFactor = (io_viewDepth - blendStart) / (cascadeRange * CASCADE_BLEND_BAND);
			}
			return i;
		}
	}
	return in_cascadeCount - 1;
}

// How much of the sun the fragment misses, 0 (lit) to 1 (in shadow).
float sunShadow(vec3 normal, vec3 toSun) {
	float bias = computeBias(normal, toSun);
	float blendFactor;
	int cascade = selectCascade(blendFactor);
	float maxShadow = 0.0;
	for (int caster = 0; caster < in_shadowCasterCount; caster++) {
		int mapIndex = caster * in_cascadeCount + cascade;
		if (mapIndex < MAX_SHADOW_MAPS) {
			float shadowVal = calculateShadow(mapIndex, bias);
			if (blendFactor > 0.0 && cascade + 1 < in_cascadeCount) {
				int nextMapIndex = caster * in_cascadeCount + cascade + 1;
				if (nextMapIndex < MAX_SHADOW_MAPS) {
					shadowVal = mix(shadowVal, calculateShadow(nextMapIndex, bias), blendFactor);
				}
			}
			maxShadow = max(maxShadow, shadowVal);
		}
	}
	return maxShadow;
}
// ---- End of the copied cascaded shadow code ---------------------------------------------------------

// Coverage (0 to 1) of the lines of a grid of `spacing` metres at the ground point p, about `width` pixels wide;
// faded out where they would come closer than a few pixels to each other.
float gridLines(vec2 p, float spacing, float width) {
	vec2 coord = p / spacing;
	vec2 perPixel = max(fwidth(coord), vec2(1.0e-6));
	vec2 distance = abs(fract(coord - 0.5) - 0.5) / perPixel;
	float line = 1.0 - min(min(distance.x, distance.y) / width, 1.0);
	float fade = 1.0 - smoothstep(0.08, 0.25, max(perPixel.x, perPixel.y));
	return line * fade;
}

// Coverage of the line where the ground coordinate v is 0, about `width` pixels wide.
float axisLine(float v, float width) {
	return 1.0 - min(abs(v) / max(fwidth(v), 1.0e-6) / width, 1.0);
}

void main(void) {
	vec3 normal = normalize(io_normal);
	if (dot(normal, in_eye - io_worldPosition) < 0.0) {
		normal = -normal;
	}
	vec3 albedo = io_color.rgb;
	if (in_grid != 0) {
		vec2 p = io_worldPosition.xz;
		float lines = max(gridLines(p, 1.0, 1.0) * 0.16, max(gridLines(p, 5.0, 1.4) * 0.32,
				gridLines(p, 25.0, 2.0) * 0.5));
		albedo = mix(albedo, vec3(0.16, 0.18, 0.14), lines);
		albedo = mix(albedo, vec3(0.75, 0.16, 0.12), axisLine(p.y, 2.0) * 0.8);
		albedo = mix(albedo, vec3(0.14, 0.30, 0.80), axisLine(p.x, 2.0) * 0.8);
	}
	float shadow = 0.0;
	if (in_shadowCasterCount > 0 && in_cascadeCount > 0) {
		shadow = sunShadow(normal, in_toSun);
	}
	vec3 ambient = mix(in_groundAmbient, in_skyAmbient, 0.5 + 0.5 * normal.y);
	vec3 direct = in_sunColor * (max(dot(normal, in_toSun), 0.0) * (1.0 - in_shadowStrength * shadow));
	out_Color = vec4(albedo * (ambient + direct), io_color.a);
}
