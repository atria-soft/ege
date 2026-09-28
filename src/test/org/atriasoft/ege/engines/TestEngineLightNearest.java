package org.atriasoft.ege.engines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.atriasoft.ege.Light;
import org.atriasoft.ege.components.ComponentLight;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector3f;
import org.junit.jupiter.api.Test;

/** The lights given to a drawn object: nearest first, within a configurable range. */
class TestEngineLightNearest {

	/** A local light standing at a fixed place (no position component needed). */
	private static ComponentLight lampAt(final float x, final float z, final Color color) {
		final Vector3f where = new Vector3f(x, 3.0f, z);
		return new ComponentLight(new Light(color, Vector3f.ZERO, new Vector3f(1.0f, 0.0f, 0.1f))) {
			@Override
			public Vector3f getPosition() {
				return where;
			}
		};
	}

	@Test
	void theNearestLightsComeFirstWhateverTheInsertionOrder() {
		final EngineLight engine = new EngineLight(null);
		final Color far = new Color(0.1f, 0.1f, 0.1f, 1.0f);
		final Color near = new Color(0.9f, 0.9f, 0.9f, 1.0f);
		final Color middle = new Color(0.5f, 0.5f, 0.5f, 1.0f);
		engine.componentAdd(lampAt(40.0f, 0.0f, far));
		engine.componentAdd(lampAt(2.0f, 0.0f, near));
		engine.componentAdd(lampAt(20.0f, 0.0f, middle));
		final Light[] lights = engine.getNearest(Vector3f.ZERO);
		assertEquals(near, lights[0].getColor());
		assertEquals(middle, lights[1].getColor());
		assertEquals(far, lights[2].getColor());
		assertNull(lights[3]);
	}

	@Test
	void theRangeDecidesWhichLightsReachAnObject() {
		final EngineLight engine = new EngineLight(null);
		engine.componentAdd(lampAt(264.0f, 0.0f, new Color(1.0f, 1.0f, 1.0f, 1.0f)));
		assertNull(engine.getNearest(Vector3f.ZERO)[0], "out of the default 50 m");
		engine.setLightRange(300.0f);
		assertEquals(new Color(1.0f, 1.0f, 1.0f, 1.0f), engine.getNearest(Vector3f.ZERO)[0].getColor());
		assertThrows(IllegalArgumentException.class, () -> engine.setLightRange(0.0f));
	}

	@Test
	void atMostEightLightsKeepingTheNearest() {
		final EngineLight engine = new EngineLight(null);
		for (int i = 12; i > 0; i--) {
			engine.componentAdd(lampAt(i, 0.0f, new Color(i / 12.0f, 0.0f, 0.0f, 1.0f)));
		}
		final Light[] lights = engine.getNearest(Vector3f.ZERO);
		assertEquals(EngineLight.MAX_LIGHTS, lights.length);
		for (int i = 0; i < EngineLight.MAX_LIGHTS; i++) {
			assertEquals((i + 1) / 12.0f, lights[i].getColor().r(), 1.0e-6f, "slot " + i);
		}
	}
}
