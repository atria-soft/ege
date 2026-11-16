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
	void anOrbitGoesUnderTheGroundOnlyWhenAllowed() {
		final LabCamera camera = new LabCamera();
		camera.setUnderground(true);
		camera.turn(0, -100000);
		assertTrue(camera.eye().y() < camera.target().y() - 1.0f, "under the target: " + camera.eye());
		assertEquals(-LabCamera.MAX_ELEVATION, camera.elevation(), EPSILON);
		camera.setUnderground(false);
		assertEquals(LabCamera.MIN_ORBIT_ELEVATION, camera.elevation(), EPSILON);
		camera.turn(0, -100000);
		assertEquals(LabCamera.MIN_ORBIT_ELEVATION, camera.elevation(), EPSILON);
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

	@Test
	void theRayOfAPixelFindsWhatLiesUnderIt() {
		final LabCamera camera = new LabCamera();
		camera.setDirection(0.0f, (float) Math.toRadians(89.0));
		camera.lookAt(new Vector3f(3.0f, 0.0f, -2.0f), 20.0f);
		final float fovX = (float) Math.toRadians(70.0);
		// The middle of the picture: the target.
		final Vector3f middle = camera.ray(400.0f, 300.0f, 800.0f, 600.0f, fovX);
		assertNear(camera.back().multiply(-1.0f), middle);
		final LabPointer.Event centre = new LabPointer.Event(LabPointer.Action.HOVER, 0, 400.0f, 300.0f, camera.eye(),
				middle, false, false);
		assertNear(new Vector3f(3.0f, 0.0f, -2.0f), centre.ground());
		// Right of the middle: east (X grows); above it: north (Z falls) for an eye south of the target looking down.
		final Vector3f right = new LabPointer.Event(LabPointer.Action.HOVER, 0, 800.0f, 300.0f, camera.eye(), camera.ray(
				800.0f, 300.0f, 800.0f, 600.0f, fovX), false, false).ground();
		assertEquals(3.0f + 20.0f * (float) Math.tan(fovX / 2.0), right.x(), 0.2f);
		final Vector3f top = new LabPointer.Event(LabPointer.Action.HOVER, 0, 400.0f, 600.0f, camera.eye(), camera.ray(
				400.0f, 600.0f, 800.0f, 600.0f, fovX), false, false).ground();
		assertTrue(top.z() < -2.0f - 5.0f, "north: " + top);
		// Looking above the horizon: no ground.
		final LabCamera level = new LabCamera();
		level.setDirection(0.0f, (float) Math.toRadians(5.0));
		assertEquals(null, new LabPointer.Event(LabPointer.Action.HOVER, 0, 400.0f, 600.0f, level.eye(), level.ray(400.0f,
				600.0f, 800.0f, 600.0f, fovX), false, false).ground());
	}

	@Test
	void aWalkerKeepsHisEyeAtItsHeightWhateverHeDoes() {
		final LabCamera camera = new LabCamera();
		camera.lookAt(new Vector3f(4.0f, 3.0f, -6.0f), 40.0f);
		final Vector3f before = camera.eye();
		camera.setMode(LabCamera.Mode.WALK);
		// Where the eye stood, down to the height of an eye, looking level.
		assertNear(new Vector3f(before.x(), LabCamera.EYE_HEIGHT, before.z()), camera.eye());
		assertEquals(0.0f, camera.elevation(), EPSILON);
		camera.turn(35.0f, -80.0f);
		camera.drive(1, 1, 1, 0.5f);
		camera.zoom(3.0f);
		camera.pan(30.0f, 40.0f, 600.0f, 1.0f);
		camera.setDirection(1.0f, 1.2f);
		camera.lookAt(new Vector3f(0.0f, 20.0f, 0.0f), 5.0f);
		camera.frame(new float[] { -50, 0, -50, 50, 30, 50 }, (float) Math.toRadians(70.0), 1.5f);
		for (int i = 0; i < 40; i++) {
			camera.turn(7.0f, 13.0f);
			camera.drive(1, -1, -1, 0.1f);
		}
		assertEquals(LabCamera.EYE_HEIGHT, camera.eye().y(), EPSILON, "never leaves the ground plane");
	}

	@Test
	void aWalkerWalksOnTheGroundAndRunsWithShift() {
		final LabCamera camera = new LabCamera();
		// From (10, 20), looking north (an azimuth of 0: the eye south of what it looks at).
		camera.walkFrom(10.0f, 20.0f, 0.0f);
		assertNear(new Vector3f(10.0f, LabCamera.EYE_HEIGHT, 20.0f), camera.eye());
		// Looking down does not walk into the ground: a second ahead is WALK_SPEED metres north.
		camera.turn(0.0f, -100.0f);
		camera.drive(1, 0, 1, 1.0f);
		assertNear(new Vector3f(10.0f, LabCamera.EYE_HEIGHT, 20.0f - LabCamera.WALK_SPEED), camera.eye());
		// A step aside: east, to the right of a walker looking north.
		camera.drive(0, 1, 0, 1.0f);
		assertNear(new Vector3f(10.0f + LabCamera.WALK_SPEED, LabCamera.EYE_HEIGHT, 20.0f - LabCamera.WALK_SPEED),
				camera.eye());
		// Running, on a diagonal: as fast.
		camera.setRunning(true);
		final Vector3f from = camera.eye();
		camera.drive(-1, -1, 0, 1.0f);
		assertEquals(LabCamera.RUN_SPEED, camera.eye().less(from).length(), EPSILON);
		// Framing leaves the walker where he stands; an orbit again keeps the eye.
		final Vector3f standing = camera.eye();
		camera.frame(new float[] { -5, 0, -5, 5, 10, 5 }, (float) Math.toRadians(70.0), 1.5f);
		assertNear(standing, camera.eye());
		camera.setMode(LabCamera.Mode.ORBIT);
		assertNear(standing, camera.eye());
	}
}
