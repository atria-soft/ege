package sample.atriasoft.ege.mapFactory.model;

import sample.atriasoft.ege.mapFactory.Ground;

public class Map {
	public Ground ground = new Ground();
	
	public void updateMesh() {
		this.ground.updateMesh();
	}
	
}
