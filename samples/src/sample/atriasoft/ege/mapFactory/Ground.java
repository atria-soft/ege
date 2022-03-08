package sample.atriasoft.ege.mapFactory;

import org.atriasoft.etk.math.Vector4f;
import org.atriasoft.loader3d.model.Material;
import org.atriasoft.loader3d.resources.ResourceMeshHeightMap;

public class Ground {
	int sizeX = 16;
	int sizeY = 16;
	float[][] heightMap = new float[this.sizeY][this.sizeX];
	String[][] colorMap = new String[this.sizeY][this.sizeX * 2];
	ResourceMeshHeightMap mesh = new ResourceMeshHeightMap();
	String baseNamePalette = "palette:grass_1";
	String baseNamePalette2 = "palette:grass_2";
	String baseNamePalette3 = "palette:grass_3";
	String baseNamePalette4 = "palette:grass_4";
	
	public Ground() {
		for (int yyy = 0; yyy < this.sizeY; yyy++) {
			for (int xxx = 0; xxx < this.sizeX; xxx++) {
				this.heightMap[yyy][xxx] = 0.0f;
				if (xxx % 2 == 0) {
					this.colorMap[yyy][xxx * 2] = this.baseNamePalette;
					this.colorMap[yyy][xxx * 2 + 1] = this.baseNamePalette2;
				} else {
					this.colorMap[yyy][xxx * 2] = this.baseNamePalette3;
					this.colorMap[yyy][xxx * 2 + 1] = this.baseNamePalette4;
				}
			}
		}
	}
	
	public ResourceMeshHeightMap createMesh() {
		return this.mesh;
	}
	
	public void updateMesh() {
		Material mat = new Material();
		this.mesh.addMaterial(this.baseNamePalette, mat);
		mat.setAmbientFactor(new Vector4f(1, 0, 0, 1.0f));
		mat = new Material();
		this.mesh.addMaterial(this.baseNamePalette2, mat);
		mat.setAmbientFactor(new Vector4f(0, 1, 0, 1.0f));
		mat = new Material();
		this.mesh.addMaterial(this.baseNamePalette3, mat);
		mat.setAmbientFactor(new Vector4f(0, 0, 1, 1.0f));
		mat = new Material();
		this.mesh.addMaterial(this.baseNamePalette4, mat);
		mat.setAmbientFactor(new Vector4f(1, 1, 0, 1.0f));
		try {
			this.mesh.udateData(this.heightMap, this.colorMap, this.sizeX, this.sizeY);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
