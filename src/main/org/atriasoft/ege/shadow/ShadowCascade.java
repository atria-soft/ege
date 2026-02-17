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

	public ShadowCascade() {
	}

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
		final Vector3f lightPos = new Vector3f(
				center.x() + lightDir.x() * radius * 2.0f,
				center.y() + lightDir.y() * radius * 2.0f,
				center.z() + lightDir.z() * radius * 2.0f);

		// Build a proper view matrix (Matrix4f.createMatrixLookAt is a rotation-only
		// matrix used elsewhere — it doesn't produce a correct view matrix with
		// translation dot products).
		final Matrix4f lightView = buildLookAtMatrix(lightPos, center);

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

		// 5. Expand Z range slightly to capture shadow casters near the edges.
		// Push minZ (far from light) a bit further to catch objects behind the frustum.
		// Keep expansion moderate to preserve depth buffer precision.
		final float zRange = maxZ - minZ;
		final float zMargin = Math.max(zRange * 0.5f, 10.0f);
		minZ -= zMargin;
		maxZ += 1.0f; // small margin on near side

		// 6. Build orthographic projection
		// In the view matrix, the camera looks in -Z direction.
		// Objects in front have negative Z values in view space.
		// Ortho maps [nearVal, farVal] to [-1, 1] in NDC.
		// We need nearVal = -maxZ and farVal = -minZ to map the visible range correctly.
		final Matrix4f lightProjection = Matrix4f.createMatrixOrtho(
				minX, maxX, minY, maxY, -maxZ, -minZ);

		this.lightSpaceMatrix = lightProjection.multiply(lightView);

		// Temporary debug logging (throttled to every 120 frames)
		if (logCounter % 120 == 0) {
			LOGGER.info("=== ShadowCascade debug (frame {}) ===", logCounter);
			LOGGER.info("  lightDir=({}, {}, {})", lightDir.x(), lightDir.y(), lightDir.z());
			LOGGER.info("  camPos=({}, {}, {})", camera.getPosition().x(), camera.getPosition().y(), camera.getPosition().z());
			LOGGER.info("  camForward=({}, {}, {})", camera.getForward().x(), camera.getForward().y(), camera.getForward().z());
			LOGGER.info("  camUp=({}, {}, {})", camera.getUp().x(), camera.getUp().y(), camera.getUp().z());
			LOGGER.info("  splitNear={}, splitFar={}", this.splitNear, this.splitFar);
			LOGGER.info("  frustumCenter=({}, {}, {})", center.x(), center.y(), center.z());
			LOGGER.info("  radius={}, lightPos=({}, {}, {})", radius, lightPos.x(), lightPos.y(), lightPos.z());
			LOGGER.info("  AABB in light space: X=[{}, {}], Y=[{}, {}], Z=[{}, {}]",
					minX, maxX, minY, maxY, minZ, maxZ);
			LOGGER.info("  Ortho near={}, far={}, depth range={}", -maxZ, -minZ, (-minZ) - (-maxZ));
			for (int ci = 0; ci < frustumCorners.length; ci++) {
				final Vector3f tc = lightView.multiply(frustumCorners[ci]);
				LOGGER.info("  corner[{}] world=({},{},{}) light=({},{},{})", ci,
						frustumCorners[ci].x(), frustumCorners[ci].y(), frustumCorners[ci].z(),
						tc.x(), tc.y(), tc.z());
			}
		}
		logCounter++;

		return this.lightSpaceMatrix;
	}

	/**
	 * Build a proper lookAt view matrix (standard OpenGL convention).
	 * <p>
	 * Unlike {@code Matrix4f.createMatrixLookAt} (which is used as a rotation-only
	 * matrix elsewhere in the engine), this produces a full view matrix with correct
	 * translation via dot products.
	 *
	 * @param eye    Camera position
	 * @param target Point to look at
	 * @return The view matrix
	 */
	private static Matrix4f buildLookAtMatrix(final Vector3f eye, final Vector3f target) {
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

		// Continuous up-vector blending to avoid discontinuity at the zenith.
		// When forward is horizontal, up = (0,0,1) (Z-up world).
		// When forward approaches vertical (|fz| → 1), up blends towards (0,1,0).
		// t=0 → Z-up, t=1 → Y-up. Smooth transition using abs(fz).
		final float absFz = Math.abs(fz);
		final float t = Math.max(0.0f, Math.min(1.0f, (absFz - 0.7f) / 0.25f));
		// up = lerp((0,0,1), (0,1,0), t) then normalize
		float upX = 0.0f;
		float upY = t;
		float upZ = 1.0f - t;
		final float upLen = (float) Math.sqrt(upY * upY + upZ * upZ);
		if (upLen > 0.0001f) {
			upY /= upLen;
			upZ /= upLen;
		}

		// Right = normalize(up x forward)
		float rx = upY * fz - upZ * fy;
		float ry = upZ * fx - upX * fz;
		float rz = upX * fy - upY * fx;
		final float rLen = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
		if (rLen > 0.0001f) {
			rx /= rLen;
			ry /= rLen;
			rz /= rLen;
		}

		// Recompute up = forward x right (ensures orthonormality)
		final float ux = fy * rz - fz * ry;
		final float uy = fz * rx - fx * rz;
		final float uz = fx * ry - fy * rx;

		// View matrix = rotation * translation
		// Translation uses dot products: -dot(axis, eye)
		final float tx = -(rx * eye.x() + ry * eye.y() + rz * eye.z());
		final float ty = -(ux * eye.x() + uy * eye.y() + uz * eye.z());
		final float tz = -(fx * eye.x() + fy * eye.y() + fz * eye.z());

		return new Matrix4f(
				rx, ry, rz, tx,
				ux, uy, uz, ty,
				fx, fy, fz, tz,
				0.0f, 0.0f, 0.0f, 1.0f);
	}

	/**
	 * Compute the 8 corners of the frustum slice [splitNear, splitFar] in world space.
	 */
	private Vector3f[] computeFrustumCorners(
			final Camera camera,
			final float fovY,
			final float aspectRatio) {
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
		final Vector3f nearCenter = new Vector3f(
				camPos.x() + forward.x() * this.splitNear,
				camPos.y() + forward.y() * this.splitNear,
				camPos.z() + forward.z() * this.splitNear);
		final Vector3f farCenter = new Vector3f(
				camPos.x() + forward.x() * this.splitFar,
				camPos.y() + forward.y() * this.splitFar,
				camPos.z() + forward.z() * this.splitFar);

		// 8 frustum corners: near plane (4) + far plane (4)
		return new Vector3f[] {
				// Near plane
				addScaled(nearCenter, right, -nearW, camUp, nearH),
				addScaled(nearCenter, right, nearW, camUp, nearH),
				addScaled(nearCenter, right, nearW, camUp, -nearH),
				addScaled(nearCenter, right, -nearW, camUp, -nearH),
				// Far plane
				addScaled(farCenter, right, -farW, camUp, farH),
				addScaled(farCenter, right, farW, camUp, farH),
				addScaled(farCenter, right, farW, camUp, -farH),
				addScaled(farCenter, right, -farW, camUp, -farH),
		};
	}

	/**
	 * Helper: center + a*scaleA + b*scaleB
	 */
	private static Vector3f addScaled(
			final Vector3f center,
			final Vector3f a, final float scaleA,
			final Vector3f b, final float scaleB) {
		return new Vector3f(
				center.x() + a.x() * scaleA + b.x() * scaleB,
				center.y() + a.y() * scaleA + b.y() * scaleB,
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

	/**
	 * Release all GPU resources.
	 */
	public void destroy() {
		this.resources.destroy();
	}
}
