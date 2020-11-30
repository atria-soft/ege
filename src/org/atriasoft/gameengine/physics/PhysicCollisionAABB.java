package org.atriasoft.gameengine.physics;

import org.atriasoft.etk.math.Vector3f;

public class PhysicCollisionAABB {
	public float minX;
	public float minY;
	public float minZ;
	public float maxX;
	public float maxY;
	public float maxZ;
	public PhysicCollisionAABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		super();
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}
	public boolean intersect(PhysicCollisionAABB other) {
		if (this == other) {
			return false;
		}
		if (minX > other.maxX) {
			return false;
		}
		if (maxX < other.minX) {
			return false;
		}
		if (minY > other.maxY) {
			return false;
		}
		if (maxY < other.minY) {
			return false;
		}
		if (minZ > other.maxZ) {
			return false;
		}
		if (maxZ < other.minZ) {
			return false;
		}
		return true;
	}
	public void update(Vector3f point) {
		if (minX > point.x) {
			minX = point.x;
		}
		if (maxX < point.x) {
			maxX = point.x;
		}
		if (minY > point.y) {
			minY = point.y;
		}
		if (maxY < point.y) {
			maxY = point.y;
		}
		if (minZ > point.z) {
			minZ = point.z;
		}
		if (maxZ < point.z) {
			maxZ = point.z;
		}
	}
	public Vector3f getMin() {
		return new Vector3f(minX, minY, minZ);
	}
	public Vector3f getMax() {
		return new Vector3f(maxX, maxY, maxZ);
	}
	public static PhysicCollisionAABB beforeCalculated() {
		// TODO Auto-generated method stub
		return new PhysicCollisionAABB(999999,999999,999999,-999999,-999999,-999999);
	}
}


