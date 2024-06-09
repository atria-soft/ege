package test.atriasoft.phyligram;

import java.util.stream.Stream;

import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.phyligram.PhysicSphere;
import org.atriasoft.phyligram.PhysicTriangle;
import org.atriasoft.phyligram.math.ToolCollisionSphereWithTriangle;
import org.atriasoft.phyligram.shape.AABB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestCollisionSphereTriangle {
	
	static Stream<Arguments> generateData() {
		return Stream.of( // ...
				Arguments.of(1, new Vector3f(0.0f, 0.0f, 1.001f), false), // test just on top Z
				Arguments.of(2, new Vector3f(0.0f, 0.0f, -1.001f), false), // test just on bottom Z
				Arguments.of(2, new Vector3f(2.001f, 0.0f, 0.0f), false), // test on the top X
				Arguments.of(2, new Vector3f(-2.001f, 0.0f, 0.0f), false), // test on the bottom X
				Arguments.of(2, new Vector3f(0.0f, 1.7f, 0.0f), false), // test on the top Y
				Arguments.of(2, new Vector3f(0.0f, -1.7f, 0.0f), false), // test on the bottom Y
				// test collision
				Arguments.of(2, new Vector3f(0.0f, 0.0f, 0.9f), true), // in center
				Arguments.of(1, new Vector3f(0.0f, 0.0f, 0.999f), true), // test just on top Z
				Arguments.of(2, new Vector3f(0.0f, 0.0f, -0.999f), true), // test just on bottom Z
				Arguments.of(2, new Vector3f(1.999f, 0.0f, 0.0f), true), // test on the top X
				Arguments.of(2, new Vector3f(-1.999f, 0.0f, 0.0f), true), // test on the bottom X
				Arguments.of(2, new Vector3f(0.0f, 1.5f, 0.0f), true), // test on the top Y
				Arguments.of(2, new Vector3f(0.0f, -1.5f, 0.0f), true), // test on the bottom Y
				Arguments.of(2, new Vector3f(1.5f, 1.5f, 0.0f), true), // test on corner A
				Arguments.of(2, new Vector3f(1.5f, -1.5f, 0.0f), true) // test on corner B
		);
	}
	
	@ParameterizedTest
	@MethodSource("generateData")
	void testsphereOut(final int testId, final Vector3f position, final boolean resultTheoricValue) {
		System.out.println("AAAAA ");
		final float testCoefficient = 1.0f;
		final PhysicSphere sphere = new PhysicSphere();
		sphere.setSize(testCoefficient);
		final PhysicTriangle triangle = new PhysicTriangle();
		triangle.setPoints(new Vector3f(testCoefficient, testCoefficient, 0.0f),
				new Vector3f(testCoefficient, -testCoefficient, 0.0f), new Vector3f(-testCoefficient, 0.0f, 0.0f));
		final Transform3D transformGlobalTriangle = Transform3D.IDENTITY;
		final Transform3D transformGlobalsphere = new Transform3D(position.multiply(testCoefficient));
		final AABB aabb = new AABB();
		sphere.updateAABB(transformGlobalsphere, aabb);
		sphere.updateForNarrowCollision(transformGlobalsphere);
		triangle.updateAABB(transformGlobalTriangle, aabb);
		triangle.updateForNarrowCollision(transformGlobalTriangle);
		final boolean result = ToolCollisionSphereWithTriangle.testCollide(sphere, triangle);
		Assertions.assertEquals(resultTheoricValue, result);
	}
	
	@Test
	void testsphereOutTop() {
		final float testCoefficient = 2.0f;
		final PhysicSphere sphere = new PhysicSphere();
		sphere.setSize(testCoefficient);
		final PhysicTriangle triangle = new PhysicTriangle();
		triangle.setPoints(new Vector3f(testCoefficient, testCoefficient, 0.0f),
				new Vector3f(testCoefficient, -testCoefficient, 0.0f), new Vector3f(-testCoefficient, 0.0f, 0.0f));
		final Transform3D transformGlobalTriangle = Transform3D.IDENTITY;
		final Transform3D transformGlobalsphere = new Transform3D(new Vector3f(0.0f, 0.0f, testCoefficient + 0.001f));
		final AABB aabb = new AABB();
		sphere.updateAABB(transformGlobalsphere, aabb);
		sphere.updateForNarrowCollision(transformGlobalsphere);
		triangle.updateAABB(transformGlobalTriangle, aabb);
		triangle.updateForNarrowCollision(transformGlobalTriangle);
		final boolean result = ToolCollisionSphereWithTriangle.testCollide(sphere, triangle);
		Assertions.assertFalse(result);
	}
}
