package org.atriasoft.ege.lab;

/**
 * How a row of the control panel shares its width ({@link LabRow}): every
 * piece first gets the width it wants (its title whole, its buttons); when
 * that is more than the row has, the widest pieces that can shrink (their
 * text shortened with {@code ...}) give way first, each down to its own
 * least width at most; when there is width left, the pieces that expand share
 * it. Pure Java.
 */
final class LabLayout {

	private LabLayout() {}

	/**
	 * The widths of the pieces of a row {@code room} pixels wide.
	 *
	 * @param least   the narrowest each piece may be (its whole width for a piece that never shrinks)
	 * @param natural the width each piece wants (never less than its least)
	 * @param expand  whether each piece takes a share of the width left over
	 * @param room    the width of the row
	 * @return the width of each piece: their wishes when they fit (the rest shared by those that expand), else
	 *         the widest ones shrunk to one same width, never under their least (the row overflows only when
	 *         even the least widths do not fit)
	 */
	static float[] share(final float[] least, final float[] natural, final boolean[] expand, final float room) {
		final int count = least.length;
		final float[] widths = new float[count];
		final float width = room > 0.0f ? room : 0.0f;
		float wanted = 0.0f;
		float floor = 0.0f;
		float widest = 0.0f;
		int expanding = 0;
		for (int i = 0; i < count; i++) {
			final float wish = wish(least[i], natural[i]);
			wanted += wish;
			floor += low(least[i]);
			widest = Math.max(widest, wish);
			if (expand[i]) {
				expanding++;
			}
		}
		if (wanted <= width) {
			final float extra = expanding > 0 ? (width - wanted) / expanding : 0.0f;
			for (int i = 0; i < count; i++) {
				widths[i] = wish(least[i], natural[i]) + (expand[i] ? extra : 0.0f);
			}
			return widths;
		}
		if (floor >= width) {
			for (int i = 0; i < count; i++) {
				widths[i] = low(least[i]);
			}
			return widths;
		}
		// The one width the widest pieces shrink to: what they take grows with it, so a bisection finds it.
		float below = 0.0f;
		float above = widest;
		for (int step = 0; step < 50; step++) {
			final float cap = (below + above) * 0.5f;
			if (taken(least, natural, cap) > width) {
				above = cap;
			} else {
				below = cap;
			}
		}
		for (int i = 0; i < count; i++) {
			widths[i] = capped(least[i], natural[i], below);
		}
		return widths;
	}

	/** The width all the pieces take when none is wider than {@code cap} but for its least. */
	private static float taken(final float[] least, final float[] natural, final float cap) {
		float sum = 0.0f;
		for (int i = 0; i < least.length; i++) {
			sum += capped(least[i], natural[i], cap);
		}
		return sum;
	}

	private static float capped(final float least, final float natural, final float cap) {
		return Math.max(low(least), Math.min(wish(least, natural), cap));
	}

	/** The least width of a piece: 0 for none (or no number). */
	private static float low(final float least) {
		return least > 0.0f ? least : 0.0f;
	}

	/** The width a piece wants: its least for none (or no number). */
	private static float wish(final float least, final float natural) {
		final float low = low(least);
		return natural > low ? natural : low;
	}
}
