package org.atriasoft.gameengine.components;

import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;

public class ComponentPositionPlayer extends ComponentPosition {
	private Vector3f angles = new Vector3f(0,0,0);


	public ComponentPositionPlayer() {
		super();
	}

	public ComponentPositionPlayer(Transform3D transform) {
		super(transform);
		// TODO deduce angle of the player
	}

	public Vector3f getAngles() {
		return angles;
	}

	public void setAngles(Vector3f angles) {
		this.angles = angles.clone();
		// TODO update transform3D
		this.transform.getOrientation().setEulerAngles(this.angles);
	}
	
}
