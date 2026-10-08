package org.atriasoft.ege.lab;

import org.atriasoft.etk.math.Vector2b;
import org.atriasoft.ewol.widget.Sizer;
import org.atriasoft.ewol.widget.Windows;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeySpecial;

/**
 * The window of a lab: the 3D view ({@link LabView}) and the control panel
 * docked on its right ({@link LabPanelWidgets}), side by side, so the clicks
 * on the panel go to its widgets and the drags over the view to the camera.
 * Every key goes through the window first, whatever widget has the focus: the
 * arrows and Page up/down drive the camera, a key of a control runs it
 * (without Control, Alt or Meta), any other key goes on to the focused widget.
 */
final class LabWindow extends Windows {

	private final Lab lab;
	private final LabView view = new LabView();
	private final LabPanelWidgets panel;
	private boolean closed;

	LabWindow(final Lab lab) {
		this.lab = lab;
		setPropertyTitle(LabText.ascii(lab.title()));
		this.view.setPropertyExpand(Vector2b.TRUE);
		this.view.setPropertyFill(Vector2b.TRUE);
		try {
			lab.start(this.view);
		} catch (final Throwable e) {
			this.view.report("start", e);
		}
		this.view.addViewControls();
		this.view.attach(lab);
		this.panel = new LabPanelWidgets(this.view.controls());
		this.panel.sync();
		this.view.setAfterUpdate(this.panel::sync);
		final Sizer row = new Sizer(Sizer.DisplayMode.HORIZONTAL);
		row.setPropertyExpand(Vector2b.TRUE);
		row.setPropertyFill(Vector2b.TRUE);
		row.subWidgetAdd(this.view);
		row.subWidgetAdd(this.panel.widget());
		setSubWidget(row);
	}

	LabView view() {
		return this.view;
	}

	@Override
	public boolean onEventShortCut(final KeySpecial special, final Character value, final KeyKeyboard type,
			final boolean isDown) {
		if (this.view.holdKey(type, isDown)) {
			return true;
		}
		final boolean plain = special == null || !special.getCtrl() && !special.getAlt() && !special.getMeta();
		if (plain && isDown && this.view.controls().press(type, value)) {
			return true;
		}
		if (value == null) {
			// Widget.onEventShortCut takes the character for granted.
			return false;
		}
		return super.onEventShortCut(special, value, type, isDown);
	}

	/** Stop the lab and give the view back. Idempotent. */
	void close() {
		if (this.closed) {
			return;
		}
		this.closed = true;
		try {
			this.lab.close();
		} finally {
			this.view.release();
		}
	}
}
