package org.atriasoft.ege.lab;

import java.util.Arrays;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector3f;

/**
 * What a lab shows, as the triangles and lines its renderer draws (metres,
 * X east, Y up, Z south): opaque triangles, flat shaded with a colour each,
 * lit by the sun and casting its shadows; translucent triangles (a colour of
 * alpha under 1: proxies, helpers, drawn over without writing the depth);
 * lines (skeletons, axes, boxes), either hidden by what stands before them or
 * drawn on top of everything.
 * <p>
 * Each triangle is kept as the renderer takes it: three vertices of
 * {@link #LIT_FLOATS} floats (position, unit normal of the face as wound,
 * colour); each line as two vertices of {@link #LINE_FLOATS} floats
 * (position, colour). A face is lit on the side the eye sees it from: the
 * winding only decides the normal before that. Built on any thread (the
 * build thread of a lab), handed over once complete ({@code LabView.setContent})
 * and not changed afterwards. Pure Java, tested headless.
 */
public final class LabMesh {

	/** Floats per vertex of a triangle: position (3), normal of its face (3), colour (4). */
	public static final int LIT_FLOATS = 10;
	/** Floats per vertex of a line: position (3), colour (4). */
	public static final int LINE_FLOATS = 7;

	/** A growable array of floats. */
	static final class Floats {
		private float[] data = new float[64];
		private int size;

		void add(final float value) {
			if (this.size == this.data.length) {
				this.data = Arrays.copyOf(this.data, this.data.length * 2);
			}
			this.data[this.size++] = value;
		}

		void addAll(final Floats other) {
			if (this.size + other.size > this.data.length) {
				this.data = Arrays.copyOf(this.data, Math.max(this.size + other.size, this.data.length * 2));
			}
			System.arraycopy(other.data, 0, this.data, this.size, other.size);
			this.size += other.size;
		}

		/** The array (its first {@link #size()} floats are the values). */
		float[] data() {
			return this.data;
		}

		int size() {
			return this.size;
		}

		float get(final int index) {
			return this.data[index];
		}
	}

	private final Floats opaque = new Floats();
	private final Floats translucent = new Floats();
	private final Floats lines = new Floats();
	private final Floats linesOnTop = new Floats();

	/** A triangle wound {@code a, b, c} in {@code color}: opaque, or translucent when its alpha is under 1. */
	public LabMesh triangle(final Color color, final Vector3f a, final Vector3f b, final Vector3f c) {
		return triangle(color.r(), color.g(), color.b(), color.a(), a.x(), a.y(), a.z(), b.x(), b.y(), b.z(), c.x(),
				c.y(), c.z());
	}

	/** {@link #triangle(Color, Vector3f, Vector3f, Vector3f)} without the vectors (meshes of thousands of triangles). */
	public LabMesh triangle(final float r, final float g, final float b, final float alpha, final float ax,
			final float ay, final float az, final float bx, final float by, final float bz, final float cx,
			final float cy, final float cz) {
		final float ux = bx - ax;
		final float uy = by - ay;
		final float uz = bz - az;
		final float vx = cx - ax;
		final float vy = cy - ay;
		final float vz = cz - az;
		float nx = uy * vz - uz * vy;
		float ny = uz * vx - ux * vz;
		float nz = ux * vy - uy * vx;
		final float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
		if (length > 1.0e-12f) {
			nx /= length;
			ny /= length;
			nz /= length;
		} else {
			// Degenerate: it covers no pixel anyway.
			nx = 0.0f;
			ny = 1.0f;
			nz = 0.0f;
		}
		final Floats out = alpha >= 1.0f ? this.opaque : this.translucent;
		final float a = Math.max(0.0f, Math.min(1.0f, alpha));
		litVertex(out, ax, ay, az, nx, ny, nz, r, g, b, a);
		litVertex(out, bx, by, bz, nx, ny, nz, r, g, b, a);
		litVertex(out, cx, cy, cz, nx, ny, nz, r, g, b, a);
		return this;
	}

