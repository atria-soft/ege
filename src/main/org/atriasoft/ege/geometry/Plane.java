package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Vector3f;

public record Plane(
		Vector3f normal,
		Vector3f point) {
	public Plane(Triangle tri) {
		this(tri.getNormal(), tri.getCenter());
	}
	
	public Plane(Vector3f p1, Vector3f p2, Vector3f p3) {
		this(Triangle.getNormal(p1, p2, p3), Triangle.getCenter(p1, p2, p3));
	}
	
	public float distance(Vector3f point) {
		return this.normal.dot(point.less(this.point));
	}
	
	/**
	 * Determines the point of intersection between the plane and a line defined by a point and a direction vector.
	 *
	 * @param linePoint     A point on the line.
	 * @param lineDirection The direction Vector3f of the line.
	 * @return The point of intersection between the line and the plane, null if the line is parallel to the plane.
	 */
	public Vector3f lineIntersectionPoint(Vector3f linePoint, Vector3f lineDirection) {
		if (this.normal.dot(lineDirection.normalize()) == 0) {
			return null;
		}
		float t = (this.normal.dot(this.point) - this.normal.dot(linePoint)) / this.normal.dot(lineDirection.normalize());
		return linePoint.add(lineDirection.normalize().multiply(t));
	}
	
	public Vector3f minimalIntersectionPoint(Vector3f point) {
		return lineIntersectionPoint(point, this.normal.multiply(-1.0f));
	}
}
