package org.atriasoft.ege.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.atriasoft.gale.key.KeyKeyboard;
import org.junit.jupiter.api.Test;

/**
 * The registry of the actions of a lab: keys, refused keys, auto-repeat,
 * cycling, problems told and cleared, help, and a control for each action.
 */
class LabControlsTest {

	/** A small lab: one of each kind. */
	private static final class Model {
		private final List<String> species = new ArrayList<>(List.of("oak", "fir", "birch"));
		private int chosen;
		private long seed = 1;
		private boolean proxies;
		private int rebuilt;
	}

	/** The problems reported, by name. */
	private static final class Problems implements LabReporter {
		private final Map<String, String> shown = new LinkedHashMap<>();

		@Override
		public void report(final String what, final Throwable error) {
			this.shown.put(what, error.getMessage());
		}

		@Override
		public void report(final String what, final String message) {
			this.shown.put(what, message);
		}

		@Override
		public void clear(final String what) {
			this.shown.remove(what);
		}
	}

	/** The pieces of a panel as strings. */
	private static final class Strings implements LabControlPanel.Factory<String> {
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

		@Override
		public String text(final LabControl.Text control) {
			return "field " + control.title();
		}

		@Override
		public String palette(final LabControl.Palette control) {
			final List<String> items = new ArrayList<>();
			for (final LabControl.Palette.Item item : control.items()) {
				items.add(item.title());
			}
			return "palette " + control.title() + ": " + String.join(", ", items);
		}
	}

	private static LabControls controls(final Model model) {
		return controls(model, new Problems());
	}

	private static LabControls controls(final Model model, final Problems problems) {
		final LabControls controls = new LabControls();
		controls.setReporter(problems);
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
		assertTrue(controls.press(KeyKeyboard.CHARACTER, 'p', false));
		assertEquals(1, model.chosen);
		// Upper case too (Shift).
		assertTrue(controls.press(KeyKeyboard.CHARACTER, 'P', false));
		assertEquals(2, model.chosen);
		// Round the list.
		controls.press(KeyKeyboard.CHARACTER, 'p', false);
		assertEquals(0, model.chosen);
		controls.press(KeyKeyboard.CHARACTER, 'o', false);
		assertEquals(2, model.chosen);
		controls.press(KeyKeyboard.CHARACTER, 'i', false);
		assertEquals(2, model.seed);
		controls.press(KeyKeyboard.CHARACTER, 'u', false);
		controls.press(KeyKeyboard.CHARACTER, 'u', false);
		assertEquals(0, model.seed);
		controls.press(KeyKeyboard.CHARACTER, 'h', false);
		assertTrue(model.proxies);
		controls.press(KeyKeyboard.CHARACTER, 'h', false);
		assertFalse(model.proxies);
		assertTrue(controls.press(KeyKeyboard.F5, null, false));
		assertEquals(1, model.rebuilt);
		assertFalse(controls.press(KeyKeyboard.CHARACTER, 'z', false));
		assertFalse(controls.press(KeyKeyboard.F6, null, false));
	}

	@Test
	void aKeyThatCannotBeBoundIsDroppedAndToldTheControlKept() {
		final Model model = new Model();
		final Problems problems = new Problems();
		final LabControls controls = new LabControls();
		controls.setReporter(problems);
		// As the view does before the lab declares its controls.
		LabView.reserveKitKeys(controls);
		controls.toggle("Proxies", LabKey.of('h'), () -> model.proxies, value -> model.proxies = value);
		controls.action("Again", LabKey.of('H'), () -> model.rebuilt += 10);
		controls.toggle("Framing", LabKey.of('f'), () -> false, value -> {});
		controls.stepper("Zoom", LabKey.of(KeyKeyboard.PAGE_DOWN), LabKey.of('z'), () -> "1", () -> {}, () -> {});
		controls.action("Close", LabKey.of('\u001b'), () -> {});
		controls.action("Next tab", LabKey.of('\t'), () -> {});
		controls.action("Inspect", LabKey.of(KeyKeyboard.F12), () -> {});
		controls.action("Help", LabKey.of(KeyKeyboard.F1), () -> {});
		assertTrue(problems.shown.get("Key H of 'Again'").contains("already runs 'Proxies'"), problems.shown.toString());
		assertTrue(problems.shown.get("Key F of 'Framing'").contains("the kit keeps it for 'Frame the model'"));
		assertTrue(problems.shown.get("Key Page down of 'Zoom'").contains("camera"));
		assertTrue(problems.shown.get("Key Esc of 'Close'").contains("drop-down"));
		assertTrue(problems.shown.get("Key Tab of 'Next tab'").contains("never reaches"));
		assertTrue(problems.shown.get("Key F12 of 'Inspect'").contains("inspector"));
		assertTrue(problems.shown.get("Key F1 of 'Help'").contains("Key help"));
		assertEquals(7, problems.shown.size());
		// Kept without the key, still clickable; the other key of the stepper kept.
		final List<LabControl> all = controls.all();
		assertEquals(8, all.size());
		assertEquals("Again", all.get(1).title());
		assertEquals("Framing", all.get(2).title());
		assertEquals("Zoom [Z]", all.get(3).title());
		controls.press(KeyKeyboard.CHARACTER, 'h', false);
		assertTrue(model.proxies, "H still runs the proxies");
		assertEquals(0, model.rebuilt);
		controls.activate((LabControl.Action) all.get(1));
		assertEquals(10, model.rebuilt);
		// The kit takes its own keys.
		controls.declareAsKit(() -> controls.action("Frame the model", LabKey.of('f'), () -> model.rebuilt++));
		assertTrue(controls.press(KeyKeyboard.CHARACTER, 'F', false));
		assertEquals(11, model.rebuilt);
		assertEquals(7, problems.shown.size());
		assertNull(LabControls.refusal(LabKey.of('q')));
	}

