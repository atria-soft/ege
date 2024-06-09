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
		final Matrix3f orientation = Matrix3f.IDENTITY;
		orientation.multiply(Matrix3f.createMatrixRotate(new Vector3f(0, 0, 1), (float) Math.toRadians(45)));
		final OBB shape = new OBB(new Vector3f(4, 4, 4), new Vector3f(1, 2, 3), orientation);
		Assertions.assertFalse(Geometry3D.pointInOBB(new Vector3f(0, 0, 0), shape));
		Assertions.assertFalse(Geometry3D.pointInOBB(new Vector3f(6, 6, 6), shape));
		Assertions.assertTrue(Geometry3D.pointInOBB(new Vector3f(3, 3, 3), shape));
		Assertions.assertTrue(Geometry3D.pointInOBB(new Vector3f(4, 4, 4), shape));
		Assertions.assertTrue(Geometry3D.pointInOBB(new Vector3f(4, 4, 1.0001f), shape));
		Assertions.assertTrue(Geometry3D.pointInOBB(new Vector3f(4, 2.0001f, 4), shape));
		Assertions.assertTrue(Geometry3D.pointInOBB(new Vector3f(3.0001f, 4, 4), shape));
	}
	
	@Test
	void testPointInPlane() {
		final Plane____ shape = new Plane____((new Vector3f(4, 4, 4)).normalize(), (float) Math.sqrt(1 * 1 + 1 * 1));
		Assertions.assertFalse(Geometry3D.pointInPlane(new Vector3f(0, 0, 0), shape));
		Assertions.assertFalse(Geometry3D.pointInPlane(new Vector3f(6, 6, 6), shape));
		Assertions.assertTrue(Geometry3D.pointInPlane(new Vector3f(3, 3, 3), shape));
		Assertions.assertTrue(Geometry3D.pointInPlane(new Vector3f(4, 4, 4), shape));
		Assertions.assertTrue(Geometry3D.pointInPlane(new Vector3f(4, 4, 1.0001f), shape));
		Assertions.assertTrue(Geometry3D.pointInPlane(new Vector3f(4, 2.0001f, 4), shape));
		Assertions.assertTrue(Geometry3D.pointInPlane(new Vector3f(3.0001f, 4, 4), shape));
	}
	
	@Test
	void testPointInTriangle() {
		final Triangle shape = new Triangle(new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1));
		//		assertTrue(Geometry3D.pointInTriangle(new Vector3f(1,0,0), shape));
		//		assertTrue(Geometry3D.pointInTriangle(new Vector3f(0,1,0), shape));
		//		assertTrue(Geometry3D.pointInTriangle(new Vector3f(0,0,1), shape));
		//		assertFalse(Geometry3D.pointInTriangle(new Vector3f(5252,25252521,41458), shape));
		//		assertFalse(Geometry3D.pointInTriangle(new Vector3f(1,1,1), shape));
		Assertions.assertFalse(Geometry3D.pointInTriangle(new Vector3f(0.1f, 0.1f, 0.1f), shape));
		Assertions.assertFalse(Geometry3D.pointInTriangle(new Vector3f(0, 0, 0), shape));
	}
}
