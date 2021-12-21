package org.atriasoft.phyligram;

import org.atriasoft.ege.geometry.Plane;
import org.atriasoft.etk.math.FMath;
import org.atriasoft.etk.math.Vector3f;

// https://realtimecollisiondetection.net/blog/?p=103
public class ToolCollisionSphereWithTriangle {
	public static float clamp(float val, float min, float max) {
		return Math.max(min, Math.min(max, val));
	}
	
	public static Vector3f getClosestPointOnFiniteLine(Vector3f point, Vector3f lineStart, Vector3f lineEnd) {
		Vector3f lineDirection = lineEnd.less(lineStart);
		float lineLength = lineDirection.length();
		lineDirection = lineDirection.normalize();
		float position = point.less(lineStart).dot(lineDirection);
		float ProjectionLength = clamp(position, 0, lineLength);
		return lineStart.add(lineDirection.multiply(ProjectionLength));
	}
	
	public static ColisionPoint getCollisionPoint(PhysicSphere sphere1, PhysicTriangle shapeReference) {
		Plane plane = new Plane(shapeReference.getTriangleGlobalPos());
		float distance = plane.distance(sphere1.narrowPhaseGlobalPos);
		float distance1 = distance * distance;
		float distance2 = sphere1.getSize() * sphere1.getSize();
		if (distance1 > distance2) {
			return null;
		}
		/*
		if (distance < 0.0f) {
			System.out.println("Distance : (BOTTOM) " + distance);
		} else {
			System.out.println("Distance :   (TOP)  " + distance);
		}
		*/
		boolean inCenter = false;
		// check the position with the perpendicular plan of each side of the triangle
		Plane plane1 = new Plane(shapeReference.getTriangleGlobalPos().p1, shapeReference.getTriangleGlobalPos().p2, shapeReference.getTriangleGlobalPos().p1.add(plane.normal()));
		float distanceBorder = plane1.distance(sphere1.narrowPhaseGlobalPos);
		inCenter = distanceBorder < 0.0f;
		if (inCenter == true) {
			Plane plane2 = new Plane(shapeReference.getTriangleGlobalPos().p2, shapeReference.getTriangleGlobalPos().p3, shapeReference.getTriangleGlobalPos().p2.add(plane.normal()));
			distanceBorder = plane2.distance(sphere1.narrowPhaseGlobalPos);
			inCenter = distanceBorder < 0.0f;
		}
		if (inCenter == true) {
			Plane plane3 = new Plane(shapeReference.getTriangleGlobalPos().p3, shapeReference.getTriangleGlobalPos().p1, shapeReference.getTriangleGlobalPos().p3.add(plane.normal()));
			distanceBorder = plane3.distance(sphere1.narrowPhaseGlobalPos);
			inCenter = distanceBorder < 0.0f;
		}
		if (inCenter == true) {
			Vector3f impact = plane.minimalIntersectionPoint(sphere1.narrowPhaseGlobalPos);
			Vector3f force = plane.normal();
			if (distance < 0) {
				force = force.multiply(-(sphere1.getSize() + distance));
			} else {
				force = force.multiply(-(sphere1.getSize() + distance));
			}
			return new ColisionPoint(impact, force);
		}
		
		System.out.println("Not in center");
		
		// now we need to check if we have a collision with the border.
		Vector3f nearestPointP1 = getClosestPointOnFiniteLine(sphere1.narrowPhaseGlobalPos, shapeReference.getTriangleGlobalPos().p1, shapeReference.getTriangleGlobalPos().p2);
		float distanceP1Square = Vector3f.length2(nearestPointP1, sphere1.narrowPhaseGlobalPos);
		System.out.println("distanceP1Square=" + distanceP1Square);
		Vector3f nearestPointP2 = getClosestPointOnFiniteLine(sphere1.narrowPhaseGlobalPos, shapeReference.getTriangleGlobalPos().p2, shapeReference.getTriangleGlobalPos().p3);
		float distanceP2Square = Vector3f.length2(nearestPointP2, sphere1.narrowPhaseGlobalPos);
		System.out.println("distanceP2Square=" + distanceP2Square);
		Vector3f nearestPointP3 = getClosestPointOnFiniteLine(sphere1.narrowPhaseGlobalPos, shapeReference.getTriangleGlobalPos().p3, shapeReference.getTriangleGlobalPos().p1);
		float distanceP3Square = Vector3f.length2(nearestPointP3, sphere1.narrowPhaseGlobalPos);
		System.out.println("distanceP3Square=" + distanceP3Square);
		float distanceFinal;
		Vector3f impact;
		if (distanceP1Square < distanceP2Square && distanceP1Square < distanceP3Square) {
			// P1 is the smallest
			distanceFinal = FMath.sqrt(distanceP1Square);
			impact = nearestPointP1;
		} else if (distanceP2Square < distanceP3Square) {
			// P2 is the smallest
			distanceFinal = FMath.sqrt(distanceP2Square);
			impact = nearestPointP2;
		} else {
			// P3 is the smallest
			distanceFinal = FMath.sqrt(distanceP3Square);
			impact = nearestPointP3;
		}
		
		Vector3f force = sphere1.narrowPhaseGlobalPos.less(impact).safeNormalize();
		force = force.multiply(sphere1.getSize() - distanceFinal);
		return new ColisionPoint(impact, force);
	}
	
