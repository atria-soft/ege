package sample.atriasoft.ege.mapFactory.tools;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector2i;
import org.atriasoft.ewol.compositing.CompositingDrawing;
import org.atriasoft.ewol.compositing.CompositingText;
import org.atriasoft.ewol.resource.font.FontMode;
import org.atriasoft.ewol.widget.ListFileSystem;
import org.atriasoft.ewol.widget.model.ListRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ListFileSystem subclass that supports a "validated" line
 * with distinct visual style (bold text on dark orange background).
 * Validation occurs on double-click (signalFileValidate).
 */
public class MeshFileList extends ListFileSystem {
	private static final Logger LOGGER = LoggerFactory.getLogger(MeshFileList.class);

	private static final Color VALIDATED_BG_COLOR = new Color(0xCC, 0x70, 0x00, 0xFF);
	private static final Color VALIDATED_FG_COLOR = Color.WHITE;

	private int validatedLine = -1;

	public MeshFileList() {
		super();
		// Connect to our own select signal to track the validated line on single click
		this.signalFileSelect.connect(path -> {
			this.validatedLine = this.selectedLine;
			markToRedraw();
		});
		// Connect to our own validate signal to track the validated line on double click
		this.signalFileValidate.connect(path -> {
			this.validatedLine = this.selectedLine;
			LOGGER.info("Validated mesh file: {}", path);
			markToRedraw();
		});
	}

	/**
	 * Get the validated file path, or null if none is validated.
	 */
	public String getValidatedFilePath() {
		if (this.validatedLine < 0) {
			return null;
		}
		int offset = 0;
		if (this.propertyShowFolder) {
			if (this.propertyPath.equals("/")) {
				offset = 1;
			} else {
				offset = 2;
			}
		}
		final int listIndex = this.validatedLine - offset;
		if (listIndex >= 0 && listIndex < this.list.size()) {
			return this.list.get(listIndex).getPath();
		}
		return null;
	}

	@Override
	protected Object getData(final ListRole role, final Vector2i pos) {
		if (pos.y() == this.validatedLine) {
			switch (role) {
				case BgColor:
					return VALIDATED_BG_COLOR;
				case FgColor:
					return VALIDATED_FG_COLOR;
				default:
					break;
			}
		}
		return super.getData(role, pos);
	}

	@Override
	protected void drawElement(final Vector2i pos, final Vector2f start, final Vector2f size) {
		if (pos.y() == this.validatedLine) {
			// Draw background
			if (getData(ListRole.BgColor, pos) instanceof final Color bg) {
				if (getComposeElemnent("drawing") instanceof final CompositingDrawing BGOObjects) {
					BGOObjects.setColor(bg);
					BGOObjects.setPos(new Vector2f(start.x(), start.y()));
					BGOObjects.rectangleWidth(size);
				}
			}
			// Draw text in bold
			if (getData(ListRole.Text, pos) instanceof final String myTextToWrite && !myTextToWrite.isEmpty()) {
				if (getComposeElemnent("text") instanceof final CompositingText tmpText) {
					tmpText.setFontMode(FontMode.BOLD);
					tmpText.setColor(VALIDATED_FG_COLOR);
					tmpText.setPos(new Vector2f(start.x() + this.paddingSizeX, start.y() + this.paddingSizeY));
					tmpText.print(myTextToWrite);
					tmpText.setFontMode(FontMode.REGULAR);
				}
			}
			return;
		}
		super.drawElement(pos, start, size);
	}

	// Factory methods

	public static MeshFileList create() {
		return new MeshFileList();
	}

	public static MeshFileList create(final String path) {
		final MeshFileList list = new MeshFileList();
		list.setPropertyPath(path);
		return list;
	}
}
