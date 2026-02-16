package sample.atriasoft.ege.mapFactory;

import java.nio.file.Path;
import java.util.function.Consumer;

import org.atriasoft.ege.Entity;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector2i;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.resource.ResourceColorFile;
import org.atriasoft.ewol.widget.WidgetList;
import org.atriasoft.ewol.widget.model.ListRole;
import org.atriasoft.gale.key.KeyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sample.atriasoft.ege.mapFactory.model.Map;

public class EntityListWidget extends WidgetList {
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityListWidget.class);

	private final Map map;
	private final Consumer<Entity> onEntitySelected;
	private int selectedLine = -1;

	private final ResourceColorFile colorProperty;
	private final int colorIdText;
	private final int colorIdBackground1;
	private final int colorIdBackground2;
	private final int colorIdBackgroundSelected;

	public EntityListWidget(final Map map, final Consumer<Entity> onEntitySelected) {
		this.map = map;
		this.onEntitySelected = onEntitySelected;
		this.colorProperty = new ResourceColorFile(new Uri("THEME", "/color/ListFileSystem.json", "ewol"));
		this.colorIdText = this.colorProperty.request("text");
		this.colorIdBackground1 = this.colorProperty.request("background1");
		this.colorIdBackground2 = this.colorProperty.request("background2");
		this.colorIdBackgroundSelected = this.colorProperty.request("selected");
		setMouseLimit(1);
	}

	@Override
	protected Vector2i getMatrixSize() {
		return new Vector2i(1, this.map.placedEntities.size());
	}

	@Override
	protected Color getBasicBG() {
		return this.colorProperty.get(this.colorIdBackground1);
	}

	@Override
	protected Object getData(final ListRole role, final Vector2i pos) {
		final int index = pos.y();
		if (index < 0 || index >= this.map.placedEntities.size()) {
			return null;
		}
		switch (role) {
			case Text: {
				final Entity entity = this.map.placedEntities.get(index);
				final String meshPath = this.map.entityMeshPaths.get(entity);
				if (meshPath == null) {
					return "Entity #" + index;
				}
				final String fileName = Path.of(meshPath).getFileName().toString();
				return index + ": " + fileName;
			}
			case FgColor:
				return this.colorProperty.get(this.colorIdText);
			case BgColor:
				if (this.selectedLine == index) {
					return this.colorProperty.get(this.colorIdBackgroundSelected);
				}
				if (index % 2 == 0) {
					return this.colorProperty.get(this.colorIdBackground1);
				}
				return this.colorProperty.get(this.colorIdBackground2);
			default:
				break;
		}
		return null;
	}

	@Override
	protected boolean onItemEvent(final EventInput event, final Vector2i pos, final Vector2f mousePosition) {
		if (event.inputId() == 1 && event.status() == KeyStatus.pressSingle) {
			final int index = pos.y();
			if (index >= 0 && index < this.map.placedEntities.size()) {
				this.selectedLine = index;
				final Entity entity = this.map.placedEntities.get(index);
				LOGGER.info("Entity selected from list: {}", index);
				this.onEntitySelected.accept(entity);
				markToRedraw();
				return true;
			}
		}
		return false;
	}

	public void refresh() {
		markToRedraw();
	}

	public void setSelectedEntity(final Entity entity) {
		if (entity == null) {
			this.selectedLine = -1;
		} else {
			this.selectedLine = this.map.placedEntities.indexOf(entity);
		}
		markToRedraw();
	}
}
