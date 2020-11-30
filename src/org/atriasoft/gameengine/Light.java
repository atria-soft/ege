package org.atriasoft.gameengine;

import org.atriasoft.etk.math.Vector3f;

public class Light {
    private Vector3f color;
    // A light is linked with an entity, then the entity position the object and the light have a relative position with the entity
    private Vector3f positionDelta;
    private Vector3f attenuation;
	public Light(Vector3f color, Vector3f positionDelta, Vector3f attenuation) {
		this.color = color;
		this.positionDelta = positionDelta;
		this.attenuation = attenuation;
	}
	public Light() {
		this.color = new Vector3f(1.0f,1.0f,1.0f);
		this.positionDelta = new Vector3f(0.0f,0.0f,0.0f);
		this.attenuation = new Vector3f(0.0f,0.0f,0.0f);;
	}
	public Vector3f getColor() {
		return color;
	}
	public void setColor(Vector3f color) {
		this.color = color;
	}
	public Vector3f getPositionDelta() {
		return positionDelta;
	}
	public void setPositionDelta(Vector3f positionDelta) {
		this.positionDelta = positionDelta;
	}
	public Vector3f getAttenuation() {
		return attenuation;
	}
	public void setAttenuation(Vector3f attenuation) {
		this.attenuation = attenuation;
	}
}
