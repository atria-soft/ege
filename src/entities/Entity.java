package entities;

import org.atriasoft.etk.math.Vector3f;

import models.TexturedModel;

public class Entity {
	private TexturedModel model;
	private Vector3f position;
	private Vector3f rotation;
	private float scale;
	private int textureIndex = 0;
	
	public Entity(final TexturedModel model, final int textureIndex, final Vector3f position, final Vector3f rotation, final float scale) {
		this.model = model;
		this.textureIndex = textureIndex;
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
	}
	
	public Entity(final TexturedModel model, final Vector3f position, final Vector3f rotation, final float scale) {
		this.model = model;
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
	}
	
	public TexturedModel getModel() {
		return this.model;
	}
	
	public Vector3f getPosition() {
		return this.position;
	}
	
	public Vector3f getRotation() {
		return this.rotation;
	}
	
	public float getScale() {
		return this.scale;
	}
	
	public float getTextureXOffset() {
		int column = this.textureIndex % this.model.getTexture().getNumberOfRows();
		return (float) column / (float) this.model.getTexture().getNumberOfRows();
	}
	
	public float getTextureYOffset() {
		int row = this.textureIndex / this.model.getTexture().getNumberOfRows();
		return (float) row / (float) this.model.getTexture().getNumberOfRows();
	}
	
	public void increasePosition(final float dx, final float dy, final float dz) {
		this.position = new Vector3f(this.position.x() + dx, this.position.y() + dy, this.position.z() + dz);
	}
	
	public void increasePosition(final Vector3f delta) {
		this.position = new Vector3f(this.position.x() + delta.x(), this.position.y() + delta.y(), this.position.z() + delta.z());
	}
	
	public void increaseRotation(final float dx, final float dy, final float dz) {
		this.rotation = new Vector3f(this.rotation.x() + dx, this.rotation.y() + dy, this.rotation.z() + dz);
	}
	
	public void increaseRotation(final Vector3f delta) {
		this.rotation = new Vector3f(this.rotation.x() + delta.x(), this.rotation.y() + delta.y(), this.rotation.z() + delta.z());
	}
	
	public void setModel(final TexturedModel model) {
		this.model = model;
	}
	
	public void setPosition(final Vector3f position) {
		this.position = position;
	}
	
	public void setRotation(final Vector3f rotation) {
		this.rotation = rotation;
	}
	
	public void setScale(final float scale) {
		this.scale = scale;
	}
	
}
