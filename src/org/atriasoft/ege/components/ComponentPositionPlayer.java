package org.atriasoft.ege.components;

import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;

public class ComponentPositionPlayer extends ComponentPosition {
	private Vector3f angles = new Vector3f(0, 0, 0);
	
	public ComponentPositionPlayer() {
		super();
	}
	
	public ComponentPositionPlayer(final Transform3D transform) {
		super(transform);
		// TODO deduce angle of the player
	}
	
	public Vector3f getAngles() {
		return this.angles;
	}
	
	public void setAngles(final Vector3f angles) {
		this.angles = angles;
		// TODO update transform3D
		this.transform = this.transform.withOrientation(Quaternion.fromEulerAngles(this.angles));
	}
	
}
