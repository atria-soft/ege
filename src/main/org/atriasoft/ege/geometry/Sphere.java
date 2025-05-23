package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Vector3f;

public class Sphere {
	public Vector3f position;
	public float radius;
	
	public Sphere() {
		this.position = Vector3f.ZERO;
		this.radius = 1.0f;
	}
	
	public Sphere(final Vector3f position, final float radius) {
		this.position = position;
		this.radius = radius;
	}
	
	@Override
	public String toString() {
		return "Sphere [position=" + this.position + ", radius=" + this.radius + "]";
	}
}
