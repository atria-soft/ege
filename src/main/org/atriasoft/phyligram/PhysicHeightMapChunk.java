package org.atriasoft.phyligram;

import org.atriasoft.ege.geometry.Triangle;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.phyligram.shape.AABB;

public class PhysicHeightMapChunk extends PhysicShape {
	// Box size property in X, Y and Z
	private Triangle[] triangles = null;
	public Triangle[] trianglesGlobalPos = null;
	
	public PhysicHeightMapChunk() {
		
	}
	
	public PhysicHeightMapChunk(Triangle[] triangles) {
		this.triangles = triangles;
	}
	
	public Triangle[] getTriangles() {
		return this.triangles;
	}
	
	@Override
	public void renderDebug(Transform3D transformGlobal, ResourceColored3DObject debugDrawProperty) {
		
	}
	
	public void setTriangles(Triangle[] triangles) {
		this.triangles = triangles;
		this.trianglesGlobalPos = new Triangle[triangles.length];
		for (int iii = 0; iii < this.triangles.length; iii++) {
			Vector3f data1 = this.transformGlobal.multiply(this.transform.multiply(this.triangles[iii].p1));
			Vector3f data2 = this.transformGlobal.multiply(this.transform.multiply(this.triangles[iii].p2));
			Vector3f data3 = this.transformGlobal.multiply(this.transform.multiply(this.triangles[iii].p3));
			this.trianglesGlobalPos[iii] = new Triangle(data1, data2, data3);
		}
	}
	
	@Override
	public void updateAABB(Transform3D transformGlobal, AABB aabb) {
		// store it, many time usefull...
		this.transformGlobal = transformGlobal;
		this.colisionPoints.clear();
		for (int iii = 0; iii < this.triangles.length; iii++) {
			aabb.update(transformGlobal.multiply(this.transform.multiply(this.triangles[iii].p1)));
			aabb.update(transformGlobal.multiply(this.transform.multiply(this.triangles[iii].p2)));
			aabb.update(transformGlobal.multiply(this.transform.multiply(this.triangles[iii].p3)));
		}
	}
	
	@Override
	public void updateForNarrowCollision(Transform3D transformGlobal) {
		for (int iii = 0; iii < this.triangles.length; iii++) {
			this.trianglesGlobalPos[iii].p1 = this.transformGlobal.multiply(this.transform.multiply(this.triangles[iii].p1));
			this.trianglesGlobalPos[iii].p2 = this.transformGlobal.multiply(this.transform.multiply(this.triangles[iii].p2));
			this.trianglesGlobalPos[iii].p3 = this.transformGlobal.multiply(this.transform.multiply(this.triangles[iii].p3));
		}
	}
	
}
