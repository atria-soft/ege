package org.atriasoft.ege.lab;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.Dimension2f;
import org.atriasoft.etk.DimensionInsets;
import org.atriasoft.etk.Distance;
import org.atriasoft.etk.math.Vector2b;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.Gravity;
import org.atriasoft.ewol.widget.Button;
import org.atriasoft.ewol.widget.Entry;
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
 * a check box per toggle, a drop-down list under its title and its previous
 * and next buttons per choice, minus and plus buttons around the value per
 * stepper, a text field under its title per text, a grid of buttons per
 * palette (the item chosen lit); each label followed by its keys in brackets
 * ({@code Proxies [H]}).
 * The groups of the lab scroll; the group of the view ({@link LabView#VIEW_GROUP}:
 * frame, human, flight, help, Quit) stays in a footer under them, always in
 * sight. A widget runs the same {@link LabControls} method as the key;
 * {@link #sync()} (every frame) shows the state of every control, and lays the
 * panel out again when controls were declared since.
 * <p>
 * The items of a drop-down list are shown at most {@link #ITEM_CHARS}
 * characters long (cut with {@code ...}: the list keeps the panel's width;
 * the lab is told the index).
 * <p>
 * The widgets are connected with {@code connectAuto} on this object (kept by
 * the window), never with a connection left to the garbage collector. Each
 * widget used gives the focus back to the 3D view ({@code release}): the keys
 * go to the lab again, never to a text field left behind nor to a button that
 * Enter would press again.
 */
final class LabPanelWidgets implements LabControlPanel.Factory<Widget> {

	/** Width of the value of a stepper, pixels. */
	private static final float VALUE_WIDTH = 96.0f;
	/** The longest item a drop-down list shows, characters. */
	static final int ITEM_CHARS = 34;
	/** The buttons of a palette on a row. */
	static final int PALETTE_COLUMNS = 2;
	/** The colour of the item of a palette chosen, and of the others. */
	static final Color CHOSEN = new Color(1.0f, 0.84f, 0.42f, 1.0f);
	static final Color NOT_CHOSEN = Color.WHITE;

	private final LabControls controls;
	/** Gives the focus back to the 3D view. */
	private final Runnable release;
	private final Sizer panel = new Sizer(Sizer.DisplayMode.VERTICAL);
	private final Sizer column = new Sizer(Sizer.DisplayMode.VERTICAL);
	private final Sizer footer = new Sizer(Sizer.DisplayMode.VERTICAL);
	private final ScrollView scroll = new ScrollView();
	/** What each control shows of its state, run every frame. */
	private final List<Runnable> syncs = new ArrayList<>();
	private int builtVersion = -1;

	/**
	 * @param controls the controls laid out
	 * @param release  gives the focus back to the 3D view (after a widget was used, or Enter in a text field)
	 */
	LabPanelWidgets(final LabControls controls, final Runnable release) {
		this.controls = controls;
		this.release = release;
		for (final Sizer part : new Sizer[] { this.column, this.footer }) {
			part.setPropertyExpand(new Vector2b(true, false));
			part.setPropertyFill(new Vector2b(true, false));
			// Away from the scroll bar on the right.
			part.setPropertyBorderSize(new Dimension2f(new Vector2f(10, 2), Distance.PIXEL));
		}
		this.scroll.setPropertyShowHorizontal(false);
		this.scroll.setPropertyExpand(Vector2b.TRUE);
		this.scroll.setPropertyFill(Vector2b.TRUE);
		this.scroll.setPropertyMinSize(new Dimension2f(new Vector2f(0, 100), Distance.PIXEL));
		this.scroll.setSubWidget(this.column);
		this.panel.setPropertyExpand(Vector2b.TRUE);
		this.panel.setPropertyFill(Vector2b.TRUE);
		this.panel.subWidgetAdd(this.scroll);
		this.panel.subWidgetAdd(this.footer);
	}

	/**
	 * The panel to dock beside the view ({@link LabSplit} sets its width): the controls of the lab (scrolling), the
	 * footer of the view under them.
	 */
	Widget widget() {
		return this.panel;
	}

	/** Show the state of every control; lay the panel out again when controls were declared since. */
	void sync() {
		if (this.builtVersion != this.controls.version()) {
			this.builtVersion = this.controls.version();
			this.syncs.clear();
			this.column.subWidgetRemoveAll();
			for (final Widget piece : LabControlPanel.layout(this.controls, this,
					group -> !LabView.VIEW_GROUP.equals(group))) {
				this.column.subWidgetAdd(piece);
			}
			this.footer.subWidgetRemoveAll();
			for (final Widget piece : footerPieces()) {
				this.footer.subWidgetAdd(piece);
			}
		}
		for (final Runnable sync : this.syncs) {
			sync.run();
		}
	}

	/** The group of the view: its heading, its buttons side by side on one row, its check boxes under them. */
	private List<Widget> footerPieces() {
		final List<Widget> pieces = new ArrayList<>();
		final List<LabControl> view = this.controls.byGroup().get(LabView.VIEW_GROUP);
		if (view == null) {
			return pieces;
		}
		pieces.add(heading(LabView.VIEW_GROUP));
		final Sizer buttons = row();
		for (final LabControl control : view) {
			if (control instanceof final LabControl.Action action) {
				buttons.subWidgetAdd(action(action));
			}
		}
		pieces.add(buttons);
		for (final LabControl control : view) {
			if (!(control instanceof LabControl.Action)) {
				pieces.add(switch (control) {
					case final LabControl.Toggle toggle -> toggle(toggle);
					case final LabControl.Choice choice -> choice(choice);
					case final LabControl.Stepper stepper -> stepper(stepper);
					case final LabControl.Text text -> text(text);
					case final LabControl.Palette palette -> palette(palette);
					case final LabControl.Action action -> action(action);
				});
			}
		}
		return pieces;
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

	/** A button with thin borders (the panel holds more rows). */
	private static Button button(final String text) {
		final Button button = Button.createLabelButton(text);
		button.setPropertyBorderWidth(new DimensionInsets(2));
		button.setPropertyPadding(new DimensionInsets(1));
		return button;
	}

	private static Sizer row() {
		final Sizer row = new Sizer(Sizer.DisplayMode.HORIZONTAL);
		wide(row);
		return row;
	}

	/** The items of a list as shown: each at most {@link #ITEM_CHARS} characters. */
	static List<String> shown(final List<String> items) {
		final List<String> out = new ArrayList<>(items.size());
		for (final String item : items) {
			out.add(LabText.shorten(LabText.ascii(item), ITEM_CHARS));
		}
		return out;
	}

	@Override
	public Widget heading(final String text) {
		final Sizer block = new Sizer(Sizer.DisplayMode.VERTICAL);
		wide(block);
		final Spacer gap = new Spacer();
		gap.setPropertyMinSize(new Dimension2f(new Vector2f(2, 2), Distance.PIXEL));
		block.subWidgetAdd(gap);
		final Label title = label("<b>" + text + "</b>");
		title.setPropertyFontSize(14);
		wide(title);
		block.subWidgetAdd(title);
		return block;
	}

	@Override
	public Widget action(final LabControl.Action control) {
		final Button button = button(control.title());
		wide(button);
		button.signalClick.connectAuto(this, (final LabPanelWidgets self) -> {
			self.release.run();
			self.controls.activate(control);
		});
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
				self.release.run();
				self.controls.set(control, value);
			}
		});
		text.signalPressed.connectAuto(this, (final LabPanelWidgets self) -> {
			self.release.run();
			self.controls.set(control, !self.controls.value(control));
		});
		this.syncs.add(() -> {
			final boolean on = this.controls.value(control);
			if (!Objects.equals(tick.getPropertyValue(), on)) {
				tick.setPropertyValue(on);
			}
		});
		return row;
	}

	/** The title and the previous and next buttons on one row, the drop-down list alone on the whole row under. */
	@Override
	public Widget choice(final LabControl.Choice control) {
		final Sizer block = new Sizer(Sizer.DisplayMode.VERTICAL);
		wide(block);
		final Sizer top = row();
		final Label title = label(control.title());
		wide(title);
		top.subWidgetAdd(title);
		final Button previous = button(" Prev ");
		previous.setPropertyExpand(Vector2b.FALSE);
		previous.signalClick.connectAuto(this, (final LabPanelWidgets self) -> {
			self.release.run();
			self.controls.step(control, -1);
		});
		top.subWidgetAdd(previous);
		final Button next = button(" Next ");
		next.setPropertyExpand(Vector2b.FALSE);
		next.signalClick.connectAuto(this, (final LabPanelWidgets self) -> {
			self.release.run();
			self.controls.step(control, 1);
		});
		top.subWidgetAdd(next);
		block.subWidgetAdd(top);
		final Select select = new Select();
		wide(select);
		select.signalSelectionChanged.connectAuto(this, (final LabPanelWidgets self, final Integer index) -> {
			if (index != null && index >= 0) {
				self.release.run();
				self.controls.select(control, index);
			}
		});
		block.subWidgetAdd(select);
		this.syncs.add(() -> {
			final List<String> items = shown(this.controls.items(control));
			if (!items.equals(select.getItems())) {
				select.setItems(items);
			}
			final int index = this.controls.selected(control);
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
		less.setPropertyExpand(Vector2b.FALSE);
		less.signalClick.connectAuto(this, (final LabPanelWidgets self) -> {
			self.release.run();
			self.controls.step(control, -1);
		});
		row.subWidgetAdd(less);
		final Label value = label("");
		value.setPropertyGravity(Gravity.CENTER);
		value.setPropertyMinSize(new Dimension2f(new Vector2f(VALUE_WIDTH, 10), Distance.PIXEL));
		row.subWidgetAdd(value);
		final Button more = button(" + ");
		more.setPropertyExpand(Vector2b.FALSE);
		more.signalClick.connectAuto(this, (final LabPanelWidgets self) -> {
			self.release.run();
			self.controls.step(control, 1);
		});
		row.subWidgetAdd(more);
		this.syncs.add(() -> {
			final String shown = LabText.ascii(this.controls.text(control));
			if (!shown.equals(value.getPropertyValue())) {
				value.setPropertyValue(shown);
			}
		});
		return row;
	}

	/**
	 * The title (Enter takes what is typed), the text field alone on the row under. While it is not typed in, it
	 * shows the text of the control; Enter gives the focus back to the view and what is typed to the control (the text
	 * of the control itself when it was left as shown: the field shows it without its accents).
	 */
	@Override
	public Widget text(final LabControl.Text control) {
		final Sizer block = new Sizer(Sizer.DisplayMode.VERTICAL);
		wide(block);
		final Label title = label(control.title() + " (Enter)");
		wide(title);
		block.subWidgetAdd(title);
		final Entry entry = new Entry();
		wide(entry);
		entry.setPropertyPadding(new DimensionInsets(2));
		entry.signalEnter.connectAuto(this, (final LabPanelWidgets self, final String typed) -> {
			self.release.run();
			// The field shows the text in the letters the fonts draw: left as shown, the text itself is given (no accent
			// lost).
			final String value = self.controls.text(control);
			self.controls.enter(control, LabText.ascii(value).equals(typed) ? value : typed);
		});
		block.subWidgetAdd(entry);
		this.syncs.add(() -> {
			if (entry.isFocused()) {
				return;
			}
			final String shown = LabText.ascii(this.controls.text(control));
			if (!shown.equals(entry.getPropertyValue())) {
				entry.setPropertyValue(shown);
			}
		});
		return block;
	}

	/** The title, then the items as buttons, {@link #PALETTE_COLUMNS} on a row, the item chosen lit. */
	@Override
	public Widget palette(final LabControl.Palette control) {
		final Sizer block = new Sizer(Sizer.DisplayMode.VERTICAL);
		wide(block);
		final Label title = label(control.title());
		wide(title);
		block.subWidgetAdd(title);
		final List<Button> buttons = new ArrayList<>();
		Sizer line = null;
		for (int i = 0; i < control.items().size(); i++) {
			if (i % PALETTE_COLUMNS == 0) {
				line = row();
				block.subWidgetAdd(line);
			}
			final int index = i;
			final Button button = button(control.items().get(i).title());
			wide(button);
			button.signalClick.connectAuto(this, (final LabPanelWidgets self) -> {
				self.release.run();
				self.controls.select(control, index);
			});
			line.subWidgetAdd(button);
			buttons.add(button);
		}
		if (line != null) {
			// The last row as wide as the others.
			for (int i = control.items().size() % PALETTE_COLUMNS; i > 0 && i < PALETTE_COLUMNS; i++) {
				final Spacer filler = new Spacer();
				wide(filler);
				line.subWidgetAdd(filler);
			}
		}
		this.syncs.add(() -> {
			final int chosen = this.controls.selected(control);
			for (int i = 0; i < buttons.size(); i++) {
				final Color color = i == chosen ? CHOSEN : NOT_CHOSEN;
				if (!color.equals(buttons.get(i).getPropertyColor())) {
					buttons.get(i).setPropertyColor(color);
				}
			}
		});
		return block;
	}
}
