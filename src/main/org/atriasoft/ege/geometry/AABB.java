package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Vector3f;

public class AABB {
	public Vector3f position;
	public Vector3f size; // HALF SIZE!
	
	public AABB() {
		this.position = Vector3f.ZERO;
		this.size = Vector3f.ONE;
	}
	
	public AABB(final Vector3f position, final Vector3f size) {
		this.position = position;
		this.size = size;
	}
	
	public Vector3f getMax() {
		return new Vector3f(this.position.x() + this.size.x(), this.position.y() + this.size.y(), this.position.z() + this.size.z());
	}
	
	public Vector3f getMin() {
		return new Vector3f(this.position.x() - this.size.x(), this.position.y() - this.size.y(), this.position.z() - this.size.z());
	}
	
	public void setMinMax(final Vector3f min, final Vector3f max) {
		this.position = min.add(max).multiply(0.5f);
		this.size = max.less(min).multiply(0.5f);
	}
	
	@Override
	public String toString() {
		return "AABB [position=" + this.position + ", size=" + this.size + "]";
	}
}
