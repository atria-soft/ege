package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Vector3f;

public class Ray {
	public static Ray createFromPoint(final Vector3f origin, final Vector3f destination) {
		Ray out = new Ray(origin, destination.less(origin));
		out.normalizeDirection();
		return out;
	}
	
	public Vector3f origin;
	
	public Vector3f direction;
	
	public Ray() {
		this.origin = Vector3f.ZERO;
		this.direction = new Vector3f(0.0f, 0.0f, 1.0f);
	}
	
	public Ray(final Vector3f origin, final Vector3f direction) {
		this.origin = origin;
		this.direction = direction;
	}
	
	public void normalizeDirection() {
		this.direction = this.direction.safeNormalize();
	}
	
	@Override
	public String toString() {
		return "Ray [origin=" + this.origin + ", direction=" + this.direction + "]";
	}
}
