package org.atriasoft.gameengine.camera;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;

//import entities.Player;
//import renderEngine.DisplayManager;

public class Camera {
	private Vector3f position = new Vector3f(0,0,2);
	private float pitch = 0;
	private float yaw = 0;
	private float roll = 0;
	
	public Camera() {
		
	}

	public Matrix4f getConvertionMatrix() {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		matrix.rotate(new Vector3f(1,0,0), getPitch());
		matrix.rotate(new Vector3f(0,1,0), getYaw());
		matrix.rotate(new Vector3f(0,0,1), getRoll());
		matrix.translate(new Vector3f(-position.x,-position.y,-position.z));
		return matrix;
	}
	public Vector3f getPosition() {
		return position;
	}
	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getYaw() {
		return yaw;
	}
	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getRoll() {
		return roll;
	}
	public void setRoll(float roll) {
		this.roll = roll;
	}
	
}