	private static void litVertex(final Floats out, final float x, final float y, final float z, final float nx,
			final float ny, final float nz, final float r, final float g, final float b, final float a) {
		out.add(x);
		out.add(y);
		out.add(z);
		out.add(nx);
		out.add(ny);
		out.add(nz);
		out.add(r);
		out.add(g);
		out.add(b);
		out.add(a);
	}

	/** A quad {@code a, b, c, d} (convex, wound in that order) as two triangles. */
	public LabMesh quad(final Color color, final Vector3f a, final Vector3f b, final Vector3f c, final Vector3f d) {
		triangle(color, a, b, c);
		return triangle(color, a, c, d);
	}

	/** A line from {@code a} to {@code b}, hidden by what stands before it. */
	public LabMesh line(final Color color, final Vector3f a, final Vector3f b) {
		addLine(this.lines, color, a, b);
		return this;
	}

	/** A line from {@code a} to {@code b} drawn over everything (a skeleton inside its bark). */
	public LabMesh lineOnTop(final Color color, final Vector3f a, final Vector3f b) {
		addLine(this.linesOnTop, color, a, b);
		return this;
	}

	private static void addLine(final Floats out, final Color color, final Vector3f a, final Vector3f b) {
		for (final Vector3f p : new Vector3f[] { a, b }) {
			out.add(p.x());
			out.add(p.y());
			out.add(p.z());
			out.add(color.r());
			out.add(color.g());
			out.add(color.b());
			out.add(color.a());
		}
	}

	/** Everything of {@code other} added to this mesh. */
	public LabMesh add(final LabMesh other) {
		this.opaque.addAll(other.opaque);
		this.translucent.addAll(other.translucent);
		this.lines.addAll(other.lines);
		this.linesOnTop.addAll(other.linesOnTop);
		return this;
	}

	/** Opaque triangles. */
	public int opaqueTriangles() {
		return this.opaque.size() / (3 * LIT_FLOATS);
	}

	/** Translucent triangles. */
	public int translucentTriangles() {
		return this.translucent.size() / (3 * LIT_FLOATS);
	}

	/** Lines, hidden or on top. */
	public int lineCount() {
		return (this.lines.size() + this.linesOnTop.size()) / (2 * LINE_FLOATS);
	}

	/** Whether it holds nothing. */
	public boolean isEmpty() {
		return this.opaque.size() == 0 && this.translucent.size() == 0 && this.lines.size() == 0
				&& this.linesOnTop.size() == 0;
	}

	/**
	 * The box of everything it holds, {@code {minX, minY, minZ, maxX, maxY,
	 * maxZ}}, {@code null} when it holds nothing.
	 */
	public float[] bounds() {
		float[] box = null;
		box = grow(box, this.opaque, LIT_FLOATS);
		box = grow(box, this.translucent, LIT_FLOATS);
		box = grow(box, this.lines, LINE_FLOATS);
		return grow(box, this.linesOnTop, LINE_FLOATS);
	}

	private static float[] grow(final float[] box, final Floats floats, final int stride) {
		float[] out = box;
		for (int i = 0; i + 2 < floats.size(); i += stride) {
			if (out == null) {
				out = new float[] { Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
						Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY };
			}
			for (int axis = 0; axis < 3; axis++) {
				final float value = floats.get(i + axis);
				out[axis] = Math.min(out[axis], value);
				out[axis + 3] = Math.max(out[axis + 3], value);
			}
		}
		return out;
	}

	/** The opaque triangles as the renderer takes them ({@link #LIT_FLOATS} floats a vertex). */
	Floats opaque() {
		return this.opaque;
	}

	/** The translucent triangles ({@link #LIT_FLOATS} floats a vertex). */
	Floats translucent() {
		return this.translucent;
	}

	/** The lines hidden by what stands before them ({@link #LINE_FLOATS} floats a vertex). */
	Floats lines() {
		return this.lines;
	}

	/** The lines drawn on top ({@link #LINE_FLOATS} floats a vertex). */
	Floats linesOnTop() {
		return this.linesOnTop;
	}
}
