package sample.atriasoft.ege.mapFactory;

import java.util.function.BiFunction;

import org.atriasoft.ege.geometry.Ray;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.etk.math.Vector4f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.loader3d.model.Material;
import org.atriasoft.loader3d.resources.ResourceMeshHeightMap;

import toolbox.Maths;

public class Ground {
	public int sizeX = 64;
	public int sizeY = 64;
	public float[][] heightMap = new float[this.sizeY][this.sizeX];
	public String[][] colorMap = new String[this.sizeY][this.sizeX * 2];
	ResourceMeshHeightMap mesh = new ResourceMeshHeightMap();
	public String baseNamePalette = "palette:grass_1";
	public String baseNamePalette2 = "palette:grass_2";
	public String baseNamePalette3 = "palette:grass_3";
	public String baseNamePalette4 = "palette:grass_4";
	
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
	
	public void reset() {
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
		updateMesh();
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
	
	/**
	 * Get the height at a given X,Y position using bilinear interpolation.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @return interpolated height at the position
	 */
	public float getHeightAt(final float x, final float y) {
		// Clamp to valid range
		final float clampedX = Math.max(0, Math.min(x, this.sizeX - 1));
		final float clampedY = Math.max(0, Math.min(y, this.sizeY - 1));

		// Get integer coordinates
		final int x0 = (int) Math.floor(clampedX);
		final int y0 = (int) Math.floor(clampedY);
		final int x1 = Math.min(x0 + 1, this.sizeX - 1);
		final int y1 = Math.min(y0 + 1, this.sizeY - 1);

		// Get fractional part
		final float fx = clampedX - x0;
		final float fy = clampedY - y0;

		// Bilinear interpolation
		final float h00 = this.heightMap[y0][x0];
		final float h10 = this.heightMap[y0][x1];
		final float h01 = this.heightMap[y1][x0];
		final float h11 = this.heightMap[y1][x1];

		final float h0 = h00 * (1 - fx) + h10 * fx;
		final float h1 = h01 * (1 - fx) + h11 * fx;

		return h0 * (1 - fy) + h1 * fy;
	}

	/**
	 * Find the intersection point between a ray and the heightmap using ray marching.
	 * Walks along the ray and detects when it crosses below the terrain surface,
	 * then refines with binary search.
	 * @param ray The ray to intersect with the terrain
	 * @return The intersection point on the terrain, or null if no intersection
	 */
	public Vector3f intersectRay(final Ray ray) {
		final float step = 0.5f;
		final float maxDistance = 500.0f;
		boolean wasAbove = true;
		float prevT = 0.0f;
		for (float t = 0.0f; t < maxDistance; t += step) {
			final Vector3f point = ray.origin().add(ray.direction().multiply(t));
			final float px = point.x();
			final float py = point.y();
			// Only test when within (or near) the heightmap bounds
			if (px >= -1 && px <= this.sizeX && py >= -1 && py <= this.sizeY) {
				final float terrainHeight = getHeightAt(px, py);
				final boolean isAbove = point.z() > terrainHeight;
				if (wasAbove && !isAbove) {
					// Crossed the terrain — binary search between prevT and t
					float tLow = prevT;
					float tHigh = t;
					for (int i = 0; i < 16; i++) {
						final float tMid = (tLow + tHigh) * 0.5f;
						final Vector3f midPoint = ray.origin().add(ray.direction().multiply(tMid));
						if (midPoint.z() > getHeightAt(midPoint.x(), midPoint.y())) {
							tLow = tMid;
						} else {
							tHigh = tMid;
						}
					}
					final float tFinal = (tLow + tHigh) * 0.5f;
					final Vector3f hit = ray.origin().add(ray.direction().multiply(tFinal));
					return new Vector3f(hit.x(), hit.y(), getHeightAt(hit.x(), hit.y()));
				}
				wasAbove = isAbove;
			}
			prevT = t;
		}
		return null;
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