	@Test
	void theAutoRepeatOfAHeldKeyStepsButNeverRepeatsAnActionOrAToggle() {
		final Model model = new Model();
		final LabControls controls = controls(model);
		for (int i = 0; i < 5; i++) {
			assertTrue(controls.press(KeyKeyboard.CHARACTER, 'i', true));
			assertTrue(controls.press(KeyKeyboard.CHARACTER, 'p', true));
			assertTrue(controls.press(KeyKeyboard.CHARACTER, 'h', true));
			assertTrue(controls.press(KeyKeyboard.F5, null, true));
		}
		assertEquals(6, model.seed);
		assertEquals(2, model.chosen);
		assertFalse(model.proxies);
		assertEquals(0, model.rebuilt);
	}

	@Test
	void aProblemIsClearedOnceTheSameThingSucceeds() {
		final Problems problems = new Problems();
		final LabControls controls = controls(new Model(), problems);
		final boolean[] fail = { true };
		controls.action("Flaky", LabKey.of('y'), () -> {
			if (fail[0]) {
				throw new IllegalStateException("boom");
			}
		});
		controls.toggle("Broken state", LabKey.of('w'), () -> {
			if (fail[0]) {
				throw new IllegalStateException("no state");
			}
			return true;
		}, value -> {});
		final LabControl.Toggle toggle = (LabControl.Toggle) controls.all().get(5);
		controls.press(KeyKeyboard.CHARACTER, 'y', false);
		assertFalse(controls.value(toggle));
		assertEquals("boom", problems.shown.get("Flaky"));
		assertEquals("no state", problems.shown.get("Broken state" + LabControls.STATE));
		fail[0] = false;
		controls.press(KeyKeyboard.CHARACTER, 'y', false);
		assertTrue(controls.value(toggle));
		assertTrue(problems.shown.isEmpty(), problems.shown.toString());
	}

	@Test
	void theStateOfABrokenSupplierFallsBack() {
		final Problems problems = new Problems();
		final LabControls controls = new LabControls();
		controls.setReporter(problems);
		controls.choice("List", null, null, () -> {
			throw new IllegalStateException("no items");
		}, () -> {
			throw new IllegalStateException("no index");
		}, index -> {});
		controls.stepper("Number", null, null, () -> {
			throw new IllegalStateException("no value");
		}, () -> {}, () -> {});
		final LabControl.Choice choice = (LabControl.Choice) controls.all().get(0);
		final LabControl.Stepper stepper = (LabControl.Stepper) controls.all().get(1);
		assertEquals(List.of(), controls.items(choice));
		assertEquals(-1, controls.selected(choice));
		assertEquals("?", controls.text(stepper));
		// Each supplier under its own name: one that works never clears another that fails.
		assertEquals(3, problems.shown.size());
		assertEquals("no items", problems.shown.get("List" + LabControls.ITEMS));
		assertEquals("no index", problems.shown.get("List" + LabControls.SELECTED));
		assertEquals("no value", problems.shown.get("Number" + LabControls.VALUE));
	}

	@Test
	void aThrowingCallbackGoesToTheReporter() {
		final LabControls controls = new LabControls();
		final Problems problems = new Problems();
		controls.setReporter(problems);
		controls.action("Explode", LabKey.of('x'), () -> {
			throw new IllegalStateException("boom");
		});
		assertTrue(controls.press(KeyKeyboard.CHARACTER, 'x', false));
		assertEquals(Map.of("Explode", "boom"), problems.shown);
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
		assertEquals("Esc", LabKey.of('\u001b').name());
		assertEquals("Page up", LabKey.of(KeyKeyboard.PAGE_UP).name());
	}

