package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Vector3f;

public class Sphere {
	public Vector3f position;
	public float radius;

	public Sphere(Vector3f position, float radius) {
		this.position = position;
		this.radius = radius;
	}
	public Sphere() {
		this.position = new Vector3f();
		this.radius = 1.0f;
	}
	@Override
	public String toString() {
		return "Sphere [position=" + position + ", radius=" + radius + "]";
	}
}
