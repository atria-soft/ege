package org.atriasoft.gameengine.physics;

import org.atriasoft.etk.math.Vector3f;

public class ColisionPoints {
	public Vector3f position;
	public Vector3f force;

	public ColisionPoints(Vector3f position, Vector3f force) {
		super();
		this.position = position;
		this.force = force;
	}
}
