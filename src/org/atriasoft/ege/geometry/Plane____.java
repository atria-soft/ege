package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Vector3f;

public class Plane____ {
	public Vector3f normal;
	public float distance;

	public Plane____(Vector3f normal, float distance) {
		this.normal = normal;
		this.distance = distance;
	}
	public Plane____() {
		this.normal = new Vector3f(1.0f, 0.0f, 0.0f);
		this.distance = 0;
	}
	@Override
	public String toString() {
		return "Plane [normal=" + normal + ", distance=" + distance + "]";
	}
}
