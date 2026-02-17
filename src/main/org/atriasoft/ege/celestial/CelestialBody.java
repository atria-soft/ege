package org.atriasoft.ege.celestial;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector3f;

/**
 * Represents a celestial body (sun or moon) orbiting in the sky.
 * <p>
 * The body moves along a circular orbit defined by its angular speed,
 * orbital inclination and orbital elevation:
 * <ul>
 *   <li>{@code orbitalInclination} — tilts the orbital plane sideways (Y axis).
 *       0 = orbit in the XZ plane, higher values spread the path in Y.</li>
 *   <li>{@code orbitalElevation} — raises the entire orbit above the horizon.
 *       Simulates latitude effect / seasonal declination:
 *       <ul>
 *         <li>{@code elevation = 0} — orbit passes through the horizon (normal day/night)</li>
 *         <li>{@code elevation > 0} — orbit center raised, lowest point stays above horizon
 *             (midnight sun / polar day)</li>
 *         <li>{@code elevation < 0} — orbit center lowered, highest point stays below
 *             horizon (polar night)</li>
 *       </ul>
 *   </li>
 * </ul>
 * <p>
 * When the body is below the horizon ({@code direction.z < 0}), it does not
 * contribute light or cast shadows.
 */
public class CelestialBody {
	private final CelestialBodyType type;
	private float angularSpeed;
	private float orbitalInclination;
	private float orbitalElevation;
	private float currentAngle;
	private Color lightColor;
	private float intensity;
	private boolean castsShadow;

	/**
	 * Create a celestial body.
	 * @param type               SUN or MOON
	 * @param angularSpeed       Angular speed in radians per second
	 * @param orbitalInclination Orbital tilt in radians (0 = equinox)
	 * @param initialAngle       Starting angle in radians
	 * @param lightColor         Color of the light emitted
	 * @param intensity          Light intensity (sun ~1.0, moon ~0.15)
	 * @param castsShadow        Whether this body casts shadows
	 */
	public CelestialBody(
			final CelestialBodyType type,
			final float angularSpeed,
			final float orbitalInclination,
			final float initialAngle,
			final Color lightColor,
			final float intensity,
			final boolean castsShadow) {
		this.type = type;
		this.angularSpeed = angularSpeed;
		this.orbitalInclination = orbitalInclination;
		this.orbitalElevation = 0.0f;
		this.currentAngle = initialAngle;
		this.lightColor = lightColor;
		this.intensity = intensity;
		this.castsShadow = castsShadow;
	}

	/**
	 * Advance the orbital angle based on elapsed time.
	 * @param deltaMili Time elapsed in milliseconds
	 */
	public void update(final long deltaMili) {
		final float deltaSec = deltaMili / 1000.0f;
		this.currentAngle += this.angularSpeed * deltaSec;
		// Keep angle in [0, 2π] to avoid float precision drift
		final float twoPi = (float) (2.0 * Math.PI);
		if (this.currentAngle > twoPi) {
			this.currentAngle -= twoPi;
		} else if (this.currentAngle < 0.0f) {
			this.currentAngle += twoPi;
		}
	}

	/**
	 * Compute the direction vector of this body on the celestial sphere.
	 * <p>
	 * The world uses Z-up convention. The body orbits on a circle:
	 * <ol>
	 *   <li>Base orbit in XZ plane: {@code (cos(angle), 0, sin(angle))}</li>
	 *   <li>Inclination tilts the orbit sideways into Y</li>
	 *   <li>Elevation shifts the whole orbit up/down in Z (latitude effect)</li>
	 * </ol>
	 * The result is normalized so it stays on the unit sphere.
	 * {@code direction.z() > 0} means the body is above the horizon.
	 * @return Normalized direction vector from world center to the body
	 */
	public Vector3f getDirection() {
		final float cosAngle = (float) Math.cos(this.currentAngle);
		final float sinAngle = (float) Math.sin(this.currentAngle);
		final float cosIncl = (float) Math.cos(this.orbitalInclination);
		final float sinIncl = (float) Math.sin(this.orbitalInclination);
		final float cosElev = (float) Math.cos(this.orbitalElevation);
		final float sinElev = (float) Math.sin(this.orbitalElevation);
		// Base orbit in XZ plane, tilted by inclination into Y
		final float ox = cosAngle;
		final float oy = sinAngle * sinIncl;
		final float oz = sinAngle * cosIncl;
		// Elevation rotates the orbit around the X axis, raising Z
		// Rotation around X: y' = y*cos - z*sin, z' = y*sin + z*cos
		final float x = ox;
		final float y = oy * cosElev - oz * sinElev;
		final float z = oy * sinElev + oz * cosElev;
		// Normalize (should be near unit length but ensure precision)
		final float len = (float) Math.sqrt(x * x + y * y + z * z);
		if (len < 1e-6f) {
			return new Vector3f(1.0f, 0.0f, 0.0f);
		}
		return new Vector3f(x / len, y / len, z / len);
	}

	/**
	 * @return true if the body is above the horizon (visible in the sky)
	 */
	public boolean isAboveHorizon() {
		return getDirection().z() > 0.0f;
	}

	/**
	 * @return The effective brightness, accounting for height above horizon.
	 *         Returns 0 if below horizon.
	 */
	public float getEffectiveBrightness() {
		final float height = getDirection().z();
		if (height <= 0.0f) {
			return 0.0f;
		}
		return this.intensity * height;
	}

	public CelestialBodyType getType() {
		return this.type;
	}

	public float getAngularSpeed() {
		return this.angularSpeed;
	}

	public void setAngularSpeed(final float angularSpeed) {
		this.angularSpeed = angularSpeed;
	}

	public float getOrbitalInclination() {
		return this.orbitalInclination;
	}

	public void setOrbitalInclination(final float orbitalInclination) {
		this.orbitalInclination = orbitalInclination;
	}

	public float getOrbitalElevation() {
		return this.orbitalElevation;
	}

	public void setOrbitalElevation(final float orbitalElevation) {
		this.orbitalElevation = orbitalElevation;
	}

	public float getCurrentAngle() {
		return this.currentAngle;
	}

	public void setCurrentAngle(final float currentAngle) {
		this.currentAngle = currentAngle;
	}

	public Color getLightColor() {
		return this.lightColor;
	}

	public void setLightColor(final Color lightColor) {
		this.lightColor = lightColor;
	}

	public float getIntensity() {
		return this.intensity;
	}

	public void setIntensity(final float intensity) {
		this.intensity = intensity;
	}

	public boolean isCastsShadow() {
		return this.castsShadow;
	}

	public void setCastsShadow(final boolean castsShadow) {
		this.castsShadow = castsShadow;
	}

	@Override
	public String toString() {
		return "CelestialBody[type=" + this.type
				+ ", angle=" + this.currentAngle
				+ ", speed=" + this.angularSpeed
				+ ", inclination=" + this.orbitalInclination
				+ ", elevation=" + this.orbitalElevation
				+ ", intensity=" + this.intensity + "]";
	}
}
