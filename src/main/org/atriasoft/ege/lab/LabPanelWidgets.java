package org.atriasoft.ege.lab;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.atriasoft.etk.Dimension2f;
import org.atriasoft.etk.Distance;
import org.atriasoft.etk.math.Vector2b;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.Gravity;
import org.atriasoft.ewol.widget.Button;
import org.atriasoft.ewol.widget.Label;
import org.atriasoft.ewol.widget.ScrollView;
import org.atriasoft.ewol.widget.Select;
import org.atriasoft.ewol.widget.Sizer;
import org.atriasoft.ewol.widget.Spacer;
import org.atriasoft.ewol.widget.Tick;
import org.atriasoft.ewol.widget.Widget;

/**
 * The control panel of a lab in ewol widgets, docked beside the 3D view: for
 * each group of its {@link LabControls} a heading, then a button per action,
 * a check box per toggle, a drop-down list with previous and next buttons
 * per choice, minus and plus buttons around the value per stepper; each
 * label followed by its keys in brackets ({@code Proxies [H]}). A widget runs
 * the same {@link LabControls} method as the key; {@link #sync()} (every
 * frame) shows the state of every control, and lays the panel out again when
 * controls were declared since.
 * <p>
 * The widgets are connected with {@code connectAuto} on this object (kept by
 * the window), never with a connection left to the garbage collector.
 */
final class LabPanelWidgets implements LabControlPanel.Factory<Widget> {

	/** Width of the panel, pixels. */
	static final float WIDTH = 330.0f;
	/** Width of the value of a stepper, pixels. */
	private static final float VALUE_WIDTH = 96.0f;

	private final LabControls controls;
	private final Sizer column = new Sizer(Sizer.DisplayMode.VERTICAL);
	private final ScrollView scroll = new ScrollView();
	/** What each control shows of its state, run every frame. */
	private final List<Runnable> syncs = new ArrayList<>();
	private int builtVersion = -1;

	LabPanelWidgets(final LabControls controls) {
		this.controls = controls;
		this.column.setPropertyExpand(new Vector2b(true, false));
		this.column.setPropertyFill(new Vector2b(true, false));
		this.column.setPropertyBorderSize(new Dimension2f(new Vector2f(8, 4), Distance.PIXEL));
		this.scroll.setPropertyShowHorizontal(false);
		this.scroll.setPropertyExpand(new Vector2b(false, true));
		this.scroll.setPropertyFill(Vector2b.TRUE);
		this.scroll.setPropertyMinSize(new Dimension2f(new Vector2f(WIDTH, 100), Distance.PIXEL));
		this.scroll.setSubWidget(this.column);
	}

	/** The panel to dock beside the view. */
	Widget widget() {
		return this.scroll;
	}

	/** Show the state of every control; lay the panel out again when controls were declared since. */
	void sync() {
		if (this.builtVersion != this.controls.version()) {
			this.builtVersion = this.controls.version();
			this.syncs.clear();
			this.column.subWidgetRemoveAll();
			for (final Widget piece : LabControlPanel.layout(this.controls, this)) {
				this.column.subWidgetAdd(piece);
			}
		}
		for (final Runnable sync : this.syncs) {
			sync.run();
		}
	}

	private static void wide(final Widget widget) {
		widget.setPropertyExpand(new Vector2b(true, false));
		widget.setPropertyFill(new Vector2b(true, false));
	}

	private static Label label(final String text) {
		final Label label = new Label(text);
		label.setPropertyAutoTranslate(false);
		label.setPropertyGravity(Gravity.LEFT);
		return label;
	}

	private static Button button(final String text) {
		final Button button = Button.createLabelButton(text);
		return button;
	}

	private static Sizer row() {
		final Sizer row = new Sizer(Sizer.DisplayMode.HORIZONTAL);
		wide(row);
		return row;
	}

