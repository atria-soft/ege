package sample.atriasoft.ege.mapFactory.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;

import sample.atriasoft.ege.mapFactory.Ground;

public class Map {
	public Ground ground = new Ground();
	public final List<Entity> placedEntities = new ArrayList<>();
	public final java.util.Map<Entity, String> entityMeshPaths = new HashMap<>();

	public void reset(final Environement env) {
		for (final Entity entity : this.placedEntities) {
			env.rmEntity(entity);
		}
		this.placedEntities.clear();
		this.entityMeshPaths.clear();
		this.ground.reset();
	}

	public void updateMesh() {
		this.ground.updateMesh();
	}

	/**
	 * Update the Y position of all placed entities to match the current heightmap.
	 * Call this after modifying the heightmap so objects follow the terrain.
	 */
	public void updateEntityPositions() {
		for (final Entity entity : this.placedEntities) {
			final ComponentPosition posComp = (ComponentPosition) entity.getComponent("position");
			if (posComp == null) {
				continue;
			}
			final Transform3D current = posComp.getTransform();
			final Vector3f pos = current.position();
			final float newY = this.ground.getHeightAt(pos.x(), pos.z());
			if (pos.y() != newY) {
				posComp.setTransform(new Transform3D(new Vector3f(pos.x(), newY, pos.z()), current.orientation(), current.scale()));
			}
		}
	}
}
