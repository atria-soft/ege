package org.atriasoft.gameengine;

import org.atriasoft.etk.math.Vector3f;

public class Material {
	// Minimum of the ambient value of the material (default minimum color of the element)
	private Vector3f ambientFactor;
	// Material diffuse his own color (for lights...) 
	private Vector3f diffuseFactor;
	// Reflection of the lights
	private Vector3f specularFactor;
	// Distance of witch the camera must to be to receive the the reflection
	private float shininess;
	public Material(Vector3f ambientFactor, Vector3f diffuseFactor, Vector3f specularFactor, float shininess) {
		this.ambientFactor = ambientFactor;
		this.diffuseFactor = diffuseFactor;
		this.specularFactor = specularFactor;
		this.shininess = shininess;
	}
	public Material() {
		this.ambientFactor = new Vector3f(1,1,1);
		this.diffuseFactor = new Vector3f(0,0,0);
		this.specularFactor = new Vector3f(0,0,0);
		this.shininess = 1.0f;
	}
	public Vector3f getAmbientFactor() {
		return ambientFactor;
	}
	public void setAmbientFactor(Vector3f ambientFactor) {
		this.ambientFactor = ambientFactor;
	}
	public Vector3f getDiffuseFactor() {
		return diffuseFactor;
	}
	public void setDiffuseFactor(Vector3f diffuseFactor) {
		this.diffuseFactor = diffuseFactor;
	}
	public Vector3f getSpecularFactor() {
		return specularFactor;
	}
	public void setSpecularFactor(Vector3f specularFactor) {
		this.specularFactor = specularFactor;
	}
	public float getShininess() {
		return shininess;
	}
	public void setShininess(float shininess) {
		this.shininess = shininess;
	}
	
}
