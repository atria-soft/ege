package org.atriasoft.ege.engines;

import java.util.Vector;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.Light;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentLight;
import org.atriasoft.ege.components.ComponentLightSun;
import org.atriasoft.etk.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineLight extends Engine {
	static final Logger LOGGER = LoggerFactory.getLogger(EngineLight.class);
	public static final String ENGINE_NAME = "light";
	/** Lights a shader receives at most (MAX_LIGHT_NUMBER of the shaders). */
	public static final int MAX_LIGHTS = 8;
	/** Default reach of a local light, metres (the former hard-coded limit). */
	public static final float DEFAULT_LIGHT_RANGE = 50.0f;
	private final Vector<ComponentLight> componentLights = new Vector<>();
	private float lightRange = DEFAULT_LIGHT_RANGE;
	/** Scratch of {@link #getNearest}: nearest local lights (rendering thread only). */
	private final ComponentLight[] nearestLights = new ComponentLight[MAX_LIGHTS];
	/** Scratch of {@link #getNearest}: their positions. */
	private final Vector3f[] nearestPositions = new Vector3f[MAX_LIGHTS];
	/** Scratch of {@link #getNearest}: their squared distances. */
	private final float[] nearestDistances = new float[MAX_LIGHTS];
	private final Vector<ComponentLightSun> componentSuns = new Vector<>();
	
	public EngineLight(final Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void componentRemove(final Component ref) {
		this.componentLights.remove(ref);
		this.componentSuns.remove(ref);
	}
	
	@Override
	public void componentAdd(final Component ref) {
		if (ref instanceof final ComponentLightSun refTyped) {
			this.componentSuns.add(refTyped);
			return;
		}
		if (ref instanceof final ComponentLight refTyped) {
			this.componentLights.add(refTyped);
		}
	}
	
	@Override
	public void update(final long deltaMili) {
		// nothing to do ...
	}
	
	@Override
	public void render(final long deltaMili, final Camera camera) {
		// nothing to do ...
	}
	
	@Override
	public void renderDebug(final long deltaMili, final Camera camera) {
		// nothing to do ...
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return EngineLight.ENGINE_NAME;
	}
	
	/** Distance beyond which a local light is not given to a drawn object, metres. */
	public float getLightRange() {
		return this.lightRange;
	}

	/**
	 * Set how far a local light reaches when choosing the lights of a drawn
	 * object (see {@link #getNearest}).
	 *
	 * @param range metres, must be positive
	 */
	public void setLightRange(final float range) {
		if (!(range > 0.0f)) {
			throw new IllegalArgumentException("light range must be positive: " + range);
		}
		this.lightRange = range;
	}

	/**
	 * Lights of an object drawn around {@code position}: every sun first, then
	 * the local lights within {@link #getLightRange()} of it, NEAREST FIRST,
	 * up to {@link #MAX_LIGHTS} in all (unused slots stay {@code null}).
	 *
	 * @param position reference point of the drawn object (its centre)
	 */
	public Light[] getNearest(final Vector3f position) {
		final Light[] out = new Light[MAX_LIGHTS];
		int count = 0;
		for (final ComponentLightSun elem : this.componentSuns) {
			if (count >= MAX_LIGHTS) {
				LOGGER.error("more suns than light slots ({})", MAX_LIGHTS);
				return out;
			}
			out[count] = new Light(elem.getLight().getColor(), elem.getPosition(), elem.getLight().getAttenuation());
			out[count].setRadius(elem.getLight().getRadius());
			count++;
		}
		final int free = MAX_LIGHTS - count;
		if (free <= 0 || this.componentLights.isEmpty()) {
			return out;
		}
		// The nearest local lights so far, nearest first, in fixed arrays (no allocation, no boxing).
		final ComponentLight[] nearest = this.nearestLights;
		final Vector3f[] positions = this.nearestPositions;
		final float[] distances = this.nearestDistances;
		final float maxDistance2 = this.lightRange * this.lightRange;
		int kept = 0;
		for (final ComponentLight elem : this.componentLights) {
			final Vector3f where = elem.getPosition();
			if (where == null) {
				continue;
			}
			final float distance2 = where.distance2(position);
			if (!(distance2 < maxDistance2) || (kept == free && distance2 >= distances[kept - 1])) {
				continue;
			}
			// Insert after the lights at the same distance (the first added stays first); drop the farthest when full.
			int index = kept < free ? kept++ : free - 1;
			while (index > 0 && distances[index - 1] > distance2) {
				distances[index] = distances[index - 1];
				positions[index] = positions[index - 1];
				nearest[index] = nearest[index - 1];
				index--;
			}
			distances[index] = distance2;
			positions[index] = where;
			nearest[index] = elem;
		}
		for (int iii = 0; iii < kept; iii++) {
			final Light src = nearest[iii].getLight();
			out[count] = new Light(src.getColor(), positions[iii], src.getAttenuation(), src.getDirection(), 0.0f,
					0.0f);
			nearest[iii] = null;
			positions[iii] = null;
			// Pass cutoff cosines directly (already computed)
			out[count].setCutoffCos(src.getCutoffCos());
			out[count].setCutoffCosInner(src.getCutoffCosInner());
			out[count].setRadius(src.getRadius());
			count++;
		}
		return out;
	}
	
}
