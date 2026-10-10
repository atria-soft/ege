package org.atriasoft.ege.lab;

import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.widget.Entry;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeyStatus;

/**
 * ewol's text field that can show a text all selected, so that what is typed
 * replaces it (the value of a stepper made a text field), the arrows still
 * moving in it. Enter gives its whole text ({@code signalEnter}), a part of it
 * selected or not (ewol's field takes the selection away first).
 */
final class LabField extends Entry {

	/** Far beyond any field, pixels: the start and the end of its text. */
	private static final float FAR = 1.0e7f;

	/** Show {@code text} (in the letters the fonts draw) all selected. */
	void showSelected(final String text) {
		setPropertyValue(LabText.ascii(text));
		// The cursor before the first letter, then moved past the last one with the selection.
		updateCursorPosition(new Vector2f(-FAR, 0.0f), false);
		updateCursorPosition(new Vector2f(FAR, 0.0f), true);
	}

	@Override
	public boolean onEventEntry(final EventEntry event) {
		if (event.type() == KeyKeyboard.CHARACTER && event.status() == KeyStatus.down
				&& (event.getChar() == '\r' || event.getChar() == '\n')) {
			this.signalEnter.emit(getPropertyValue());
			return true;
		}
		return super.onEventEntry(event);
	}
}
