package test.atriasoft.ege;

import org.atriasoft.ege.geometry.AABB;
import org.atriasoft.ege.geometry.Geometry3D;
import org.atriasoft.ege.geometry.OBB;
import org.atriasoft.ege.geometry.Plane;
import org.atriasoft.ege.geometry.Sphere;
import org.atriasoft.ege.geometry.Triangle;
import org.atriasoft.etk.math.Matrix3f;
import org.atriasoft.etk.math.Vector3f;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class TestTransformation3D {
	
	@Test
	void testPointInAABB() {
		final AABB shape = new AABB(new Vector3f(4, 4, 4), new Vector3f(1, 2, 3));
		Assert.assertFalse(Geometry3D.pointInAABB(new Vector3f(0, 0, 0), shape));
		Assert.assertFalse(Geometry3D.pointInAABB(new Vector3f(6, 6, 6), shape));
		Assert.assertTrue(Geometry3D.pointInAABB(new Vector3f(3, 3, 3), shape));
		Assert.assertTrue(Geometry3D.pointInAABB(new Vector3f(4, 4, 4), shape));
		Assert.assertTrue(Geometry3D.pointInAABB(new Vector3f(4, 4, 1.0001f), shape));
		Assert.assertTrue(Geometry3D.pointInAABB(new Vector3f(4, 2.0001f, 4), shape));
		Assert.assertTrue(Geometry3D.pointInAABB(new Vector3f(3.0001f, 4, 4), shape));
	}
	
	@Test
	void testPointInLine() {
		final Sphere shape = new Sphere(new Vector3f(4, 4, 4), 2);
		Assert.assertFalse(Geometry3D.pointInSphere(new Vector3f(0, 0, 0), shape));
		Assert.assertFalse(Geometry3D.pointInSphere(new Vector3f(6, 6, 6), shape));
		Assert.assertTrue(Geometry3D.pointInSphere(new Vector3f(3, 3, 3), shape));
		Assert.assertTrue(Geometry3D.pointInSphere(new Vector3f(4, 4, 4), shape));
		Assert.assertTrue(Geometry3D.pointInSphere(new Vector3f(4, 4, 2.0001f), shape));
		Assert.assertTrue(Geometry3D.pointInSphere(new Vector3f(4, 2.0001f, 4), shape));
		Assert.assertTrue(Geometry3D.pointInSphere(new Vector3f(2.0001f, 4, 4), shape));
	}
	
	@Test
	void testPointInOBB() {
		final Matrix3f orientation = Matrix3f.IDENTITY;
		orientation.multiply(Matrix3f.createMatrixRotate(new Vector3f(0, 0, 1), (float) Math.toRadians(45)));
		final OBB shape = new OBB(new Vector3f(4, 4, 4), new Vector3f(1, 2, 3), orientation);
		Assert.assertFalse(Geometry3D.pointInOBB(new Vector3f(0, 0, 0), shape));
		Assert.assertFalse(Geometry3D.pointInOBB(new Vector3f(6, 6, 6), shape));
		Assert.assertTrue(Geometry3D.pointInOBB(new Vector3f(3, 3, 3), shape));
		Assert.assertTrue(Geometry3D.pointInOBB(new Vector3f(4, 4, 4), shape));
		Assert.assertTrue(Geometry3D.pointInOBB(new Vector3f(4, 4, 1.0001f), shape));
		Assert.assertTrue(Geometry3D.pointInOBB(new Vector3f(4, 2.0001f, 4), shape));
		Assert.assertTrue(Geometry3D.pointInOBB(new Vector3f(3.0001f, 4, 4), shape));
	}
	
	@Test
	void testPointInPlane() {
		final Plane shape = new Plane((new Vector3f(4, 4, 4)).normalize(), (float) Math.sqrt(1 * 1 + 1 * 1));
		Assert.assertFalse(Geometry3D.pointInPlane(new Vector3f(0, 0, 0), shape));
		Assert.assertFalse(Geometry3D.pointInPlane(new Vector3f(6, 6, 6), shape));
		Assert.assertTrue(Geometry3D.pointInPlane(new Vector3f(3, 3, 3), shape));
		Assert.assertTrue(Geometry3D.pointInPlane(new Vector3f(4, 4, 4), shape));
		Assert.assertTrue(Geometry3D.pointInPlane(new Vector3f(4, 4, 1.0001f), shape));
		Assert.assertTrue(Geometry3D.pointInPlane(new Vector3f(4, 2.0001f, 4), shape));
		Assert.assertTrue(Geometry3D.pointInPlane(new Vector3f(3.0001f, 4, 4), shape));
	}
	
	@Test
	void testPointInTriangle() {
		final Triangle shape = new Triangle(new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1));
		//		assertTrue(Geometry3D.pointInTriangle(new Vector3f(1,0,0), shape));
		//		assertTrue(Geometry3D.pointInTriangle(new Vector3f(0,1,0), shape));
		//		assertTrue(Geometry3D.pointInTriangle(new Vector3f(0,0,1), shape));
		//		assertFalse(Geometry3D.pointInTriangle(new Vector3f(5252,25252521,41458), shape));
		//		assertFalse(Geometry3D.pointInTriangle(new Vector3f(1,1,1), shape));
		Assert.assertFalse(Geometry3D.pointInTriangle(new Vector3f(0.1f, 0.1f, 0.1f), shape));
		Assert.assertFalse(Geometry3D.pointInTriangle(new Vector3f(0, 0, 0), shape));
	}
}
