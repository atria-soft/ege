package org.atriasoft.ege.skybox;

import org.atriasoft.etk.Uri;

/**
 * Configuration for a cubemap skybox.
 * <p>
 * Holds the 6 face texture URIs and optional rendering parameters.
 * Face order follows the OpenGL cubemap convention:
 * <ul>
 *   <li>{@code right}  = +X ({@code GL_TEXTURE_CUBE_MAP_POSITIVE_X})</li>
 *   <li>{@code left}   = -X ({@code GL_TEXTURE_CUBE_MAP_NEGATIVE_X})</li>
 *   <li>{@code top}    = +Y ({@code GL_TEXTURE_CUBE_MAP_POSITIVE_Y})</li>
 *   <li>{@code bottom} = -Y ({@code GL_TEXTURE_CUBE_MAP_NEGATIVE_Y})</li>
 *   <li>{@code front}  = +Z ({@code GL_TEXTURE_CUBE_MAP_POSITIVE_Z})</li>
 *   <li>{@code back}   = -Z ({@code GL_TEXTURE_CUBE_MAP_NEGATIVE_Z})</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * SkyboxConfig config = new SkyboxConfig(
 *     new Uri("RES", "skybox/right.png"),
 *     new Uri("RES", "skybox/left.png"),
 *     new Uri("RES", "skybox/top.png"),
 *     new Uri("RES", "skybox/bottom.png"),
 *     new Uri("RES", "skybox/front.png"),
 *     new Uri("RES", "skybox/back.png"));
 * env.setSkybox(config);
 * }</pre>
 */
public class SkyboxConfig {
	private final Uri right;
	private final Uri left;
	private final Uri top;
	private final Uri bottom;
	private final Uri front;
	private final Uri back;
	private float rotationSpeed;

	/**
	 * Create a skybox configuration with 6 face textures.
	 * @param right  +X face URI
	 * @param left   -X face URI
	 * @param top    +Y face URI
	 * @param bottom -Y face URI
	 * @param front  +Z face URI
	 * @param back   -Z face URI
	 */
	public SkyboxConfig(
			final Uri right,
			final Uri left,
			final Uri top,
			final Uri bottom,
			final Uri front,
			final Uri back) {
		this.right = right;
		this.left = left;
		this.top = top;
		this.bottom = bottom;
		this.front = front;
		this.back = back;
		this.rotationSpeed = 0.0f;
	}

	public Uri getRight() {
		return this.right;
	}

	public Uri getLeft() {
		return this.left;
	}

	public Uri getTop() {
		return this.top;
	}

	public Uri getBottom() {
		return this.bottom;
	}

	public Uri getFront() {
		return this.front;
	}

	public Uri getBack() {
		return this.back;
	}

	/**
	 * Get the rotation speed in radians per second around the Y axis.
	 * @return Rotation speed (0 = no rotation)
	 */
	public float getRotationSpeed() {
		return this.rotationSpeed;
	}

	/**
	 * Set the rotation speed in radians per second around the Y axis.
	 * Read by the skybox engine at each update: it can be changed while the
	 * sky is displayed.
	 * @param rotationSpeed Rotation speed (0 = no rotation, positive =
	 *        counter-clockwise seen from above)
	 */
	public void setRotationSpeed(final float rotationSpeed) {
		this.rotationSpeed = rotationSpeed;
	}
}