	@Test
	void everyActionHasItsControlUnderItsHeading() {
		final LabControls controls = controls(new Model());
		final List<String> pieces = LabControlPanel.layout(controls, new Strings());
		assertEquals(List.of("# Subject", "list Species [O/P]", "steps Seed [U/I]", "# Debug", "check Proxies [H]",
				"# Data", "button Build again [F5]"), pieces);
		// Each control once: the panel, like the help, is made from the registry alone.
		assertEquals(controls.all().size() + controls.byGroup().size(), pieces.size());
		// A group left out (the window lays the group of the view out in its footer).
		final List<String> withoutDebug = LabControlPanel.layout(controls, new Strings(), group -> !"Debug".equals(group));
		assertEquals(List.of("# Subject", "list Species [O/P]", "steps Seed [U/I]", "# Data", "button Build again [F5]"),
				withoutDebug);
	}

	@Test
	void theHelpListsEveryControl() {
		final List<String> help = controls(new Model()).help();
		assertEquals(List.of("Subject", "  [O/P]  Species", "  [U/I]  Seed", "Debug", "  [H]  Proxies", "Data",
				"  [F5]  Build again"), help);
	}

	@Test
	void keysWithControlRunTheirOwnControls() {
		final Problems problems = new Problems();
		final LabControls controls = new LabControls();
		controls.setReporter(problems);
		final List<String> ran = new ArrayList<>();
		controls.action("Zoom", LabKey.of('z'), () -> ran.add("zoom"));
		controls.action("Undo", LabKey.ctrl('Z'), () -> ran.add("undo"));
		assertEquals("Ctrl+Z", LabKey.ctrl('z').name());
		assertEquals("Undo [Ctrl+Z]", controls.all().get(1).title());
		assertTrue(controls.press(KeyKeyboard.CHARACTER, 'z', true, false));
		assertTrue(controls.press(KeyKeyboard.CHARACTER, 'Z', false, false));
		assertEquals(List.of("undo", "zoom"), ran);
		assertFalse(LabKey.ctrl('z').matches(KeyKeyboard.CHARACTER, 'z'), "without Control");
		assertFalse(controls.press(KeyKeyboard.CHARACTER, 'q', true, false));
		assertTrue(problems.shown.isEmpty(), problems.shown.toString());
	}

	@Test
	void aPaletteChoosesAnItemByItsKeyAndATextTakesWhatIsTyped() {
		final Problems problems = new Problems();
		final LabControls controls = new LabControls();
		controls.setReporter(problems);
		final int[] chosen = { -1 };
		final String[] name = { "keep" };
		controls.group("Edit");
		controls.action("Help", LabKey.of('#'), () -> {});
		controls.palette("Paint", List.of(new LabControl.Palette.Item("Wall", LabKey.of('#')),
				new LabControl.Palette.Item("Door", LabKey.of('+')), new LabControl.Palette.Item("Outside", null)),
				() -> chosen[0], index -> chosen[0] = index);
		controls.text("Name", () -> name[0], typed -> name[0] = typed);
		// The key of an item already bound is dropped from that item alone.
		assertTrue(problems.shown.containsKey("Key # of 'Paint'"), problems.shown.toString());
		final LabControl.Palette palette = (LabControl.Palette) controls.all().get(1);
		assertEquals(List.of(LabKey.of('+')), palette.keys());
		assertEquals("Paint", palette.title());
		assertEquals("Wall", palette.items().get(0).title());
		assertEquals("Door [+]", palette.items().get(1).title());
		assertTrue(controls.press(KeyKeyboard.CHARACTER, '+', false, false));
		assertEquals(1, chosen[0]);
		assertEquals(1, controls.selected(palette));
		assertTrue(controls.press(KeyKeyboard.CHARACTER, '+', false, true), "bound, its repeat dropped");
		controls.select(palette, 2);
		assertEquals(2, chosen[0]);
		final LabControl.Text text = (LabControl.Text) controls.all().get(2);
		assertEquals(List.of(), text.keys());
		assertEquals("keep", controls.text(text));
		controls.enter(text, "tower");
		assertEquals("tower", name[0]);
		assertEquals(List.of("# Edit", "button Help [#]", "palette Paint: Wall, Door [+], Outside", "field Name"),
				LabControlPanel.layout(controls, new Strings()));
		assertEquals(List.of("Edit", "  [#]  Help", "  [-]  Paint: Wall", "  [+]  Paint: Door", "  [-]  Paint: Outside",
				"  [Enter]  Name (a text field)"), controls.help());
	}

	@Test
	void theReservedKeysAreRefusedWithControlToo() {
		final Problems problems = new Problems();
		final LabControls controls = new LabControls();
		controls.setReporter(problems);
		controls.action("Escape", LabKey.ctrl('\u001b'), () -> {});
		controls.action("Left", new LabKey(KeyKeyboard.LEFT, '\0', true), () -> {});
		controls.action("Inspector", new LabKey(KeyKeyboard.F12, '\0', true), () -> {});
		for (final LabControl control : controls.all()) {
			assertEquals(List.of(), control.keys(), control.label() + " keeps no key");
		}
		assertEquals(3, problems.shown.size(), problems.shown.toString());
		assertNull(LabControls.refusal(LabKey.ctrl('z')));
	}
}
