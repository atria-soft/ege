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
		// The point, relative to the center, projected on each axis of the box (the rows of the orientation)
		// must not go further than the half size on that axis.
		final Vector3f dir = point.less(obb.position);
		if (Math.abs(dir.dot(obb.orientation.getRow(0))) > obb.size.x()) {
			return false;
		}
		if (Math.abs(dir.dot(obb.orientation.getRow(1))) > obb.size.y()) {
			return false;
		}
		if (Math.abs(dir.dot(obb.orientation.getRow(2))) > obb.size.z()) {
			return false;
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
		// The point must lie in the plane of the triangle (a point beside it is not in it).
		final Vector3f faceNormal = t.p2.less(t.p1).cross(t.p3.less(t.p1));
		final float faceLength = faceNormal.length();
		if (faceLength < 1e-12f) {
			return false; // degenerate triangle
		}
		final float tolerance = 1e-5f;
		if (Math.abs(faceNormal.dot(p.less(t.p1))) > tolerance * faceLength) {
			return false;
		}
		// Move the triangle so that the point is at the origin
		final Vector3f a = t.p1.less(p);
		final Vector3f b = t.p2.less(p);
		final Vector3f c = t.p3.less(p);
		final Vector3f normPBC = b.cross(c); // Normal of PBC (u)
		final Vector3f normPCA = c.cross(a); // Normal of PCA (v)
		final Vector3f normPAB = a.cross(b); // Normal of PAB (w)
		// Inside (or on an edge) when the three normals face the same way: no negative dot product
		// (a null one is a point on an edge or a vertex).
		if (normPBC.dot(normPCA) < -tolerance) {
			return false;
		}
		if (normPBC.dot(normPAB) < -tolerance) {
			return false;
		}
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
