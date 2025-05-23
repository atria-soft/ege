package org.atriasoft.ege;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector3f;

public class Light {
    private Color color;
    // A light is linked with an entity, then the entity position the object and the light have a relative position with the entity
    private Vector3f positionDelta;
    private Vector3f attenuation;
	public Light(final Color color, final Vector3f positionDelta, final Vector3f attenuation) {
		this.color = color;
		this.positionDelta = positionDelta;
		this.attenuation = attenuation;
	}
	public Light() {
		this.color = Color.WHITE;
		this.positionDelta = Vector3f.ZERO;
		this.attenuation = Vector3f.ZERO;
	}
	public Color getColor() {
		return this.color;
	}
	public void setColor(final Color color) {
		this.color = color;
	}
	public Vector3f getPositionDelta() {
		return this.positionDelta;
	}
	public void setPositionDelta(final Vector3f positionDelta) {
		this.positionDelta = positionDelta;
	}
	public Vector3f getAttenuation() {
		return this.attenuation;
	}
	public void setAttenuation(final Vector3f attenuation) {
		this.attenuation = attenuation;
	}
	@Override
	public String toString() {
		return "Light [color=" + this.color + ", positionDelta=" + this.positionDelta + ", attenuation=" + this.attenuation + "]";
	}
	
}
