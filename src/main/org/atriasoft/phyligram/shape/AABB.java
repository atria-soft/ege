package org.atriasoft.phyligram.shape;

import org.atriasoft.etk.math.FMath;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.phyligram.math.Ray;

public class AABB {
	/**
	 * Create an inverted AABB cube (min* has maximum and max* has minimum)
	 * @return A new AABB(max-float, min-float)
	 */
	public static AABB createInvertedEmpty() {
		// TODO Auto-generated method stub
		return new AABB(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
	}
	
	public static AABB mergeAABB(AABB aaa, AABB bbb) {
		return new AABB(Math.min(aaa.minX, bbb.minX), Math.min(aaa.minY, bbb.minY), Math.min(aaa.minZ, bbb.minZ), Math.max(aaa.maxX, bbb.maxX), Math.max(aaa.maxY, bbb.maxY),
				Math.max(aaa.maxZ, bbb.maxZ));
	}
	
	public float minX;
	public float minY;
	public float minZ;
	public float maxX;
	public float maxY;
	public float maxZ;
	
	public AABB() {}
	
	public AABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}
	
	public void addMax(float deltaX, float deltaY, float deltaZ) {
		this.maxX += deltaX;
		this.maxY += deltaY;
		this.maxZ += deltaZ;
	}
	
	public void addMin(float deltaX, float deltaY, float deltaZ) {
		this.minX += deltaX;
		this.minY += deltaY;
		this.minZ += deltaZ;
	}
	
	@Override
	public AABB clone() {
		return new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
	}
	
	/**
	 * Return true if the current AABB contains the AABB given in parameter
	 * @param aabb AABB box that is contains in the current.
	 * @return true The parameter in contained inside
	 */
	public boolean contains(final AABB aabb) {
		if (this.minX > aabb.minX) {
			return false;
		}
		if (this.minY > aabb.minY) {
			return false;
		}
		if (this.minZ > aabb.minZ) {
			return false;
		}
		if (this.maxX < aabb.maxX) {
			return false;
		}
		if (this.maxY < aabb.maxY) {
			return false;
		}
		if (this.maxZ < aabb.maxZ) {
			return false;
		}
		return true;
	}
	
	public Vector3f getMax() {
		return new Vector3f(this.maxX, this.maxY, this.maxZ);
	}
	
	public Vector3f getMin() {
		return new Vector3f(this.minX, this.minY, this.minZ);
	}
	
	/**
	 * Get the cube total volume.
	 * @return
	 */
	public float getVolume() {
		return ((this.maxX - this.minX) * (this.maxY - this.minY) * (this.maxZ - this.minZ));
	}
	
	public boolean intersect(AABB other) {
		if (this == other) {
			return false;
		}
		if (null == other) {
			return false;
		}
		if (this.minX > other.maxX) {
			return false;
		}
		if (this.maxX < other.minX) {
			return false;
		}
		if (this.minY > other.maxY) {
			return false;
		}
		if (this.maxY < other.minY) {
			return false;
		}
		if (this.minZ > other.maxZ) {
			return false;
		}
		if (this.maxZ < other.minZ) {
			return false;
		}
		return true;
	}
	
	public void lessMax(float deltaX, float deltaY, float deltaZ) {
		this.maxX -= deltaX;
		this.maxY -= deltaY;
		this.maxZ -= deltaZ;
	}
	
	public void lessMin(float deltaX, float deltaY, float deltaZ) {
		this.minX -= deltaX;
		this.minY -= deltaY;
		this.minZ -= deltaZ;
	}
	
	public void mergeTwoAABBs(AABB aaa, AABB bbb) {
		this.minX = Math.min(aaa.minX, bbb.minX);
		this.minY = Math.min(aaa.minY, bbb.minY);
		this.minZ = Math.min(aaa.minZ, bbb.minZ);
		this.minX = Math.max(aaa.maxX, bbb.maxX);
		this.minY = Math.max(aaa.maxY, bbb.maxY);
		this.minZ = Math.max(aaa.maxZ, bbb.maxZ);
	}
	
	public void set(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}
	
	/*
	 * check if the ray intersects the AABB
	 * This method use the line vs AABB raycasting technique described in
	 * Real-time Collision Detection by Christer Ericson.
	 * @param ray Ray to test
	 * @return true The raytest intersect the AABB box
	 */
	public boolean testRayIntersect(final Ray ray) {
		final Vector3f point2 = ray.point2.less(ray.point1).multiply(ray.maxFraction).add(ray.point1);
		final float eeeX = this.maxX - this.minX;
		final float eeeY = this.maxY - this.minY;
		final float eeeZ = this.maxZ - this.minZ;
		final Vector3f d = point2.less(ray.point1);
		final float mmmX = ray.point1.x() - ray.point2.x() - this.minX - this.maxX;
		final float mmmY = ray.point1.y() - ray.point2.y() - this.minY - this.maxY;
		final float mmmZ = ray.point1.z() - ray.point2.z() - this.minZ - this.maxZ;
		// Test if the AABB face normals are separating axis
		float adx = FMath.abs(d.x());
		if (FMath.abs(mmmX) > eeeX + adx) {
			return false;
		}
		float ady = FMath.abs(d.y());
		if (FMath.abs(mmmY) > eeeY + ady) {
			return false;
		}
		float adz = FMath.abs(d.z());
		if (FMath.abs(mmmZ) > eeeZ + adz) {
			return false;
		}
		// Add in an epsilon term to counteract arithmetic errors when segment is
		// (near) parallel to a coordinate axis (see text for detail)
		final float epsilon = 0.00001f;
		adx += epsilon;
		ady += epsilon;
		adz += epsilon;
		// Test if the cross products between face normals and ray direction are
		// separating axis
		if (FMath.abs(mmmY * d.z() - mmmZ * d.y()) > eeeY * adz + eeeZ * ady) {
			return false;
		}
		if (FMath.abs(mmmZ * d.x() - mmmX * d.z()) > eeeX * adz + eeeZ * adx) {
			return false;
		}
		if (FMath.abs(mmmX * d.y() - mmmY * d.x()) > eeeX * ady + eeeY * adx) {
			return false;
		}
		// No separating axis has been found
		return true;
	}
	
	public void update(Vector3f point) {
		if (this.minX > point.x()) {
			this.minX = point.x();
		}
		if (this.maxX < point.x()) {
			this.maxX = point.x();
		}
		if (this.minY > point.y()) {
			this.minY = point.y();
		}
		if (this.maxY < point.y()) {
			this.maxY = point.y();
		}
		if (this.minZ > point.z()) {
			this.minZ = point.z();
		}
		if (this.maxZ < point.z()) {
			this.maxZ = point.z();
		}
	}
}
