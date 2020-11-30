package org.atriasoft.gameengine.geometry;

import org.atriasoft.etk.math.Matrix3f;
import org.atriasoft.etk.math.Vector3f;

public class AABB {
	public Vector3f position;
	public Vector3f size; // HALF SIZE!
	
	public AABB(Vector3f position, Vector3f size) {
		this.position = position;
		this.size = size;
	}
	public AABB() {
		this.position = new Vector3f();
		this.size = new Vector3f(1.0f, 1.0f, 1.0f);
	}
	public void setMinMax(Vector3f min, Vector3f max) {
		this.position = min.addNew(max).multiply(0.5f);
		this.size = max.lessNew(min).multiply(0.5f);
	}
	
	public Vector3f getMin() {
		return new Vector3f(position.x-size.x, position.y-size.y, position.z-size.z);
	}
	public Vector3f getMax() {
		return new Vector3f(position.x+size.x, position.y+size.y, position.z+size.z);
	}
	@Override
	public String toString() {
		return "AABB [position=" + position + ", size=" + size + "]";
	}
}
