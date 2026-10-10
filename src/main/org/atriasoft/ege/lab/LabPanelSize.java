package org.atriasoft.ege.lab;

/**
 * The width of the control panel of a lab, which the user drags with the
 * splitter on its left edge: the width chosen, in pixels, kept as it is when
 * the window changes size (an absolute width: the controls need so many
 * pixels whatever the window), and the width shown, the one chosen held
 * between {@link #MIN} (the controls stay usable) and {@link #widest} (60 % of
 * the window, and never more than leaves the 3D view its own minimum). A drag
 * moves it once the pointer went {@link #DEAD_ZONE} pixels from the press (a
 * click, a double click that shakes, never changes it), and tells at its end
 * whether the width chosen changed (to remember it). A double click on the
 * splitter goes back to {@link #DEFAULT}. Pure Java.
 */
final class LabPanelSize {

	/** The width of a lab that never chose one, pixels. */
	static final float DEFAULT = 330.0f;
	/** The narrowest panel, pixels: every control keeps a few letters of its title and its buttons. */
	static final float MIN = 240.0f;
	/** The widest panel, as a share of the window. */
	static final float MAX_SHARE = 0.6f;
	/** The width of the splitter between the view and the panel, pixels. */
	static final float SPLITTER = 8.0f;
	/** The narrowest 3D view, pixels. */
	static final float VIEW_MIN = 320.0f;
	/** How far the pointer goes from the press before the splitter moves, pixels. */
	static final float DEAD_ZONE = 3.0f;

	private float chosen;
	/** The drag: whether one goes on, whether it moved yet, its start (width shown, pointer), the width chosen then. */
	private boolean dragging;
	private boolean moved;
	private float startWidth;
	private float startX;
	private float before;

	/** @param chosen the width chosen before (read from the settings), {@link #DEFAULT} when it is no width */
	LabPanelSize(final float chosen) {
		this.chosen = valid(chosen) ? chosen : DEFAULT;
	}

	/** Whether {@code width} may be a width chosen (a settings file may hold anything). */
	static boolean valid(final float width) {
		return width >= MIN && width <= 10000.0f;
	}

	/**
	 * The widest panel in a window {@code window} pixels wide: 60 % of it, the view kept at its minimum, at least
	 * {@link #MIN}.
	 */
	static float widest(final float window) {
		if (!(window > 0.0f)) {
			return MIN;
		}
		return Math.max(MIN, (float) Math.floor(Math.min(MAX_SHARE * window, window - VIEW_MIN - SPLITTER)));
	}

	/** {@code wanted} held between the bounds of a window {@code window} pixels wide, whole pixels. */
	static float clamp(final float wanted, final float window) {
		if (Float.isNaN(wanted)) {
			return Math.min(DEFAULT, widest(window));
		}
		return Math.round(Math.max(MIN, Math.min(wanted, widest(window))));
	}

	/** The width chosen (the one remembered). */
	float chosen() {
		return this.chosen;
	}

	/** The width shown in a window {@code window} pixels wide. */
	float shown(final float window) {
		return clamp(this.chosen, window);
	}

	/**
	 * The splitter pressed at {@code x} (pixels from the left of a window {@code window} pixels wide): a drag starts
	 * from the width shown.
	 */
	void startDrag(final float x, final float window) {
		this.dragging = true;
		this.moved = false;
		this.startWidth = shown(window);
		this.startX = x;
		this.before = this.chosen;
	}

	/**
	 * The splitter dragged to {@code x}: the panel grows as it goes left, within the bounds, once it went
	 * {@link #DEAD_ZONE} pixels from the press.
	 *
	 * @return whether the width chosen changed (the window to lay out again)
	 */
	boolean dragTo(final float x, final float window) {
		if (!this.dragging || !this.moved && !(Math.abs(x - this.startX) >= DEAD_ZONE)) {
			return false;
		}
		this.moved = true;
		final float previous = this.chosen;
		this.chosen = clamp(this.startWidth + this.startX - x, window);
		return this.chosen != previous;
	}

	/**
	 * The drag ends (the splitter released).
	 *
	 * @return whether it changed the width chosen (to remember it)
	 */
	boolean endDrag() {
		final boolean changed = this.dragging && this.moved && this.chosen != this.before;
		this.dragging = false;
		this.moved = false;
		return changed;
	}

	/** Back to {@link #DEFAULT} (a double click on the splitter). */
	void reset() {
		this.chosen = DEFAULT;
	}
}