	@Override
	public Widget heading(final String text) {
		final Sizer block = new Sizer(Sizer.DisplayMode.VERTICAL);
		wide(block);
		final Spacer gap = new Spacer();
		gap.setPropertyMinSize(new Dimension2f(new Vector2f(4, 4), Distance.PIXEL));
		block.subWidgetAdd(gap);
		final Label title = label("<b>" + text + "</b>");
		title.setPropertyFontSize(15);
		wide(title);
		block.subWidgetAdd(title);
		return block;
	}

	@Override
	public Widget action(final LabControl.Action control) {
		final Button button = button(control.title());
		wide(button);
		button.signalClick.connectAuto(this, (final LabPanelWidgets self) -> self.controls.activate(control));
		return button;
	}

	@Override
	public Widget toggle(final LabControl.Toggle control) {
		final Sizer row = row();
		final Tick tick = new Tick();
		tick.setPropertyGravity(Gravity.CENTER);
		row.subWidgetAdd(tick);
		final Label text = label(" " + control.title());
		wide(text);
		row.subWidgetAdd(text);
		// The tick sets the value it shows; a click on the words flips it.
		tick.signalValue.connectAuto(this, (final LabPanelWidgets self, final Boolean value) -> {
			if (value != null && value != self.controls.value(control)) {
				self.controls.set(control, value);
			}
		});
		text.signalPressed.connectAuto(this, (final LabPanelWidgets self) -> self.controls.set(control, !self.controls.value(control)));
		this.syncs.add(() -> {
			final boolean on = this.controls.value(control);
			if (!Objects.equals(tick.getPropertyValue(), on)) {
				tick.setPropertyValue(on);
			}
		});
		return row;
	}

	@Override
	public Widget choice(final LabControl.Choice control) {
		final Sizer block = new Sizer(Sizer.DisplayMode.VERTICAL);
		wide(block);
		final Label title = label(control.title());
		wide(title);
		block.subWidgetAdd(title);
		final Sizer row = row();
		final Button previous = button("Prev");
		previous.signalClick.connectAuto(this, (final LabPanelWidgets self) -> self.controls.step(control, -1));
		row.subWidgetAdd(previous);
		final Select select = new Select();
		wide(select);
		select.signalSelectionChanged.connectAuto(this, (final LabPanelWidgets self, final Integer index) -> {
			if (index != null && index >= 0) {
				self.controls.select(control, index);
			}
		});
		row.subWidgetAdd(select);
		final Button next = button("Next");
		next.signalClick.connectAuto(this, (final LabPanelWidgets self) -> self.controls.step(control, 1));
		row.subWidgetAdd(next);
		block.subWidgetAdd(row);
		this.syncs.add(() -> {
			final List<String> items = control.items().get();
			if (!items.equals(select.getItems())) {
				select.setItems(items);
			}
			final int index = control.selected().getAsInt();
			if (select.getPropertySelectedIndex() != index) {
				select.setPropertySelectedIndex(index);
			}
		});
		return block;
	}

	@Override
	public Widget stepper(final LabControl.Stepper control) {
		final Sizer row = row();
		final Label title = label(control.title());
		wide(title);
		row.subWidgetAdd(title);
		final Button less = button(" - ");
		less.signalClick.connectAuto(this, (final LabPanelWidgets self) -> self.controls.step(control, -1));
		row.subWidgetAdd(less);
		final Label value = label("");
		value.setPropertyGravity(Gravity.CENTER);
		value.setPropertyMinSize(new Dimension2f(new Vector2f(VALUE_WIDTH, 10), Distance.PIXEL));
		row.subWidgetAdd(value);
		final Button more = button(" + ");
		more.signalClick.connectAuto(this, (final LabPanelWidgets self) -> self.controls.step(control, 1));
		row.subWidgetAdd(more);
		this.syncs.add(() -> {
			final String shown = LabText.ascii(control.value().get());
			if (!shown.equals(value.getPropertyValue())) {
				value.setPropertyValue(shown);
			}
		});
		return row;
	}
}
