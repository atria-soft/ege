package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Vector3f;

public class Geometry3D {
	public static boolean CMP(final float x, final float y) {
		return Math.abs(x - y) <= 0.000001f;
	}
	
	public static boolean pointInAABB(final Vector3f point, final AABB aabb) {
		Vector3f min = aabb.getMin();
		Vector3f max = aabb.getMax();
		if (point.x() < min.x() || point.y() < min.y() || point.z() < min.z()) {
			return false;
		}
		if (point.x() > max.x() || point.y() > max.y() || point.z() > max.z()) {
			return false;
		}
		return true;
	}
	
	public static boolean pointInOBB(final Vector3f point, final OBB obb) {
		Vector3f dir = point.less(obb.position);
		{
			int iii = 0;
			Vector3f axis = obb.orientation.getRow(iii * 3);
			float distance = Vector3f.length2(dir, axis);
			float squareDistance = obb.size.x() * obb.size.x();
			if (distance > squareDistance || distance < -squareDistance) {
				return false;
			}
		}
		{
			int iii = 1;
			Vector3f axis = obb.orientation.getRow(iii * 3);
			float distance = Vector3f.length2(dir, axis);
			float squareDistance = obb.size.y() * obb.size.y();
			if (distance > squareDistance || distance < -squareDistance) {
				return false;
			}
		}
		{
			int iii = 2;
			Vector3f axis = obb.orientation.getRow(iii * 3);
			float distance = Vector3f.length2(dir, axis);
			float squareDistance = obb.size.z() * obb.size.z();
			if (distance > squareDistance || distance < -squareDistance) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean pointInPlane(final Vector3f point, final Plane____ plane) {
		// This should probably use an epsilon!
		//return Dot(point, plane.normal) - plane.distance == 0.0f;
		return CMP(point.dot(plane.normal) - plane.distance, 0.0f);
	}
	
	public static boolean pointInSphere(final Vector3f point, final Sphere sphere) {
		return Vector3f.length2(point, sphere.position) < sphere.radius * sphere.radius;
	}
	
	public static boolean pointInTriangle(final Vector3f p, final Triangle t) {
		// Move the triangle so that the point is  
		// now at the origin of the triangle
		Vector3f a = t.p1.less(p);
		Vector3f b = t.p2.less(p);
		Vector3f c = t.p3.less(p);
		// The point should be moved too, so they are both
		// relative, but because we don't use p in the
		// equation anymore, we don't need it!
		// p -= p; // This would just equal the zero vector!
		Vector3f normPBC = b.cross(c); // Normal of PBC (u)
		Vector3f normPCA = c.cross(a); // Normal of PCA (v)
		Vector3f normPAB = a.cross(b); // Normal of PAB (w)
		// Test to see if the normals are facing 
		// the same direction, return false if not
		float val = normPBC.dot(normPCA);
		if (CMP(val, 0.0f) == false) {
			return false;
		}
		val = normPBC.dot(normPAB);
		if (CMP(val, 0.0f) == false) {
			return false;
		}
		// All normals facing the same way, return true
		return true;
	}
	
	// select the point under the oriented triangle
	public static boolean pointUnderTriangle(final Vector3f p, final Triangle t) {
		// Move the triangle so that the point is  
		// now at the origin of the triangle
		Vector3f a = t.p1.less(p);
		Vector3f b = t.p2.less(p);
		Vector3f c = t.p3.less(p);
		
		// The point should be moved too, so they are both
		// relative, but because we don't use p in the
		// equation anymore, we don't need it!
		// p -= p; // This would just equal the zero vector!
		
		Vector3f normPBC = b.cross(c); // Normal of PBC (u)
		Vector3f normPCA = c.cross(a); // Normal of PCA (v)
		Vector3f normPAB = a.cross(b); // Normal of PAB (w)
		
		// Test to see if the normals are facing 
		// the same direction, return false if not
		float val = normPBC.dot(normPCA);
		if (val < 0.0f) {
			return false;
		}
		val = normPBC.dot(normPAB);
		if (val < 0.0f) {
			return false;
		}
		
		// All normals facing the same way, return true
		return true;
	}
	
}
