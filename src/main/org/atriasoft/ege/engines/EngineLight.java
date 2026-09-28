package org.atriasoft.ege.engines;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

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
	/** Reach of the local lights given one of their own ({@link #setLightRange(ComponentLight, float)}), metres. */
	private final Map<ComponentLight, Float> ownRanges = new ConcurrentHashMap<>();
	private final Vector<ComponentLightSun> componentSuns = new Vector<>();
	
	public EngineLight(final Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void componentRemove(final Component ref) {
		this.componentLights.remove(ref);
		this.componentSuns.remove(ref);
		if (ref instanceof final ComponentLight light) {
			this.ownRanges.remove(light);
		}
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
	
	/**
	 * Distance beyond which a local light without a range of its own is not
	 * given to a drawn object, metres.
	 */
	public float getLightRange() {
		return this.lightRange;
	}

	/**
	 * Set how far a local light without a range of its own reaches when
	 * choosing the lights of a drawn object (see {@link #getNearest}).
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
	 * Give {@code light} a reach of its own when choosing the lights of a
	 * drawn object, instead of {@link #getLightRange()}: the other lights
	 * keep theirs. Forgotten when the light leaves the engine.
	 *
	 * @param light a local light of this engine (or about to be added)
	 * @param range metres, must be positive
	 */
	public void setLightRange(final ComponentLight light, final float range) {
		if (!(range > 0.0f)) {
			throw new IllegalArgumentException("light range must be positive: " + range);
		}
		this.ownRanges.put(light, range);
	}

	/**
	 * How far {@code light} reaches when choosing the lights of a drawn
	 * object: its own range, else {@link #getLightRange()}.
	 */
	public float getLightRange(final ComponentLight light) {
		final Float own = this.ownRanges.get(light);
		return own != null ? own : this.lightRange;
	}

	/**
	 * Lights of an object drawn around {@code position}: every sun first, then
	 * the local lights within reach of it ({@link #getLightRange(ComponentLight)}),
	 * NEAREST FIRST, up to {@link #MAX_LIGHTS} in all (unused slots stay
	 * {@code null}).
	 * <p>
	 * Safe to call from several threads or while another call runs: the work
	 * arrays are local to the call. It allocates the returned array, one
	 * {@link Light} per slot used, a few small work arrays, and one position
	 * per local light examined ({@link ComponentLight#getPosition()}).
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
		// The nearest local lights so far, nearest first, in arrays of this call (re-entrant, no boxing).
		final ComponentLight[] nearest = new ComponentLight[free];
		final Vector3f[] positions = new Vector3f[free];
		final float[] distances = new float[free];
		final float defaultDistance2 = this.lightRange * this.lightRange;
		final boolean anyOwnRange = !this.ownRanges.isEmpty();
		int kept = 0;
		for (final ComponentLight elem : this.componentLights) {
			final Vector3f where = elem.getPosition();
			if (where == null) {
				continue;
			}
			float maxDistance2 = defaultDistance2;
			if (anyOwnRange) {
				final Float own = this.ownRanges.get(elem);
				if (own != null) {
					maxDistance2 = own * own;
				}
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
			// Pass cutoff cosines directly (already computed)
			out[count].setCutoffCos(src.getCutoffCos());
			out[count].setCutoffCosInner(src.getCutoffCosInner());
			out[count].setRadius(src.getRadius());
			count++;
		}
		return out;
	}
	
}
