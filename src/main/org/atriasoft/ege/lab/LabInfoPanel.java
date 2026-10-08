package org.atriasoft.ege.lab;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.compositing.CompositingGC;
import org.atriasoft.ewol.compositing.CompositingText;

/**
 * The texts drawn over the 3D view of a lab with ewol's compositing layers:
 * the info panel (figures, checks, errors: {@link LabText.Line}s) in the top
 * left corner, the help of the keys (F1) in the top right one. A line too
 * long for its box is wrapped between words. Laid out on the GUI thread
 * ({@link #build}, again only when something changed), drawn on the
 * rendering thread ({@link #draw}).
 */
final class LabInfoPanel {

	private static final int FONT = 13;
	private static final int TITLE_FONT = 16;
	/** Width of a box, pixels (narrower on a narrow view). */
	static final float WIDTH = 380.0f;
	static final float MARGIN = 10.0f;
	private static final float PADDING = 8.0f;
	private static final Color BACK = new Color(0.0f, 0.0f, 0.0f, 0.6f);
	private static final Color SHADOW = new Color(0.0f, 0.0f, 0.0f, 0.65f);
	private static final Color TITLE = new Color(1.0f, 0.88f, 0.6f, 1.0f);
	private static final Color NORMAL = new Color(0.92f, 0.92f, 0.92f, 1.0f);
	private static final Color GOOD = new Color(0.45f, 0.95f, 0.45f, 1.0f);
	private static final Color WARN = new Color(1.0f, 0.82f, 0.35f, 1.0f);
	private static final Color BAD = new Color(1.0f, 0.42f, 0.32f, 1.0f);
	private static final Color DIM = new Color(0.72f, 0.74f, 0.78f, 1.0f);

	private final CompositingGC shapes = new CompositingGC();
	private final CompositingText text = new CompositingText("", FONT);
	private final CompositingText title = new CompositingText("", TITLE_FONT);
	private Vector2f builtSize;
	private List<LabText.Line> builtInfo;
	private List<LabText.Line> builtHelp;
	private boolean released;

	/** Lay the boxes out for a view of {@code size} pixels; the help box only when {@code help} is not empty. */
	void build(final Vector2f size, final List<LabText.Line> info, final List<LabText.Line> help) {
		if (this.released || size.equals(this.builtSize) && info.equals(this.builtInfo) && help.equals(this.builtHelp)) {
			return;
		}
		this.builtSize = size;
		this.builtInfo = List.copyOf(info);
		this.builtHelp = List.copyOf(help);
		this.shapes.clear();
		this.text.clear();
		this.title.clear();
		final float width = width(size);
		if (width >= 120.0f) {
			if (!info.isEmpty()) {
				box(MARGIN, size.y() - MARGIN, width, info);
			}
			if (!help.isEmpty()) {
				box(size.x() - MARGIN - width, size.y() - MARGIN, width, help);
			}
		}
		this.shapes.flush();
		this.text.flush();
		this.title.flush();
	}

	/** Width of a box in a view of {@code size} pixels. */
	static float width(final Vector2f size) {
		return Math.min(WIDTH, (size.x() - 3.0f * MARGIN) * 0.5f);
	}

	/** A box of {@code lines} whose top-left corner is {@code (x, top)}. */
	private void box(final float x, final float top, final float width, final List<LabText.Line> lines) {
		final float inner = width - 2.0f * PADDING;
		final List<LabText.Line> wrapped = new ArrayList<>();
		for (final LabText.Line line : lines) {
			for (final String part : wrap(LabText.ascii(line.text()), inner, fontOf(line))) {
				wrapped.add(new LabText.Line(part, line.kind()));
			}
		}
		float height = 2.0f * PADDING;
		for (final LabText.Line line : wrapped) {
			height += fontOf(line).getHeight();
		}
		this.shapes.setColor(BACK);
		this.shapes.setPos(x, top - height);
		this.shapes.rectangle(x + width, top);
		float y = top - PADDING;
		for (final LabText.Line line : wrapped) {
			y -= fontOf(line).getHeight();
			if (!line.text().isEmpty()) {
				print(fontOf(line), line.text(), x + PADDING, y, colorOf(line.kind()));
			}
		}
	}

	private CompositingText fontOf(final LabText.Line line) {
		return line.kind() == LabText.Kind.TITLE ? this.title : this.text;
	}

	private static void print(final CompositingText font, final String line, final float x, final float y,
			final Color color) {
		font.setColor(SHADOW);
		font.setPos(new Vector2f(x + 1.0f, y - 1.0f));
		font.print(line);
		font.setColor(color);
		font.setPos(new Vector2f(x, y));
		font.print(line);
	}

	/**
	 * {@code line} cut between words into pieces no wider than {@code width},
	 * the pieces after the first indented; a word too long (a path) is cut
	 * between its letters.
	 */
	private static List<String> wrap(final String line, final float width, final CompositingText font) {
		final List<String> parts = new ArrayList<>();
		if (line.isEmpty() || font.calculateSize(line).x() <= width) {
			parts.add(line);
			return parts;
		}
		int start = 0;
		while (start < line.length() && line.charAt(start) == ' ') {
			start++;
		}
		final String indent = line.substring(0, start);
		final StringBuilder current = new StringBuilder(indent);
		for (final String word : line.substring(start).split(" ")) {
			final String candidate = current.toString().isBlank() ? current + word : current + " " + word;
			if (font.calculateSize(candidate).x() <= width) {
				current.setLength(0);
				current.append(candidate);
				continue;
			}
			if (!current.isEmpty() && !current.toString().isBlank()) {
				parts.add(current.toString());
				current.setLength(0);
			}
			String rest = (parts.isEmpty() ? indent : indent + "  ") + word;
			while (font.calculateSize(rest).x() > width && rest.length() > 3) {
				int cut = rest.length() - 1;
				while (cut > 3 && font.calculateSize(rest.substring(0, cut)).x() > width) {
					cut--;
				}
				parts.add(rest.substring(0, cut));
				rest = indent + "  " + rest.substring(cut);
			}
			current.setLength(0);
			current.append(rest);
		}
		if (!current.isEmpty()) {
			parts.add(current.toString());
		}
		return parts;
	}

	private static Color colorOf(final LabText.Kind kind) {
		return switch (kind) {
			case TITLE -> TITLE;
			case NORMAL -> NORMAL;
			case GOOD -> GOOD;
			case WARN -> WARN;
			case BAD -> BAD;
			case DIM -> DIM;
		};
	}

	/** Draw what {@link #build} laid out. */
	void draw() {
		if (this.released) {
			return;
		}
		this.shapes.draw(true);
		this.text.draw(true);
		this.title.draw(true);
	}

	/** Give the layers back (from any thread); nothing is built nor drawn afterwards. */
	void release() {
		if (!this.released) {
			this.released = true;
			this.shapes.release();
			this.text.release();
			this.title.release();
		}
	}
}
