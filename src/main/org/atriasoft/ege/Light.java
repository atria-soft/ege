package org.atriasoft.ege;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector3f;

public class Light {
	private Color color;
	// A light is linked with an entity, then the entity position the object and the light have a relative position with the entity
	private Vector3f positionDelta;
	private Vector3f attenuation;
	// Spot light: direction of the cone (normalized). Zero = omnidirectional (point light).
	private Vector3f direction;
	// Spot light: cosine of the outer cutoff half-angle. 0 = point light (no cone).
	private float cutoffCos;
	// Spot light: cosine of the inner cutoff half-angle. Light is full intensity inside this cone.
	// innerAngle = outerAngle * (1 - blend). When blend=0, inner=outer (hard edge).
	private float cutoffCosInner;
	// Physical radius of the light source. 0 = point source. Used for soft shadows and source visualization.
	private float radius;

	public Light(final Color color, final Vector3f positionDelta, final Vector3f attenuation) {
		this.color = color;
		this.positionDelta = positionDelta;
		this.attenuation = attenuation;
		this.direction = Vector3f.ZERO;
		this.cutoffCos = 0.0f;
		this.cutoffCosInner = 0.0f;
		this.radius = 0.0f;
	}

	/**
	 * @param cutoffAngle full cone angle in radians (as exported by Blender).
	 *                    Internally halved to get the half-angle for cosine comparison.
	 * @param blend       softness factor [0..1]. 0 = hard edge, 1 = fully soft.
	 *                    Inner cone half-angle = outerHalfAngle * (1 - blend).
	 */
	public Light(final Color color, final Vector3f positionDelta, final Vector3f attenuation,
			final Vector3f direction, final float cutoffAngle, final float blend) {
		this.color = color;
		this.positionDelta = positionDelta;
		this.attenuation = attenuation;
		this.direction = direction;
		final float outerHalfAngle = cutoffAngle * 0.5f;
		this.cutoffCos = (float) Math.cos(outerHalfAngle);
		final float innerHalfAngle = outerHalfAngle * (1.0f - blend);
		this.cutoffCosInner = (float) Math.cos(innerHalfAngle);
		this.radius = 0.0f;
	}

	public Light() {
		this.color = Color.WHITE;
		this.positionDelta = Vector3f.ZERO;
		this.attenuation = Vector3f.ZERO;
		this.direction = Vector3f.ZERO;
		this.cutoffCos = 0.0f;
		this.cutoffCosInner = 0.0f;
		this.radius = 0.0f;
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

	public Vector3f getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector3f direction) {
		this.direction = direction;
	}

	public float getCutoffCos() {
		return this.cutoffCos;
	}

	public void setCutoffCos(final float cutoffCos) {
		this.cutoffCos = cutoffCos;
	}

	public float getCutoffCosInner() {
		return this.cutoffCosInner;
	}

	public void setCutoffCosInner(final float cutoffCosInner) {
		this.cutoffCosInner = cutoffCosInner;
	}

	public float getRadius() {
		return this.radius;
	}

	public void setRadius(final float radius) {
		this.radius = radius;
	}

	@Override
	public String toString() {
		return "Light [color=" + this.color + ", positionDelta=" + this.positionDelta
				+ ", attenuation=" + this.attenuation + ", direction=" + this.direction
				+ ", cutoffCos=" + this.cutoffCos + ", cutoffCosInner=" + this.cutoffCosInner
				+ ", radius=" + this.radius + "]";
	}
}
