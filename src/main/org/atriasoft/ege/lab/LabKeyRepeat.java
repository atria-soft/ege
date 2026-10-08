package org.atriasoft.ege.lab;

import java.util.HashMap;
import java.util.Map;

import org.atriasoft.gale.key.KeyKeyboard;

/**
 * Tells a new press of a key from the auto-repeat of a held key, as the
 * game's {@code KeyPressFilter} does: gale hands each auto-repeat as a
 * release and a press at the same instant (ewol forwards them as plain
 * presses), so a press this soon after a release of the same key, or while
 * it is still down, is the auto-repeat. A release lost while the window had
 * no focus costs at most one press: the next release clears it. Times in
 * seconds on a monotonic clock. Pure Java, tested headless.
 */
public final class LabKeyRepeat {

	/** A press this soon after a release of the same key is its auto-repeat, seconds. */
	public static final double AUTO_REPEAT_WINDOW = 0.05;

	/** What a key event is. */
	public enum Press {
		/** A key pressed. */
		NEW,
		/** The auto-repeat of a held key. */
		REPEAT,
		/** A key released. */
		RELEASE
	}

	/** One key: when it was released last, whether it is down. */
	private static final class State {
		private double lastRelease = Double.NEGATIVE_INFINITY;
		private boolean down;
	}

	private final Map<String, State> keys = new HashMap<>();

	/** The name of a key event: its special key, or its character in lower case. */
	private static String name(final KeyKeyboard type, final Character value) {
		if (type != KeyKeyboard.CHARACTER) {
			return type.name();
		}
		return value != null ? String.valueOf(Character.toLowerCase(value)) : "";
	}

	/**
	 * A key went down or up at {@code now}.
	 *
	 * @param type  the key as gale hands it
	 * @param value its character ({@link KeyKeyboard#CHARACTER})
	 * @param down  whether it went down (else up)
	 * @param now   time of the event, seconds
	 */
	public Press onKey(final KeyKeyboard type, final Character value, final boolean down, final double now) {
		final State state = this.keys.computeIfAbsent(name(type, value), key -> new State());
		if (!down) {
			state.lastRelease = now;
			state.down = false;
			return Press.RELEASE;
		}
		final boolean repeat = state.down || now - state.lastRelease < AUTO_REPEAT_WINDOW;
		state.down = true;
		return repeat ? Press.REPEAT : Press.NEW;
	}
}
