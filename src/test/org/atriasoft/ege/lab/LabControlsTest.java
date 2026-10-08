package org.atriasoft.ege.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.gale.key.KeyKeyboard;
import org.junit.jupiter.api.Test;

/** The registry of the actions of a lab: keys, cycling, errors, help, and a control for each action. */
class LabControlsTest {

	/** A small lab: one of each kind. */
	private static final class Model {
		private final List<String> species = new ArrayList<>(List.of("oak", "fir", "birch"));
		private int chosen;
		private long seed = 1;
		private boolean proxies;
		private int rebuilt;
	}

	private static LabControls controls(final Model model) {
		final LabControls controls = new LabControls();
		controls.group("Subject");
		controls.choice("Species", LabKey.of('o'), LabKey.of('p'), () -> model.species, () -> model.chosen,
				index -> model.chosen = index);
		controls.stepper("Seed", LabKey.of('u'), LabKey.of('i'), () -> Long.toString(model.seed),
				() -> model.seed = Math.max(0, model.seed - 1), () -> model.seed++);
		controls.group("Debug");
		controls.toggle("Proxies", LabKey.of('h'), () -> model.proxies, value -> model.proxies = value);
		controls.group("Data");
		controls.action("Build again", LabKey.of(KeyKeyboard.F5), () -> model.rebuilt++);
		return controls;
	}

	@Test
	void keysRunTheirControls() {
		final Model model = new Model();
		final LabControls controls = controls(model);
		assertTrue(controls.press(KeyKeyboard.CHARACTER, 'p'));
		assertEquals(1, model.chosen);
		// Upper case too (Shift).
		assertTrue(controls.press(KeyKeyboard.CHARACTER, 'P'));
		assertEquals(2, model.chosen);
		// Round the list.
		controls.press(KeyKeyboard.CHARACTER, 'p');
		assertEquals(0, model.chosen);
		controls.press(KeyKeyboard.CHARACTER, 'o');
		assertEquals(2, model.chosen);
		controls.press(KeyKeyboard.CHARACTER, 'i');
		assertEquals(2, model.seed);
		controls.press(KeyKeyboard.CHARACTER, 'u');
		controls.press(KeyKeyboard.CHARACTER, 'u');
		assertEquals(0, model.seed);
		controls.press(KeyKeyboard.CHARACTER, 'h');
		assertTrue(model.proxies);
		controls.press(KeyKeyboard.CHARACTER, 'h');
		assertFalse(model.proxies);
		assertTrue(controls.press(KeyKeyboard.F5, null));
		assertEquals(1, model.rebuilt);
		assertFalse(controls.press(KeyKeyboard.CHARACTER, 'z'));
		assertFalse(controls.press(KeyKeyboard.F6, null));
	}

	@Test
	void aKeyBoundTwiceIsRefused() {
		final LabControls controls = controls(new Model());
		final IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> controls.action("Again", LabKey.of('H'), () -> {}));
		assertTrue(e.getMessage().contains("Proxies"), e.getMessage());
	}

	@Test
	void aThrowingCallbackGoesToTheHandler() {
		final LabControls controls = new LabControls();
		final List<String> told = new ArrayList<>();
		controls.onError((what, error) -> told.add(what + ": " + error.getMessage()));
		controls.action("Explode", LabKey.of('x'), () -> {
			throw new IllegalStateException("boom");
		});
		assertTrue(controls.press(KeyKeyboard.CHARACTER, 'x'));
		assertEquals(List.of("Explode: boom"), told);
	}

	@Test
	void stepsRoundAListAndStartFromAnEnd() {
		assertEquals(0, LabControls.stepIndex(-1, 1, 3));
		assertEquals(2, LabControls.stepIndex(-1, -1, 3));
		assertEquals(0, LabControls.stepIndex(2, 1, 3));
		assertEquals(2, LabControls.stepIndex(0, -1, 3));
		assertEquals(0, LabControls.stepIndex(5, 1, 3));
	}

	@Test
	void titlesShowTheKeys() {
		final List<LabControl> all = controls(new Model()).all();
		assertEquals("Species [O/P]", all.get(0).title());
		assertEquals("Seed [U/I]", all.get(1).title());
		assertEquals("Proxies [H]", all.get(2).title());
		assertEquals("Build again [F5]", all.get(3).title());
		assertEquals("Tab", LabKey.of('\t').name());
		assertEquals("Esc", LabKey.ESCAPE.name());
		assertEquals("Page up", LabKey.of(KeyKeyboard.PAGE_UP).name());
	}

	@Test
	void everyActionHasItsControlUnderItsHeading() {
		final LabControls controls = controls(new Model());
		final List<String> pieces = LabControlPanel.layout(controls, new LabControlPanel.Factory<String>() {
			@Override
			public String heading(final String text) {
				return "# " + text;
			}

			@Override
			public String action(final LabControl.Action control) {
				return "button " + control.title();
			}

			@Override
			public String toggle(final LabControl.Toggle control) {
				return "check " + control.title();
			}

			@Override
			public String choice(final LabControl.Choice control) {
				return "list " + control.title();
			}

			@Override
			public String stepper(final LabControl.Stepper control) {
				return "steps " + control.title();
			}
		});
		assertEquals(List.of("# Subject", "list Species [O/P]", "steps Seed [U/I]", "# Debug", "check Proxies [H]",
				"# Data", "button Build again [F5]"), pieces);
		// Each control once: the panel, like the help, is made from the registry alone.
		assertEquals(controls.all().size() + controls.byGroup().size(), pieces.size());
	}

	@Test
	void theHelpListsEveryControl() {
		final List<String> help = controls(new Model()).help();
		assertEquals(List.of("Subject", "  [O/P]  Species", "  [U/I]  Seed", "Debug", "  [H]  Proxies", "Data",
				"  [F5]  Build again"), help);
	}
}
