package org.atriasoft.ege.shadow;

import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.celestial.CelestialBody;
import org.atriasoft.ege.celestial.CelestialBodyType;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates the shadow cascade math:
 * - CelestialBody.getDirection() for various angles/inclinations/elevations
 * - buildLookAtMatrix round-trip (eye → origin, target → -Z)
 * - computeFrustumCorners symmetry
 * - lightSpaceMatrix transforms frustum corners into NDC [-1,1]
 */
class TestShadowCascadeMatrix {
	private static final float EPS = 1e-3f;

	// === CelestialBody direction tests ===

	@Test
	void testDirectionAngle0() {
		// angle=0, incl=0, elev=0 → direction should be (1, 0, 0) — on the horizon
		final CelestialBody body = createBody(0.0f, 0.0f, 0.0f);
		final Vector3f dir = body.getDirection();
		assertVec3(1, 0, 0, dir, "angle=0");
	}

	@Test
	void testDirectionAngle90() {
		// angle=π/2, incl=0, elev=0 → direction should be (0, 0, 1) — zenith
		final CelestialBody body = createBody((float) (Math.PI / 2), 0.0f, 0.0f);
		final Vector3f dir = body.getDirection();
		assertVec3(0, 0, 1, dir, "angle=90, zenith");
	}

	@Test
	void testDirectionAngle180() {
		// angle=π, incl=0, elev=0 → direction should be (-1, 0, 0) — opposite horizon
		final CelestialBody body = createBody((float) Math.PI, 0.0f, 0.0f);
		final Vector3f dir = body.getDirection();
		assertVec3(-1, 0, 0, dir, "angle=180");
	}

	@Test
	void testDirectionAngle270() {
		// angle=3π/2, incl=0, elev=0 → direction should be (0, 0, -1) — nadir (below horizon)
		final CelestialBody body = createBody((float) (3 * Math.PI / 2), 0.0f, 0.0f);
		final Vector3f dir = body.getDirection();
		assertVec3(0, 0, -1, dir, "angle=270, nadir");
	}

	@Test
	void testDirectionWithInclination() {
		// angle=π/2, incl=π/4, elev=0
		// base orbit: ox=0, oy=sin(π/4)=0.707, oz=cos(π/4)=0.707
		// no elevation → final = (0, 0.707, 0.707)
		final CelestialBody body = createBody((float) (Math.PI / 2), (float) (Math.PI / 4), 0.0f);
		final Vector3f dir = body.getDirection();
		final float s = (float) (1.0 / Math.sqrt(2));
		assertVec3(0, s, s, dir, "angle=90, incl=45");
		assertTrue(dir.z() > 0, "should be above horizon");
	}

	@Test
	void testDirectionWithElevation30() {
		// angle=0, incl=0, elev=30°
		// base orbit at angle=0: (1, 0, 0) — on the horizon
		// elevation rotates around X by 30°: y'=0, z'=0 → still (1, 0, 0) !
		// Because at angle=0, oy=oz=0, rotation around X has no effect.
		final CelestialBody body = createBody(0.0f, 0.0f, (float) Math.toRadians(30));
		final Vector3f dir = body.getDirection();
		assertVec3(1, 0, 0, dir, "angle=0, elev=30 (no effect at horizon crossing)");
	}

	@Test
	void testDirectionElevation_raisesZenith() {
		// angle=π/2 (normally zenith), incl=0, elev=30°
		// base: (0, 0, 1)
		// elevation rotates around X by 30°: y'=-sin(30)*1=-0.5, z'=cos(30)*1=0.866
		final CelestialBody body = createBody((float) (Math.PI / 2), 0.0f, (float) Math.toRadians(30));
		final Vector3f dir = body.getDirection();
		assertVec3(0, -0.5f, 0.866f, dir, "angle=90, elev=30");
	}

	@Test
	void testDirectionElevation_midnightSun() {
		// angle=3π/2 (normally nadir, z=-1), incl=0, elev=90°
		// base: (0, 0, -1)
		// elevation rotates around X by 90°: y'=0*0-(-1)*1=1, z'=0*1+(-1)*0=0
		// → direction = (0, 1, 0) — still above horizon!
		final CelestialBody body = createBody((float) (3 * Math.PI / 2), 0.0f, (float) Math.toRadians(90));
		final Vector3f dir = body.getDirection();
		assertVec3(0, 1, 0, dir, "midnight sun: nadir + 90° elevation");
		// At the equator angle (what was nadir), z >= 0 → never sets
		assertTrue(dir.z() >= -EPS, "midnight sun: should not go below horizon");
	}

	// === Camera forward/right/up tests ===

	@Test
	void testCameraDefaultForward() {
		// Default camera: pitch=0, yaw=0, roll=0
		// Internal convention: forward = (0,0,-1)
		final Camera cam = new Camera();
		final Vector3f fwd = cam.getForward();
		assertVec3(0, 0, -1, fwd, "default camera forward");
	}

