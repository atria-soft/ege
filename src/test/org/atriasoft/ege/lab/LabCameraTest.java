package org.atriasoft.ege.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;
import org.junit.jupiter.api.Test;

/** The orbit and the flight of the camera of a lab. */
class LabCameraTest {

	private static final float EPSILON = 1.0e-3f;

	private static void assertNear(final Vector3f expected, final Vector3f actual) {
		assertEquals(expected.x(), actual.x(), EPSILON, "x of " + actual);
		assertEquals(expected.y(), actual.y(), EPSILON, "y of " + actual);
		assertEquals(expected.z(), actual.z(), EPSILON, "z of " + actual);
	}

	@Test
	void theEyeStartsSouthEastAndAbove() {
		final LabCamera camera = new LabCamera();
		final Vector3f eye = camera.eye().less(camera.target());
		assertTrue(eye.x() > 0 && eye.z() > 0 && eye.y() > 0, "eye " + eye);
		assertEquals(camera.distance(), eye.length(), EPSILON);
	}

	@Test
	void aFramedBoxIsWholeInThePicture() {
		final LabCamera camera = new LabCamera();
		final float fovX = (float) Math.toRadians(70);
		final float aspect = 1.6f;
		final float[] box = { -6, 0, -5, 7, 24, 6 };
		camera.frame(box, fovX, aspect);
		final Matrix4f view = camera.view();
		final double tanX = Math.tan(fovX * 0.5);
		final double tanY = tanX / aspect;
		for (int i = 0; i < 8; i++) {
			final Vector3f corner = new Vector3f((i & 1) == 0 ? box[0] : box[3], (i & 2) == 0 ? box[1] : box[4],
					(i & 4) == 0 ? box[2] : box[5]);
			final Vector3f p = view.multiply(corner);
			assertTrue(p.z() < 0, "in front: " + p);
			assertTrue(Math.abs(p.x()) <= -p.z() * tanX, "within the width: " + p);
			assertTrue(Math.abs(p.y()) <= -p.z() * tanY, "within the height: " + p);
		}
		// The target in the middle of the picture.
		final Vector3f centre = view.multiply(camera.target());
		assertEquals(0.0f, centre.x(), EPSILON);
		assertEquals(0.0f, centre.y(), EPSILON);
	}

	@Test
	void aBoxFramedBesideAPanelStaysInTheFreePart() {
		final LabCamera camera = new LabCamera();
		final float fovX = (float) Math.toRadians(70);
		final float aspect = 1.25f;
		final float share = 0.4f;
		// A long row along X: its near end is the widest in the picture.
		final float[] box = { -90, 0, -15, 90, 26, 15 };
		camera.frame(box, fovX, aspect, share);
		final Matrix4f view = camera.view();
		final double tanX = Math.tan(fovX * 0.5);
		final double tanY = tanX / aspect;
		double least = Double.POSITIVE_INFINITY;
		double most = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < 8; i++) {
			final Vector3f corner = new Vector3f((i & 1) == 0 ? box[0] : box[3], (i & 2) == 0 ? box[1] : box[4],
					(i & 4) == 0 ? box[2] : box[5]);
			final Vector3f p = view.multiply(corner);
			assertTrue(p.z() < 0, "in front: " + p);
			final double x = p.x() / -p.z();
			least = Math.min(least, x);
			most = Math.max(most, x);
			assertTrue(Math.abs(p.y()) <= -p.z() * tanY + 1.0e-4, "within the height: " + p);
		}
		// Right of the panel, within the picture, the margins even.
		assertTrue(least >= tanX * (2 * share - 1) - 1.0e-4, "left of the free part: " + least);
		assertTrue(most <= tanX + 1.0e-4, "right of the picture: " + most);
		assertEquals(tanX - most, least - tanX * (2 * share - 1), 1.0e-3);
	}

	@Test
	void anOrbitKeepsTheTargetAndTheDistance() {
		final LabCamera camera = new LabCamera();
		final Vector3f target = camera.target();
		final float distance = camera.distance();
		camera.turn(120, -40);
		assertNear(target, camera.target());
		assertEquals(distance, camera.eye().less(target).length(), EPSILON);
		// Never under the ground plane of the target.
		camera.turn(0, -100000);
		assertTrue(camera.eye().y() > target.y());
	}

	@Test
	void aPanMovesInThePlaneOfThePicture() {
		final LabCamera camera = new LabCamera();
		final Vector3f before = camera.target();
		final Vector3f look = camera.back();
		camera.pan(50, 30, 600, 1.0f);
		final Vector3f moved = camera.target().less(before);
		assertTrue(moved.length() > 0.1f);
		assertEquals(0.0f, moved.dot(look), EPSILON);
	}

	@Test
	void aFlightKeepsTheEyeWhenTurningAndChangingMode() {
		final LabCamera camera = new LabCamera();
		final Vector3f eye = camera.eye();
		camera.setMode(LabCamera.Mode.FLY);
		assertNear(eye, camera.eye());
		camera.turn(80, 25);
		assertNear(eye, camera.eye());
		camera.drive(1, 0, 0, 0.5f);
		assertTrue(camera.eye().less(eye).length() > 0.5f);
		final Vector3f flown = camera.eye();
		camera.setMode(LabCamera.Mode.ORBIT);
		assertNear(flown, camera.eye());
	}

	@Test
	void aDirectionIsSetAroundTheTargetOrTheEye() {
		final LabCamera camera = new LabCamera();
		final Vector3f target = camera.target();
		camera.setDirection((float) (Math.PI / 2), 0.3f);
		final Vector3f east = camera.eye().less(target);
		assertTrue(east.x() > 0 && Math.abs(east.z()) < EPSILON, "east of the target: " + east);
		assertEquals(0.3f, camera.elevation(), EPSILON);
		assertNear(target, camera.target());
		// The orbit keeps the eye over the ground of the target, the azimuth turns round.
		camera.setDirection((float) (3 * Math.PI), -1.0f);
		assertTrue(camera.elevation() > 0, "elevation " + camera.elevation());
		assertEquals((float) -Math.PI, camera.azimuth(), EPSILON);
		// A flight turns the look around the eye.
		camera.setMode(LabCamera.Mode.FLY);
		final Vector3f eye = camera.eye();
		camera.setDirection(0.0f, -0.5f);
		assertNear(eye, camera.eye());
		assertEquals(-0.5f, camera.elevation(), EPSILON);
	}
}
