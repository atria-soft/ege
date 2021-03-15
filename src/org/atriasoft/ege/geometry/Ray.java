package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Vector3f;

public class Ray {
	public Vector3f origin;
	public Vector3f direction;
	
	public Ray() {
		this.origin = new Vector3f();
		this.direction = new Vector3f(0.0f, 0.0f, 1.0f);
	}
	public Ray(Vector3f origin, Vector3f direction) {
		this.origin = origin;
		this.direction = direction;
	}
	public static Ray createFromPoint(Vector3f origin, Vector3f destination) {
		Ray out = new Ray(origin, destination.lessNew(origin));
		out.normalizeDirection();
		return out;
	}
	public void normalizeDirection() {
		direction.safeNormalize();
	}
	@Override
	public String toString() {
		return "Ray [origin=" + origin + ", direction=" + direction + "]";
	}
}
