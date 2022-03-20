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
		float d2 = tc*tc - L.length2();
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
		float d2 = tc*tc - L.length2();
		
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
	
	Vector3f intersectTriangle( 
			Vector3f orig, Vector3f dir, 
			Vector3f v0, Vector3f v1, Vector3f v2) { 
			float t = 0; // output distance.
		    // compute plane's normal
			Vector3f v0v1 = v1.less(v0); 
			Vector3f v0v2 = v2.less(v0); 
		    // no need to normalize
			Vector3f N = v0v1.cross(v0v2); // N 
		    float area2 = N.length(); 
		 
		    // Step 1: finding P
		 
		    // check if ray and plane are parallel ?
		    float NdotRayDirection = N.dot(dir); 
		    if (Math.abs(NdotRayDirection) < 0.0000001) // almost 0 
		        return null; // they are parallel so they don't intersect ! 
		 
		    // compute d parameter using equation 2
		    float d = -N.dot(v0); 
		 
		    // compute t (equation 3)
		    t = -(N.dot(orig) + d) / NdotRayDirection; 
		 
		    // check if the triangle is in behind the ray
		    if (t < 0) {
		    	return null; // the triangle is behind 
		    }
		 
		    // compute the intersection point using equation 1
		    Vector3f P = orig.add(dir.multiply(t)); 
		 
		    // Step 2: inside-outside test
		    Vector3f C; // vector perpendicular to triangle's plane 
		 
		    // edge 0
		    Vector3f edge0 = v1.less(v0); 
		    Vector3f vp0 = P.less(v0); 
		    C = edge0.cross(vp0); 
		    if (N.dot(C) < 0) {
		    	return null; // P is on the right side 
		    }
		 
		    // edge 1
		    Vector3f edge1 = v2.less(v1); 
		    Vector3f vp1 = P.less(v1); 
		    C = edge1.cross(vp1); 
		    if (N.dot(C) < 0) {
		    	return null; // P is on the right side 
		    }
		 
		    // edge 2
		    Vector3f edge2 = v0.less(v2); 
		    Vector3f vp2 = P.less(v2); 
		    C = edge2.cross(vp2); 
		    if (N.dot(C) < 0) {
		    	return null; // P is on the right side; 
		    }
		 
		    return this.origin.add(this.direction().multiply(t)); // this ray hits the triangle 
		} 
	
	

}