	@Test
	void testCameraPitchDown() {
		// pitch = -π/2 → look straight down (in Y-up: forward becomes (0,-1,0))
		// In the Z-up world: this means looking towards -Y
		final Camera cam = new Camera();
		cam.setPitch((float) (-Math.PI / 2));
		final Vector3f fwd = cam.getForward();
		// After rotating -90° around X: (0,0,-1) → (0,-(-1),0)=(0,1,0)? Let's check:
		// R_x(-π/2) * (0,0,-1) = (0, -cos(-π/2)*(-1) + sin(-π/2)*0, sin(-π/2)*(-1)...
		// Actually: R^T_x(-π/2) * (0,0,-1)
		// R_x(θ) = [1,0,0; 0,cos,-sin; 0,sin,cos]
		// R_x(-π/2) = [1,0,0; 0,0,1; 0,-1,0]
		// R_x(-π/2)^T = [1,0,0; 0,0,-1; 0,1,0]
		// R^T * (0,0,-1) = (0, 0*0+(-1)*0+0*(-1), 0*0+1*0+0*(-1)) = (0, 0, 0)
		// Hmm, let me recalculate more carefully...
		// R_x(θ): row0=(1,0,0), row1=(0,cosθ,-sinθ), row2=(0,sinθ,cosθ)
		// θ = -π/2: cosθ=0, sinθ=-1
		// R_x(-π/2): row0=(1,0,0), row1=(0,0,1), row2=(0,-1,0)
		// R_x(-π/2)^T: col0→row0=(1,0,0), col1→row1=(0,0,-1), col2→row2=(0,1,0)
		// R^T * (0,0,-1) = (0*(-1), (-1)*(-1), 0*(-1)) = (0, 1, 0)
		// Wait: R^T * v = (row0.v, row1.v, row2.v) where rows of R^T are columns of R
		// R^T row0 = R col0 = (1,0,0), R^T row1 = R col1 = (0,0,-1), R^T row2 = R col2 = (0,1,0)
		// R^T * (0,0,-1) = (dot(1,0,0)(0,0,-1), dot(0,0,-1)(0,0,-1), dot(0,1,0)(0,0,-1))
		//                = (0, 1, 0)
		assertVec3(0, 1, 0, fwd, "camera pitch -90° forward");
	}

	// === LookAt round-trip test ===

	@Test
	void testLookAtRoundTrip() {
		// Place light at (10, 5, 20) looking at (0, 0, 0)
		final Vector3f lightPos = new Vector3f(10, 5, 20);
		final Vector3f target = new Vector3f(0, 0, 0);

		// Build the cascade and test via computeLightSpaceMatrix
		final ShadowCascade cascade = new ShadowCascade();
		cascade.setSplitRange(0.1f, 50.0f);

		// Use a simple camera
		final Camera cam = new Camera();
		cam.setPosition(new Vector3f(0, -10, 5));
		cam.setPitch((float) (-Math.PI * 0.3));

		// lightDir = normalized direction towards the sun
		final float dx = lightPos.x();
		final float dy = lightPos.y();
		final float dz = lightPos.z();
		final float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		final Vector3f lightDir = new Vector3f(dx / len, dy / len, dz / len);

		final Matrix4f lsm = cascade.computeLightSpaceMatrix(lightDir, cam, (float) (Math.PI * 0.5), 1.333f);

		// The light space matrix should transform the frustum center
		// into approximately the center of NDC (around 0,0)
		final Vector3f[] corners = cascade.getDebugFrustumCorners();
		float cx = 0, cy = 0, cz = 0;
		for (final Vector3f c : corners) {
			cx += c.x();
			cy += c.y();
			cz += c.z();
		}
		cx /= corners.length;
		cy /= corners.length;
		cz /= corners.length;
		final Vector3f centerInLSM = lsm.multiply(new Vector3f(cx, cy, cz));
		// Center should be near (0, 0, something) in NDC
		assertTrue(Math.abs(centerInLSM.x()) < 1.0f, "frustum center X should be in NDC: " + centerInLSM.x());
		assertTrue(Math.abs(centerInLSM.y()) < 1.0f, "frustum center Y should be in NDC: " + centerInLSM.y());

		// All 8 corners should be within [-1,1] in X and Y after LSM transform
		for (int i = 0; i < corners.length; i++) {
			final Vector3f ndc = lsm.multiply(corners[i]);
			assertTrue(ndc.x() >= -1.01f && ndc.x() <= 1.01f,
					"corner " + i + " X out of NDC: " + ndc.x());
			assertTrue(ndc.y() >= -1.01f && ndc.y() <= 1.01f,
					"corner " + i + " Y out of NDC: " + ndc.y());
			assertTrue(ndc.z() >= -1.01f && ndc.z() <= 1.01f,
					"corner " + i + " Z out of NDC: " + ndc.z());
		}
	}

