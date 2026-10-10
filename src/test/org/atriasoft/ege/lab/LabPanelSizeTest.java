package org.atriasoft.ege.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** The width of the control panel: its bounds, the drag of the splitter, the default on a double click. */
class LabPanelSizeTest {

	/** The window of a lab, pixels. */
	private static final float WINDOW = 1280.0f;

	@Test
	void theWidthStaysBetweenItsBounds() {
		// 60 % of the window at most.
		assertEquals(768.0f, LabPanelSize.widest(WINDOW));
		assertEquals(768.0f, LabPanelSize.clamp(2000.0f, WINDOW));
		assertEquals(LabPanelSize.MIN, LabPanelSize.clamp(10.0f, WINDOW));
		assertEquals(500.0f, LabPanelSize.clamp(500.0f, WINDOW));
		assertEquals(501.0f, LabPanelSize.clamp(500.6f, WINDOW), "whole pixels");
		// A narrow window keeps its view at its minimum, the panel never under its own.
		assertEquals(800.0f - LabPanelSize.VIEW_MIN - LabPanelSize.SPLITTER, LabPanelSize.widest(800.0f));
		assertEquals(LabPanelSize.MIN, LabPanelSize.widest(400.0f));
		assertEquals(LabPanelSize.MIN, LabPanelSize.widest(0.0f));
		assertEquals(LabPanelSize.MIN, LabPanelSize.widest(Float.NaN));
		assertEquals(LabPanelSize.DEFAULT, LabPanelSize.clamp(Float.NaN, WINDOW));
		assertEquals(768.0f, LabPanelSize.clamp(Float.POSITIVE_INFINITY, WINDOW));
	}

	@Test
	void theWidthChosenIsKeptInPixelsWhenTheWindowChanges() {
		final LabPanelSize size = new LabPanelSize(500.0f);
		assertEquals(500.0f, size.shown(WINDOW));
		assertEquals(500.0f, size.shown(1920.0f), "absolute: not scaled with the window");
		// A narrower window holds it within its bounds, a wider one gives it back.
		assertEquals(LabPanelSize.widest(700.0f), size.shown(700.0f));
		assertEquals(500.0f, size.shown(WINDOW));
		assertEquals(500.0f, size.chosen());
	}

	@Test
	void draggingTheSplitterLeftWidensThePanelWithinItsBounds() {
		final LabPanelSize size = new LabPanelSize(LabPanelSize.DEFAULT);
		// Pressed at 946 (the panel 330 wide), dragged 200 pixels left.
		size.startDrag(946.0f, WINDOW);
		assertTrue(size.dragTo(746.0f, WINDOW));
		assertEquals(530.0f, size.chosen());
		assertEquals(530.0f, size.shown(WINDOW));
		// Far right: the least width; far left: the widest; past a bound nothing changes.
		assertTrue(size.dragTo(1270.0f, WINDOW));
		assertEquals(LabPanelSize.MIN, size.chosen());
		assertFalse(size.dragTo(1275.0f, WINDOW), "past the bound: nothing to lay out again");
		assertTrue(size.dragTo(0.0f, WINDOW));
		assertEquals(768.0f, size.chosen());
		assertTrue(size.endDrag(), "changed: remembered");
		// No drag going on: a move changes nothing.
		assertFalse(size.dragTo(500.0f, WINDOW));
		assertEquals(768.0f, size.chosen());
	}

	@Test
	void aClickOrAShakeChangesNothingNorIsRemembered() {
		final LabPanelSize size = new LabPanelSize(LabPanelSize.DEFAULT);
		size.startDrag(946.0f, WINDOW);
		assertFalse(size.endDrag(), "a click");
		size.startDrag(946.0f, WINDOW);
		assertFalse(size.dragTo(948.0f, WINDOW), "within the dead zone");
		assertFalse(size.dragTo(944.0f, WINDOW));
		assertEquals(LabPanelSize.DEFAULT, size.chosen());
		assertFalse(size.endDrag());
		// Out of it, then back to where it was pressed: the same width, nothing to remember.
		size.startDrag(946.0f, WINDOW);
		assertTrue(size.dragTo(940.0f, WINDOW));
		assertEquals(336.0f, size.chosen());
		assertTrue(size.dragTo(946.0f, WINDOW), "back: laid out again");
		assertEquals(LabPanelSize.DEFAULT, size.chosen());
		assertFalse(size.endDrag());
	}

	@Test
	void aShakeKeepsAWidthChosenWiderThanTheWindowAllows() {
		// Remembered 800, shown 768 in this window: a press that shakes keeps the 800 for a wider window.
		final LabPanelSize size = new LabPanelSize(800.0f);
		assertEquals(768.0f, size.shown(WINDOW));
		size.startDrag(504.0f, WINDOW);
		assertFalse(size.dragTo(505.0f, WINDOW));
		assertFalse(size.endDrag());
		assertEquals(800.0f, size.chosen());
		assertEquals(800.0f, size.shown(1920.0f));
	}

	@Test
	void aDoubleClickGivesTheDefaultBack() {
		final LabPanelSize size = new LabPanelSize(600.0f);
		size.reset();
		assertEquals(LabPanelSize.DEFAULT, size.chosen());
		assertEquals(LabPanelSize.DEFAULT, size.shown(WINDOW));
	}

	@Test
	void aWidthReadThatIsNoWidthGivesTheDefault() {
		assertEquals(LabPanelSize.DEFAULT, new LabPanelSize(Float.NaN).chosen());
		assertEquals(LabPanelSize.DEFAULT, new LabPanelSize(-5.0f).chosen());
		assertEquals(LabPanelSize.DEFAULT, new LabPanelSize(100.0f).chosen(), "under the least width");
		assertEquals(LabPanelSize.DEFAULT, new LabPanelSize(Float.POSITIVE_INFINITY).chosen());
		assertTrue(LabPanelSize.valid(LabPanelSize.MIN));
		assertFalse(LabPanelSize.valid(1.0e9f));
		assertEquals(900.0f, new LabPanelSize(900.0f).chosen(), "kept for a wider window");
	}
}
