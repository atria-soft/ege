package sample.atriasoft.ege.mapFactory;

import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.components.ComponentMesh;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderMeshPalette;
import org.atriasoft.ege.components.ComponentTexturePalette;
import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Transform3D;

import sample.atriasoft.ege.mapFactory.model.Map;

public class DeleteEntityAction implements MapAction {
	private final Entity entity;
	private final String meshPath;
	private final Transform3D transform;
	private final Map map;
	private final ApplScene scene;

	public DeleteEntityAction(final Entity entity, final String meshPath, final Transform3D transform,
			final Map map, final ApplScene scene) {
		this.entity = entity;
		this.meshPath = meshPath;
		this.transform = transform;
		this.map = map;
		this.scene = scene;
	}

	@Override
	public void undo() {
		// Recreate the entity with all its components
		final Environement env = this.scene.getEnvironement();
		this.entity.addComponent(new ComponentPosition(this.transform));
		final Uri meshUri = new Uri("FILE", this.meshPath);
		this.entity.addComponent(new ComponentMesh(meshUri));
		this.entity.addComponent(new ComponentTexturePalette(meshUri));
		this.entity.addComponent(new ComponentRenderMeshPalette(
				new Uri("DATA", "basicPalette.vert"),
				new Uri("DATA", "basicPalette.frag"),
				(EngineLight) env.getEngine(EngineLight.ENGINE_NAME)));
		env.addEntity(this.entity);
		this.map.placedEntities.add(this.entity);
		this.map.entityMeshPaths.put(this.entity, this.meshPath);
	}

	@Override
	public void redo() {
		final Environement env = this.scene.getEnvironement();
		env.rmEntity(this.entity);
		this.map.placedEntities.remove(this.entity);
		this.map.entityMeshPaths.remove(this.entity);
	}
}
