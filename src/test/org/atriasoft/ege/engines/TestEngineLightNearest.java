package org.atriasoft.ege.engines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.atriasoft.ege.Light;
import org.atriasoft.ege.components.ComponentLight;
import org.atriasoft.ege.components.ComponentLightSun;
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

	@Test
	void theSunsComeFirstAndTheLocalLightsFillTheRemainingSlots() {
		final EngineLight engine = new EngineLight(null);
		final Color sunColor = new Color(1.0f, 0.9f, 0.8f, 1.0f);
		final Vector3f sunPosition = new Vector3f(100.0f, 100.0f, 50.0f);
		engine.componentAdd(new ComponentLightSun(new Light(sunColor, Vector3f.ZERO, new Vector3f(1.0f, 0.0f, 0.0f))) {
			@Override
			public Vector3f getPosition() {
				return sunPosition;
			}
		});
		for (int i = 12; i > 0; i--) {
			engine.componentAdd(lampAt(i, 0.0f, new Color(i / 12.0f, 0.0f, 0.0f, 1.0f)));
		}
		final Light[] lights = engine.getNearest(Vector3f.ZERO);
		assertEquals(sunColor, lights[0].getColor());
		assertEquals(sunPosition, lights[0].getPositionDelta());
		for (int i = 1; i < EngineLight.MAX_LIGHTS; i++) {
			assertEquals(i / 12.0f, lights[i].getColor().r(), 1.0e-6f, "slot " + i);
			assertEquals(new Vector3f(i, 3.0f, 0.0f), lights[i].getPositionDelta(), "slot " + i);
		}
	}

	@Test
	void equalDistancesKeepTheOrderOfAdditionAndLightsWithoutPlaceAreSkipped() {
		final EngineLight engine = new EngineLight(null);
		final Color first = new Color(0.2f, 0.0f, 0.0f, 1.0f);
		final Color second = new Color(0.4f, 0.0f, 0.0f, 1.0f);
		engine.componentAdd(new ComponentLight(new Light(Color.WHITE, Vector3f.ZERO, new Vector3f(1.0f, 0.0f, 0.0f))));
		engine.componentAdd(lampAt(5.0f, 0.0f, first));
		engine.componentAdd(lampAt(-5.0f, 0.0f, second));
		final Light[] lights = engine.getNearest(Vector3f.ZERO);
		assertEquals(first, lights[0].getColor());
		assertEquals(second, lights[1].getColor());
		assertNull(lights[2], "the light without position is not given");
	}

	@Test
	void eachCallStartsAfresh() {
		final EngineLight engine = new EngineLight(null);
		final Color near = new Color(0.9f, 0.0f, 0.0f, 1.0f);
		final Color far = new Color(0.1f, 0.0f, 0.0f, 1.0f);
		engine.componentAdd(lampAt(2.0f, 0.0f, near));
		engine.componentAdd(lampAt(30.0f, 0.0f, far));
		assertEquals(near, engine.getNearest(Vector3f.ZERO)[0].getColor());
		final Light[] fromFar = engine.getNearest(new Vector3f(30.0f, 0.0f, 0.0f));
		assertEquals(far, fromFar[0].getColor());
		assertEquals(near, fromFar[1].getColor());
		final Light[] alone = engine.getNearest(new Vector3f(75.0f, 0.0f, 0.0f));
		assertEquals(far, alone[0].getColor(), "the near lamp is out of range from there");
		assertNull(alone[1]);
	}
}
