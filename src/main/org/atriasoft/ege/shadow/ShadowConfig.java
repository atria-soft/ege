package org.atriasoft.ege.shadow;

/**
 * Configuration for the shadow mapping system.
 * <p>
 * Controls cascade count, resolution, split distances, and PCF quality.
 * Defaults provide a reasonable balance for medium-sized outdoor scenes.
 */
public class ShadowConfig {
	/** Number of cascades (1 to 4). More cascades = better quality at distance, higher cost. */
	private int cascadeCount = 3;
	/** Shadow map resolution per cascade (width = height). Power of 2 recommended. */
	private int shadowMapResolution = 1024;
	/** Maximum shadow rendering distance from camera. */
	private float shadowDistance = 300.0f;
	/**
	 * Blend factor between logarithmic and linear cascade splits (0.0 = linear, 1.0 = logarithmic).
	 * Logarithmic splits give more resolution to nearby objects, linear distributes evenly.
	 * A value of 0.5 provides a practical compromise.
	 */
	private float cascadeSplitLambda = 0.5f;
	/** Maximum number of simultaneous shadow-casting celestial bodies. */
	private int maxShadowCasters = 2;
	/** PCF kernel size: 1 = hard shadows, 3 = medium, 5 = soft. */
	private int pcfKernelSize = 3;
	/**
	 * Whether the cascades are stabilised: each one a square fitted to the
	 * bounding sphere of its slice (the same size whatever the camera looks
	 * at), its origin snapped to the texels of its shadow map. The edges of
	 * the shadows then stay still while the camera moves or turns, instead of
	 * shimmering, at the cost of some resolution. Off by default: the tight
	 * fit of before.
	 */
	private boolean stabilized = false;

	public ShadowConfig() {
	}

	/**
	 * Compute cascade split distances using a practical logarithmic scheme.
	 * <p>
	 * Blends between linear and logarithmic distribution controlled by
	 * {@code cascadeSplitLambda}:
	 * <ul>
	 *   <li>lambda = 0.0 → pure linear (equal distance per cascade)</li>
	 *   <li>lambda = 1.0 → pure logarithmic (more resolution close to camera)</li>
	 *   <li>lambda = 0.5 → practical compromise (default)</li>
	 * </ul>
	 *
	 * @return The absolute split distances (in world units) for each cascade boundary.
	 *         Array length = cascadeCount - 1.
	 */
	public float[] getCascadeSplitDistances() {
		final int splitCount = Math.max(0, this.cascadeCount - 1);
		final float[] distances = new float[splitCount];
		final float nearClip = 0.1f;
		final float ratio = this.shadowDistance / nearClip;
		for (int i = 0; i < splitCount; i++) {
			final float p = (float) (i + 1) / this.cascadeCount;
			final float logSplit = nearClip * (float) Math.pow(ratio, p);
			final float linearSplit = nearClip + (this.shadowDistance - nearClip) * p;
			distances[i] = this.cascadeSplitLambda * logSplit + (1.0f - this.cascadeSplitLambda) * linearSplit;
		}
		return distances;
	}

	/**
	 * @param cascadeIndex The cascade index (0-based)
	 * @return The near distance for this cascade
	 */
	public float getCascadeNear(final int cascadeIndex) {
		if (cascadeIndex == 0) {
			return 0.1f;
		}
		final float[] distances = getCascadeSplitDistances();
		return distances[cascadeIndex - 1];
	}

	/**
	 * @param cascadeIndex The cascade index (0-based)
	 * @return The far distance for this cascade
	 */
	public float getCascadeFar(final int cascadeIndex) {
		final float[] distances = getCascadeSplitDistances();
		if (cascadeIndex < distances.length) {
			return distances[cascadeIndex];
		}
		return this.shadowDistance;
	}

	public int getCascadeCount() {
		return this.cascadeCount;
	}

	public void setCascadeCount(final int cascadeCount) {
		this.cascadeCount = Math.max(1, Math.min(4, cascadeCount));
	}

	public int getShadowMapResolution() {
		return this.shadowMapResolution;
	}

	public void setShadowMapResolution(final int shadowMapResolution) {
		this.shadowMapResolution = shadowMapResolution;
	}

	public float getShadowDistance() {
		return this.shadowDistance;
	}

	public void setShadowDistance(final float shadowDistance) {
		this.shadowDistance = shadowDistance;
	}

	public float getCascadeSplitLambda() {
		return this.cascadeSplitLambda;
	}

	public void setCascadeSplitLambda(final float cascadeSplitLambda) {
		this.cascadeSplitLambda = Math.max(0.0f, Math.min(1.0f, cascadeSplitLambda));
	}

	public int getMaxShadowCasters() {
		return this.maxShadowCasters;
	}

	public void setMaxShadowCasters(final int maxShadowCasters) {
		this.maxShadowCasters = maxShadowCasters;
	}

	/** @return whether the cascades are stabilised (see {@link #setStabilized}) */
	public boolean isStabilized() {
		return this.stabilized;
	}

	/**
	 * Stabilise the cascades: each one fitted to the bounding sphere of its
	 * slice and snapped to the texels of its shadow map, so that the shadow
	 * edges do not shimmer while the camera moves ({@code false}, the default:
	 * the tight fit to the slice).
	 */
	public void setStabilized(final boolean stabilized) {
		this.stabilized = stabilized;
	}

	public int getPcfKernelSize() {
		return this.pcfKernelSize;
	}

	public void setPcfKernelSize(final int pcfKernelSize) {
		this.pcfKernelSize = pcfKernelSize;
	}
}
