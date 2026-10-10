package org.atriasoft.ege.lab;

import java.util.function.Supplier;

import org.atriasoft.etk.math.Vector2b;
import org.atriasoft.ewol.Ewol;
import org.atriasoft.ewol.widget.Entry;
import org.atriasoft.ewol.widget.Sizer;
import org.atriasoft.ewol.widget.Widget;
import org.atriasoft.ewol.widget.Windows;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeySpecial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The window of a lab: the 3D view ({@link LabView}) and the control panel
 * docked on its right ({@link LabPanelWidgets}), side by side, so the clicks
 * on the panel go to its widgets and the drags over the view to the camera.
 * <p>
 * Every key goes through the window first, whatever widget has the focus:
 * the arrows and Page up/down drive the camera, a key of a control runs it
 * (with Control or not as the key says; never with the left Alt nor Meta;
 * AltGr, which types the symbols of a keyboard, counts as no key held; the
 * auto-repeat of a held key only steps the steppers and choices,
 * {@link LabKeyRepeat}), any other key goes on to the focused widget. While a
 * drop-down list is open it has the keys, and while a text field has the
 * focus every key is its own but those with Control (a field types nothing
 * with them: Ctrl+S still saves), Escape giving the focus back to the 3D view
 * (a release still lets a camera key go).
 * <p>
 * Nothing a lab does while the window opens kills it: a lab that cannot be
 * made, a {@link Lab#start} that throws, a control the kit cannot add, a
 * first state that cannot be read are all reported in the info panel.
 */
final class LabWindow extends Windows {

	private static final Logger LOGGER = LoggerFactory.getLogger(LabWindow.class);

	/** What runs when the lab could not be made: an empty view and its problem. */
	private static final class NoLab implements Lab {
		@Override
		public String title() {
			return "Lab";
		}

		@Override
		public void start(final LabView view) {
			// Nothing to declare.
		}

		@Override
		public void update(final LabView view, final float seconds) {
			// Nothing to show.
		}

		@Override
		public void close() {
			// Nothing to stop.
		}
	}

	private final Lab lab;
	private final LabView view = new LabView();
	private final LabPanelWidgets panel;
	private final LabKeyRepeat repeats = new LabKeyRepeat();
	private boolean closed;

	LabWindow(final Supplier<Lab> factory) {
		this.view.setPropertyExpand(Vector2b.TRUE);
		this.view.setPropertyFill(Vector2b.TRUE);
		Lab made = null;
		try {
			made = factory.get();
		} catch (final Throwable e) {
			this.view.report("opening the lab", e);
		}
		this.lab = made != null ? made : new NoLab();
		setPropertyTitle(LabText.ascii(title(this.lab)));
		try {
			this.lab.start(this.view);
		} catch (final Throwable e) {
			this.view.report("start", e);
		}
		try {
			this.view.addViewControls();
		} catch (final Throwable e) {
			this.view.report("the controls of the view", e);
		}
		this.view.attach(this.lab);
		this.panel = new LabPanelWidgets(this.view.controls(), this.view::keepFocus);
		try {
			this.panel.sync();
		} catch (final Throwable e) {
			this.view.report(LabView.PANEL, e);
		}
		this.view.setAfterUpdate(this.panel::sync);
		final Sizer row = new Sizer(Sizer.DisplayMode.HORIZONTAL);
		row.setPropertyExpand(Vector2b.TRUE);
		row.setPropertyFill(Vector2b.TRUE);
		row.subWidgetAdd(this.view);
		row.subWidgetAdd(this.panel.widget());
		setSubWidget(row);
	}

	private String title(final Lab made) {
		try {
			return made.title();
		} catch (final Throwable e) {
			this.view.report("title", e);
			return "Lab";
		}
	}

	LabView view() {
		return this.view;
	}

	@Override
	public boolean onEventShortCut(final KeySpecial special, final Character value, final KeyKeyboard type,
			final boolean isDown) {
		final LabKeyRepeat.Press press = this.repeats.onKey(type, value, isDown, System.nanoTime() * 1.0e-9);
		if (!isDown) {
			// A release always lets a camera key go, a list open or not.
			this.view.holdKey(type, false);
		}
		// AltGr types the symbols of a keyboard (# | @ on a French one): no key held. Windows hands it with Control.
		final boolean altGr = special != null && special.getAltGr();
		final boolean ctrl = special != null && special.getCtrl() && !altGr;
		final boolean other = special != null && (special.getAltLeft() || special.getMeta());
		final boolean repeat = press == LabKeyRepeat.Press.REPEAT;
		if (typing()) {
			// A text field has the focus: the keys are its own, but the controls with Control (the field types nothing
			// with Control: Ctrl+S saves); Escape gives the focus back to the view.
			if (isDown && type == KeyKeyboard.CHARACTER && value != null && value == '\u001b') {
				this.view.keepFocus();
				return true;
			}
			return ctrl && !other && isDown && this.view.controls().press(type, value, true, repeat);
		}
		if (popUpCount() == 0) {
			if (LabView.isCameraKey(type)) {
				this.view.holdKey(type, isDown);
				return true;
			}
			if (!other && isDown && this.view.controls().press(type, value, ctrl, repeat)) {
				return true;
			}
		}
		if (value == null) {
			// Widget.onEventShortCut takes the character for granted.
			return false;
		}
		return super.onEventShortCut(special, value, type, isDown);
	}

	/** Whether a text field has the focus (the keys go to it). */
	static boolean typing() {
		final Widget focused = Ewol.getContext().getWidgetManager().focusGet();
		return focused instanceof Entry && focused.isFocused();
	}

	/** Stop the lab and give the view back. Idempotent; whatever the lab throws is logged. */
	void close() {
		if (this.closed) {
			return;
		}
		this.closed = true;
		try {
			this.lab.close();
		} catch (final Throwable e) {
			LOGGER.error("The lab did not close cleanly: {}", e.toString(), e);
		} finally {
			this.view.release();
		}
	}
}
