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
	 * Cascade split ratios (length = cascadeCount - 1).
	 * Each value is a ratio of shadowDistance defining where one cascade ends and the next begins.
	 * <p>
	 * For 3 cascades with splits {0.07, 0.27}:
	 * <ul>
	 *   <li>Cascade 0: 0 to 0.07 * shadowDistance</li>
	 *   <li>Cascade 1: 0.07 to 0.27 * shadowDistance</li>
	 *   <li>Cascade 2: 0.27 to 1.0 * shadowDistance</li>
	 * </ul>
	 */
	private float[] cascadeSplits = {0.07f, 0.27f};
	/** Maximum number of simultaneous shadow-casting celestial bodies. */
	private int maxShadowCasters = 2;
	/** PCF kernel size: 1 = hard shadows, 3 = medium, 5 = soft. */
	private int pcfKernelSize = 3;

	public ShadowConfig() {
	}

	/**
	 * @return The absolute split distances (in world units) for each cascade boundary.
	 *         Array length = cascadeCount - 1.
	 */
	public float[] getCascadeSplitDistances() {
		final int splitCount = Math.max(0, this.cascadeCount - 1);
		final float[] distances = new float[splitCount];
		for (int i = 0; i < splitCount; i++) {
			if (i < this.cascadeSplits.length) {
				distances[i] = this.cascadeSplits[i] * this.shadowDistance;
			} else {
				// Linearly distribute remaining splits
				distances[i] = this.shadowDistance * (float) (i + 1) / this.cascadeCount;
			}
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

	public float[] getCascadeSplits() {
		return this.cascadeSplits;
	}

	public void setCascadeSplits(final float[] cascadeSplits) {
		this.cascadeSplits = cascadeSplits;
	}

	public int getMaxShadowCasters() {
		return this.maxShadowCasters;
	}

	public void setMaxShadowCasters(final int maxShadowCasters) {
		this.maxShadowCasters = maxShadowCasters;
	}

	public int getPcfKernelSize() {
		return this.pcfKernelSize;
	}

	public void setPcfKernelSize(final int pcfKernelSize) {
		this.pcfKernelSize = pcfKernelSize;
	}
}
