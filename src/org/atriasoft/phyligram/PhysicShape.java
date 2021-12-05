package org.atriasoft.phyligram;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;




public abstract class PhysicShape {

	
	
	protected List<Collision> colisionPoints = new ArrayList<>();
	// protected Quaternion quaternion;
	// protected Vector3f origin;
	protected Transform3D transform;
	protected Transform3D transformGlobal;
	protected float mass = 0;
	protected final PhysicShapeType type;

	public PhysicShape(PhysicShapeType type) {
		this.type = type;
		this.transform = Transform3D.IDENTITY;
		// this.quaternion = Quaternion.identity();
		// this.origin = Vector3f.zero();
		this.mass = 0;
	}

	public PhysicShape(PhysicShapeType type, Quaternion quaternion, Vector3f origin, float mass) {
		this.type = type;
		this.transform = new Transform3D(origin, quaternion);
		// this.quaternion = quaternion;
		// this.origin = origin;
		this.mass = mass;
	}

	public Quaternion getQuaternionFull() {
		return transformGlobal.getOrientation().multiply(transform.getOrientation());
	}

	public Quaternion getQuaternion() {
		return transform.getOrientation();
	}

	public void setQuaternion(Quaternion quaternion) {
		this.transform = this.transform.withOrientation(quaternion);
	}

	public Vector3f getOrigin() {
		return this.transform.getPosition();
	}

	public void setOrigin(Vector3f origin) {
		this.transform = this.transform.withPosition(origin);
	}

	public Transform3D getTransform() {
		return transform;
	}

	public void setTransform(Transform3D transform) {
		this.transform = transform;
	}

	public Transform3D getTransformGlobal() {
		return transformGlobal;
	}

	public void setTransformGlobal(Transform3D transform) {
		this.transformGlobal = transform;
	}

	public float getMass() {
		return mass;
	}

	public void setMass(float mass) {
		this.mass = mass;
	}

	public PhysicShapeType getType() {
		return type;
	}

	public void addColision(Collision colision) {
		colisionPoints.add(colision);
	}
	
	
	public abstract void updateAABB(Transform3D transform, PhysicCollisionAABB aabb);

	public abstract void updateForNarrowCollision(Transform3D transform);

	public abstract void renderDebug(Transform3D transform, ResourceColored3DObject debugDrawProperty);

}
