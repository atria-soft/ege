package org.atriasoft.phyligram;

import org.atriasoft.etk.math.Vector3f;

public class ColisionPoints {
	public Vector3f position;
	public Vector3f force;

	public ColisionPoints(Vector3f position, Vector3f force) {
		this.position = position;
		this.force = force;
	}
}
