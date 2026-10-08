package org.atriasoft.ege.lab;

import java.util.Locale;

import org.atriasoft.gale.key.KeyKeyboard;

/**
 * A key of the keyboard that runs a control of a lab: a character (a letter
 * matches in either case) or a special key (F1, Home...).
 *
 * @param type      {@link KeyKeyboard#CHARACTER} for a character, else the special key
 * @param character the character, lower case ({@code '\0'} for a special key)
 */
public record LabKey(KeyKeyboard type, char character) {

	/** Escape (the kit leaves it to ewol: it closes a drop-down list). Tab never reaches gale (AWT keeps it). */
	public static final LabKey ESCAPE = of('\u001b');

	/** The key typing {@code character} (a letter in either case). */
	public static LabKey of(final char character) {
		return new LabKey(KeyKeyboard.CHARACTER, Character.toLowerCase(character));
	}

	/** The special key {@code type} (F1 to F11, Home, Insert...; F12 belongs to ewol's widget inspector). */
	public static LabKey of(final KeyKeyboard type) {
		if (type == KeyKeyboard.CHARACTER) {
			throw new IllegalArgumentException("a character key needs its character: LabKey.of('x')");
		}
		return new LabKey(type, '\0');
	}

	/** Whether a key event of {@code type} and {@code value} (as gale hands them) is this key. */
	public boolean matches(final KeyKeyboard type, final Character value) {
		if (this.type != KeyKeyboard.CHARACTER) {
			return type == this.type;
		}
		return type == KeyKeyboard.CHARACTER && value != null && Character.toLowerCase(value) == this.character;
	}

	/** The name shown in brackets after the label of a control: {@code H}, {@code F5}, {@code Tab}, {@code Esc}. */
	public String name() {
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
