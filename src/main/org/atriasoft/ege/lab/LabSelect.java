package org.atriasoft.ege.lab;

import org.atriasoft.etk.math.Vector2b;
import org.atriasoft.ewol.widget.Select;

/**
 * ewol's drop-down list whose item shown is a {@link LabLabel}: as wide as
 * the panel gives it, the item shortened with {@code ...} when it is longer,
 * never pushing the panel wider nor clipped. Its list opens as ewol's.
 */
final class LabSelect extends Select {

	/** The size of the font of ewol's drop-down lists. */
	private static final int FONT = 12;

	/** The item shown ({@code null} while ewol's constructor runs). */
	private final LabLabel shown;

	LabSelect() {
		final LabLabel label = new LabLabel("", FONT, false, true, 0.0f);
		label.setPropertyExpand(new Vector2b(true, false));
		label.setPropertyFill(Vector2b.TRUE);
		this.contentSizer.subWidgetReplace(this.displayLabel, label);
		this.shown = label;
		updateDisplayLabel();
	}

	@Override
	protected void updateDisplayLabel() {
		if (this.shown == null) {
			return;
		}
		final int index = getPropertySelectedIndex();
		this.shown.setText(index >= 0 && index < this.items.size() ? this.items.get(index) : "");
	}
}
