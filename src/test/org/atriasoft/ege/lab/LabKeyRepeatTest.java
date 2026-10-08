package org.atriasoft.ege.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.atriasoft.gale.key.KeyKeyboard;
import org.junit.jupiter.api.Test;

/** A held key repeats as a release and a press at once: told from a new press. */
class LabKeyRepeatTest {

	@Test
	void aHeldKeyRepeatsATappedKeyPressesAgain() {
		final LabKeyRepeat keys = new LabKeyRepeat();
		assertEquals(LabKeyRepeat.Press.NEW, keys.onKey(KeyKeyboard.CHARACTER, 'i', true, 10.0));
		// gale's auto-repeat: a release and a press at the same instant.
		assertEquals(LabKeyRepeat.Press.RELEASE, keys.onKey(KeyKeyboard.CHARACTER, 'i', false, 10.5));
		assertEquals(LabKeyRepeat.Press.REPEAT, keys.onKey(KeyKeyboard.CHARACTER, 'i', true, 10.501));
		// A press while still down (no release seen).
		assertEquals(LabKeyRepeat.Press.REPEAT, keys.onKey(KeyKeyboard.CHARACTER, 'I', true, 10.6));
		keys.onKey(KeyKeyboard.CHARACTER, 'i', false, 11.0);
		// Tapped again a tenth of a second later: a new press.
		assertEquals(LabKeyRepeat.Press.NEW, keys.onKey(KeyKeyboard.CHARACTER, 'i', true, 11.1));
	}

	@Test
	void eachKeyIsItsOwn() {
		final LabKeyRepeat keys = new LabKeyRepeat();
		keys.onKey(KeyKeyboard.CHARACTER, 'h', true, 1.0);
		keys.onKey(KeyKeyboard.CHARACTER, 'h', false, 1.2);
		assertEquals(LabKeyRepeat.Press.NEW, keys.onKey(KeyKeyboard.CHARACTER, 's', true, 1.21));
		assertEquals(LabKeyRepeat.Press.NEW, keys.onKey(KeyKeyboard.F5, '\0', true, 1.22));
		assertEquals(LabKeyRepeat.Press.REPEAT, keys.onKey(KeyKeyboard.F5, '\0', true, 1.3));
	}
}
