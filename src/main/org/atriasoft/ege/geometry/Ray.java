package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Vector3f;

import toolbox.Maths;

public record Ray(Vector3f origin, Vector3f direction) {
	public static Ray createFromPoint(final Vector3f origin, final Vector3f destination) {
		return new Ray(origin, destination.less(origin).safeNormalize());
	}
	
	public Ray() {
		this(Vector3f.ZERO, new Vector3f(0.0f, 0.0f, 1.0f));
	}
	
	public Ray(final Vector3f origin, final Vector3f direction) {
		this.origin = origin;
		this.direction = direction;
	}
		
	@Override
	public String toString() {
		return "Ray [origin=" + this.origin + ", direction=" + this.direction + "]";
	}

	/**
	 * Get the position on the top or bottom plane describe in parameters.
	 * @param normalPlane Normal description of the plane.
	 * @param distancePlane distance to define the plane position.
	 * @return position on the plane intersection (null if not collide).
	 */
	public Vector3f intersectPlane(Vector3f normalPlane, float distancePlane) {
		float denom = normalPlane.dot(this.direction);
		// Prevent divide by zero:
		if (Math.abs(denom) <= 1e-4f) {
			return null;
		}
		float t = -(normalPlane.dot(this.origin) + distancePlane) / normalPlane.dot(this.direction);
		
		// Use pointy end of the ray.
		// It is technically correct to compare t < 0,
		// but that may be undesirable in a raytracer.
		if (t <= 1e-4) {
			return null;
		}
		return this.origin.add(this.direction.multiply(t));
	}
	/**
	 * Get the position on the top plane describe in parameters.
	 * @param normalPlane Normal description of the plane.
	 * @param distancePlane distance to define the plane position.
	 * @return position on the plane intersection (null if not collide).
	 */
	public Vector3f intersectPlaneTop(Vector3f normalPlane, float distancePlane) {
		float denom = normalPlane.dot(this.direction);
		
		if (-denom <= 1e-4f) {
		     return null;
		}
		
		float t = -(normalPlane.dot(this.origin) + distancePlane) / normalPlane.dot(this.direction);
		
		// Use pointy end of the ray.
		// It is technically correct to compare t < 0,
		// but that may be undesirable in a raytracer.
		if (t <= 1e-4) {
			return null;
		}
		return this.origin.add(this.direction.multiply(t));
	}

	public boolean intersectSphere(Vector3f sphereCenter, float sphereSize) {
		//solve for tc
		Vector3f L = sphereCenter.less(this.origin);
		float tc = L.dot(this.direction);
		if ( tc < 0.0f ) {
			return false;
		}
		float d2 = L.length2() - tc*tc;
		float radius2 = sphereSize * sphereSize;
		if ( d2 > radius2) {
			return false;
		}
		return true;
	}
	public record ReturnIntersectSphere(Vector3f pos1, Vector3f pos2) {};
	
	public ReturnIntersectSphere intersectSpherePos(Vector3f sphereCenter, float sphereSize) {
		//solve for tc
		Vector3f L = sphereCenter.less(this.origin);
		float tc = L.dot(this.direction);
		if ( tc < 0.0f ) {
			return null;
		}
		float d2 = L.length2() - tc*tc;
		
		float radius2 = sphereSize * sphereSize;
		if ( d2 > radius2) {
			return null;
		}
		
		//solve for t1c
		float t1c = (float) Math.sqrt( radius2 - d2 );
		
		//solve for intersection points
		float t1 = tc - t1c;
		float t2 = tc + t1c;
		
		return new ReturnIntersectSphere(this.origin.add(this.direction().multiply(t1)),
				                         this.origin.add(this.direction().multiply(t2)) );
	}
	
	/**
	 * Test intersection between this ray and a triangle defined by 3 vertices.
	 * Uses Moller-Trumbore-like algorithm (geometric approach).
	 * @param v0 First vertex of the triangle
	 * @param v1 Second vertex of the triangle
	 * @param v2 Third vertex of the triangle
	 * @return The intersection point, or null if no intersection
	 */
	public Vector3f intersectTriangle(final Vector3f v0, final Vector3f v1, final Vector3f v2) {
		final Vector3f v0v1 = v1.less(v0);
		final Vector3f v0v2 = v2.less(v0);
		final Vector3f N = v0v1.cross(v0v2);

		final float NdotDir = N.dot(this.direction);
		if (Math.abs(NdotDir) < 1e-7f) {
			return null; // parallel
		}

		final float d = -N.dot(v0);
		final float t = -(N.dot(this.origin) + d) / NdotDir;
		if (t < 0) {
			return null; // triangle is behind
		}

		final Vector3f P = this.origin.add(this.direction.multiply(t));

		// inside-outside test
		if (N.dot((v1.less(v0)).cross(P.less(v0))) < 0) {
			return null;
		}
		if (N.dot((v2.less(v1)).cross(P.less(v1))) < 0) {
			return null;
		}
		if (N.dot((v0.less(v2)).cross(P.less(v2))) < 0) {
			return null;
		}

		return P;
	}

}
