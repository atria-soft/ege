package org.atriasoft.ege.shadow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.atriasoft.ege.camera.Camera;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;
import org.junit.jupiter.api.Test;

/**
 * Stabilised cascades ({@link ShadowConfig#setStabilized}): the same size
 * whatever the camera looks at, a point of the world on the same texel
 * whatever the camera does, the slice inside the map.
 */
class TestShadowCascadeStable {

	private static final int RESOLUTION = 1024;
	private static final float FOV = 1.0f;
	private static final float ASPECT = 16.0f / 9.0f;
	private static final float ANGLE = (float) Math.acos(2.0 / 3.0);
	private static final float INCLINATION = (float) Math.atan(0.5);
	private static final Vector3f LIGHT = new Vector3f(1.0f, 1.0f, 0.5f).safeNormalize();

	private static Matrix4f fit(final ShadowCascade cascade, final Vector3f position, final float yaw,
			final float pitch, final int resolution) {
		final Camera camera = new Camera();
		camera.setPosition(position);
		camera.setYaw(yaw);
		camera.setPitch(pitch);
		return cascade.computeLightSpaceMatrix(LIGHT, ANGLE, INCLINATION, camera, FOV, ASPECT, resolution);
	}

	/** Where a world point falls in the shadow map, texels. */
	private static float[] texel(final Matrix4f lightSpace, final Vector3f point) {
		final Vector3f ndc = lightSpace.multiply(point);
		return new float[] { (ndc.x() * 0.5f + 0.5f) * RESOLUTION, (ndc.y() * 0.5f + 0.5f) * RESOLUTION };
	}

	private static float width(final Matrix4f lightSpace) {
		// The X scale of the orthographic box: 2 / width, along the right axis of the light.
		final float sx = (float) Math.sqrt(lightSpace.a1() * lightSpace.a1() + lightSpace.b1() * lightSpace.b1()
				+ lightSpace.c1() * lightSpace.c1());
		return 2.0f / sx;
	}

	@Test
	void theSizeDoesNotChangeWhileTheCameraTurns() {
		final ShadowCascade cascade = new ShadowCascade();
		cascade.setSplitRange(0.1f, 20.0f);
		final float first = width(fit(cascade, new Vector3f(10.0f, 2.0f, 10.0f), 0.0f, 0.0f, RESOLUTION));
		for (int step = 1; step < 12; step++) {
			final float yaw = step * 0.53f;
			final float pitch = (step % 3 - 1) * 0.4f;
			assertEquals(first, width(fit(cascade, new Vector3f(10.0f, 2.0f, 10.0f), yaw, pitch, RESOLUTION)), 1.0e-3f,
					"yaw " + yaw);
		}
		// The tight fit of before changes with the view.
		final float tightA = width(fit(cascade, new Vector3f(10.0f, 2.0f, 10.0f), 0.0f, 0.0f, 0));
		final float tightB = width(fit(cascade, new Vector3f(10.0f, 2.0f, 10.0f), 0.8f, 0.3f, 0));
		assertFalse(Math.abs(tightA - tightB) < 1.0e-3f, "the tight fit follows the view");
	}

	@Test
	void aPointOfTheWorldStaysOnTheGridOfTheTexelsWhileTheCameraMoves() {
		final ShadowCascade cascade = new ShadowCascade();
		cascade.setSplitRange(0.1f, 20.0f);
		final Vector3f point = new Vector3f(13.37f, 0.5f, 11.11f);
		final float[] reference = texel(fit(cascade, new Vector3f(10.0f, 2.0f, 10.0f), 0.2f, -0.1f, RESOLUTION),
				point);
		for (int step = 1; step < 40; step++) {
			// Sub-texel and larger moves of the camera, and turns.
			final Vector3f eye = new Vector3f(10.0f + step * 0.0137f, 2.0f, 10.0f - step * 0.0071f);
			final float[] at = texel(fit(cascade, eye, 0.2f + step * 0.002f, -0.1f, RESOLUTION), point);
			for (int axis = 0; axis < 2; axis++) {
				final float moved = at[axis] - reference[axis];
				assertEquals(Math.round(moved), moved, 2.0e-2f, "step " + step + " axis " + axis
						+ ": the point moved by " + moved + " texels");
			}
		}
	}

	/** The Z scale of the orthographic box: 2 / depth range, along the view axis of the light. */
	private static float depthScale(final Matrix4f lightSpace) {
		return (float) Math.sqrt(lightSpace.a3() * lightSpace.a3() + lightSpace.b3() * lightSpace.b3()
				+ lightSpace.c3() * lightSpace.c3());
	}

	@Test
	void theSizeAndTheDepthComeFromTheSliceAloneNeverFromTheTurnOfTheCamera() {
		final ShadowCascade cascade = new ShadowCascade();
		cascade.setSplitRange(4.3f, 37.9f);
		final double radius = ShadowCascade.sliceRadius(4.3f, 37.9f, FOV, ASPECT);
		final float expected = (float) (Math.ceil(radius * 16.0) / 16.0) * (1.0f + 4.0f / RESOLUTION) * 2.0f;
		final Vector3f eye = new Vector3f(31.0f, 7.0f, -12.0f);
		final Matrix4f first = fit(cascade, eye, 0.0f, 0.0f, RESOLUTION);
		final float firstDepth = depthScale(first);
		// A thousand views, a fine sweep of yaw and pitch: never another size nor another depth range.
		for (int step = 0; step < 1000; step++) {
			final float yaw = step * 0.0123f;
			final float pitch = (float) Math.sin(step * 0.071) * 1.2f;
			final Matrix4f lightSpace = fit(cascade, eye, yaw, pitch, RESOLUTION);
			assertEquals(expected, width(lightSpace), 1.0e-3f, "size at step " + step);
			assertEquals(firstDepth, depthScale(lightSpace), 1.0e-6f, "depth range at step " + step);
			// The centre of the slice stays at the same depth of the map (the snap moves across the light only).
			final Camera camera = new Camera();
			camera.setPosition(eye);
			camera.setYaw(yaw);
			camera.setPitch(pitch);
			final Vector3f centre = eye.add(camera.getForward().multiply((4.3f + 37.9f) * 0.5f));
			assertEquals(first.multiply(eye.add(fitForward(eye).multiply((4.3f + 37.9f) * 0.5f))).z(),
					lightSpace.multiply(centre).z(), 1.0e-3f, "depth of the centre at step " + step);
		}
	}

	private static Vector3f fitForward(final Vector3f eye) {
		final Camera camera = new Camera();
		camera.setPosition(eye);
		return camera.getForward();
	}

	@Test
	void theSliceStaysInsideTheMap() {
		final ShadowCascade cascade = new ShadowCascade();
		cascade.setSplitRange(5.0f, 60.0f);
		for (int step = 0; step < 16; step++) {
			final Matrix4f lightSpace = fit(cascade, new Vector3f(step * 3.3f, 1.7f, -step * 1.1f), step * 0.41f,
					(step % 5 - 2) * 0.3f, RESOLUTION);
			for (final Vector3f corner : cascade.getDebugFrustumCorners()) {
				final Vector3f ndc = lightSpace.multiply(corner);
				assertTrue(Math.abs(ndc.x()) <= 1.0f && Math.abs(ndc.y()) <= 1.0f && Math.abs(ndc.z()) <= 1.0f,
						"corner out of the map at step " + step + ": " + ndc);
			}
		}
	}
}
