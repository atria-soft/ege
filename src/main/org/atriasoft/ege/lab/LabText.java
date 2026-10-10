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
	 * @param text what it says (no line break: a long line is wrapped between words, a monospace one between
	 *             characters)
	 * @param kind its colour
	 * @param mono whether it is drawn in a monospace font (ASCII drawings, tables: their columns line up)
	 */
	public record Line(String text, Kind kind, boolean mono) {

		public Line {
			text = text != null ? text : "";
			kind = kind != null ? kind : Kind.NORMAL;
		}

		/** A line in the proportional font. */
		public Line(final String text, final Kind kind) {
			this(text, kind, false);
		}

		/** A plain line in the monospace font ({@code .#HHH+..}: the columns line up). */
		public static Line mono(final String text) {
			return new Line(text, Kind.NORMAL, true);
		}

		/** A line in the monospace font, of {@code kind} (an error at its line of a drawing: {@link Kind#BAD}). */
		public static Line mono(final String text, final Kind kind) {
			return new Line(text, kind, true);
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
	 * {@code line} cut into pieces no wider than {@code width} as
	 * {@code measure} measures them, between any two characters (spaces kept:
	 * a monospace drawing keeps its columns), each piece taking at least one
	 * character, so the cutting always ends.
	 */
	public static List<String> wrapChars(final String line, final float width, final ToDoubleFunction<String> measure) {
		final List<String> parts = new ArrayList<>();
		if (line.isEmpty() || measure.applyAsDouble(line) <= width) {
			parts.add(line);
			return parts;
		}
		int start = 0;
		while (start < line.length()) {
			int end = start + 1;
			while (end < line.length() && measure.applyAsDouble(line.substring(start, end + 1)) <= width) {
				end++;
			}
			parts.add(line.substring(start, end));
			start = end;
		}
		return parts;
	}

	/**
	 * {@code text} no longer than {@code max} characters: cut, its end
	 * replaced by {@code ...} (an item of a drop-down list that would push
	 * the panel wider).
	 */
	public static String shorten(final String text, final int max) {
		if (text == null) {
			return "";
		}
		if (text.length() <= max || max < 4) {
			return text.length() <= max ? text : text.substring(0, Math.max(0, max));
		}
		return text.substring(0, max - 3) + "...";
	}

	/**
	 * {@code text} as it fits in {@code width} as {@code measure} measures it: whole when it fits, else without its
	 * keys in brackets at the end ({@code Tree (shape or species)}: the keys are in the help), else cut and ended by
	 * {@code ...} ({@code Tree (shape o...}); {@code ...} alone when no letter fits, nothing when even that does not.
	 * Never wider than {@code width}: a label of the panel is shortened, never clipped.
	 */
	public static String fit(final String text, final float width, final ToDoubleFunction<String> measure) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		if (measure.applyAsDouble(text) <= width) {
			return text;
		}
		final int keys = text.endsWith("]") ? text.lastIndexOf(" [") : -1;
		final String words = keys > 0 ? text.substring(0, keys) : text;
		if (keys > 0 && measure.applyAsDouble(words) <= width) {
			return words;
		}
		final String head = longestHead(words, width, cut -> measure.applyAsDouble(cut + "..."));
		if (!head.isEmpty()) {
			return head + "...";
		}
		return measure.applyAsDouble("...") <= width ? "..." : "";
	}

	/**
	 * The longest beginning of {@code text} (spaces at its end taken off) for which {@code measure} stays within
	 * {@code width}, {@code ""} when not even one letter does.
	 */
	private static String longestHead(final String text, final float width, final ToDoubleFunction<String> measure) {
		int fits = 0;
		int fails = text.length() + 1;
		// What a beginning measures grows with its length: a bisection.
		while (fails - fits > 1) {
			final int middle = (fits + fails) >>> 1;
			if (measure.applyAsDouble(text.substring(0, middle)) <= width) {
				fits = middle;
			} else {
				fails = middle;
			}
		}
		return text.substring(0, fits).stripTrailing();
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
