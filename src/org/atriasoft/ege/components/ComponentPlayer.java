package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.engines.EnginePlayer;

public class ComponentPlayer extends Component {
	private float runSpeed = 20;
	private float strafSpeed = 25;
	private float turnSpeed = 0.45f;
	private float jumpPower = 30;
	private float walkFactor = 0.37f;
	
	public ComponentPlayer() {
		
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return EnginePlayer.ENGINE_NAME;
	}

	public void update(float timeStep) {
		
	}

	public float getRunSpeed() {
		return runSpeed;
	}

	public void setRunSpeed(float runSpeed) {
		this.runSpeed = runSpeed;
	}

	public float getTurnSpeed() {
		return turnSpeed;
	}

	public void setTurnSpeed(float turnSpeed) {
		this.turnSpeed = turnSpeed;
	}

	public float getJumpPower() {
		return jumpPower;
	}

	public void setJumpPower(float jumpPower) {
		this.jumpPower = jumpPower;
	}

	public float getStrafSpeed() {
		return strafSpeed;
	}

	public void setStrafSpeed(float strafSpeed) {
		this.strafSpeed = strafSpeed;
	}

	public float getWalkFactor() {
		return walkFactor;
	}

	public void setWalkFactor(float walkFactor) {
		this.walkFactor = walkFactor;
	}

	
}
