package org.atriasoft.gameengine.geometry;

import org.atriasoft.etk.math.Vector3f;

public class Line {
	public Vector3f start;
	public Vector3f end;

	public Line(Vector3f start, Vector3f end) {
		this.start = start;
		this.end = end;
	}
	public Line() {
		this.start = new Vector3f();
		this.end = new Vector3f();
	}
	@Override
	public String toString() {
		return "Line [start=" + start + ", end=" + end + "]";
	}

	public float length2() {
		return this.start.lessNew(this.end).length2();
	}
	public float length() {
		return this.start.lessNew(this.end).length();
	}

}
