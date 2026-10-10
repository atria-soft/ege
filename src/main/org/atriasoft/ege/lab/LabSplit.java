package org.atriasoft.ege.lab;

import org.atriasoft.etk.math.Vector2b;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.widget.ContainerN;
import org.atriasoft.ewol.widget.Widget;
import org.atriasoft.gale.key.KeyStatus;

/**
 * The window of a lab laid out: the 3D view on the left, the splitter
 * ({@link LabSplitter}), the control panel on the right, as wide as the user
 * chose it ({@link LabPanelSize}: kept in pixels when the window changes
 * size, within its bounds). While the splitter is dragged the three are
 * placed again at most once a frame, without measuring the widgets again:
 * the view gets the rest of the window (its camera follows its new aspect),
 * the panel lays its rows out in its new width. The width is remembered when
 * the drag ends and when a double click puts the default back.
 */
final class LabSplit extends ContainerN {

	private final Widget view;
	private final LabSplitter splitter;
	private final Widget panel;
	private final LabPanelSize width;
	/** Remembers the width chosen (the settings of the lab). */
	private final Runnable remember;
	private boolean placeAgain;

	/**
	 * @param view     the 3D view (left)
	 * @param panel    the control panel (right)
	 * @param width    the width of the panel
	 * @param remember remembers {@code width.chosen()} (run when the user chose a width)
	 */
	LabSplit(final Widget view, final Widget panel, final LabPanelSize width, final Runnable remember) {
		this.view = view;
		this.panel = panel;
		this.width = width;
		this.remember = remember;
		this.splitter = new LabSplitter(this);
		setPropertyExpand(Vector2b.TRUE);
		setPropertyFill(Vector2b.TRUE);
		subWidgetAdd(view);
		subWidgetAdd(this.splitter);
		subWidgetAdd(panel);
	}

	/** The splitter is pressed at {@code x} (pixels from the left of the window): a drag starts. */
	void dragStarts(final float x) {
		this.width.startDrag(x, this.size.x());
	}

	/** The splitter dragged to {@code x}: placed again at the next frame when the width changed. */
	void dragTo(final float x) {
		if (this.width.dragTo(x, this.size.x())) {
			this.placeAgain = true;
			markToRedraw();
		}
	}

	/** The drag ended: the width remembered when it changed. */
	void dropped() {
		if (this.width.endDrag()) {
			this.remember.run();
		}
	}

	/** Back to the default width (a double click on the splitter), remembered. */
	void resetWidth() {
		this.width.reset();
		this.placeAgain = true;
		markToRedraw();
		this.remember.run();
	}

	@Override
	public void calculateMinMaxSize() {
		this.subExpend = Vector2b.FALSE;
		float height = 0.0f;
		for (final Widget piece : this.subWidget) {
			piece.calculateMinMaxSize();
			height = Math.max(height, piece.getCalculateMinSize().y());
		}
		this.minSize = new Vector2f(this.view.getCalculateMinSize().x() + LabPanelSize.SPLITTER + LabPanelSize.MIN,
				height);
		this.maxSize = Vector2f.MAX_VALUE;
		checkMinSize();
	}

	@Override
	public void onChangeSize() {
		markToRedraw();
		this.placeAgain = false;
		final float total = (float) Math.floor(this.size.x());
		final float panelWidth = Math.min(total, this.width.shown(total));
		final float viewWidth = Math.max(0.0f, total - panelWidth - LabPanelSize.SPLITTER);
		final Vector2f origin = this.origin.add(this.offset);
		place(this.view, origin, viewWidth);
		place(this.splitter, origin.add(viewWidth, 0.0f), LabPanelSize.SPLITTER);
		place(this.panel, origin.add(viewWidth + LabPanelSize.SPLITTER, 0.0f),
				Math.max(0.0f, total - viewWidth - LabPanelSize.SPLITTER));
	}

	private void place(final Widget piece, final Vector2f at, final float pieceWidth) {
		piece.setOrigin(Vector2f.clipInt(at));
		piece.setSize(new Vector2f((float) Math.floor(pieceWidth), (float) Math.floor(this.size.y())));
		piece.onChangeSize();
	}

	/**
	 * A press of a button anywhere in the window gives the focus back to the view first (a text field left open
	 * closes, the keys go to the lab again): the widget pressed then takes it if it wants it (a text field does, on
	 * the press). Never taken: the press goes on to the widget under the pointer.
	 */
	@Override
	protected boolean onEventInput(final EventInput event) {
		if (event.status() == KeyStatus.down && event.inputId() >= 1 && event.inputId() <= 3) {
			this.view.keepFocus();
		}
		return false;
	}

	/** Each frame: the three placed again when the splitter moved since, then drawn as they are. */
	@Override
	public void onRegenerateDisplay() {
		// Clears the flag, so that the next markToRedraw asks for a frame.
		needRedraw();
		if (this.placeAgain) {
			onChangeSize();
		}
		super.onRegenerateDisplay();
	}
}
