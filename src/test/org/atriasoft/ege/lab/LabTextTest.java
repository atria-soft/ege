package org.atriasoft.ege.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.ToDoubleFunction;

import org.junit.jupiter.api.Test;

/** The texts of the panels in the characters the fonts draw. */
class LabTextTest {

	@Test
	void accentsAndSignsBecomeAscii() {
		assertEquals("Chene x 2 - 30 deg ~ 4 m2 ... ?", LabText.ascii("Chêne × 2 – 30° ≈ 4 m² … ☃"));
		assertEquals("a b", LabText.ascii("a\tb"));
		assertEquals("", LabText.ascii(null));
	}

	/** Seven pixels a character. */
	private static final ToDoubleFunction<String> MONO = text -> 7.0 * text.length();

	@Test
	void aLongLineIsWrappedBetweenWordsAndAPathBetweenItsLetters() {
		assertEquals(List.of("short"), LabText.wrap("short", 70, MONO));
		assertEquals(List.of("one two", "  three", "  four"), LabText.wrap("one two three four", 56, MONO));
		assertEquals(List.of("  one", "    two"), LabText.wrap("  one two", 49, MONO));
		final List<String> path = LabText.wrap("/home/heero/dev/perso/jatria_soft/species.json", 70, MONO);
		for (final String piece : path) {
			assertTrue(MONO.applyAsDouble(piece) <= 70, piece);
		}
		assertEquals("/home/heero/dev/perso/jatria_soft/species.json",
				String.join("", path.stream().map(String::strip).toList()));
	}

	@Test
	void aMonospaceLineIsWrappedBetweenCharactersItsSpacesKept() {
		assertEquals(List.of(" 12  .#HH"), LabText.wrapChars(" 12  .#HH", 70, MONO));
		assertEquals(List.of(" 12  .#", "HH:CC #", ".."), LabText.wrapChars(" 12  .#HH:CC #..", 49, MONO));
		// Narrower than one character: one a piece, and it ends.
		assertEquals(List.of("a", " ", "b"), LabText.wrapChars("a b", 3, MONO));
		assertEquals(List.of(""), LabText.wrapChars("", 3, MONO));
		final LabText.Line row = LabText.Line.mono(".#H+#.");
		assertTrue(row.mono());
		assertEquals(LabText.Kind.NORMAL, row.kind());
		assertEquals(LabText.Kind.BAD, LabText.Line.mono("12 .#x", LabText.Kind.BAD).kind());
		assertEquals(new LabText.Line("text", LabText.Kind.WARN, false), LabText.Line.warn("text"));
		assertTrue(!new LabText.Line("text", LabText.Kind.DIM).mono());
	}

	@Test
	void aLongItemIsShortened() {
		assertEquals("farmhouse", LabText.shorten("farmhouse", 12));
		assertEquals("roofs/fuz...", LabText.shorten("roofs/fuzz20261004n371", 12));
		assertEquals(12, LabText.shorten("roofs/fuzz20261004n371", 12).length());
		assertEquals("ab", LabText.shorten("abcdef", 2));
		assertEquals("", LabText.shorten(null, 5));
	}

	@Test
	void aBoxNarrowerThanTheIndentStillEnds() {
		// The indent and one letter do not fit: no indent, one letter a piece at worst.
		final List<String> pieces = LabText.wrap("                    abcdef ghij", 14, MONO);
		assertTrue(pieces.size() <= 12, pieces.toString());
		assertEquals("abcdefghij", String.join("", pieces.stream().map(String::strip).toList()));
		// Narrower than a single letter: still one letter a piece, and it ends.
		assertEquals(List.of("a", "b", "c"), LabText.wrap("abc", 3, MONO));
		// A deep indent is capped.
		final List<String> deep = LabText.wrap(" ".repeat(30) + "word word word", 140, MONO);
		assertTrue(deep.get(0).startsWith(" ".repeat(LabText.MAX_INDENT) + "word"), deep.toString());
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
