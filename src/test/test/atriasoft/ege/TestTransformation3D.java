package test.atriasoft.ege;

import org.atriasoft.ege.geometry.AABB;
import org.atriasoft.ege.geometry.Geometry3D;
import org.atriasoft.ege.geometry.OBB;
import org.atriasoft.ege.geometry.Plane____;
import org.atriasoft.ege.geometry.Sphere;
import org.atriasoft.ege.geometry.Triangle;
import org.atriasoft.etk.math.Matrix3f;
import org.atriasoft.etk.math.Vector3f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestTransformation3D {
	
	@Test
	void testPointInAABB() {
		final AABB shape = new AABB(new Vector3f(4, 4, 4), new Vector3f(1, 2, 3));
		Assertions.assertFalse(Geometry3D.pointInAABB(new Vector3f(0, 0, 0), shape));
		Assertions.assertFalse(Geometry3D.pointInAABB(new Vector3f(6, 6, 6), shape));
		Assertions.assertTrue(Geometry3D.pointInAABB(new Vector3f(3, 3, 3), shape));
		Assertions.assertTrue(Geometry3D.pointInAABB(new Vector3f(4, 4, 4), shape));
		Assertions.assertTrue(Geometry3D.pointInAABB(new Vector3f(4, 4, 1.0001f), shape));
		Assertions.assertTrue(Geometry3D.pointInAABB(new Vector3f(4, 2.0001f, 4), shape));
		Assertions.assertTrue(Geometry3D.pointInAABB(new Vector3f(3.0001f, 4, 4), shape));
	}
	
	@Test
	void testPointInLine() {
		final Sphere shape = new Sphere(new Vector3f(4, 4, 4), 2);
		Assertions.assertFalse(Geometry3D.pointInSphere(new Vector3f(0, 0, 0), shape));
		Assertions.assertFalse(Geometry3D.pointInSphere(new Vector3f(6, 6, 6), shape));
		Assertions.assertTrue(Geometry3D.pointInSphere(new Vector3f(3, 3, 3), shape));
		Assertions.assertTrue(Geometry3D.pointInSphere(new Vector3f(4, 4, 4), shape));
		Assertions.assertTrue(Geometry3D.pointInSphere(new Vector3f(4, 4, 2.0001f), shape));
		Assertions.assertTrue(Geometry3D.pointInSphere(new Vector3f(4, 2.0001f, 4), shape));
		Assertions.assertTrue(Geometry3D.pointInSphere(new Vector3f(2.0001f, 4, 4), shape));
	}
	
	@Test
	void testPointInOBB() {
		final OBB shape = new OBB(new Vector3f(4, 4, 4), new Vector3f(1, 2, 3), Matrix3f.IDENTITY);
		Assertions.assertFalse(Geometry3D.pointInOBB(new Vector3f(0, 0, 0), shape));
		Assertions.assertFalse(Geometry3D.pointInOBB(new Vector3f(6, 6, 6), shape));
		Assertions.assertTrue(Geometry3D.pointInOBB(new Vector3f(3, 3, 3), shape));
		Assertions.assertTrue(Geometry3D.pointInOBB(new Vector3f(4, 4, 4), shape));
		Assertions.assertTrue(Geometry3D.pointInOBB(new Vector3f(4, 4, 1.0001f), shape));
		Assertions.assertTrue(Geometry3D.pointInOBB(new Vector3f(4, 2.0001f, 4), shape));
		Assertions.assertTrue(Geometry3D.pointInOBB(new Vector3f(3.0001f, 4, 4), shape));
	}
	
	@Test
	void testPointInOBBRotated() {
		// Rotated by 45 degrees around Z: the box is a diamond in the XY plane
		final Matrix3f orientation = Matrix3f.createMatrixRotate(new Vector3f(0, 0, 1), (float) Math.toRadians(45));
		final OBB shape = new OBB(new Vector3f(0, 0, 0), new Vector3f(2, 1, 1), orientation);
		final float along = 1.9f / (float) Math.sqrt(2); // 1.9 on the long axis, whichever way the rotation turns
		Assertions.assertTrue(Geometry3D.pointInOBB(new Vector3f(along, along, 0), shape)
				|| Geometry3D.pointInOBB(new Vector3f(along, -along, 0), shape));
		// (1.9, 0, 0) is inside the axis-aligned box but outside the diamond
		Assertions.assertFalse(Geometry3D.pointInOBB(new Vector3f(1.9f, 0, 0), shape));
	}

	@Test
	void testPointInPlane() {
		// A plane is the points where dot(point, normal) == distance: it holds no volume
		final Vector3f normal = (new Vector3f(1, 1, 1)).normalize();
		final Plane____ shape = new Plane____(normal, 2.0f);
		Assertions.assertFalse(Geometry3D.pointInPlane(new Vector3f(0, 0, 0), shape));
		Assertions.assertFalse(Geometry3D.pointInPlane(new Vector3f(6, 6, 6), shape));
		Assertions.assertFalse(Geometry3D.pointInPlane(normal.multiply(2.1f), shape));
		Assertions.assertTrue(Geometry3D.pointInPlane(normal.multiply(2.0f), shape));
		// Moving along the plane keeps the point in it
		final Vector3f along = (new Vector3f(1, -1, 0)).normalize().multiply(3.0f);
		Assertions.assertTrue(Geometry3D.pointInPlane(normal.multiply(2.0f).add(along), shape));
	}
	
	@Test
	void testPointInTriangle() {
		final Triangle shape = new Triangle(new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1));
		Assertions.assertTrue(Geometry3D.pointInTriangle(new Vector3f(1, 0, 0), shape));
		Assertions.assertTrue(Geometry3D.pointInTriangle(new Vector3f(0, 1, 0), shape));
		Assertions.assertTrue(Geometry3D.pointInTriangle(new Vector3f(0, 0, 1), shape));
		Assertions.assertTrue(Geometry3D.pointInTriangle(new Vector3f(0.5f, 0.5f, 0), shape));
		Assertions.assertTrue(Geometry3D.pointInTriangle(new Vector3f(1f / 3, 1f / 3, 1f / 3), shape));
		Assertions.assertFalse(Geometry3D.pointInTriangle(new Vector3f(5252, 25252521, 41458), shape));
		Assertions.assertFalse(Geometry3D.pointInTriangle(new Vector3f(1, 1, 1), shape));
		// In the plane of the triangle but outside it
		Assertions.assertFalse(Geometry3D.pointInTriangle(new Vector3f(1, 1, -1), shape));
		Assertions.assertFalse(Geometry3D.pointInTriangle(new Vector3f(0.1f, 0.1f, 0.1f), shape));
		Assertions.assertFalse(Geometry3D.pointInTriangle(new Vector3f(0, 0, 0), shape));
	}
}
