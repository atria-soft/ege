package entities;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;

import renderEngine.DisplayManager;

public class Camera {
	
	private float distanceFromPlayer = 20;
	private float angleAroundPlayer = 0;
	
	private Vector3f position = new Vector3f(0,5,0);
	private float pitch = 0;// (float) Math.toRadians(10);
	private float yaw = 0;
	private float roll = 0;

	private Player player;
	
	public Camera(Player player) {
		this.player = player;
	}
	public void move() {
		calculateZoom();
		calculatePitch();
		CalculateAngleAroundPlayer();
		float horinzontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horinzontalDistance, verticalDistance);
		this.yaw = 3.141596f - player.getRotation().y - angleAroundPlayer ;
	}

	private void calculateCameraPosition (float horizontalDistance, float verticalDistance) {
		float theta = 3.141596f +  player.getRotation().y + angleAroundPlayer;
		float offsetX = (float) (horizontalDistance * Math.sin(theta));
		float offsetZ = (float) (horizontalDistance * Math.cos(theta));
		position.x = player.getPosition().x + offsetX;
		position.z = player.getPosition().z + offsetZ;
		position.y = player.getPosition().y+5 + verticalDistance;
		
	}
	
	
	private float calculateHorizontalDistance() {
		return (float) (distanceFromPlayer * Math.cos(pitch));
	}
	
	private float calculateVerticalDistance() {
		return (float) (distanceFromPlayer * Math.sin(pitch));
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}
	
	private void calculateZoom() { 
		float zoomLevel = DisplayManager.getDWheel() * 1;
		distanceFromPlayer -= zoomLevel;
	}
	
	private void calculatePitch() {
		if (DisplayManager.isButtonRightDown() ) {
			float pitchChange = DisplayManager.getDY() * 0.01f;
			pitch += pitchChange;
			if (pitch < 0) {
				pitch = 0;
			} else if (pitch > 3.14159f/2f) {
				pitch = 3.14159f/2f;
			}
		}
	}
	
	private void CalculateAngleAroundPlayer() {
		if (DisplayManager.isButtonLeftDown()) {
			float angleChange = DisplayManager.getDX() * 0.003f;
			angleAroundPlayer -= angleChange;
		}
	}
	
}
