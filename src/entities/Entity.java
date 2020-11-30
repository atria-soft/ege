package entities;

import org.atriasoft.etk.math.Vector3f;

import models.TexturedModel;

public class Entity {
	private TexturedModel model;
	private Vector3f position;
	private Vector3f rotation;
	private float scale;
	private int textureIndex = 0;
	public Entity(TexturedModel model, Vector3f position, Vector3f rotation, float scale) {
		this.model = model;
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
	}
	public Entity(TexturedModel model, int textureIndex, Vector3f position, Vector3f rotation, float scale) {
		this.model = model;
		this.textureIndex = textureIndex;
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
	}

	public float getTextureXOffset() {
		int column = textureIndex%model.getTexture().getNumberOfRows();
		return (float)column/(float)model.getTexture().getNumberOfRows();
	}
	public float getTextureYOffset() {
		int row = textureIndex/model.getTexture().getNumberOfRows();
		return (float)row/(float)model.getTexture().getNumberOfRows();
	}
	
	
	public void increasePosition(float dx, float dy, float dz) {
		this.position = new Vector3f(position.x + dx, position.y + dy, position.z + dz);
	}
	public void increasePosition(Vector3f delta) {
		this.position = new Vector3f(position.x + delta.x, position.y + delta.y, position.z + delta.z);
	}
	public void increaseRotation(float dx, float dy, float dz) {
		this.rotation = new Vector3f(rotation.x + dx, rotation.y + dy, rotation.z + dz);
	}
	public void increaseRotation(Vector3f delta) {
		this.rotation = new Vector3f(rotation.x + delta.x, rotation.y + delta.y, rotation.z + delta.z);
	}
	
	public TexturedModel getModel() {
		return model;
	}
	public void setModel(TexturedModel model) {
		this.model = model;
	}
	public Vector3f getPosition() {
		return position;
	}
	public void setPosition(Vector3f position) {
		this.position = position;
	}
	public Vector3f getRotation() {
		return rotation;
	}
	public void setRotation(Vector3f rotation) {
		this.rotation = rotation;
	}
	public float getScale() {
		return scale;
	}
	public void setScale(float scale) {
		this.scale = scale;
	}
	
	
}
