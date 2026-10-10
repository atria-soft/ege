package org.atriasoft.ege.lab;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** How a row of the panel shares its width: wishes when they fit, the widest texts shortened first when not. */
class LabLayoutTest {

	private static final float DELTA = 0.01f;

	private static float sum(final float[] widths) {
		float total = 0.0f;
		for (final float width : widths) {
			total += width;
		}
		return total;
	}

	@Test
	void whenEverythingFitsTheRestGoesToThePiecesThatExpand() {
		// A title that expands, two small buttons.
		final float[] widths = LabLayout.share(new float[] { 28, 40, 40 }, new float[] { 150, 40, 40 },
				new boolean[] { true, false, false }, 300);
		assertArrayEquals(new float[] { 220, 40, 40 }, widths, DELTA);
		// Nothing expands: each piece at its wish, the rest left empty.
		assertArrayEquals(new float[] { 150, 40 }, LabLayout.share(new float[] { 28, 40 }, new float[] { 150, 40 },
				new boolean[] { false, false }, 300), DELTA);
		// Two buttons that expand share the rest equally.
		assertArrayEquals(new float[] { 140, 160 }, LabLayout.share(new float[] { 28, 28 }, new float[] { 90, 110 },
				new boolean[] { true, true }, 300), DELTA);
	}

	@Test
	void whenRoomIsShortTheWidestShrinksFirstAndTheButtonsKeepTheirWidth() {
		// Title 150, minus 22, value 96, plus 22 in 260: the title gives way alone.
		final float[] widths = LabLayout.share(new float[] { 28, 22, 28, 22 }, new float[] { 150, 22, 96, 22 },
				new boolean[] { true, false, false, false }, 260);
		assertEquals(120.0f, widths[0], DELTA);
		assertEquals(22.0f, widths[1], DELTA);
		assertEquals(96.0f, widths[2], DELTA);
		assertEquals(22.0f, widths[3], DELTA);
		// Narrower: the title and the value shrink to one same width.
		final float[] narrow = LabLayout.share(new float[] { 28, 22, 28, 22 }, new float[] { 150, 22, 96, 22 },
				new boolean[] { true, false, false, false }, 164);
		assertEquals(60.0f, narrow[0], DELTA);
		assertEquals(60.0f, narrow[2], DELTA);
		assertEquals(164.0f, sum(narrow), DELTA);
		// Never under their least: the row overflows only when even those do not fit.
		assertArrayEquals(new float[] { 28, 22, 28, 22 }, LabLayout.share(new float[] { 28, 22, 28, 22 },
				new float[] { 150, 22, 96, 22 }, new boolean[] { true, false, false, false }, 50), DELTA);
	}

	@Test
	void aPieceWithANarrowLeastShrinksBelowTheOthers() {
		// The second piece may not go under 80: the first takes what is left.
		final float[] widths = LabLayout.share(new float[] { 10, 80 }, new float[] { 200, 200 },
				new boolean[] { true, true }, 150);
		assertEquals(70.0f, widths[0], DELTA);
		assertEquals(80.0f, widths[1], DELTA);
		assertEquals(150.0f, sum(widths), DELTA);
	}

	@Test
	void aHiddenPieceTakesNoWidth() {
		// A hidden piece counts 0 and 0: the others share the row as if it were not there.
		final float[] widths = LabLayout.share(new float[] { 28, 0, 22 }, new float[] { 150, 0, 22 },
				new boolean[] { true, false, false }, 300);
		assertArrayEquals(new float[] { 278, 0, 22 }, widths, DELTA);
		final float[] narrow = LabLayout.share(new float[] { 28, 0, 22 }, new float[] { 150, 0, 22 },
				new boolean[] { true, false, false }, 100);
		assertArrayEquals(new float[] { 78, 0, 22 }, narrow, DELTA);
	}

	@Test
	void whenRoomIsShortThePiecesThatExpandGetNothingMore() {
		// Two buttons that expand, too long for the row: shortened to one same width, nothing added.
		final float[] widths = LabLayout.share(new float[] { 28, 28, 40 }, new float[] { 200, 160, 40 },
				new boolean[] { true, true, false }, 240);
		assertEquals(100.0f, widths[0], DELTA);
		assertEquals(100.0f, widths[1], DELTA);
		assertEquals(40.0f, widths[2], DELTA);
	}

	@Test
	void noWidthOrNoNumberGivesTheLeastWidths() {
		assertArrayEquals(new float[] { 28, 22 }, LabLayout.share(new float[] { 28, 22 }, new float[] { 150, 22 },
				new boolean[] { true, false }, Float.NaN), DELTA);
		assertArrayEquals(new float[] { 28, 22 }, LabLayout.share(new float[] { 28, 22 },
				new float[] { Float.NaN, 22 }, new boolean[] { false, false }, 100), DELTA);
		assertArrayEquals(new float[0], LabLayout.share(new float[0], new float[0], new boolean[0], 100), DELTA);
	}
}
