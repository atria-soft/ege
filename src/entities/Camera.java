package entities;

import org.atriasoft.etk.math.Vector3f;

import renderEngine.DisplayManager;

public class Camera {
	
	private float distanceFromPlayer = 20;
	private float angleAroundPlayer = 0;
	
	private Vector3f position = new Vector3f(0, 5, 0);
	private float pitch = 0;// (float) Math.toRadians(10);
	private float yaw = 0;
	private float roll = 0;
	
	private Player player;
	
	public Camera(final Player player) {
		this.player = player;
	}
	
	private void CalculateAngleAroundPlayer() {
		if (DisplayManager.isButtonLeftDown()) {
			float angleChange = DisplayManager.getDX() * 0.003f;
			this.angleAroundPlayer -= angleChange;
		}
	}
	
	private void calculateCameraPosition(final float horizontalDistance, final float verticalDistance) {
		float theta = 3.141596f + this.player.getRotation().y() + this.angleAroundPlayer;
		float offsetX = (float) (horizontalDistance * Math.sin(theta));
		float offsetZ = (float) (horizontalDistance * Math.cos(theta));
		this.position = new Vector3f(this.player.getPosition().x() + offsetX, this.player.getPosition().y() + 5 + verticalDistance, this.player.getPosition().z() + offsetZ);
	}
	
	private float calculateHorizontalDistance() {
		return (float) (this.distanceFromPlayer * Math.cos(this.pitch));
	}
	
	private void calculatePitch() {
		if (DisplayManager.isButtonRightDown()) {
			float pitchChange = DisplayManager.getDY() * 0.01f;
			this.pitch += pitchChange;
			if (this.pitch < 0) {
				this.pitch = 0;
			} else if (this.pitch > 3.14159f / 2f) {
				this.pitch = 3.14159f / 2f;
			}
		}
	}
	
	private float calculateVerticalDistance() {
		return (float) (this.distanceFromPlayer * Math.sin(this.pitch));
	}
	
	private void calculateZoom() {
		float zoomLevel = DisplayManager.getDWheel() * 1;
		this.distanceFromPlayer -= zoomLevel;
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
	
	public void move() {
		calculateZoom();
		calculatePitch();
		CalculateAngleAroundPlayer();
		float horinzontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horinzontalDistance, verticalDistance);
		this.yaw = 3.141596f - this.player.getRotation().y() - this.angleAroundPlayer;
	}
	
}