	public static boolean testCollide(PhysicSphere sphere1, PhysicTriangle shapeReference) {
		Plane plane = new Plane(shapeReference.getTriangleGlobalPos());
		float distance = plane.distance(sphere1.narrowPhaseGlobalPos);
		float distance1 = distance * distance;
		float distance2 = sphere1.getSize() * sphere1.getSize();
		if (distance1 > distance2) {
			return false;
		}
		/*
		if (distance < 0.0f) {
			System.out.println("Distance : (BOTTOM) " + distance);
		} else {
			System.out.println("Distance :   (TOP)  " + distance);
		}
		*/
		boolean inCenter = false;
		// check the position with the perpendicular plan of each side of the triangle
		Plane plane1 = new Plane(shapeReference.getTriangleGlobalPos().p1, shapeReference.getTriangleGlobalPos().p2, shapeReference.getTriangleGlobalPos().p1.add(plane.normal()));
		float distanceBorder = plane1.distance(sphere1.narrowPhaseGlobalPos);
		inCenter = distanceBorder < 0.0f;
		if (inCenter == true) {
			Plane plane2 = new Plane(shapeReference.getTriangleGlobalPos().p2, shapeReference.getTriangleGlobalPos().p3, shapeReference.getTriangleGlobalPos().p2.add(plane.normal()));
			distanceBorder = plane2.distance(sphere1.narrowPhaseGlobalPos);
			inCenter = distanceBorder < 0.0f;
		}
		if (inCenter == true) {
			Plane plane3 = new Plane(shapeReference.getTriangleGlobalPos().p3, shapeReference.getTriangleGlobalPos().p1, shapeReference.getTriangleGlobalPos().p3.add(plane.normal()));
			distanceBorder = plane3.distance(sphere1.narrowPhaseGlobalPos);
			inCenter = distanceBorder < 0.0f;
		}
		if (inCenter == true) {
			return true;
		}
		
		System.out.println("Not in center");
		
		// now we need to check if we have a collision with the border.
		Vector3f nearestPointP1 = getClosestPointOnFiniteLine(sphere1.narrowPhaseGlobalPos, shapeReference.getTriangleGlobalPos().p1, shapeReference.getTriangleGlobalPos().p2);
		float distanceP1Square = Vector3f.length2(nearestPointP1, sphere1.narrowPhaseGlobalPos);
		System.out.println("distanceP1Square=" + distanceP1Square);
		if (distanceP1Square < distance2) {
			return true;
		}
		Vector3f nearestPointP2 = getClosestPointOnFiniteLine(sphere1.narrowPhaseGlobalPos, shapeReference.getTriangleGlobalPos().p2, shapeReference.getTriangleGlobalPos().p3);
		float distanceP2Square = Vector3f.length2(nearestPointP2, sphere1.narrowPhaseGlobalPos);
		System.out.println("distanceP2Square=" + distanceP2Square);
		if (distanceP2Square < distance2) {
			return true;
		}
		Vector3f nearestPointP3 = getClosestPointOnFiniteLine(sphere1.narrowPhaseGlobalPos, shapeReference.getTriangleGlobalPos().p3, shapeReference.getTriangleGlobalPos().p1);
		float distanceP3Square = Vector3f.length2(nearestPointP3, sphere1.narrowPhaseGlobalPos);
		System.out.println("distanceP3Square=" + distanceP3Square);
		if (distanceP3Square < distance2) {
			return true;
		}
		return false;
	}
	
	private ToolCollisionSphereWithTriangle() {}
}
