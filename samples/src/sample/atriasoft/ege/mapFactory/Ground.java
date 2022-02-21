package sample.atriasoft.ege.mapFactory;

import org.atriasoft.etk.math.Vector4f;
import org.atriasoft.loader3d.model.Material;
import org.atriasoft.loader3d.resources.ResourceMeshHeightMap;

public class Ground {
	int width = 512;
	int length = 512;
	float[][] heightMap = new float[512][512];
	String[][] colorMap = new String[512][512 * 2];
	ResourceMeshHeightMap mesh = new ResourceMeshHeightMap();
	
	public Ground() {
		for (int yyy = 0; yyy < this.length; yyy++) {
			for (int xxx = 0; xxx < this.width; xxx++) {
				this.heightMap[yyy][xxx] = 0.0f;
				this.colorMap[yyy][xxx] = "grass_1";
			}
		}
	}
	
	public ResourceMeshHeightMap createMesh() {
		return this.mesh;
	}
	
	public void updateMesh() {
		Material mat = new Material();
		this.mesh.addMaterial("grass_1", mat);
		mat.setAmbientFactor(new Vector4f(1.0f, 0, 0, 1.0f));
	}
	
}
