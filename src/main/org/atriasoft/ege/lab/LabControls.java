package org.atriasoft.ege.lab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.atriasoft.gale.key.KeyKeyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The actions of a lab, the single source of its control panel, of its keys
 * and of its F1 help: each one declared once ({@link #action},
 * {@link #toggle}, {@link #choice}, {@link #stepper}, {@link #text},
 * {@link #palette}) under the heading set by {@link #group}.
 * <p>
 * A key that cannot run the control is dropped from it, the control kept
 * (clickable) and the reason reported ({@link #setReporter}): a key already
 * bound, a key of the kit ({@code F}, {@code F1} to {@code F3}, reserved
 * before the lab declares its controls), a key the window keeps or never
 * receives ({@link #refusal}). {@link #press} (a key, with Control or not)
 * and the widgets of the panel run the same methods ({@link #activate},
 * {@link #set}, {@link #select}, {@link #step}, {@link #enter}); the state the
 * widgets show is asked through {@link #value}, {@link #items},
 * {@link #selected}, {@link #text}.
 * Whatever a callback or a supplier throws is reported, never further, and
 * cleared once it succeeds again. Not thread-safe: declared and run on the
 * GUI thread. Pure Java, tested headless.
 */
public final class LabControls {

	private static final Logger LOGGER = LoggerFactory.getLogger(LabControls.class);
	/** The heading of the controls declared before any {@link #group}. */
	public static final String DEFAULT_GROUP = "Lab";
	/** What a supplier of a control is reported under, after its label: {@code Proxies (state)}. */
	static final String STATE = " (state)";
	static final String ITEMS = " (items)";
	static final String SELECTED = " (item chosen)";
	static final String VALUE = " (value)";
	static final String TEXT = " (text)";

	/** The keys a lab cannot bind, and why. */
	private static final Map<LabKey, String> REFUSED = new HashMap<>();
	static {
		for (final KeyKeyboard camera : new KeyKeyboard[] { KeyKeyboard.UP, KeyKeyboard.DOWN, KeyKeyboard.LEFT,
				KeyKeyboard.RIGHT, KeyKeyboard.PAGE_UP, KeyKeyboard.PAGE_DOWN }) {
			REFUSED.put(LabKey.of(camera), "it drives the camera");
		}
		REFUSED.put(LabKey.of(KeyKeyboard.F12), "it opens the widget inspector of ewol");
		REFUSED.put(LabKey.of('\t'), "AWT keeps it to move the focus: it never reaches the lab");
		REFUSED.put(LabKey.of('\u001b'), "it is left to the drop-down lists");
	}

	private static final LabReporter LOG = new LabReporter() {
		@Override
		public void report(final String what, final Throwable error) {
			LOGGER.error("Lab: {} failed: {}", what, error.toString(), error);
		}

		@Override
		public void report(final String what, final String message) {
			LOGGER.warn("Lab: {}: {}", what, message);
		}

		@Override
		public void clear(final String what) {
			// Nothing shown.
		}
	};

	private final List<LabControl> controls = new ArrayList<>();
	/** The keys of the kit, by the label of the control of the kit that takes each. */
	private final Map<LabKey, String> reserved = new LinkedHashMap<>();
	private String group = DEFAULT_GROUP;
	/** Whether the kit declares its own controls (its reserved keys accepted). */
	private boolean kit;
	/** Changes with every control declared: the panel lays itself out again. */
	private int version;
	private LabReporter reporter = LOG;

	/** Where the problems go (the info panel of the view); the log until it is set. */
	public void setReporter(final LabReporter next) {
		this.reporter = Objects.requireNonNull(next);
	}

	/** Why {@code key} cannot run a control of a lab, {@code null} when it can (if it is free). */
	public static String refusal(final LabKey key) {
		return REFUSED.get(key);
	}

	/** Keep {@code key} for the control {@code label} of the kit: a lab declaring it loses it. */
	void reserve(final LabKey key, final String label) {
		this.reserved.put(key, label);
	}

	/** Run {@code declarations} of the kit's own controls, which take the keys reserved for them. */
	void declareAsKit(final Runnable declarations) {
		this.kit = true;
		try {
			declarations.run();
		} finally {
			this.kit = false;
		}
	}

	/** The controls declared from now on go under {@code heading}. */
	public LabControls group(final String heading) {
		this.group = Objects.requireNonNull(heading);
		return this;
	}

	/** A one-shot action (a button). */
	public LabControls action(final String label, final LabKey key, final Runnable run) {
		return add(new LabControl.Action(this.group, label, key, Objects.requireNonNull(run)));
	}

	/** Something on or off (a check box); its key flips it. */
	public LabControls toggle(final String label, final LabKey key, final BooleanSupplier value,
			final Consumer<Boolean> set) {
		return add(new LabControl.Toggle(this.group, label, key, Objects.requireNonNull(value),
				Objects.requireNonNull(set)));
	}

	/** One item of a list (a drop-down list); its keys choose the previous or the next item, round the list. */
	public LabControls choice(final String label, final LabKey previous, final LabKey next,
			final Supplier<List<String>> items, final IntSupplier selected, final IntConsumer select) {
		return add(new LabControl.Choice(this.group, label, previous, next, Objects.requireNonNull(items),
				Objects.requireNonNull(selected), Objects.requireNonNull(select)));
	}

	/** A number stepped down or up (two buttons around the value shown); its keys repeat while held. */
	public LabControls stepper(final String label, final LabKey less, final LabKey more, final Supplier<String> value,
			final Runnable decrease, final Runnable increase) {
		return add(new LabControl.Stepper(this.group, label, less, more, Objects.requireNonNull(value),
				Objects.requireNonNull(decrease), Objects.requireNonNull(increase)));
	}

	/** A line of text typed in (a text field); what is typed goes to {@code set} on Enter. No key. */
	public LabControls text(final String label, final Supplier<String> value, final Consumer<String> set) {
		return add(new LabControl.Text(this.group, label, Objects.requireNonNull(value), Objects.requireNonNull(set)));
	}

	/** One of a few items, each with its key (a grid of buttons, the item chosen lit). */
	public LabControls palette(final String label, final List<LabControl.Palette.Item> items, final IntSupplier selected,
			final IntConsumer select) {
		return add(new LabControl.Palette(this.group, label, items, Objects.requireNonNull(selected),
				Objects.requireNonNull(select)));
	}

	private LabControls add(final LabControl control) {
		LabControl kept = control;
		for (final LabKey key : control.keys()) {
			final String why = whyNot(key);
			if (why != null) {
				this.reporter.report("Key " + key.name() + " of '" + control.label() + "'",
						"not bound: " + why + " (the control is still in the panel)");
				kept = without(kept, key);
			}
		}
		this.controls.add(kept);
		this.version++;
		return this;
	}

	/** Why {@code key} cannot be bound now, {@code null} when it can. */
	private String whyNot(final LabKey key) {
		final String refused = refusal(key);
		if (refused != null) {
			return refused;
		}
		final String kitControl = this.reserved.get(key);
		if (kitControl != null && !this.kit) {
			return "the kit keeps it for '" + kitControl + "'";
		}
		for (final LabControl control : this.controls) {
			if (control.keys().contains(key)) {
				return "it already runs '" + control.label() + "'";
			}
		}
		return null;
	}

	/** {@code control} without {@code key}. */
	static LabControl without(final LabControl control, final LabKey key) {
		return switch (control) {
			case final LabControl.Action a -> new LabControl.Action(a.group(), a.label(),
					key.equals(a.key()) ? null : a.key(), a.run());
			case final LabControl.Toggle t -> new LabControl.Toggle(t.group(), t.label(),
					key.equals(t.key()) ? null : t.key(), t.value(), t.set());
			case final LabControl.Choice c -> new LabControl.Choice(c.group(), c.label(),
					key.equals(c.previous()) ? null : c.previous(), key.equals(c.next()) ? null : c.next(), c.items(),
					c.selected(), c.select());
			case final LabControl.Stepper s -> new LabControl.Stepper(s.group(), s.label(),
					key.equals(s.less()) ? null : s.less(), key.equals(s.more()) ? null : s.more(), s.value(),
					s.decrease(), s.increase());
			case final LabControl.Text t -> t;
			case final LabControl.Palette p -> {
				final List<LabControl.Palette.Item> items = new ArrayList<>();
				for (final LabControl.Palette.Item item : p.items()) {
					items.add(key.equals(item.key()) ? new LabControl.Palette.Item(item.label(), null) : item);
				}
				yield new LabControl.Palette(p.group(), p.label(), items, p.selected(), p.select());
			}
		};
	}

	/** Every control, in the order declared. */
	public List<LabControl> all() {
		return List.copyOf(this.controls);
	}

	/** The controls by heading, the headings in the order they first appear. */
	public Map<String, List<LabControl>> byGroup() {
		final Map<String, List<LabControl>> groups = new LinkedHashMap<>();
		for (final LabControl control : this.controls) {
			groups.computeIfAbsent(control.group(), key -> new ArrayList<>()).add(control);
		}
		return groups;
	}

	/** A number that changes whenever a control is declared. */
	public int version() {
		return this.version;
	}

	/**
	 * Run the control bound to a key going down without Control ({@code type} and {@code value} as gale hands
	 * them): {@link #press(KeyKeyboard, Character, boolean, boolean)}.
	 *
	 * @return whether a control is bound to it
	 */
	public boolean press(final KeyKeyboard type, final Character value, final boolean repeat) {
		return press(type, value, false, repeat);
	}

	/**
	 * Run the control bound to a key going down ({@code type} and
	 * {@code value} as gale hands them, Control held or not). The auto-repeat
	 * of a held key ({@code repeat}) steps the steppers and the choices, never
	 * an action, a toggle nor a palette.
	 *
	 * @return whether a control is bound to it
	 */
	public boolean press(final KeyKeyboard type, final Character value, final boolean ctrl, final boolean repeat) {
		for (final LabControl control : this.controls) {
			for (final LabKey key : control.keys()) {
				if (key.matches(type, value, ctrl)) {
					if (!repeat || control instanceof LabControl.Choice || control instanceof LabControl.Stepper) {
						pressed(control, key);
					}
					return true;
				}
			}
		}
		return false;
	}

	private void pressed(final LabControl control, final LabKey key) {
		switch (control) {
			case final LabControl.Action action -> activate(action);
			case final LabControl.Toggle toggle -> set(toggle, !value(toggle));
			case final LabControl.Choice choice -> step(choice, key.equals(choice.previous()) ? -1 : 1);
			case final LabControl.Stepper stepper -> step(stepper, key.equals(stepper.less()) ? -1 : 1);
			case final LabControl.Palette palette -> select(palette, palette.indexOf(key));
			case final LabControl.Text text -> {
				// No key.
			}
		}
	}

	/** Run an action. */
	public void activate(final LabControl.Action action) {
		guard(action, action.run());
	}

	/** Set a toggle on or off. */
	public void set(final LabControl.Toggle toggle, final boolean on) {
		guard(toggle, () -> toggle.set().accept(on));
	}

	/** Choose the item {@code index} of a choice. */
	public void select(final LabControl.Choice choice, final int index) {
		guard(choice, () -> choice.select().accept(index));
	}

	/** Choose the item {@code index} of a palette. */
	public void select(final LabControl.Palette palette, final int index) {
		guard(palette, () -> palette.select().accept(index));
	}

	/** Give {@code typed} to a text field (Enter). */
	public void enter(final LabControl.Text text, final String typed) {
		guard(text, () -> text.set().accept(typed != null ? typed : ""));
	}

	/** The item {@code direction} after the one chosen, round the list (the first or the last when none is chosen). */
	public void step(final LabControl.Choice choice, final int direction) {
		guard(choice, () -> {
			final int count = choice.items().get().size();
			if (count == 0) {
				return;
			}
			choice.select().accept(stepIndex(choice.selected().getAsInt(), direction, count));
		});
	}

	/** The index {@code direction} after {@code current} in a list of {@code count}, round it. */
	static int stepIndex(final int current, final int direction, final int count) {
		if (current < 0 || current >= count) {
			return direction > 0 ? 0 : count - 1;
		}
		return Math.floorMod(current + direction, count);
	}

	/** Step a number down ({@code direction} below 0) or up. */
	public void step(final LabControl.Stepper stepper, final int direction) {
		guard(stepper, direction < 0 ? stepper.decrease() : stepper.increase());
	}

	/** Whether a toggle is on now (off when its supplier throws). */
	public boolean value(final LabControl.Toggle toggle) {
		return state(toggle.label() + STATE, () -> toggle.value().getAsBoolean(), false);
	}

	/** The items of a choice now (none when its supplier throws). */
	public List<String> items(final LabControl.Choice choice) {
		final List<String> items = state(choice.label() + ITEMS, choice.items(), List.of());
		return items != null ? items : List.of();
	}

	/** The index of the item chosen now (-1 when its supplier throws). */
	public int selected(final LabControl.Choice choice) {
		return state(choice.label() + SELECTED, () -> choice.selected().getAsInt(), -1);
	}

	/** The index of the item of a palette chosen now (-1 when its supplier throws). */
	public int selected(final LabControl.Palette palette) {
		return state(palette.label() + SELECTED, () -> palette.selected().getAsInt(), -1);
	}

	/** The text of a text field now ({@code ""} when its supplier throws). */
	public String text(final LabControl.Text text) {
		final String value = state(text.label() + TEXT, text.value(), "");
		return value != null ? value : "";
	}

	/** The value of a stepper as shown now ({@code ?} when its supplier throws). */
	public String text(final LabControl.Stepper stepper) {
		final String text = state(stepper.label() + VALUE, stepper.value(), "?");
		return text != null ? text : "";
	}

	/** What {@code supplier} gives, {@code fallback} (reported under {@code what}) when it throws. */
	private <T> T state(final String what, final Supplier<T> supplier, final T fallback) {
		try {
			final T value = supplier.get();
			this.reporter.clear(what);
			return value;
		} catch (final Throwable e) {
			this.reporter.report(what, e);
			return fallback;
		}
	}

	private void guard(final LabControl control, final Runnable run) {
		try {
			run.run();
			this.reporter.clear(control.label());
		} catch (final Throwable e) {
			this.reporter.report(control.label(), e);
		}
	}

	/**
	 * The lines of the F1 help: each heading, then {@code [H]  Proxies} per control ({@code [#]  Paint: Wall} per item
	 * of a palette, {@code [Enter]  Name} for a text field).
	 */
	public List<String> help() {
		final List<String> lines = new ArrayList<>();
		for (final Map.Entry<String, List<LabControl>> entry : byGroup().entrySet()) {
			lines.add(entry.getKey());
			for (final LabControl control : entry.getValue()) {
				if (control instanceof final LabControl.Palette palette) {
					for (final LabControl.Palette.Item item : palette.items()) {
						lines.add("  [" + (item.key() != null ? item.key().name() : "-") + "]  " + palette.label() + ": "
								+ item.label());
					}
					continue;
				}
				if (control instanceof LabControl.Text) {
					lines.add("  [Enter]  " + control.label() + " (a text field)");
					continue;
				}
				final List<String> names = new ArrayList<>();
				for (final LabKey key : control.keys()) {
					names.add(key.name());
				}
				final String keys = names.isEmpty() ? "-" : String.join("/", names);
				lines.add("  [" + keys + "]  " + control.label());
			}
		}
		return lines;
	}
}
