package org.atriasoft.phyligram;

import org.atriasoft.ege.geometry.Triangle;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.phyligram.shape.AABB;

public class PhysicTriangle extends PhysicShape {
	// Box size property in X, Y and Z
	private Triangle triangle;
	
	// only needed for the narrow phase calculation ...
	public Triangle narrowPhaseGlobalTriangle;
	
	public PhysicTriangle() {}
	
	public Triangle getTriangle() {
		return this.triangle;
	}
	
	public Triangle getTriangleGlobalPos() {
		return this.narrowPhaseGlobalTriangle;
	}
	
	@Override
	public void renderDebug(Transform3D transform, ResourceColored3DObject debugDrawProperty) {
		debugDrawProperty.drawTriangle(this.triangle.p1, this.triangle.p2, this.triangle.p3, this.transform.getOpenGLMatrix().multiply(transform.getOpenGLMatrix()), new Color(0, 1, 0, 1));
		
	}
	
	public void setPoints(Vector3f p1, Vector3f p2, Vector3f p3) {
		this.triangle = new Triangle(p1, p2, p3);
	}
	
	public void setTriangle(Triangle data) {
		this.triangle = data.clone();
	}
	
	@Override
	public void updateAABB(Transform3D transformGlobal, AABB aabb) {
		// store it, many time usefull...
		this.transformGlobal = transformGlobal;
		Vector3f basePositionP1 = transformGlobal.multiply(this.transform.getPosition().add(this.triangle.p1));
		Vector3f basePositionP2 = transformGlobal.multiply(this.transform.getPosition().add(this.triangle.p2));
		Vector3f basePositionP3 = transformGlobal.multiply(this.transform.getPosition().add(this.triangle.p3));
		this.narrowPhaseGlobalTriangle = new Triangle(basePositionP1, basePositionP2, basePositionP3);
		aabb.update(basePositionP1);
		aabb.update(basePositionP2);
		aabb.update(basePositionP3);
	}
	
	@Override
	public void updateForNarrowCollision(Transform3D transform) {
		Vector3f basePositionP1 = this.transformGlobal.multiply(this.transform.multiply(this.triangle.p1));
		Vector3f basePositionP2 = this.transformGlobal.multiply(this.transform.multiply(this.triangle.p2));
		Vector3f basePositionP3 = this.transformGlobal.multiply(this.transform.multiply(this.triangle.p3));
		this.narrowPhaseGlobalTriangle = new Triangle(basePositionP1, basePositionP2, basePositionP3);
	}
}