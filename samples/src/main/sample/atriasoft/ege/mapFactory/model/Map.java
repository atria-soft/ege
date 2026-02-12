package sample.atriasoft.ege.mapFactory.model;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.ege.Entity;

import sample.atriasoft.ege.mapFactory.Ground;

public class Map {
	public Ground ground = new Ground();
	public final List<Entity> placedEntities = new ArrayList<>();
	
	public void updateMesh() {
		this.ground.updateMesh();
	}
	
}
