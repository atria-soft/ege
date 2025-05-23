package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Vector3f;

public class Triangle {
	public static Vector3f getCenter(Vector3f p1, Vector3f p2, Vector3f p3) {
		return p1.add(p2).add(p3).multiply(0.33333333333f);
	}
	
	public static Vector3f getNormal(Vector3f p1, Vector3f p2, Vector3f p3) {
		Vector3f dir = p2.less(p1).cross(p3.less(p1));
		return dir.normalize();
	}
	
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
	public Triangle clone() {
		return new Triangle(this.p1, this.p2, this.p3);
	}
	
	public Vector3f getCenter() {
		return this.p1.add(this.p2).add(this.p3).multiply(0.33333333333f);
	}
	
	public Vector3f getNormal() {
		Vector3f dir = this.p2.less(this.p1).cross(this.p3.less(this.p1));
		return dir.normalize();
	}
	
	public Plane getPlane() {
		return new Plane(getNormal(), getCenter());
	}
	
	@Override
	public String toString() {
		return "Triangle [p1=" + this.p1 + ", p2=" + this.p2 + ", p3=" + this.p3 + "]";
	}
}
