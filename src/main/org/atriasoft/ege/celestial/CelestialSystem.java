package org.atriasoft.ege.celestial;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages all celestial bodies (suns and moons) and computes derived
 * lighting state: ambient color, sky color, and active shadow casters.
 * <p>
 * This is a service object, not an EGE Engine. It is owned by
 * {@code EngineShadow} and queried during the render pipeline.
 */
public class CelestialSystem {
	private static final Logger LOGGER = LoggerFactory.getLogger(CelestialSystem.class);

	private final List<CelestialBody> bodies = new ArrayList<>();
	private int maxShadowCasters = 2;

	// Precomputed state (updated each frame)
	private Color ambientColor = new Color(0.2f, 0.2f, 0.2f, 1.0f);
	private Color skyColor = new Color(0.5f, 0.7f, 1.0f, 1.0f);
	private final List<CelestialBody> activeShadowCasters = new ArrayList<>();

	// Night/day color references
	private static final Color NIGHT_AMBIENT = new Color(0.03f, 0.03f, 0.08f, 1.0f);
	private static final Color NIGHT_SKY = new Color(0.01f, 0.01f, 0.05f, 1.0f);
	private static final Color DAWN_SKY = new Color(0.8f, 0.4f, 0.2f, 1.0f);
	private static final Color DAY_SKY = new Color(0.5f, 0.7f, 1.0f, 1.0f);

	public CelestialSystem() {
	}

	/**
	 * Add a celestial body to the system.
	 * @param body The body to add
	 */
	public void addBody(final CelestialBody body) {
		this.bodies.add(body);
		LOGGER.debug("Added celestial body: {}", body);
	}

	/**
	 * Remove a celestial body from the system.
	 * @param body The body to remove
	 */
	public void removeBody(final CelestialBody body) {
		this.bodies.remove(body);
		LOGGER.debug("Removed celestial body: {}", body);
	}

	/**
	 * Update all orbital positions and recompute derived lighting state.
	 * @param deltaMili Time elapsed in milliseconds
	 */
	public void update(final long deltaMili) {
		// Update orbital angles
		for (final CelestialBody body : this.bodies) {
			body.update(deltaMili);
		}
		// Recompute derived state
		computeAmbientColor();
		computeSkyColor();
		computeActiveShadowCasters();
	}

	private void computeAmbientColor() {
		float totalR = NIGHT_AMBIENT.r();
		float totalG = NIGHT_AMBIENT.g();
		float totalB = NIGHT_AMBIENT.b();
		for (final CelestialBody body : this.bodies) {
			final float height = body.getDirection().z();
			if (height <= 0.0f) {
				continue;
			}
			final float contribution = body.getIntensity() * height * 0.3f;
			totalR += body.getLightColor().r() * contribution;
			totalG += body.getLightColor().g() * contribution;
			totalB += body.getLightColor().b() * contribution;
		}
		this.ambientColor = new Color(
				Math.min(totalR, 1.0f),
				Math.min(totalG, 1.0f),
				Math.min(totalB, 1.0f),
				1.0f);
	}

	private void computeSkyColor() {
		// Find the highest sun above the horizon
		float maxSunHeight = -1.0f;
		for (final CelestialBody body : this.bodies) {
			if (body.getType() == CelestialBodyType.SUN) {
				final float height = body.getDirection().z();
				if (height > maxSunHeight) {
					maxSunHeight = height;
				}
			}
		}
		if (maxSunHeight < -0.1f) {
			// Deep night
			this.skyColor = NIGHT_SKY;
		} else if (maxSunHeight < 0.05f) {
			// Dawn/dusk transition
			final float t = (maxSunHeight + 0.1f) / 0.15f; // 0..1
			this.skyColor = lerpColor(NIGHT_SKY, DAWN_SKY, t);
		} else if (maxSunHeight < 0.3f) {
			// Sunrise/sunset transition
			final float t = (maxSunHeight - 0.05f) / 0.25f; // 0..1
			this.skyColor = lerpColor(DAWN_SKY, DAY_SKY, t);
		} else {
			// Full day
			this.skyColor = DAY_SKY;
		}
	}

	private void computeActiveShadowCasters() {
		this.activeShadowCasters.clear();
		final List<CelestialBody> candidates = new ArrayList<>();
		for (final CelestialBody body : this.bodies) {
			if (body.isCastsShadow() && body.isAboveHorizon()) {
				candidates.add(body);
			}
		}
		// Sort by effective brightness descending, take top N
		candidates.sort(Comparator.comparingDouble(CelestialBody::getEffectiveBrightness).reversed());
		final int count = Math.min(candidates.size(), this.maxShadowCasters);
		for (int i = 0; i < count; i++) {
			this.activeShadowCasters.add(candidates.get(i));
		}
	}

	private static Color lerpColor(final Color a, final Color b, final float t) {
		final float clamped = Math.max(0.0f, Math.min(1.0f, t));
		return new Color(
				a.r() + (b.r() - a.r()) * clamped,
				a.g() + (b.g() - a.g()) * clamped,
				a.b() + (b.b() - a.b()) * clamped,
				1.0f);
	}

	/**
	 * @return The computed ambient color for the current frame
	 */
	public Color getAmbientColor() {
		return this.ambientColor;
	}

	/**
	 * @return The computed sky/fog color for the current frame
	 */
	public Color getSkyColor() {
		return this.skyColor;
	}

	/**
	 * @return Active shadow-casting bodies sorted by brightness (max {@code maxShadowCasters})
	 */
	public List<CelestialBody> getActiveShadowCasters() {
		return this.activeShadowCasters;
	}

	/**
	 * @return All registered celestial bodies
	 */
	public List<CelestialBody> getBodies() {
		return this.bodies;
	}

	/**
	 * @return All bodies currently above the horizon
	 */
	public List<CelestialBody> getActiveBodies() {
		final List<CelestialBody> active = new ArrayList<>();
		for (final CelestialBody body : this.bodies) {
			if (body.isAboveHorizon()) {
				active.add(body);
			}
		}
		return active;
	}

	/**
	 * Get the direction of the primary (brightest) sun for lighting calculations.
	 * @return Direction vector, or UP if no sun is above horizon
	 */
	public Vector3f getPrimarySunDirection() {
		float maxBrightness = 0.0f;
		Vector3f bestDirection = new Vector3f(0.0f, 0.0f, 1.0f);
		for (final CelestialBody body : this.bodies) {
			if (body.getType() == CelestialBodyType.SUN) {
				final float brightness = body.getEffectiveBrightness();
				if (brightness > maxBrightness) {
					maxBrightness = brightness;
					bestDirection = body.getDirection();
				}
			}
		}
		return bestDirection;
	}

	public int getMaxShadowCasters() {
		return this.maxShadowCasters;
	}

	public void setMaxShadowCasters(final int maxShadowCasters) {
		this.maxShadowCasters = maxShadowCasters;
	}
}
