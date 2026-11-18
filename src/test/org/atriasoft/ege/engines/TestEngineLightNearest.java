package org.atriasoft.ege.engines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.atriasoft.ege.Light;
import org.atriasoft.ege.components.ComponentLight;
import org.atriasoft.ege.components.ComponentLightSun;
import org.atriasoft.ege.geometry.AABB;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector3f;
import org.junit.jupiter.api.Test;

/** The lights given to a drawn object: nearest first, within a configurable range, from any thread. */
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
	void equalDistancesDoNotDependOnTheOrderOfAdditionAndLightsWithoutPlaceAreSkipped() {
		final EngineLight engine = new EngineLight(null);
		final Color first = new Color(0.2f, 0.0f, 0.0f, 1.0f);
		final Color second = new Color(0.4f, 0.0f, 0.0f, 1.0f);
		engine.componentAdd(new ComponentLight(new Light(Color.WHITE, Vector3f.ZERO, new Vector3f(1.0f, 0.0f, 0.0f))));
		engine.componentAdd(lampAt(5.0f, 0.0f, first));
		engine.componentAdd(lampAt(-5.0f, 0.0f, second));
		final Light[] lights = engine.getNearest(Vector3f.ZERO);
		assertEquals(second, lights[0].getColor(), "same distance: the stable key (position) decides, x = -5 first");
		assertEquals(first, lights[1].getColor());
		assertNull(lights[2], "the light without position is not given");
	}

	@Test
	void lightsInsideTheBoxKeepTheSameSetWhateverTheInsertionOrder() {
		final AABB box = new AABB(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(40.0f, 10.0f, 40.0f));
		final List<ComponentLight> lamps = new ArrayList<>();
		for (int i = 0; i < 12; i++) {
			// All inside the box (distance 0), at different places round its centre.
			lamps.add(lampAt((i % 6) * 5.0f - 12.0f, (i / 6) * 7.0f - 3.0f, new Color(i / 12.0f, 0.0f, 0.0f, 1.0f)));
		}
		final List<Float> expected = new ArrayList<>();
		for (int round = 0; round < 5; round++) {
			Collections.shuffle(lamps, new Random(round));
			final EngineLight engine = new EngineLight(null);
			for (final ComponentLight lamp : lamps) {
				engine.componentAdd(lamp);
			}
			final List<Float> got = new ArrayList<>();
			for (final Light light : engine.getNearest(box)) {
				got.add(light.getColor().r());
			}
			if (round == 0) {
				expected.addAll(got);
				assertEquals(EngineLight.MAX_LIGHTS, got.size());
			} else {
				assertEquals(expected, got, "round " + round);
			}
		}
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

	@Test
	void aLightWithARangeOfItsOwnReachesOnlyThatFar() {
		final EngineLight engine = new EngineLight(null);
		final Color scoped = new Color(0.9f, 0.0f, 0.0f, 1.0f);
		final Color other = new Color(0.1f, 0.0f, 0.0f, 1.0f);
		final ComponentLight scopedLamp = lampAt(20.0f, 0.0f, scoped);
		engine.componentAdd(scopedLamp);
		engine.componentAdd(lampAt(-20.0f, 0.0f, other));
		engine.setLightRange(scopedLamp, 7.0f);
		assertEquals(7.0f, engine.getLightRange(scopedLamp));
		assertEquals(EngineLight.DEFAULT_LIGHT_RANGE, engine.getLightRange(), "the others keep the engine range");
		final Light[] fromOrigin = engine.getNearest(Vector3f.ZERO);
		assertEquals(other, fromOrigin[0].getColor(), "20 m: within the engine range");
		assertNull(fromOrigin[1], "20 m: beyond its own 7 m");
		assertEquals(scoped, engine.getNearest(new Vector3f(15.0f, 0.0f, 0.0f))[0].getColor(), "within 7 m of it");
		assertThrows(IllegalArgumentException.class, () -> engine.setLightRange(scopedLamp, -1.0f));
		engine.componentRemove(scopedLamp);
		assertEquals(EngineLight.DEFAULT_LIGHT_RANGE, engine.getLightRange(scopedLamp), "forgotten with the light");
	}

	@Test
	void aBoxIsJudgedByItsDistanceNotByItsCentre() {
		final EngineLight engine = new EngineLight(null);
		final Color edge = new Color(0.2f, 0.0f, 0.0f, 1.0f);
		final Color centre = new Color(0.8f, 0.0f, 0.0f, 1.0f);
		engine.componentAdd(lampAt(0.0f, 0.0f, edge));
		engine.componentAdd(lampAt(20.0f, 0.0f, centre));
		// A box x in [0, 40], y in [-5, 5], z in [-5, 5]: both lamps (3 m up) are over it, at no distance.
		final AABB box = new AABB(new Vector3f(20.0f, 0.0f, 0.0f), new Vector3f(20.0f, 5.0f, 5.0f));
		final Light[] byBox = engine.getNearest(box);
		assertEquals(centre, byBox[0].getColor(), "both are inside the box: the nearer to its centre comes first");
		assertEquals(edge, byBox[1].getColor());
		final Light[] byCentre = engine.getNearest(box.position);
		assertEquals(centre, byCentre[0].getColor(), "by the centre the middle lamp wins");
	}

	@Test
	void aBoxReachesALightItsCentreDoesNot() {
		final EngineLight engine = new EngineLight(null);
		engine.componentAdd(lampAt(60.0f, 0.0f, new Color(1.0f, 1.0f, 1.0f, 1.0f)));
		final AABB box = new AABB(new Vector3f(0.0f, 3.0f, 0.0f), new Vector3f(30.0f, 1.0f, 1.0f));
		assertNull(engine.getNearest(box.position)[0], "60 m from the centre, out of the 50 m");
		assertEquals(new Color(1.0f, 1.0f, 1.0f, 1.0f), engine.getNearest(box)[0].getColor(), "30 m from the box");
		final AABB far = new AABB(new Vector3f(-100.0f, 3.0f, 0.0f), new Vector3f(30.0f, 1.0f, 1.0f));
		assertNull(engine.getNearest(far)[0], "130 m from the box");
	}

	@Test
	void theLightsAreReadableButNotWritable() {
		final EngineLight engine = new EngineLight(null);
		final ComponentLight lamp = lampAt(1.0f, 0.0f, new Color(1.0f, 1.0f, 1.0f, 1.0f));
		engine.componentAdd(lamp);
		assertEquals(1, engine.getLights().size());
		assertEquals(lamp, engine.getLights().get(0));
		assertTrue(engine.getSuns().isEmpty());
		assertThrows(UnsupportedOperationException.class, () -> engine.getLights().clear());
		engine.componentRemove(lamp);
		assertTrue(engine.getLights().isEmpty(), "a view, not a copy");
	}

	@Test
	void callsFromSeveralThreadsDoNotMixTheirLights() throws InterruptedException, ExecutionException {
		final EngineLight engine = new EngineLight(null);
		final Color left = new Color(0.9f, 0.0f, 0.0f, 1.0f);
		final Color right = new Color(0.1f, 0.0f, 0.0f, 1.0f);
		engine.componentAdd(lampAt(-10.0f, 0.0f, left));
		engine.componentAdd(lampAt(10.0f, 0.0f, right));
		final ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			final Callable<Boolean> fromLeft = () -> {
				for (int i = 0; i < 20_000; i++) {
					final Light[] lights = engine.getNearest(new Vector3f(-9.0f, 0.0f, 0.0f));
					if (!left.equals(lights[0].getColor()) || !right.equals(lights[1].getColor())) {
						return false;
					}
				}
				return true;
			};
			final Callable<Boolean> fromRight = () -> {
				for (int i = 0; i < 20_000; i++) {
					final Light[] lights = engine.getNearest(new Vector3f(9.0f, 0.0f, 0.0f));
					if (!right.equals(lights[0].getColor()) || !left.equals(lights[1].getColor())) {
						return false;
					}
				}
				return true;
			};
			final Future<Boolean> a = pool.submit(fromLeft);
			final Future<Boolean> b = pool.submit(fromRight);
			assertTrue(a.get(), "the calls from the left always get the left lamp first");
			assertTrue(b.get(), "the calls from the right always get the right lamp first");
		} finally {
			pool.shutdownNow();
		}
	}
}
