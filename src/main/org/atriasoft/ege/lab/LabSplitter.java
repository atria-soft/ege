package org.atriasoft.ege.lab;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.compositing.CompositingGC;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.widget.Widget;
import org.atriasoft.gale.key.KeyStatus;

/**
 * The thin bar between the 3D view and the control panel of a lab: dragged
 * with the left button, it moves the edge of the panel ({@link LabSplit});
 * a double click puts the panel back at its default width. Every event that
 * reaches it is its own: a drag that starts on it stays on it to the release
 * (ewol gives the moves and the release to the widget pressed), so it never
 * turns the camera nor clicks a widget. gale cannot change the shape of the
 * pointer: the bar shows a grip, lit while the pointer is over it, brighter
 * while it is dragged.
 */
final class LabSplitter extends Widget {

	private static final Color BAR = new Color(0.80f, 0.80f, 0.82f, 1.0f);
	private static final Color OVER = new Color(0.62f, 0.72f, 0.88f, 1.0f);
	private static final Color DRAGGED = new Color(0.36f, 0.56f, 0.88f, 1.0f);
	private static final Color GRIP = new Color(0.30f, 0.30f, 0.34f, 1.0f);
	/** The grip: dots of so many pixels, so many of them, every so many pixels, in the middle of the bar. */
	private static final float DOT = 2.0f;
	private static final int DOTS = 7;
	private static final float DOT_STEP = 5.0f;

	private final LabSplit split;
	private final CompositingGC drawing = new CompositingGC();
	private boolean over;
	private boolean dragging;

	LabSplitter(final LabSplit split) {
		this.split = split;
		// A double click is told (pressDouble).
		setMouseLimit(2);
		setPropertyCanFocus(false);
	}

	@Override
	public void calculateMinMaxSize() {
		super.calculateMinMaxSize();
		this.minSize = new Vector2f(LabPanelSize.SPLITTER, 0.0f);
	}

	@Override
	protected void onDraw() {
		this.drawing.draw();
	}

	@Override
	public void onRegenerateDisplay() {
		if (!needRedraw()) {
			return;
		}
		this.drawing.clear();
		this.drawing.setColor(this.dragging ? DRAGGED : this.over ? OVER : BAR);
		this.drawing.setPos(0.0f, 0.0f);
		this.drawing.rectangleWidth(this.size);
		this.drawing.setColor(GRIP);
		// A line down the middle, and a grip of two columns of dots half way up.
		final float middle = (float) Math.floor(this.size.x() * 0.5f);
		this.drawing.setPos(middle - 0.5f, 0.0f);
		this.drawing.rectangleWidth(new Vector2f(1.0f, this.size.y()));
		// The highest dot: the column of dots centred on the middle of the bar.
		final float top = (float) Math.floor(this.size.y() * 0.5f + (DOTS - 1) * DOT_STEP * 0.5f - DOT * 0.5f);
		for (int i = 0; i < DOTS; i++) {
			final float y = top - i * DOT_STEP;
			this.drawing.setPos(middle - 1.0f - DOT - 1.0f, y);
			this.drawing.rectangleWidth(new Vector2f(DOT, DOT));
			this.drawing.setPos(middle + 1.0f + 1.0f, y);
			this.drawing.rectangleWidth(new Vector2f(DOT, DOT));
		}
		this.drawing.flush();
	}

	@Override
	protected boolean onEventInput(final EventInput event) {
		final int button = event.inputId();
		if (button == 0) {
			final boolean next = event.status() != KeyStatus.leave;
			if (next != this.over) {
				this.over = next;
				markToRedraw();
			}
			return true;
		}
		if (button != 1) {
			// The other buttons and the wheel over the bar: nothing, and nobody else.
			return true;
		}
		switch (event.status()) {
			case down -> {
				this.dragging = true;
				this.split.dragStarts(event.pos().x());
				// No leave nor enter while it is dragged across the view or the panel.
				grabEvents();
				markToRedraw();
			}
			case move -> {
				if (this.dragging) {
					this.split.dragTo(event.pos().x());
				}
			}
			case up, abort -> {
				if (this.dragging) {
					this.dragging = false;
					// Lit until the pointer moves off it (ewol then tells it leave).
					this.over = true;
					this.split.dropped();
					markToRedraw();
				}
			}
			case pressDouble -> this.split.resetWidth();
			default -> {
				// Single clicks, enter, leave: nothing.
			}
		}
		return true;
	}
}
