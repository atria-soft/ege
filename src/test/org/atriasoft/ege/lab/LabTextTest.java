package org.atriasoft.ege.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** The texts of the panels in the characters the fonts draw. */
class LabTextTest {

	@Test
	void accentsAndSignsBecomeAscii() {
		assertEquals("Chene x 2 - 30 deg ~ 4 m2 ... ?", LabText.ascii("Chêne × 2 – 30° ≈ 4 m² … ☃"));
		assertEquals("a b", LabText.ascii("a\tb"));
		assertEquals("", LabText.ascii(null));
	}

	@Test
	void anErrorIsDescribedByTheCauseThatSaysMost() {
		final RuntimeException wrapped = new RuntimeException(new IllegalArgumentException("unknown field 'colour'"));
		assertEquals("RuntimeException: java.lang.IllegalArgumentException: unknown field 'colour'",
				LabView.describe(wrapped));
		assertEquals("IllegalStateException: no species", LabView.describe(new IllegalStateException("no species")));
		assertEquals("IllegalArgumentException: deep", LabView.describe(new RuntimeException((String) null,
				new IllegalArgumentException("deep"))));
	}
}
