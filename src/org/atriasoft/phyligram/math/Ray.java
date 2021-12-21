package org.atriasoft.phyligram.math;

import org.atriasoft.etk.math.Vector3f;

public class Ray {
	public float maxFraction; //!< Maximum fraction value
	public Vector3f point1; //!<First point of the ray (origin)
	public Vector3f point2; //!< Second point of the ray
	
	public Ray(final Ray obj) {
		this.point1 = obj.point1;
		this.point2 = obj.point2;
		this.maxFraction = obj.maxFraction;
	}
	
	/// Constructor with arguments
	public Ray(final Vector3f p1, final Vector3f p2) {
		this.point1 = p1;
		this.point2 = p2;
		this.maxFraction = 1.0f;
	}
	
	public Ray(final Vector3f p1, final Vector3f p2, final float maxFrac) {
		this.point1 = p1;
		this.point2 = p2;
		this.maxFraction = maxFrac;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Ray(this);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Ray other = (Ray) obj;
		
		return this.point1.equals(other.point1) && this.point2.equals(other.point2) && this.maxFraction == other.maxFraction;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
	
}
