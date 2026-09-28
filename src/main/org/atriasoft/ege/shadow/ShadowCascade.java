package org.atriasoft.ege.shadow;

import org.atriasoft.ege.camera.Camera;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single cascade in the Cascaded Shadow Map (CSM) system.
 * <p>
 * Each cascade covers a slice of the camera frustum defined by
 * {@code [splitNear, splitFar]} distances. The cascade computes a tight
 * orthographic light-space projection that encompasses its frustum slice.
 * <p>
 * GPU resources (FBO, depth texture) are managed by the embedded
 * {@link ShadowMapResources}.
 */
public class ShadowCascade {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShadowCascade.class);
	private static int logCounter = 0;
	
	private final ShadowMapResources resources = new ShadowMapResources();
	private Matrix4f lightSpaceMatrix = Matrix4f.IDENTITY;
	private float splitNear;
	private float splitFar;
	
	// Debug data — retained after computeLightSpaceMatrix for visualization
	private Vector3f[] debugFrustumCorners;
	private Vector3f debugLightPos;
	private Matrix4f debugLightView;
	private float debugMinX;
	private float debugMaxX;
	private float debugMinY;
	private float debugMaxY;
	private float debugMinZ;
	private float debugMaxZ;
	
	public ShadowCascade() {}
	
	/**
	 * Initialize GPU resources for this cascade.
	 * Must be called once before rendering (requires OpenGL context).
	 */
	public void init() {
		this.resources.init();
	}
	
	/**
	 * Configure the depth range for this cascade.
	 * @param splitNear Near distance from the camera
	 * @param splitFar  Far distance from the camera
	 */
	public void setSplitRange(final float splitNear, final float splitFar) {
		this.splitNear = splitNear;
		this.splitFar = splitFar;
	}
	
	/**
	 * Compute the light-space matrix for this cascade by fitting an orthographic
	 * projection around the camera's frustum slice [splitNear, splitFar].
	 *
	 * @param lightDir   Normalized direction vector FROM the world center TOWARDS the sun
	 *                   (i.e. the sun's position on the unit sphere). The light shines
	 *                   in the opposite direction (from sun towards ground).
	 * @param camera     The active camera
	 * @param fovY       The camera's vertical field of view in radians
	 * @param aspectRatio The camera's aspect ratio (width / height)
	 * @return The computed light-space matrix (lightProjection * lightView)
	 */
	public Matrix4f computeLightSpaceMatrix(
			final Vector3f lightDir,
			final Camera camera,
			final float fovY,
			final float aspectRatio) {
		return computeLightSpaceMatrix(lightDir, Float.NaN, 0.0f, camera, fovY, aspectRatio);
	}

	/**
	 * Compute the light-space matrix with an orbital angle hint for stable orientation.
	 * <p>
	 * When the light direction is near-vertical (sun at zenith), a standard lookAt
	 * matrix flips because the up vector becomes parallel to the view direction.
	 * The orbital tangent (derived from angle and inclination) provides a continuously
	 * rotating right vector that never flips.
	 *
	 * @param lightDir            Normalized direction towards the light source
	 * @param orbitalAngle        The celestial body's current orbital angle (radians),
	 *                            or {@code Float.NaN} to use the worldUp fallback
	 * @param orbitalInclination  The celestial body's orbital inclination (radians)
	 * @param camera              The active camera
	 * @param fovY                The camera's vertical field of view in radians
	 * @param aspectRatio         The camera's aspect ratio (width / height)
	 * @return The computed light-space matrix (lightProjection * lightView)
	 */
	public Matrix4f computeLightSpaceMatrix(
			final Vector3f lightDir,
			final float orbitalAngle,
			final float orbitalInclination,
			final Camera camera,
			final float fovY,
			final float aspectRatio) {
		// 1. Compute the 8 corners of the frustum slice [splitNear, splitFar]
		final Vector3f[] frustumCorners = computeFrustumCorners(camera, fovY, aspectRatio);
		
		// 2. Compute center of the frustum slice
		float centerX = 0.0f;
		float centerY = 0.0f;
		float centerZ = 0.0f;
		for (final Vector3f corner : frustumCorners) {
			centerX += corner.x();
			centerY += corner.y();
			centerZ += corner.z();
		}
		centerX /= frustumCorners.length;
		centerY /= frustumCorners.length;
		centerZ /= frustumCorners.length;
		final Vector3f center = new Vector3f(centerX, centerY, centerZ);
		
		// 3. Build light view matrix
		// lightDir points towards the sun. Place the light camera along that direction.
		final float radius = computeFrustumRadius(frustumCorners, center);
		final Vector3f lightPos = new Vector3f(center.x() + lightDir.x() * radius * 2.0f,
				center.y() + lightDir.y() * radius * 2.0f, center.z() + lightDir.z() * radius * 2.0f);
		
		// Build a proper view matrix. When an orbital angle is provided, use the
		// orbital tangent as right vector for stable orientation at the zenith.
		final Matrix4f lightView = buildLookAtMatrix(lightPos, center, orbitalAngle, orbitalInclination);

		// Store debug data for visualization
		this.debugFrustumCorners = frustumCorners;
		this.debugLightPos = lightPos;
		this.debugLightView = lightView;
		
		// 4. Transform frustum corners into light space to find tight AABB
		float minX = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		float minZ = Float.MAX_VALUE;
		float maxZ = -Float.MAX_VALUE;
		for (final Vector3f corner : frustumCorners) {
			final Vector3f transformed = lightView.multiply(corner);
			minX = Math.min(minX, transformed.x());
			maxX = Math.max(maxX, transformed.x());
			minY = Math.min(minY, transformed.y());
			maxY = Math.max(maxY, transformed.y());
			minZ = Math.min(minZ, transformed.z());
			maxZ = Math.max(maxZ, transformed.z());
		}
		
		// 5. Expand Z range to capture shadow casters outside the camera frustum.
		// In light space, Z decreases towards the light direction:
		//   maxZ = nearest to light (behind frustum, towards light)
		//   minZ = farthest from light (in front of frustum, away from light)
		// Extend maxZ generously to include objects behind the camera that cast
		// shadows into the visible frustum (e.g. trees behind casting long shadows).
		// Extend minZ moderately to catch objects just beyond the far plane.
		final float zRange = maxZ - minZ;
		minZ -= Math.max(zRange * 0.5f, 10.0f);
		maxZ += Math.max(zRange * 2.0f, 50.0f);
		
		// Store AABB bounds for debug visualization
		this.debugMinX = minX;
		this.debugMaxX = maxX;
		this.debugMinY = minY;
		this.debugMaxY = maxY;
		this.debugMinZ = minZ;
		this.debugMaxZ = maxZ;
		
		// 6. Build orthographic projection
		// In the view matrix, the camera looks in -Z direction.
		// Objects in front have negative Z values in view space.
		// Ortho maps [nearVal, farVal] to [-1, 1] in NDC.
		// We need nearVal = -maxZ and farVal = -minZ to map the visible range correctly.
		final Matrix4f lightProjection = Matrix4f.createMatrixOrtho(minX, maxX, minY, maxY, -maxZ, -minZ);
		
		this.lightSpaceMatrix = lightProjection.multiply(lightView);
		
		// Dump of the fit for debugging, every 120 fits, at TRACE only: at a higher level it floods the logs
		// of an application whose root logger is at DEBUG (16 lines every 120 fits of each cascade).
		if (LOGGER.isTraceEnabled() && logCounter % 120 == 0) {
			LOGGER.trace("=== ShadowCascade debug (frame {}) ===", logCounter);
			LOGGER.trace("  lightDir=({}, {}, {})", lightDir.x(), lightDir.y(), lightDir.z());
			LOGGER.trace("  camPos=({}, {}, {})", camera.getPosition().x(), camera.getPosition().y(),
					camera.getPosition().z());
			LOGGER.trace("  camForward=({}, {}, {})", camera.getForward().x(), camera.getForward().y(),
					camera.getForward().z());
			LOGGER.trace("  camUp=({}, {}, {})", camera.getUp().x(), camera.getUp().y(), camera.getUp().z());
			LOGGER.trace("  splitNear={}, splitFar={}", this.splitNear, this.splitFar);
			LOGGER.trace("  frustumCenter=({}, {}, {})", center.x(), center.y(), center.z());
			LOGGER.trace("  radius={}, lightPos=({}, {}, {})", radius, lightPos.x(), lightPos.y(), lightPos.z());
			LOGGER.trace("  AABB in light space: X=[{}, {}], Y=[{}, {}], Z=[{}, {}]", minX, maxX, minY, maxY, minZ,
					maxZ);
			LOGGER.trace("  Ortho near={}, far={}, depth range={}", -maxZ, -minZ, (-minZ) - (-maxZ));
			for (int ci = 0; ci < frustumCorners.length; ci++) {
				final Vector3f tc = lightView.multiply(frustumCorners[ci]);
				LOGGER.trace("  corner[{}] world=({},{},{}) light=({},{},{})", ci, frustumCorners[ci].x(),
						frustumCorners[ci].y(), frustumCorners[ci].z(), tc.x(), tc.y(), tc.z());
			}
		}
		logCounter++;
		
		return this.lightSpaceMatrix;
	}
	
	/**
	 * Build a proper lookAt view matrix (standard OpenGL convention).
	 * <p>
	 * When orbital parameters are available (non-NaN), the right vector is derived
	 * from the orbital tangent: {@code (-sin(θ), cos(θ)*cosI, cos(θ)*sinI)}.
	 * This tangent is always perpendicular to the light direction and rotates
	 * continuously — it never flips, even when the light passes through the zenith.
	 * <p>
	 * Without orbital parameters, falls back to {@code worldUp × forward} with
	 * a {@code (0,0,1)} fallback when forward is vertical.
	 *
	 * @param eye                 Light position
	 * @param target              Point to look at
	 * @param orbitalAngle        The orbital angle (radians), or NaN for worldUp fallback
	 * @param orbitalInclination  The orbital inclination (radians)
	 * @return The view matrix
	 */
	private static Matrix4f buildLookAtMatrix(
			final Vector3f eye,
			final Vector3f target,
			final float orbitalAngle,
			final float orbitalInclination) {
		// Forward = normalize(eye - target) — OpenGL camera looks in -Z
		float fx = eye.x() - target.x();
		float fy = eye.y() - target.y();
		float fz = eye.z() - target.z();
		final float fLen = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
		if (fLen > 0.0001f) {
			fx /= fLen;
			fy /= fLen;
			fz /= fLen;
		}

		float rx;
		float ry;
		float rz;
		if (!Float.isNaN(orbitalAngle)) {
			// Orbital tangent = derivative of the orbit position w.r.t. angle θ:
			//   position = (cos(θ), sin(θ)*cosI, sin(θ)*sinI)
			//   tangent  = (-sin(θ), cos(θ)*cosI, cos(θ)*sinI)
			// This is always perpendicular to the radial direction and rotates
			// continuously — no flip at the zenith.
			final float cosA = (float) Math.cos(orbitalAngle);
			final float sinA = (float) Math.sin(orbitalAngle);
			final float cosI = (float) Math.cos(orbitalInclination);
			final float sinI = (float) Math.sin(orbitalInclination);
			rx = -sinA;
			ry = cosA * cosI;
			rz = cosA * sinI;
			// Normalize (should already be unit length, but ensure precision)
			final float tLen = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
			if (tLen > 0.0001f) {
				rx /= tLen;
				ry /= tLen;
				rz /= tLen;
			}
		} else {
			// Fallback: worldUp × forward with (0,1,0)
			rx = fz;
			ry = 0;
			rz = -fx;
			final float rLen = (float) Math.sqrt(rx * rx + rz * rz);
			if (rLen > 0.0001f) {
				rx /= rLen;
				rz /= rLen;
			} else {
				// Forward is vertical — arbitrary fallback
				rx = 0.0f;
				ry = 0.0f;
				rz = 1.0f;
			}
		}

		// Recompute up = forward x right (ensures orthonormality)
		final float ux = fy * rz - fz * ry;
		final float uy = fz * rx - fx * rz;
		final float uz = fx * ry - fy * rx;

		// View matrix = rotation * translation
		final float tx = -(rx * eye.x() + ry * eye.y() + rz * eye.z());
		final float ty = -(ux * eye.x() + uy * eye.y() + uz * eye.z());
		final float tz = -(fx * eye.x() + fy * eye.y() + fz * eye.z());

		return new Matrix4f(rx, ry, rz, tx, ux, uy, uz, ty, fx, fy, fz, tz, 0.0f, 0.0f, 0.0f, 1.0f);
	}
	
	/**
	 * Compute the 8 corners of the frustum slice [splitNear, splitFar] in world space.
	 */
	private Vector3f[] computeFrustumCorners(final Camera camera, final float fovY, final float aspectRatio) {
		final float tanHalfFov = (float) Math.tan(fovY * 0.5f);
		final float nearH = tanHalfFov * this.splitNear;
		final float nearW = nearH * aspectRatio;
		final float farH = tanHalfFov * this.splitFar;
		final float farW = farH * aspectRatio;
		
		// Camera basis vectors
		final Vector3f camPos = camera.getPosition();
		final Vector3f forward = camera.getForward();
		final Vector3f right = camera.getRight();
		final Vector3f camUp = camera.getUp();
		
		// Near and far plane centers
		final Vector3f nearCenter = new Vector3f(camPos.x() + forward.x() * this.splitNear,
				camPos.y() + forward.y() * this.splitNear, camPos.z() + forward.z() * this.splitNear);
		final Vector3f farCenter = new Vector3f(camPos.x() + forward.x() * this.splitFar,
				camPos.y() + forward.y() * this.splitFar, camPos.z() + forward.z() * this.splitFar);
		
		// 8 frustum corners: near plane (4) + far plane (4)
		return new Vector3f[] {
				// Near plane
				addScaled(nearCenter, right, -nearW, camUp, nearH), addScaled(nearCenter, right, nearW, camUp, nearH),
				addScaled(nearCenter, right, nearW, camUp, -nearH), addScaled(nearCenter, right, -nearW, camUp, -nearH),
				// Far plane
				addScaled(farCenter, right, -farW, camUp, farH), addScaled(farCenter, right, farW, camUp, farH),
				addScaled(farCenter, right, farW, camUp, -farH), addScaled(farCenter, right, -farW, camUp, -farH), };
	}
	
	/**
	 * Helper: center + a*scaleA + b*scaleB
	 */
	private static Vector3f addScaled(
			final Vector3f center,
			final Vector3f a,
			final float scaleA,
			final Vector3f b,
			final float scaleB) {
		return new Vector3f(center.x() + a.x() * scaleA + b.x() * scaleB, center.y() + a.y() * scaleA + b.y() * scaleB,
				center.z() + a.z() * scaleA + b.z() * scaleB);
	}
	
	/**
	 * Compute the bounding sphere radius of the frustum corners around a center point.
	 */
	private static float computeFrustumRadius(final Vector3f[] corners, final Vector3f center) {
		float maxDist = 0.0f;
		for (final Vector3f corner : corners) {
			final float dx = corner.x() - center.x();
			final float dy = corner.y() - center.y();
			final float dz = corner.z() - center.z();
			final float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
			maxDist = Math.max(maxDist, dist);
		}
		return maxDist;
	}
	
	public ShadowMapResources getResources() {
		return this.resources;
	}
	
	public Matrix4f getLightSpaceMatrix() {
		return this.lightSpaceMatrix;
	}
	
	public float getSplitNear() {
		return this.splitNear;
	}
	
	public float getSplitFar() {
		return this.splitFar;
	}
	
	// --- Debug accessors for wireframe visualization ---
	
	/** @return The 8 frustum corners in world space, or null if not yet computed */
	public Vector3f[] getDebugFrustumCorners() {
		return this.debugFrustumCorners;
	}
	
	/** @return The light position used for the view matrix */
	public Vector3f getDebugLightPos() {
		return this.debugLightPos;
	}
	
	/** @return The light view matrix */
	public Matrix4f getDebugLightView() {
		return this.debugLightView;
	}
	
	/**
	 * Get the 8 corners of the light-space AABB in world space.
	 * Computed by transforming the AABB corners through the inverse light view matrix.
	 * @return 8 corners of the ortho projection volume in world space, or null
	 */
	public Vector3f[] getDebugLightAABBCorners() {
		if (this.debugLightView == null) {
			return null;
		}
		// Invert the light view matrix to go from light space back to world space
		final Matrix4f invView = this.debugLightView.invert();
		if (invView == null) {
			return null;
		}
		// 8 corners of the AABB box in light space
		final float x0 = this.debugMinX;
		final float x1 = this.debugMaxX;
		final float y0 = this.debugMinY;
		final float y1 = this.debugMaxY;
		final float z0 = this.debugMinZ;
		final float z1 = this.debugMaxZ;
		return new Vector3f[] { invView.multiply(new Vector3f(x0, y0, z0)), invView.multiply(new Vector3f(x1, y0, z0)),
				invView.multiply(new Vector3f(x1, y1, z0)), invView.multiply(new Vector3f(x0, y1, z0)),
				invView.multiply(new Vector3f(x0, y0, z1)), invView.multiply(new Vector3f(x1, y0, z1)),
				invView.multiply(new Vector3f(x1, y1, z1)), invView.multiply(new Vector3f(x0, y1, z1)), };
	}
	
	/**
	 * Release all GPU resources.
	 */
	public void destroy() {
		this.resources.destroy();
	}
}
