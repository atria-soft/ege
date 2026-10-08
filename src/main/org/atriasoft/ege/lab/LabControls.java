package org.atriasoft.ege.lab;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.atriasoft.gale.key.KeyKeyboard;

/**
 * The actions of a lab, the single source of its control panel, of its keys
 * and of its F1 help: each one declared once ({@link #action},
 * {@link #toggle}, {@link #choice}, {@link #stepper}) under the heading set
 * by {@link #group}. A key bound twice is refused when it is declared.
 * <p>
 * {@link #press} (a key) and the widgets of the panel run the same methods
 * ({@link #activate}, {@link #set}, {@link #select}, {@link #step}); a
 * throwable of a callback goes to the error handler ({@link #onError}), never
 * further. Not thread-safe: declared and run on the GUI thread. Pure Java,
 * tested headless.
 */
public final class LabControls {

	/** The heading of the controls declared before any {@link #group}. */
	public static final String DEFAULT_GROUP = "Lab";

	private final List<LabControl> controls = new ArrayList<>();
	private String group = DEFAULT_GROUP;
	/** Changes with every control declared: the panel lays itself out again. */
	private int version;
	private BiConsumer<String, Throwable> onError = (what, error) -> {
		throw new IllegalStateException(what + ": " + error, error);
	};

	/** Where a throwable of a callback goes: what failed ({@code Proxies}) and the throwable. */
	public void onError(final BiConsumer<String, Throwable> handler) {
		this.onError = Objects.requireNonNull(handler);
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

	/** A number stepped down or up (two buttons around the value shown). */
	public LabControls stepper(final String label, final LabKey less, final LabKey more, final Supplier<String> value,
			final Runnable decrease, final Runnable increase) {
		return add(new LabControl.Stepper(this.group, label, less, more, Objects.requireNonNull(value),
				Objects.requireNonNull(decrease), Objects.requireNonNull(increase)));
	}

	private LabControls add(final LabControl control) {
		for (final LabKey key : control.keys()) {
			final LabControl bound = boundTo(key);
			if (bound != null) {
				throw new IllegalArgumentException(
						"the key " + key.name() + " of '" + control.label() + "' already runs '" + bound.label() + "'");
			}
		}
		this.controls.add(control);
		this.version++;
		return this;
	}

	private LabControl boundTo(final LabKey key) {
		for (final LabControl control : this.controls) {
			if (control.keys().contains(key)) {
				return control;
			}
		}
		return null;
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
	 * Run the control bound to a key event ({@code type} and {@code value} as
	 * gale hands them), on a key going down.
	 *
	 * @return whether a control is bound to it
	 */
	public boolean press(final KeyKeyboard type, final Character value) {
		for (final LabControl control : this.controls) {
			final List<LabKey> keys = control.keys();
			for (int i = 0; i < keys.size(); i++) {
				if (keys.get(i).matches(type, value)) {
					pressed(control, keys.get(i));
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
		}
	}

	/** Run an action. */
	public void activate(final LabControl.Action action) {
		guard(action, action.run());
	}

	/** Whether a toggle is on now (off when its supplier throws, which is told). */
	public boolean value(final LabControl.Toggle toggle) {
		try {
			return toggle.value().getAsBoolean();
		} catch (final Throwable e) {
			this.onError.accept(toggle.label(), e);
			return false;
		}
	}

	/** Set a toggle on or off. */
	public void set(final LabControl.Toggle toggle, final boolean on) {
		guard(toggle, () -> toggle.set().accept(on));
	}

	/** Choose the item {@code index} of a choice. */
	public void select(final LabControl.Choice choice, final int index) {
		guard(choice, () -> choice.select().accept(index));
	}

	/** The item {@code direction} after the one chosen, round the list (the first or the last when none is chosen). */
	public void step(final LabControl.Choice choice, final int direction) {
		guard(choice, () -> {
			final int count = choice.items().get().size();
			if (count == 0) {
				return;
			}
			final int index = stepIndex(choice.selected().getAsInt(), direction, count);
			choice.select().accept(index);
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

	private void guard(final LabControl control, final Runnable run) {
		try {
			run.run();
		} catch (final Throwable e) {
			this.onError.accept(control.label(), e);
		}
	}

	/** The lines of the F1 help: each heading, then {@code [H]  Proxies} per control. */
	public List<String> help() {
		final List<String> lines = new ArrayList<>();
		for (final Map.Entry<String, List<LabControl>> entry : byGroup().entrySet()) {
			lines.add(entry.getKey());
			for (final LabControl control : entry.getValue()) {
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
