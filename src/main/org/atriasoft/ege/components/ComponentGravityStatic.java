package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Light;
import org.atriasoft.etk.math.Vector3f;

public class ComponentGravityStatic extends ComponentGravity {
	private Vector3f gravity;
	public ComponentGravityStatic(Vector3f gravity) {
		super();
		this.gravity = gravity;
	}
	@Override
	public Vector3f getGravityAtPosition(Vector3f position) {
		return gravity;
	}
	public Vector3f getGravity() {
		return gravity;
	}
	public void setGravity(Vector3f gravity) {
		this.gravity = gravity;
	}
}
