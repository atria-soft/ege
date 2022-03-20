package sample.atriasoft.ege.mapFactory;

import java.util.function.BiFunction;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.etk.math.Vector4f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.loader3d.model.Material;
import org.atriasoft.loader3d.resources.ResourceMeshHeightMap;

import toolbox.Maths;

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
	
	public void changeHeightOfElement(Vector3f position, float distance, BiFunction<Float, Float, Float> applyer) {
		for (int yyy = 0; yyy < this.sizeY; yyy++) {
			for (int xxx = 0; xxx < this.sizeX; xxx++) {
				float offset = 0.0f;
				if (xxx % 2 == 1) {
					offset = 0.5f;
				}
				float dist2 = position.less(xxx, yyy + offset, 0).length2();
				if (dist2 < distance * distance) {
					this.heightMap[yyy][xxx] = applyer.apply(this.heightMap[yyy][xxx], Maths.sqrt(dist2));
				}
			}
		}
		updateMesh();
	}
	
	public ResourceMeshHeightMap createMesh() {
		return this.mesh;
	}
	
	public void drawDynamicElement(ResourceColored3DObject dynamicElement, Vector3f position, float distance) {
		for (int yyy = 0; yyy < this.sizeY; yyy++) {
			for (int xxx = 0; xxx < this.sizeX; xxx++) {
				float dist2 = position.less(xxx, yyy, 0).length2();
				if (dist2 < distance * distance) {
					float coneHeight = 0.6f;
					float offset = 0.0f;
					if (xxx % 2 == 1) {
						offset = 0.5f;
					}
					Transform3D tmpTransform = new Transform3D(new Vector3f(xxx, yyy + offset, this.heightMap[yyy][xxx] + coneHeight * 0.5f));
					dynamicElement.drawCone(coneHeight * 0.5f, coneHeight, 10, 3, tmpTransform.getOpenGLMatrix(), Color.RED);
				}
			}
		}
		
	}
	
	public void updateMesh() {
		this.mesh.clearData();
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
