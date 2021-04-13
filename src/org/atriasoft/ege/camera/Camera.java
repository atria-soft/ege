package org.atriasoft.ege.camera;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;

//import entities.Player;
//import renderEngine.DisplayManager;

public class Camera {
	private float pitch = 0;
	private Vector3f position = new Vector3f(0, 0, 2);
	private float roll = 0;
	private float yaw = 0;
	
	public Camera() {
		
	}
	
	public Matrix4f getConvertionMatrix() {
		Matrix4f matrix = Matrix4f.IDENTITY;
		matrix = matrix.rotate(new Vector3f(1, 0, 0), getPitch());
		matrix = matrix.rotate(new Vector3f(0, 1, 0), getYaw());
		matrix = matrix.rotate(new Vector3f(0, 0, 1), getRoll());
		matrix = matrix.translate(new Vector3f(-this.position.x(), -this.position.y(), -this.position.z()));
		/*
		matrix = matrix.rotate(new Vector3f(1, 0, 0), 0.0f);
		matrix = matrix.rotate(new Vector3f(0, 1, 0), 0.0f);
		matrix = matrix.rotate(new Vector3f(0, 0, 1), 0.75f);
		matrix = matrix.translate(new Vector3f(0, 0, -7));
		*/
		return matrix;
	}
	
	public float getPitch() {
		return this.pitch;
	}
	
	public Vector3f getPosition() {
		return this.position;
	}
	
	public float getRoll() {
		return this.roll;
	}
	
	public float getYaw() {
		return this.yaw;
	}
	
	public void setPitch(final float pitch) {
		this.pitch = pitch;
	}
	
	public void setPosition(final Vector3f position) {
		this.position = position;
	}
	
	public void setRoll(final float roll) {
		this.roll = roll;
	}
	
	public void setYaw(final float yaw) {
		this.yaw = yaw;
	}
	
}
