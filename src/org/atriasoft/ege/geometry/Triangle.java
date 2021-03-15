package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Vector3f;

public class Triangle {
	public Vector3f p1;
	public Vector3f p2;
	public Vector3f p3;
	public Triangle(Vector3f p1, Vector3f p2, Vector3f p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}
	public Triangle() {
		this.p1 = new Vector3f();
		this.p2 = new Vector3f();
		this.p3 = new Vector3f();
	}
	@Override
	public String toString() {
		return "Triangle [p1=" + p1 + ", p2=" + p2 + ", p3=" + p3 + "]";
	}

}
