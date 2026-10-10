package org.atriasoft.ege.lab;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * One action of a lab, declared once in its {@link LabControls}: its label,
 * its group in the panel, its keys and what it does. The panel draws it as
 * a widget of its kind, the F1 help lists it, its keys run it; the widget
 * and the keys go through the same {@link LabControls} methods, so they do
 * exactly the same thing. The suppliers give the state the widget shows,
 * asked every frame on the GUI thread.
 */
public sealed interface LabControl permits LabControl.Action, LabControl.Toggle, LabControl.Choice, LabControl.Stepper,
		LabControl.Text, LabControl.Palette {

	/** The heading it is shown under (Subject, Display, Debug, Data, View...). */
	String group();

	/** What it does, in plain words. */
	String label();

	/** Its keys, none or one or two (previous / next, less / more); a palette one per item. */
	List<LabKey> keys();

	/** {@code Proxies [H]}, {@code Seed [U/I]}: the label and its keys in brackets. */
	default String title() {
		final List<LabKey> keys = keys();
		if (keys.isEmpty()) {
			return label();
		}
		final List<String> names = new ArrayList<>();
		for (final LabKey key : keys) {
			names.add(key.name());
		}
		return label() + " [" + String.join("/", names) + "]";
	}

	/**
	 * A one-shot action: a button.
	 *
	 * @param key its key, {@code null} for none
	 * @param run what it does
	 */
	record Action(String group, String label, LabKey key, Runnable run) implements LabControl {
		@Override
		public List<LabKey> keys() {
			return this.key != null ? List.of(this.key) : List.of();
		}
	}

	/**
	 * Something on or off: a check box ticked when {@code value} says so.
	 *
	 * @param key   its key (flips it), {@code null} for none
	 * @param value whether it is on now
	 * @param set   set it on or off
	 */
	record Toggle(String group, String label, LabKey key, BooleanSupplier value, Consumer<Boolean> set)
			implements LabControl {
		@Override
		public List<LabKey> keys() {
			return this.key != null ? List.of(this.key) : List.of();
		}
	}

	/**
	 * One item of a list: a drop-down list with previous and next arrows.
	 *
	 * @param previous the key of the previous item (round the list), {@code null} for none
	 * @param next     the key of the next item, {@code null} for none
	 * @param items    the items now (the list may change: data read again)
	 * @param selected the index of the item chosen now, -1 for none
	 * @param select   choose the item of an index
	 */
	record Choice(String group, String label, LabKey previous, LabKey next, Supplier<List<String>> items,
			IntSupplier selected, IntConsumer select) implements LabControl {
		@Override
		public List<LabKey> keys() {
			return pair(this.previous, this.next);
		}
	}

	/**
	 * A number stepped down or up: a pair of buttons around the value shown. A stepper that takes a value typed
	 * ({@code enter} not {@code null}) shows its value in a box: a click on it makes it a text field, Enter gives
	 * what is typed to {@code enter}, Escape (or a click elsewhere) leaves the value as it was.
	 *
	 * @param less     the key that steps down, {@code null} for none
	 * @param more     the key that steps up, {@code null} for none
	 * @param value    the value as shown now ({@code 12 years}, {@code 7})
	 * @param decrease step down
	 * @param increase step up
	 * @param enter    take a value typed (spaces around it taken off), {@code false} when it refuses it (the value
	 *                 kept, the refusal told); {@code null}: the value is only shown
	 */
	record Stepper(String group, String label, LabKey less, LabKey more, Supplier<String> value, Runnable decrease,
			Runnable increase, Predicate<String> enter) implements LabControl {
		@Override
		public List<LabKey> keys() {
			return pair(this.less, this.more);
		}

		/** Whether a value may be typed in. */
		public boolean typed() {
			return this.enter != null;
		}
	}

	/**
	 * A line of text typed in (a name, a file name): a text field under its label. What is typed is given to
	 * {@code set} when Enter is pressed; the field shows {@code value} while it is not being typed in. No key: while the
	 * field has the focus every key goes to it, Escape (or a click elsewhere) gives the keys back to the lab.
	 *
	 * @param value the text now
	 * @param set   take the text typed (Enter)
	 */
	record Text(String group, String label, Supplier<String> value, Consumer<String> set) implements LabControl {
		@Override
		public List<LabKey> keys() {
			return List.of();
		}
	}

	/**
	 * One of a few items, each with a key of its own (the tools of an editor, the colours of a palette): a grid of
	 * buttons, the item chosen lit; the key of an item chooses it.
	 *
	 * @param items    the items, each its label and its key
	 * @param selected the index of the item chosen now, -1 for none
	 * @param select   choose the item of an index
	 */
	record Palette(String group, String label, List<Item> items, IntSupplier selected, IntConsumer select)
			implements LabControl {

		/**
		 * An item of a palette.
		 *
		 * @param label what it is, in plain words
		 * @param key   its key, {@code null} for none
		 */
		public record Item(String label, LabKey key) {

			/** {@code Wall [#]}: the label and its key in brackets. */
			public String title() {
				return this.key != null ? this.label + " [" + this.key.name() + "]" : this.label;
			}
		}

		public Palette {
			items = List.copyOf(items);
		}

		@Override
		public List<LabKey> keys() {
			final List<LabKey> keys = new ArrayList<>();
			for (final Item item : this.items) {
				if (item.key() != null) {
					keys.add(item.key());
				}
			}
			return List.copyOf(keys);
		}

		/** The label alone: the keys are on the items. */
		@Override
		public String title() {
			return this.label;
		}

		/** The index of the item of {@code key}, -1 when none has it. */
		public int indexOf(final LabKey key) {
			for (int i = 0; i < this.items.size(); i++) {
				if (key.equals(this.items.get(i).key())) {
					return i;
				}
			}
			return -1;
		}
	}

	private static List<LabKey> pair(final LabKey first, final LabKey second) {
		final List<LabKey> keys = new ArrayList<>();
		if (first != null) {
			keys.add(first);
		}
		if (second != null) {
			keys.add(second);
		}
		return List.copyOf(keys);
	}
}
