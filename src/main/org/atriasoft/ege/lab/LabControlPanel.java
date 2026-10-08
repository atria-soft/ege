package org.atriasoft.ege.lab;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The layout of the control panel of a lab, apart from its widgets: a
 * heading per group of {@link LabControls}, then one control per action, of
 * its kind. {@link LabPanelWidgets} makes them ewol widgets; a test makes
 * them strings, to check that every action has its control. Pure Java.
 */
public final class LabControlPanel {

	/**
	 * Makes the pieces of the panel.
	 *
	 * @param <W> what a piece is (a widget)
	 */
	public interface Factory<W> {
		/** The heading of a group. */
		W heading(String text);

		/** A button. */
		W action(LabControl.Action control);

		/** A check box. */
		W toggle(LabControl.Toggle control);

		/** A drop-down list with previous and next arrows. */
		W choice(LabControl.Choice control);

		/** Two buttons around a value. */
		W stepper(LabControl.Stepper control);
	}

	private LabControlPanel() {}

	/** The pieces of the panel of {@code controls}, from the top: each heading followed by its controls. */
	public static <W> List<W> layout(final LabControls controls, final Factory<W> factory) {
		final List<W> pieces = new ArrayList<>();
		for (final Map.Entry<String, List<LabControl>> entry : controls.byGroup().entrySet()) {
			pieces.add(factory.heading(entry.getKey()));
			for (final LabControl control : entry.getValue()) {
				pieces.add(switch (control) {
					case final LabControl.Action action -> factory.action(action);
					case final LabControl.Toggle toggle -> factory.toggle(toggle);
					case final LabControl.Choice choice -> factory.choice(choice);
					case final LabControl.Stepper stepper -> factory.stepper(stepper);
				});
			}
		}
		return pieces;
	}
}
