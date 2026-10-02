package org.atriasoft.ege.engines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.atriasoft.ege.skybox.SkyboxConfig;
import org.atriasoft.etk.Uri;
import org.junit.jupiter.api.Test;

/** The sky turns around the Y axis at the speed of its configuration (no OpenGL needed: only update() runs). */
class TestEngineSkyboxRotation {

	private static final float EPSILON = 1.0e-5f;

	private static SkyboxConfig sky(final float rotationSpeed) {
		final SkyboxConfig config = new SkyboxConfig(
				new Uri("RES", "skybox/right.png"),
				new Uri("RES", "skybox/left.png"),
				new Uri("RES", "skybox/top.png"),
				new Uri("RES", "skybox/bottom.png"),
				new Uri("RES", "skybox/front.png"),
				new Uri("RES", "skybox/back.png"));
		config.setRotationSpeed(rotationSpeed);
		return config;
	}

	@Test
	void theAngleFollowsTheSpeedAndTheElapsedTime() {
		final EngineSkybox engine = new EngineSkybox(null);
		engine.setConfig(sky(0.5f));
		engine.update(1000);
		assertEquals(0.5f, engine.getRotationAngle(), EPSILON);
		engine.update(500);
		assertEquals(0.75f, engine.getRotationAngle(), EPSILON);
	}

	@Test
	void aSpeedChangedOnTheConfigurationAppliesAtOnce() {
		final SkyboxConfig config = sky(0.0f);
		final EngineSkybox engine = new EngineSkybox(null);
		engine.setConfig(config);
		engine.update(1000);
		assertEquals(0.0f, engine.getRotationAngle(), EPSILON, "no rotation by default");
		config.setRotationSpeed(0.2f);
		engine.update(1000);
		assertEquals(0.2f, engine.getRotationAngle(), EPSILON);
	}

	@Test
	void theAngleStaysWithinOneTurnInBothDirections() {
		final float fullTurn = (float) (2.0 * Math.PI);
		final EngineSkybox forward = new EngineSkybox(null);
		forward.setConfig(sky(1.0f));
		forward.update(7000);
		assertEquals(7.0f - fullTurn, forward.getRotationAngle(), 1.0e-4f);

		final EngineSkybox backward = new EngineSkybox(null);
		backward.setConfig(sky(-1.0f));
		backward.update(1000);
		assertEquals(fullTurn - 1.0f, backward.getRotationAngle(), 1.0e-4f);
		assertTrue(backward.getRotationAngle() >= 0.0f);
	}

	@Test
	void aNewSkyStartsUnrotatedAndNoSkyDoesNotTurn() {
		final EngineSkybox engine = new EngineSkybox(null);
		engine.update(1000);
		assertEquals(0.0f, engine.getRotationAngle(), EPSILON, "no configuration");
		engine.setConfig(sky(0.5f));
		engine.update(1000);
		engine.setConfig(sky(0.5f));
		assertEquals(0.0f, engine.getRotationAngle(), EPSILON, "reset by the new configuration");
		engine.setConfig(null);
		engine.update(1000);
		assertEquals(0.0f, engine.getRotationAngle(), EPSILON, "sky box disabled");
	}
}
