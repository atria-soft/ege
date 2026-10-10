package org.atriasoft.ege.lab;

import org.atriasoft.esignal.SignalEmpty;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.Gravity;
import org.atriasoft.ewol.compositing.CompositingText;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.resource.ResourceColorFile;
import org.atriasoft.ewol.widget.Widget;
import org.atriasoft.gale.key.KeyStatus;

/**
 * A line of text of the control panel that is never clipped: drawn whole
 * when its width allows, else without its keys in brackets, else cut and
 * ended by {@code ...} ({@link LabText#fit}). A label that shrinks asks for little
 * width ({@link #FLOOR}) and tells the width it would like
 * ({@link #naturalWidth()}): a {@link LabRow} gives it what is left beside the
 * buttons, a column its whole width. A label that never shrinks (the words of
 * a small button: Prev, Next, -, +) asks for its whole text. Drawn in the
 * colours of ewol's labels, plain text (no markup) in the letters the fonts
 * draw ({@link LabText#ascii}), bold or not.
 */
final class LabLabel extends Widget {

	/** The least width of a label that shrinks, pixels: a letter and {@code ...}. */
	static final float FLOOR = 28.0f;
	/** Room around the text, pixels, as ewol's labels keep it. */
	private static final float LEFT = 2.0f;
	private static final float SIDES = 6.0f;
	private static final float BOTTOM = 4.0f;
	private static final float HEIGHT_MORE = 6.0f;

	/** A click on the words (input 1). */
	public final SignalEmpty signalPressed = new SignalEmpty();

	private final CompositingText text;
	private final boolean bold;
	private final boolean shrinks;
	/** The least width it would like, pixels (a value that keeps its buttons in place). */
	private final float leastWish;
	private final ResourceColorFile colors;
	private final int foreground;
	private final int background;
	private String value;
	private float natural;

	/**
	 * @param value     the text (plain: no markup; accents are taken off)
	 * @param fontSize  the size of its font, 0 for the size of the panel
	 * @param bold      whether it is drawn in bold (a heading)
	 * @param shrinks   whether it may be shortened (else it asks for its whole text)
	 * @param leastWish the least width it would like when there is room, pixels
	 */
	LabLabel(final String value, final int fontSize, final boolean bold, final boolean shrinks, final float leastWish) {
		this.text = new CompositingText("", fontSize);
		this.bold = bold;
		this.shrinks = shrinks;
		this.leastWish = leastWish;
		this.value = LabText.ascii(value);
		this.colors = own(ResourceColorFile.create(new Uri("THEME", "/color/Label.json", "ewol")));
		this.foreground = this.colors != null ? this.colors.request("foreground") : -1;
		this.background = this.colors != null ? this.colors.request("background") : -1;
		setMouseLimit(1);
		setPropertyCanFocus(false);
		setPropertyGravity(Gravity.LEFT);
	}

	/** A label of the panel that shrinks, in its font. */
	static LabLabel shrinking(final String value) {
		return new LabLabel(value, 0, false, true, 0.0f);
	}

	/** The text shown (before it is shortened). */
	String text() {
		return this.value;
	}

	/** Show {@code next} (in the letters the fonts draw: {@link LabText#ascii}; laid out again when it changed). */
	void setText(final String next) {
		final String value = LabText.ascii(next);
		if (value.equals(this.value)) {
			return;
		}
		this.value = value;
		markToRedraw();
		requestUpdateSize();
	}

	/** The width it would like: its whole text, pixels (known once laid out). */
	float naturalWidth() {
		return this.natural;
	}

	/** Back to the first state of the text before a measure or a print: position, colours, mode. */
	private void prepare() {
		if (this.colors != null) {
			this.text.setDefaultColorFg(this.colors.get(this.foreground));
			this.text.setDefaultColorBg(this.colors.get(this.background));
		}
		this.text.reset();
		this.text.setFontBold(this.bold);
	}

	private float measure(final String shown) {
		prepare();
		return this.text.calculateSize(shown).x();
	}

	@Override
	public void calculateMinMaxSize() {
		super.calculateMinMaxSize();
		// Whole pixels: the parents give whole widths, a fraction short would shorten the text.
		this.natural = (float) Math.ceil(Math.max(this.leastWish, measure(this.value) + SIDES));
		final float width = this.shrinks ? Math.min(this.natural, FLOOR) : this.natural;
		prepare();
		this.minSize = Vector2f.max(this.minSize, new Vector2f(width, this.text.getHeight() + HEIGHT_MORE));
	}

	@Override
	protected void onDraw() {
		this.text.draw();
	}

	@Override
	public boolean onEventInput(final EventInput event) {
		if (event.inputId() == 1 && event.status() == KeyStatus.pressSingle) {
			this.signalPressed.emit();
			return true;
		}
		return false;
	}

	@Override
	public void onRegenerateDisplay() {
		if (!needRedraw()) {
			return;
		}
		this.text.clear();
		final String shown = LabText.fit(this.value, this.size.x() - SIDES, this::measure);
		final float width = measure(shown);
		final float height = this.text.getHeight();
		final float x = switch (this.propertyGravity.x()) {
			case CENTER -> (this.size.x() - width) * 0.5f;
			case RIGHT -> this.size.x() - LEFT - width;
			default -> LEFT;
		};
		final float y = (this.size.y() - height - HEIGHT_MORE) * 0.5f + BOTTOM;
		prepare();
		this.text.setPos(new Vector2f((float) Math.floor(x), (float) Math.floor(y)));
		this.text.setClipping(Vector2f.ZERO, this.size);
		this.text.print(shown);
		this.text.flush();
	}
}
