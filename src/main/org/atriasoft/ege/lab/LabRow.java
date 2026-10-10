package org.atriasoft.ege.lab;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.atriasoft.etk.math.Vector2b;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.widget.Sizer;
import org.atriasoft.ewol.widget.Widget;

/**
 * A row of the control panel that always keeps within its width: its pieces
 * side by side, those that never shrink (small buttons, a check box) at their
 * width, those that shrink (a title, a value, a button with a long text:
 * {@link #addShrinking}) as wide as their text when there is room, the widest
 * shortened first when there is not ({@link LabLayout#share}); the width left
 * goes to the pieces that expand. Each piece is as high as the row when it
 * expands upwards, else centred in it.
 */
final class LabRow extends Sizer {

	/** The pieces that shrink, each with the label whose text is shortened. */
	private final Map<Widget, LabLabel> shrinking = new IdentityHashMap<>();

	LabRow() {
		super(Sizer.DisplayMode.HORIZONTAL);
		setPropertyExpand(new Vector2b(true, false));
		setPropertyFill(new Vector2b(true, false));
	}

	/**
	 * Add a piece that may be narrower than its text: {@code label} itself, or the button that holds it (a piece
	 * that keeps its width is added with {@link #subWidgetAdd}).
	 */
	void addShrinking(final Widget piece, final LabLabel label) {
		this.shrinking.put(piece, label);
		subWidgetAdd(piece);
	}

	@Override
	public void onChangeSize() {
		markToRedraw();
		final List<Widget> pieces = this.subWidget;
		final int count = pieces.size();
		final float[] least = new float[count];
		final float[] natural = new float[count];
		final boolean[] expand = new boolean[count];
		for (int i = 0; i < count; i++) {
			final Widget piece = pieces.get(i);
			if (piece == null || piece.getPropertyHide()) {
				continue;
			}
			least[i] = piece.getCalculateMinSize().x();
			final LabLabel label = this.shrinking.get(piece);
			// A button wants its label's whole text and its own borders.
			natural[i] = label == null ? least[i]
					: least[i] + Math.max(0.0f, label.naturalWidth() - label.getCalculateMinSize().x());
			expand[i] = piece.canExpand().x();
		}
		final Vector2f border = this.propertyBorderSize.getPixel();
		final float height = this.size.y() - 2.0f * border.y();
		final float[] widths = LabLayout.share(least, natural, expand, this.size.x() - 2.0f * border.x());
		float x = this.origin.x() + this.offset.x() + border.x();
		final float bottom = this.origin.y() + this.offset.y() + border.y();
		for (int i = 0; i < count; i++) {
			final Widget piece = pieces.get(i);
			if (piece == null) {
				continue;
			}
			final float width = (float) Math.floor(widths[i]);
			final float pieceHeight = piece.canExpand().y() ? height
					: Math.min(height, piece.getCalculateMinSize().y());
			final float y = bottom + (height - pieceHeight) * 0.5f;
			piece.setOrigin(new Vector2f((float) Math.floor(x), (float) Math.floor(y)));
			piece.setSize(new Vector2f(width, (float) Math.floor(pieceHeight)));
			piece.onChangeSize();
			x += width;
		}
	}
}
