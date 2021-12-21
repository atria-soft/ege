package org.atriasoft.phyligram;

import org.atriasoft.ege.geometry.Triangle;
import org.atriasoft.etk.math.Vector3f;

public class ToolCollisionSphereWithHeightMapChunk {
	//intersection entre 2 droite (en 2d avec estimation en 3D
	public static Vector3f lineIntersect(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4) {
		float denom = (p4.y() - p3.y()) * (p2.x() - p1.x()) - (p4.x() - p3.x()) * (p2.y() - p1.y());
		if (denom == 0.0f) { // Lines are parallel.
			return null;
		}
		float ua = ((p4.x() - p3.x()) * (p1.y() - p3.y()) - (p4.y() - p3.y()) * (p1.x() - p3.x())) / denom;
		float ub = ((p2.x() - p1.x()) * (p1.y() - p3.y()) - (p2.y() - p1.y()) * (p1.x() - p3.x())) / denom;
		if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
			// Get the intersection point.
			return new Vector3f(p1.x() + ua * (p2.x() - p1.x()), p1.y() + ua * (p2.y() - p1.y()), p1.z() + ua * (p2.z() - p1.z()));
		}
		return null;
	}
	
	public static Vector3f middlePoint(Vector3f p1, Vector3f p2, Vector3f p3) {
		float distance1 = p1.distance(p2);
		float distance2 = p1.distance(p3);
		float ratio = distance1 / distance2;
		return p3.add(p3.less(p1).multiply(ratio));
	}
	
	private static boolean pointInTriangle(Vector3f pt, Triangle triangle) {
		float d1, d2, d3;
		boolean has_neg, has_pos;
		
		d1 = sign(pt, triangle.p1, triangle.p2);
		d2 = sign(pt, triangle.p2, triangle.p3);
		d3 = sign(pt, triangle.p3, triangle.p1);
		
		has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
		has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);
		
		return !(has_neg && has_pos);
	}
	
	private static float sign(Vector3f p1, Vector3f p2, Vector3f p3) {
		return (p1.x() - p3.x()) * (p2.y() - p3.y()) - (p2.x() - p3.x()) * (p1.y() - p3.y());
	}
	
	public static boolean testCollide(PhysicSphere sphere1, PhysicHeightMapChunk map) {
		for (Triangle elem : map.trianglesGlobalPos) {
			if (pointInTriangle(sphere1.narrowPhaseGlobalPos, elem)) {
				// calculate linear extrapolation of the z height at this position:
				Vector3f position = lineIntersect(elem.p1, sphere1.narrowPhaseGlobalPos, elem.p2, elem.p3);
				Vector3f middlePoint = middlePoint(elem.p1, sphere1.narrowPhaseGlobalPos, position);
				float distanceSquare = middlePoint.distance2(sphere1.narrowPhaseGlobalPos);
				if (sphere1.getSize() * sphere1.getSize() < distanceSquare) {
					return true;
				}
			}
		}
		return false;
	}
	
	private ToolCollisionSphereWithHeightMapChunk() {}
}
