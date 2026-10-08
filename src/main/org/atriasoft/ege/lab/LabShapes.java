package org.atriasoft.ege.lab;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector3f;

/**
 * Simple shapes added to a {@link LabMesh}: boxes, vertical cylinders,
 * polygons, wire boxes and crosses for the debug views, and the human figure
 * of 1.80 m every lab can show beside its model for scale. Faces wound
 * counter-clockwise seen from outside. Pure Java, tested headless.
 */
public final class LabShapes {

	/** Height of the human figure, metres. */
	public static final float HUMAN_HEIGHT = 1.80f;
	/** Colour of the human figure (the blue of eFlora's pictures). */
	public static final Color HUMAN = new Color(0x2F / 255.0f, 0x6F / 255.0f, 0xE0 / 255.0f, 1.0f);
	private static final Color HUMAN_HEAD = new Color(0.86f, 0.70f, 0.58f, 1.0f);

	private LabShapes() {}

	/** An axis-aligned box. */
	public static void box(final LabMesh mesh, final Color color, final float x0, final float y0, final float z0,
			final float x1, final float y1, final float z1) {
		final Vector3f p000 = new Vector3f(x0, y0, z0);
		final Vector3f p100 = new Vector3f(x1, y0, z0);
		final Vector3f p010 = new Vector3f(x0, y1, z0);
		final Vector3f p110 = new Vector3f(x1, y1, z0);
		final Vector3f p001 = new Vector3f(x0, y0, z1);
		final Vector3f p101 = new Vector3f(x1, y0, z1);
		final Vector3f p011 = new Vector3f(x0, y1, z1);
		final Vector3f p111 = new Vector3f(x1, y1, z1);
		mesh.quad(color, p000, p100, p101, p001);
		mesh.quad(color, p010, p011, p111, p110);
		mesh.quad(color, p001, p101, p111, p011);
		mesh.quad(color, p000, p010, p110, p100);
		mesh.quad(color, p100, p110, p111, p101);
		mesh.quad(color, p000, p001, p011, p010);
	}

	/**
	 * A vertical cylinder of {@code sides} faces around {@code (x, z)} from
	 * {@code y0} to {@code y1}, closed at both ends.
	 */
	public static void cylinder(final LabMesh mesh, final Color color, final float x, final float z,
			final float radius, final float y0, final float y1, final int sides) {
		final int n = Math.max(3, sides);
		final Vector3f bottom = new Vector3f(x, y0, z);
		final Vector3f top = new Vector3f(x, y1, z);
		for (int i = 0; i < n; i++) {
			final double a0 = 2.0 * Math.PI * i / n;
			final double a1 = 2.0 * Math.PI * (i + 1) / n;
			final float x0 = x + radius * (float) Math.cos(a0);
			final float z0 = z + radius * (float) Math.sin(a0);
			final float x1 = x + radius * (float) Math.cos(a1);
			final float z1 = z + radius * (float) Math.sin(a1);
			final Vector3f b0 = new Vector3f(x0, y0, z0);
			final Vector3f b1 = new Vector3f(x1, y0, z1);
			final Vector3f t0 = new Vector3f(x0, y1, z0);
			final Vector3f t1 = new Vector3f(x1, y1, z1);
			// The angle turns from +X towards +Z: seen from outside, b0 t0 t1 b1 turns counter-clockwise.
			mesh.quad(color, b0, t0, t1, b1);
			mesh.triangle(color, top, t1, t0);
			mesh.triangle(color, bottom, b0, b1);
		}
	}

	/**
	 * A convex polygon as a fan of triangles from its first corner; on both
	 * sides when {@code twoSided} (a thin translucent pane seen from anywhere).
	 */
	public static void polygon(final LabMesh mesh, final Color color, final boolean twoSided,
			final Vector3f... corners) {
		for (int i = 1; i + 1 < corners.length; i++) {
			mesh.triangle(color, corners[0], corners[i], corners[i + 1]);
			if (twoSided) {
				mesh.triangle(color, corners[0], corners[i + 1], corners[i]);
			}
		}
	}

	/** The twelve edges of the box {@code {minX, minY, minZ, maxX, maxY, maxZ}} as lines. */
	public static void wireBox(final LabMesh mesh, final Color color, final float[] box, final boolean onTop) {
		final Vector3f[] p = new Vector3f[8];
		for (int i = 0; i < 8; i++) {
			p[i] = new Vector3f((i & 1) == 0 ? box[0] : box[3], (i & 2) == 0 ? box[1] : box[4],
					(i & 4) == 0 ? box[2] : box[5]);
		}
		final int[][] edges = { { 0, 1 }, { 2, 3 }, { 4, 5 }, { 6, 7 }, { 0, 2 }, { 1, 3 }, { 4, 6 }, { 5, 7 },
				{ 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 } };
		for (final int[] edge : edges) {
			if (onTop) {
				mesh.lineOnTop(color, p[edge[0]], p[edge[1]]);
			} else {
				mesh.line(color, p[edge[0]], p[edge[1]]);
			}
		}
	}

	/** Three lines of {@code size} metres crossing at {@code p}, along the axes (a point marker). */
	public static void cross(final LabMesh mesh, final Color color, final Vector3f p, final float size,
			final boolean onTop) {
		final float h = size * 0.5f;
		final Vector3f[][] lines = { { p.add(-h, 0, 0), p.add(h, 0, 0) }, { p.add(0, -h, 0), p.add(0, h, 0) },
				{ p.add(0, 0, -h), p.add(0, 0, h) } };
		for (final Vector3f[] line : lines) {
			if (onTop) {
				mesh.lineOnTop(color, line[0], line[1]);
			} else {
				mesh.line(color, line[0], line[1]);
			}
		}
	}

	/**
	 * A human figure of {@link #HUMAN_HEIGHT} standing on the ground at
	 * {@code (x, y, z)}, facing south (+Z): legs, body, arms, neck and head in
	 * boxes, shoulders 58 cm wide.
	 */
	public static void human(final LabMesh mesh, final float x, final float y, final float z) {
		box(mesh, HUMAN, x - 0.17f, y, z - 0.08f, x - 0.03f, y + 0.86f, z + 0.08f);
		box(mesh, HUMAN, x + 0.03f, y, z - 0.08f, x + 0.17f, y + 0.86f, z + 0.08f);
		box(mesh, HUMAN, x - 0.20f, y + 0.86f, z - 0.11f, x + 0.20f, y + 1.46f, z + 0.11f);
		box(mesh, HUMAN, x - 0.29f, y + 0.80f, z - 0.06f, x - 0.21f, y + 1.44f, z + 0.06f);
		box(mesh, HUMAN, x + 0.21f, y + 0.80f, z - 0.06f, x + 0.29f, y + 1.44f, z + 0.06f);
		box(mesh, HUMAN_HEAD, x - 0.05f, y + 1.46f, z - 0.05f, x + 0.05f, y + 1.54f, z + 0.05f);
		box(mesh, HUMAN_HEAD, x - 0.10f, y + 1.54f, z - 0.11f, x + 0.10f, y + HUMAN_HEIGHT, z + 0.11f);
	}
}
