package entities;

import org.atriasoft.etk.math.Vector3f;

import models.TexturedModel;
import renderEngine.DisplayManager;
import terrains.Terrain;

public class Player extends Entity {
	
	private static final float RUN_SPEED = 35;
	private static final float TRUN_SPEED = (float) Math.toRadians(120);
	private static final float GRAVITY = -50;
	private static final float JUMP_POWER = 30;
	
	private static final float TERRAIN_HEIGHT = 0;
	
	private float currentSpeed = 0;
	private float currentTurnSpeed = 0;
	private float upwardSpeed = 0;
	
	private boolean isInAir = false;
	
	public Player(final TexturedModel model, final Vector3f position, final Vector3f rotation, final float scale) {
		super(model, position, rotation, scale);
		
	}
	
	public void checkInputs() {
		if (DisplayManager.isKeyDown('w') && DisplayManager.isKeyDown('s')) {
			this.currentSpeed = 0;
		} else if (DisplayManager.isKeyDown('w')) {
			this.currentSpeed = RUN_SPEED;
		} else if (DisplayManager.isKeyDown('s')) {
			this.currentSpeed = -RUN_SPEED;
		} else {
			this.currentSpeed = 0;
		}
		if (DisplayManager.isKeyDown('d') && DisplayManager.isKeyDown('a')) {
			this.currentTurnSpeed = 0;
		} else if (DisplayManager.isKeyDown('a')) {
			this.currentTurnSpeed = TRUN_SPEED;
		} else if (DisplayManager.isKeyDown('d')) {
			this.currentTurnSpeed = -TRUN_SPEED;
		} else {
			this.currentTurnSpeed = 0;
		}
		if (DisplayManager.isKeyDown(' ')) {
			jump();
		}
	}
	
	private void jump() {
		if (this.isInAir == true) {
			return;
		}
		this.upwardSpeed = JUMP_POWER;
		this.isInAir = true;
	}
	
	public void move(final Terrain terrain) {
		checkInputs();
		if (this.isInAir == false) {
			super.increaseRotation(0, this.currentTurnSpeed * DisplayManager.getFrameTimeSecconds(), 0);
		}
		float distance = this.currentSpeed * DisplayManager.getFrameTimeSecconds();
		float dx = (float) (distance * Math.sin(super.getRotation().y()));
		float dz = (float) (distance * Math.cos(super.getRotation().y()));
		super.increasePosition(dx, 0, dz);
		this.upwardSpeed += GRAVITY * DisplayManager.getFrameTimeSecconds();
		super.increasePosition(0, this.upwardSpeed * DisplayManager.getFrameTimeSecconds(), 0);
		float terrainHeight = terrain.getHeightOfTerrain(super.getPosition().x(), super.getPosition().z());
		if (super.getPosition().y() < terrainHeight) {
			this.upwardSpeed = 0;
			super.setPosition(new Vector3f(super.getPosition().x(), terrainHeight, super.getPosition().z()));
			this.isInAir = false;
		}
	}
	
}
