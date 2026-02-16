package sample.atriasoft.ege.mapFactory;

import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;

import sample.atriasoft.ege.mapFactory.model.Map;

public class PlaceEntityAction implements MapAction {
	private final Entity entity;
	private final String meshPath;
	private final Map map;
	private final Environement env;

	public PlaceEntityAction(final Entity entity, final String meshPath, final Map map, final Environement env) {
		this.entity = entity;
		this.meshPath = meshPath;
		this.map = map;
		this.env = env;
	}

	@Override
	public void undo() {
		this.env.rmEntity(this.entity);
		this.map.placedEntities.remove(this.entity);
		this.map.entityMeshPaths.remove(this.entity);
	}

	@Override
	public void redo() {
		this.env.addEntity(this.entity);
		this.map.placedEntities.add(this.entity);
		this.map.entityMeshPaths.put(this.entity, this.meshPath);
	}
}
