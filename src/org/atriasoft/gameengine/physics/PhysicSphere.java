package org.atriasoft.gameengine.physics;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;

public class PhysicSphere extends PhysicShape {
	// Box size property in X, Y and Z
	private float size;
	public PhysicSphere() {
		super(PhysicShapeType.SPHERE);
	}
	public float getSize() {
		return size;
	}
	public void setSize(float size) {
		this.size = size;
	}
	@Override
	public void updateAABB(Transform3D transform, PhysicCollisionAABB aabb) {
		aabb.update(transform.multiply(this.transform.getPosition()).addNew(new Vector3f(this.size,0,0)));
		aabb.update(transform.multiply(this.transform.getPosition()).addNew(new Vector3f(-this.size,0,0)));
		aabb.update(transform.multiply(this.transform.getPosition()).addNew(new Vector3f(0,this.size,0)));
		aabb.update(transform.multiply(this.transform.getPosition()).addNew(new Vector3f(0,-this.size,0)));
		aabb.update(transform.multiply(this.transform.getPosition()).addNew(new Vector3f(0,0,this.size)));
		aabb.update(transform.multiply(this.transform.getPosition()).addNew(new Vector3f(0,0,-this.size)));
	}
	@Override
	public void updateForNarrowCollision(Transform3D transform) {
		
	}
	@Override
	public void renderDebug(Transform3D transform, ResourceColored3DObject debugDrawProperty) {
		debugDrawProperty.drawSphere(this.size, 9, 9, this.transform.getOpenGLMatrix().multiplyNew(transform.getOpenGLMatrix()), new Color(0,1,0,1));
		
	}
}