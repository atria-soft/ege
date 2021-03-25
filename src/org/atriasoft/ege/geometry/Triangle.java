package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Vector3f;

public class Triangle {
	public Vector3f p1;
	public Vector3f p2;
	public Vector3f p3;
	
	public Triangle() {
		this.p1 = Vector3f.ZERO;
		this.p2 = Vector3f.ZERO;
		this.p3 = Vector3f.ZERO;
	}
	
	public Triangle(final Vector3f p1, final Vector3f p2, final Vector3f p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}
	
	@Override
	public String toString() {
		return "Triangle [p1=" + this.p1 + ", p2=" + this.p2 + ", p3=" + this.p3 + "]";
	}
	
}
