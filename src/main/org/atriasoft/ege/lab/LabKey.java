package org.atriasoft.ege.lab;

import java.util.Locale;

import org.atriasoft.gale.key.KeyKeyboard;

/**
 * A key of the keyboard that runs a control of a lab: a character (a letter
 * matches in either case), alone or with Control ({@link #ctrl}: an editor's
 * Ctrl+Z), or a special key (F5, Home...). Some keys cannot run a control:
 * {@link LabControls#refusal} says which and why.
 *
 * @param type      {@link KeyKeyboard#CHARACTER} for a character, else the special key
 * @param character the character, lower case ({@code '\0'} for a special key)
 * @param ctrl      whether Control is held with it
 */
public record LabKey(KeyKeyboard type, char character, boolean ctrl) {

	/** A key without Control. */
	public LabKey(final KeyKeyboard type, final char character) {
		this(type, character, false);
	}

	/** The key typing {@code character} (a letter in either case). */
	public static LabKey of(final char character) {
		return new LabKey(KeyKeyboard.CHARACTER, Character.toLowerCase(character), false);
	}

	/**
	 * The key typing {@code character} with Control held ({@code Ctrl+Z}): gale hands the letter of the key, not a
	 * control code, with Control set.
	 */
	public static LabKey ctrl(final char character) {
		return new LabKey(KeyKeyboard.CHARACTER, Character.toLowerCase(character), true);
	}

	/** The special key {@code type} (F1 to F11, Home, Insert...; F12 belongs to ewol's widget inspector). */
	public static LabKey of(final KeyKeyboard type) {
		if (type == KeyKeyboard.CHARACTER) {
			throw new IllegalArgumentException("a character key needs its character: LabKey.of('x')");
		}
		return new LabKey(type, '\0', false);
	}

	/** Whether a key event of {@code type} and {@code value} (as gale hands them), without Control, is this key. */
	public boolean matches(final KeyKeyboard type, final Character value) {
		return matches(type, value, false);
	}

	/** Whether a key event of {@code type} and {@code value}, Control held or not ({@code ctrl}), is this key. */
	public boolean matches(final KeyKeyboard type, final Character value, final boolean ctrl) {
		if (ctrl != this.ctrl) {
			return false;
		}
		if (this.type != KeyKeyboard.CHARACTER) {
			return type == this.type;
		}
		return type == KeyKeyboard.CHARACTER && value != null && Character.toLowerCase(value) == this.character;
	}

	/**
	 * The name shown in brackets after the label of a control: {@code H}, {@code F5}, {@code Tab}, {@code Esc},
	 * {@code Ctrl+Z}.
	 */
	public String name() {
		return (this.ctrl ? "Ctrl+" : "") + baseName();
	}

	private String baseName() {
		if (this.type != KeyKeyboard.CHARACTER) {
			final String name = this.type.name().replace('_', ' ').toLowerCase(Locale.ROOT);
			if (name.length() <= 3 && name.startsWith("f")) {
				return name.toUpperCase(Locale.ROOT);
			}
			return Character.toUpperCase(name.charAt(0)) + name.substring(1);
		}
		return switch (this.character) {
			case '\t' -> "Tab";
			case '\u001b' -> "Esc";
			case ' ' -> "Space";
			case '\r', '\n' -> "Enter";
			default -> String.valueOf(Character.toUpperCase(this.character));
		};
	}
}