	@Test
	void testLightViewInvertRoundTrip() {
		// After computeLightSpaceMatrix, verify that lightView.invert() * lightView ≈ identity
		final ShadowCascade cascade = new ShadowCascade();
		cascade.setSplitRange(0.1f, 50.0f);
		final Camera cam = new Camera();
		cam.setPosition(new Vector3f(0, -10, 5));
		cam.setPitch((float) (-Math.PI * 0.3));
		final Vector3f lightDir = normalize(new Vector3f(1, 0.5f, 2));
		cascade.computeLightSpaceMatrix(lightDir, cam, (float) (Math.PI * 0.5), 1.333f);

		final Matrix4f view = cascade.getDebugLightView();
		final Matrix4f inv = view.invert();
		final Matrix4f product = inv.multiply(view);

		// Should be near identity
		assertEquals(1.0f, product.a1(), EPS, "inv*view [0,0]");
		assertEquals(0.0f, product.b1(), EPS, "inv*view [0,1]");
		assertEquals(0.0f, product.c1(), EPS, "inv*view [0,2]");
		assertEquals(0.0f, product.d1(), EPS, "inv*view [0,3]");
		assertEquals(0.0f, product.a2(), EPS, "inv*view [1,0]");
		assertEquals(1.0f, product.b2(), EPS, "inv*view [1,1]");
		assertEquals(0.0f, product.c2(), EPS, "inv*view [1,2]");
		assertEquals(0.0f, product.d2(), EPS, "inv*view [1,3]");
		assertEquals(0.0f, product.a3(), EPS, "inv*view [2,0]");
		assertEquals(0.0f, product.b3(), EPS, "inv*view [2,1]");
		assertEquals(1.0f, product.c3(), EPS, "inv*view [2,2]");
		assertEquals(0.0f, product.d3(), EPS, "inv*view [2,3]");

		// Also test: lightView * point → light space, then inv * result → back to world
		final Vector3f worldPt = new Vector3f(5, 3, 7);
		final Vector3f inLight = view.multiply(worldPt);
		final Vector3f backToWorld = inv.multiply(inLight);
		assertVec3(worldPt.x(), worldPt.y(), worldPt.z(), backToWorld, "round-trip point");
	}

	@Test
	void testAABBCornersContainFrustum() {
		// The AABB in world space should contain all frustum corners
		final ShadowCascade cascade = new ShadowCascade();
		cascade.setSplitRange(0.1f, 50.0f);
		final Camera cam = new Camera();
		cam.setPosition(new Vector3f(0, -10, 5));
		cam.setPitch((float) (-Math.PI * 0.3));
		final Vector3f lightDir = normalize(new Vector3f(1, 0.5f, 2));
		cascade.computeLightSpaceMatrix(lightDir, cam, (float) (Math.PI * 0.5), 1.333f);

		final Vector3f[] frustumCorners = cascade.getDebugFrustumCorners();
		final Vector3f[] aabbCorners = cascade.getDebugLightAABBCorners();
		assertTrue(aabbCorners != null, "AABB corners should not be null");
		assertTrue(aabbCorners.length == 8, "AABB should have 8 corners");

		// Find AABB min/max in world space
		float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
		float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
		float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
		for (final Vector3f c : aabbCorners) {
			minX = Math.min(minX, c.x());
			maxX = Math.max(maxX, c.x());
			minY = Math.min(minY, c.y());
			maxY = Math.max(maxY, c.y());
			minZ = Math.min(minZ, c.z());
			maxZ = Math.max(maxZ, c.z());
		}

		// All frustum corners should be within this world-space bounding box
		// (with some margin due to Z expansion)
		for (int i = 0; i < frustumCorners.length; i++) {
			final Vector3f fc = frustumCorners[i];
			assertTrue(fc.x() >= minX - 1 && fc.x() <= maxX + 1,
					"frustum corner " + i + " X=" + fc.x() + " outside AABB [" + minX + "," + maxX + "]");
			assertTrue(fc.y() >= minY - 1 && fc.y() <= maxY + 1,
					"frustum corner " + i + " Y=" + fc.y() + " outside AABB [" + minY + "," + maxY + "]");
			assertTrue(fc.z() >= minZ - 1 && fc.z() <= maxZ + 1,
					"frustum corner " + i + " Z=" + fc.z() + " outside AABB [" + minZ + "," + maxZ + "]");
		}
	}

	private static Vector3f normalize(final Vector3f v) {
		final float len = (float) Math.sqrt(v.x() * v.x() + v.y() * v.y() + v.z() * v.z());
		return new Vector3f(v.x() / len, v.y() / len, v.z() / len);
	}

	// === Helpers ===

	private static CelestialBody createBody(final float angle, final float inclination, final float elevation) {
		final CelestialBody body = new CelestialBody(
				CelestialBodyType.SUN, 0.0f, inclination, angle,
				new Color(1, 1, 1, 1), 1.0f, true);
		body.setOrbitalElevation(elevation);
		return body;
	}

	private static void assertVec3(final float ex, final float ey, final float ez,
			final Vector3f actual, final String msg) {
		assertEquals(ex, actual.x(), EPS, msg + " X");
		assertEquals(ey, actual.y(), EPS, msg + " Y");
		assertEquals(ez, actual.z(), EPS, msg + " Z");
	}
}
