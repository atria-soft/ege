package org.atriasoft.ege.lab;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector3f;
import org.junit.jupiter.api.Test;

/** The content of a lab as its renderer takes it: normals of the faces, translucency, lines, bounds. */
class LabMeshTest {

	private static final float EPSILON = 1.0e-6f;

	@Test
	void aTriangleKeepsItsColourAndTheNormalOfItsWinding() {
		final LabMesh mesh = new LabMesh();
		// Counter-clockwise seen from above: the normal points up.
		mesh.triangle(new Color(0.2f, 0.4f, 0.6f, 1.0f), new Vector3f(0, 0, 0), new Vector3f(0, 0, 1),
				new Vector3f(1, 0, 0));
		assertEquals(1, mesh.opaqueTriangles());
		assertEquals(0, mesh.translucentTriangles());
		final LabMesh.Floats floats = mesh.opaque();
		assertEquals(3 * LabMesh.LIT_FLOATS, floats.size());
		for (int v = 0; v < 3; v++) {
			final int o = v * LabMesh.LIT_FLOATS;
			assertEquals(0.0f, floats.get(o + 3), EPSILON);
			assertEquals(1.0f, floats.get(o + 4), EPSILON);
			assertEquals(0.0f, floats.get(o + 5), EPSILON);
			assertEquals(0.2f, floats.get(o + 6), EPSILON);
			assertEquals(0.4f, floats.get(o + 7), EPSILON);
			assertEquals(0.6f, floats.get(o + 8), EPSILON);
			assertEquals(1.0f, floats.get(o + 9), EPSILON);
		}
		// Wound the other way: down.
		final LabMesh other = new LabMesh();
		other.triangle(Color.WHITE, new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), new Vector3f(0, 0, 1));
		assertEquals(-1.0f, other.opaque().get(4), EPSILON);
	}

	@Test
	void aColourUnderFullAlphaIsTranslucentAndADegenerateTriangleFacesUp() {
		final LabMesh mesh = new LabMesh();
		mesh.triangle(new Color(1, 0, 0, 0.45f), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), new Vector3f(2, 2, 2));
		assertEquals(0, mesh.opaqueTriangles());
		assertEquals(1, mesh.translucentTriangles());
		assertEquals(1.0f, mesh.translucent().get(4), EPSILON);
		assertEquals(0.45f, mesh.translucent().get(9), EPSILON);
	}

	@Test
	void boundsHoldEverythingAndLinesCount() {
		final LabMesh mesh = new LabMesh();
		assertTrue(mesh.isEmpty());
		assertNull(mesh.bounds());
		LabShapes.box(mesh, Color.WHITE, -1, 0, -2, 1, 3, 2);
		mesh.lineOnTop(Color.RED, new Vector3f(0, 0, 0), new Vector3f(0, 5, 0));
		mesh.line(Color.RED, new Vector3f(-4, 0, 0), new Vector3f(0, 0, 0));
		assertEquals(12, mesh.opaqueTriangles());
		assertEquals(2, mesh.lineCount());
		assertArrayEquals(new float[] { -4, 0, -2, 1, 5, 2 }, mesh.bounds(), EPSILON);
		final LabMesh sum = new LabMesh().add(mesh).add(mesh);
		assertEquals(24, sum.opaqueTriangles());
		assertEquals(4, sum.lineCount());
	}

	@Test
	void theFacesOfABoxPointOutwards() {
		final LabMesh mesh = new LabMesh();
		LabShapes.box(mesh, Color.WHITE, -1, -1, -1, 1, 1, 1);
		final LabMesh.Floats floats = mesh.opaque();
		for (int t = 0; t < mesh.opaqueTriangles(); t++) {
			final int o = t * 3 * LabMesh.LIT_FLOATS;
			float cx = 0;
			float cy = 0;
			float cz = 0;
			for (int v = 0; v < 3; v++) {
				cx += floats.get(o + v * LabMesh.LIT_FLOATS) / 3;
				cy += floats.get(o + v * LabMesh.LIT_FLOATS + 1) / 3;
				cz += floats.get(o + v * LabMesh.LIT_FLOATS + 2) / 3;
			}
			final float outward = cx * floats.get(o + 3) + cy * floats.get(o + 4) + cz * floats.get(o + 5);
			assertTrue(outward > 0, "triangle " + t);
		}
	}

	@Test
	void theHumanIsOneEightyTall() {
		final LabMesh mesh = new LabMesh();
		LabShapes.human(mesh, 3, 0, -2);
		final float[] box = mesh.bounds();
		assertEquals(0.0f, box[1], EPSILON);
		assertEquals(LabShapes.HUMAN_HEIGHT, box[4], 1.0e-5f);
		assertEquals(3.0f, (box[0] + box[3]) * 0.5f, 1.0e-5f);
		assertEquals(-2.0f, (box[2] + box[5]) * 0.5f, 1.0e-5f);
	}
}
