package org.atriasoft.ege.engines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.atriasoft.ege.celestial.CelestialBody;
import org.atriasoft.ege.celestial.CelestialBodyType;
import org.atriasoft.ege.celestial.CelestialSystem;
import org.atriasoft.etk.Color;
import org.junit.jupiter.api.Test;

/** A released shadow engine draws nothing more, and releasing twice is harmless (no OpenGL needed). */
class TestEngineShadowRelease {

	@Test
	void aReleasedEngineCastsNoShadow() {
		final CelestialSystem sky = new CelestialSystem();
		sky.addBody(new CelestialBody(CelestialBodyType.SUN, 0.0f, 0.3f, 1.0f, Color.WHITE, 1.0f, true));
		sky.update(0L);
		assertEquals(1, sky.getActiveShadowCasters().size());
		final EngineShadow shadow = new EngineShadow(null, sky);
		shadow.release();
		shadow.release();
		assertTrue(shadow.isReleased());
		// A sun that casts, yet no depth pass (which would need OpenGL) and no shadow map.
		shadow.render(20L, null);
		assertEquals(0, shadow.getActiveShadowCasterCount());
		shadow.renderDebugThumbnails();
	}
}
