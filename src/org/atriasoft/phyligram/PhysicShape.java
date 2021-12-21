package org.atriasoft.phyligram;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.phyligram.shape.AABB;

public abstract class PhysicShape {
	
	protected List<Collision> colisionPoints = new ArrayList<>();
	// protected Quaternion quaternion;
	// protected Vector3f origin;
	protected Transform3D transform;
	protected Transform3D transformGlobal;
	protected float mass = 0;
	
	public PhysicShape() {
		this.transform = Transform3D.IDENTITY;
		// this.quaternion = Quaternion.identity();
		// this.origin = Vector3f.zero();
		this.mass = 0;
	}
	
	public PhysicShape(Quaternion quaternion, Vector3f origin, float mass) {
		this.transform = new Transform3D(origin, quaternion);
		// this.quaternion = quaternion;
		// this.origin = origin;
		this.mass = mass;
	}
	
	public void addColision(Collision colision) {
		this.colisionPoints.add(colision);
	}
	
	public float getMass() {
		return this.mass;
	}
	
	public Vector3f getOrigin() {
		return this.transform.getPosition();
	}
	
	public Quaternion getQuaternion() {
		return this.transform.getOrientation();
	}
	
	public Quaternion getQuaternionFull() {
		return this.transformGlobal.getOrientation().multiply(this.transform.getOrientation());
	}
	
	public Transform3D getTransform() {
		return this.transform;
	}
	
	public Transform3D getTransformGlobal() {
		return this.transformGlobal;
	}
	
	public abstract void renderDebug(Transform3D transform, ResourceColored3DObject debugDrawProperty);
	
	public void setMass(float mass) {
		this.mass = mass;
	}
	
	public void setOrigin(Vector3f origin) {
		this.transform = this.transform.withPosition(origin);
	}
	
	public void setQuaternion(Quaternion quaternion) {
		this.transform = this.transform.withOrientation(quaternion);
	}
	
	public void setTransform(Transform3D transform) {
		this.transform = transform;
	}
	
	public void setTransformGlobal(Transform3D transform) {
		this.transformGlobal = transform;
	}
	
	public abstract void updateAABB(Transform3D transform, AABB aabb);
	
	public abstract void updateForNarrowCollision(Transform3D transform);
	
}
