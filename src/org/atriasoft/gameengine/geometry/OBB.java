package org.atriasoft.gameengine.geometry;

import org.atriasoft.etk.math.Matrix3f;
import org.atriasoft.etk.math.Vector3f;

public class OBB {
	public Vector3f position;
	public Vector3f size; // HALF SIZE!
	public Matrix3f orientation;
	
	public OBB(Vector3f position, Vector3f size, Matrix3f orientation) {
		this.position = position;
		this.size = size;
		this.orientation = orientation;
	}
	public OBB(Vector3f position, Vector3f size) {
		this.position = position;
		this.size = size;
		this.orientation = Matrix3f.identity();
	}
	public OBB() {
		this.position = new Vector3f();
		this.size = new Vector3f(1.0f, 1.0f, 1.0f);
		this.orientation = Matrix3f.identity();
	}
	@Override
	public String toString() {
		return "OBB [position=" + position + ", size=" + size + ", orientation=" + orientation + "]";
	}
}
