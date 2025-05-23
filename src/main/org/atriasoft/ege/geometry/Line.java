package org.atriasoft.ege.geometry;

import org.atriasoft.etk.math.Vector3f;

public class Line {
	public Vector3f start;
	public Vector3f end;
	
	public Line() {
		this.start = Vector3f.ZERO;
		this.end = Vector3f.ZERO;
	}
	
	public Line(final Vector3f start, final Vector3f end) {
		this.start = start;
		this.end = end;
	}
	
	public float length() {
		return this.start.less(this.end).length();
	}
	
	public float length2() {
		return this.start.less(this.end).length2();
	}
	
	@Override
	public String toString() {
		return "Line [start=" + this.start + ", end=" + this.end + "]";
	}
	
}
