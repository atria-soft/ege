package org.atriasoft.ege.lab;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * The lines of the info panel of a lab (figures, checks, errors), each with
 * the kind that colours it. Pure Java.
 */
public final class LabText {

	/** How a line is coloured. */
	public enum Kind {
		/** A heading. */
		TITLE,
		/** Plain text. */
		NORMAL,
		/** Within its budget, passed. */
		GOOD,
		/** Close to a limit, a warning. */
		WARN,
		/** Over a limit, an error. */
		BAD,
		/** A detail (paths, hints). */
		DIM
	}

	/**
	 * A line of the info panel.
	 *
	 * @param text what it says (no line break: a long line is wrapped between words)
	 * @param kind its colour
	 */
	public record Line(String text, Kind kind) {

		public Line {
			text = text != null ? text : "";
			kind = kind != null ? kind : Kind.NORMAL;
		}

		public static Line title(final String text) {
			return new Line(text, Kind.TITLE);
		}

		public static Line normal(final String text) {
			return new Line(text, Kind.NORMAL);
		}

		public static Line good(final String text) {
			return new Line(text, Kind.GOOD);
		}

		public static Line warn(final String text) {
			return new Line(text, Kind.WARN);
		}

		public static Line bad(final String text) {
			return new Line(text, Kind.BAD);
		}

		public static Line dim(final String text) {
			return new Line(text, Kind.DIM);
		}

		/** An empty line (a gap). */
		public static Line gap() {
			return new Line("", Kind.NORMAL);
		}
	}

	/** The most spaces a wrapped line keeps in front of its pieces. */
	static final int MAX_INDENT = 8;

	private LabText() {}

	/**
	 * {@code line} cut into pieces no wider than {@code width} as
	 * {@code measure} measures them: between words, the pieces after the first
	 * indented by two more spaces than the line (its own indent at most
	 * {@link #MAX_INDENT} spaces, none when even that leaves no room); a word
	 * too long for a piece (a path) is cut between its letters, each piece
	 * taking at least one letter, so the cutting always ends.
	 */
	public static List<String> wrap(final String line, final float width, final ToDoubleFunction<String> measure) {
		final List<String> parts = new ArrayList<>();
		if (line.isEmpty() || measure.applyAsDouble(line) <= width) {
			parts.add(line);
			return parts;
		}
		int start = 0;
		while (start < line.length() && line.charAt(start) == ' ') {
			start++;
		}
		String indent = " ".repeat(Math.min(start, MAX_INDENT));
		String follow = indent + "  ";
		if (measure.applyAsDouble(follow + "W") > width) {
			indent = "";
			follow = measure.applyAsDouble("  W") > width ? "" : "  ";
		}
		String current = indent;
		for (final String word : line.substring(start).split(" ")) {
			if (word.isEmpty()) {
				continue;
			}
			final String candidate = current.isBlank() ? current + word : current + " " + word;
			if (measure.applyAsDouble(candidate) <= width) {
				current = candidate;
				continue;
			}
			if (!current.isBlank()) {
				parts.add(current);
				current = follow;
			}
			String rest = word;
			while (measure.applyAsDouble(current + rest) > width && rest.length() > 1) {
				int cut = 1;
				while (cut < rest.length() - 1 && measure.applyAsDouble(current + rest.substring(0, cut + 1)) <= width) {
					cut++;
				}
				parts.add(current + rest.substring(0, cut));
				rest = rest.substring(cut);
				current = follow;
			}
			current = current + rest;
		}
		if (!current.isBlank()) {
			parts.add(current);
		}
		return parts;
	}

	/**
	 * {@code text} in the characters the fonts of ewol draw: accents taken
	 * off, a few signs spelled out ({@code ×} becomes {@code x}, {@code °}
	 * {@code deg}, dashes {@code -}), tabulations and line breaks become
	 * spaces, anything else outside ASCII a {@code ?}.
	 */
	public static String ascii(final String text) {
		if (text == null) {
			return "";
		}
		final String plain = Normalizer.normalize(text, Normalizer.Form.NFD);
		final StringBuilder out = new StringBuilder(plain.length());
		for (int i = 0; i < plain.length(); i++) {
			final char c = plain.charAt(i);
			if (Character.getType(c) == Character.NON_SPACING_MARK) {
				continue;
			}
			switch (c) {
				case '×' -> out.append('x');
				case '°' -> out.append(" deg");
				case '²' -> out.append('2');
				case '³' -> out.append('3');
				case '–', '—', '−' -> out.append('-');
				case '‘', '’' -> out.append('\'');
				case '“', '”' -> out.append('"');
				case '…' -> out.append("...");
				case '≤' -> out.append("<=");
				case '≥' -> out.append(">=");
				case '≈' -> out.append('~');
				case ' ', '\t', '\n', '\r' -> out.append(' ');
				default -> out.append(c >= 32 && c < 127 ? c : '?');
			}
		}
		return out.toString();
	}
}
