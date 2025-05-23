package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Matrix3f;
import org.atriasoft.etk.math.Vector3f;

public class OBB {
	public Vector3f position;
	public Vector3f size; // HALF SIZE!
	public Matrix3f orientation;
	
	public OBB() {
		this.position = Vector3f.ZERO;
		this.size = new Vector3f(1.0f, 1.0f, 1.0f);
		this.orientation = Matrix3f.IDENTITY;
	}
	
	public OBB(final Vector3f position, final Vector3f size) {
		this.position = position;
		this.size = size;
		this.orientation = Matrix3f.IDENTITY;
	}
	
	public OBB(final Vector3f position, final Vector3f size, final Matrix3f orientation) {
		this.position = position;
		this.size = size;
		this.orientation = orientation;
	}
	
	@Override
	public String toString() {
		return "OBB [position=" + this.position + ", size=" + this.size + ", orientation=" + this.orientation + "]";
	}
}
