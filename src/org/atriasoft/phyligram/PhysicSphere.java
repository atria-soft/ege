package org.atriasoft.phyligram;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.phyligram.shape.AABB;

public class PhysicSphere extends PhysicShape {
	// Box size property in X, Y and Z
	private float size;
	
	// only needed for the narrow phase calculation ...
	public Vector3f narrowPhaseGlobalPos;
	
	public PhysicSphere() {}
	
	public float getSize() {
		return this.size;
	}
	
	@Override
	public void renderDebug(Transform3D transform, ResourceColored3DObject debugDrawProperty) {
		debugDrawProperty.drawSphere(this.size, 9, 9, this.transform.getOpenGLMatrix().multiply(transform.getOpenGLMatrix()), new Color(0, 1, 0, 1));
		
	}
	
	public void setSize(float size) {
		this.size = size;
	}
	
	@Override
	public void updateAABB(Transform3D transformGlobal, AABB aabb) {
		// store it, many time usefull...
		this.transformGlobal = transformGlobal;
		Vector3f basePosition = transformGlobal.multiply(this.transform.getPosition());
		aabb.update(basePosition.add(this.size, 0, 0));
		aabb.update(basePosition.add(-this.size, 0, 0));
		aabb.update(basePosition.add(0, this.size, 0));
		aabb.update(basePosition.add(0, -this.size, 0));
		aabb.update(basePosition.add(0, 0, this.size));
		aabb.update(basePosition.add(0, 0, -this.size));
	}
	
	@Override
	public void updateForNarrowCollision(Transform3D transform) {
		this.narrowPhaseGlobalPos = this.transformGlobal.multiply(this.transform.multiply(new Vector3f(0, 0, 0)));
	}
}