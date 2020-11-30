package org.atriasoft.gameengine;

import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.Gale;
import org.atriasoft.gale.event.EventEntry;
import org.atriasoft.gale.event.EventInput;
import org.atriasoft.gale.event.EventTime;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.gameengine.camera.Camera;
import org.atriasoft.gameengine.internal.Log;
import org.atriasoft.gameengine.components.ComponentPlayer;
import org.atriasoft.gameengine.components.ComponentPosition;
import org.atriasoft.gameengine.components.ComponentPositionPlayer;

import renderEngine.DisplayManager;


public class ControlCameraPlayer implements ControlInterface {
	private Camera camera;
	private float distanceFromCenter = 2.5f;
	private boolean fpsMode = false;
	private Entity playerEntity;
	private ComponentPositionPlayer playerPosition;
	private ComponentPlayer player;
	private boolean moveUp = false;
	private boolean moveDown = false;
	private boolean moveLeft = false;
	private boolean moveRight = false;
	private boolean walk = false;

	public ControlCameraPlayer(Camera camera, Entity playerEntity) {
		this.camera = camera;
		this.playerEntity = playerEntity;
		this.playerPosition = (ComponentPositionPlayer)this.playerEntity.getComponent("position");
		this.player = (ComponentPlayer)this.playerEntity.getComponent("player");
	}
	private boolean getState(KeyStatus state, boolean previousState) {
		if (state == KeyStatus.down) {
			return true;
		}
		if (state == KeyStatus.up) {
			return false;
		}
		return previousState;
	}
	@Override
	public boolean onEventEntry(EventEntry event) {
		if(event.getType() == KeyKeyboard.up
			|| (event.getType() == KeyKeyboard.character && (event.getChar() == 'z' || event.getChar() == 'Z' ))) {
			moveUp = getState(event.getStatus(), moveUp);
		}
		if(event.getType() == KeyKeyboard.left
				|| (event.getType() == KeyKeyboard.character && (event.getChar() == 'q' || event.getChar() == 'Q' ))) {
			moveLeft = getState(event.getStatus(), moveLeft);
		}
		if(event.getType() == KeyKeyboard.right
				|| (event.getType() == KeyKeyboard.character && (event.getChar() == 'd' || event.getChar() == 'D' ))) {
			moveRight = getState(event.getStatus(), moveRight);
		}
		if(event.getType() == KeyKeyboard.down
				|| (event.getType() == KeyKeyboard.character && (event.getChar() == 's' || event.getChar() == 'S' ))) {
			moveDown = getState(event.getStatus(), moveDown);
		}
		if(event.getType() == KeyKeyboard.shiftLeft || event.getType() == KeyKeyboard.shiftRight) {
			walk = event.getSpecialKey().getShift();
		}
		if(event.getType() == KeyKeyboard.f10) {
			if (event.getStatus() == KeyStatus.up) {
				if (fpsMode == false) {
					fpsMode = true;
					distanceFromCenter = 0;
				} else {
					fpsMode = false;
					distanceFromCenter = 2.5f;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEventInput(EventInput event, Vector2f relativePosition) {
		// Log.info("" + event);
		// TODO Auto-generated method stub
		if (fpsMode == false) {
			if (event.getInputId() == 4) {
				if (event.getStatus() == KeyStatus.down) {
					distanceFromCenter -= 0.2;
				}
				if (distanceFromCenter < 0.0) {
					distanceFromCenter = 0.0f;
				}
				return true;
			} else if (event.getInputId() == 5) {
				if (event.getStatus() == KeyStatus.down) {
					distanceFromCenter += 0.2;
				}
				if (distanceFromCenter < 0.3) {
					distanceFromCenter = 0.3f;
				}
				return true;
			}
		} else {
			distanceFromCenter = 0;
		}
		// TODO check if grabbing is enable ...
		// in grabbing mouse only:
		if (Gale.getContext().isGrabPointerEvents() == false) {
			return false;
		}
		if (event.getStatus() == KeyStatus.move) {
			Vector2f delta = event.getPosition().clone();
			//angleZ += delta.x;
			//this.camera.setYaw(this.camera.getYaw() + (float)Math.toRadians(delta.x));
			this.camera.setPitch(this.camera.getPitch() + (float)Math.toRadians(delta.y * this.player.getTurnSpeed()));
			if (this.camera.getPitch()>0) {
				this.camera.setPitch(0);
			}
			if (this.camera.getPitch()<-Math.PI) {
				this.camera.setPitch((float)-Math.PI);
			}
			/*
			this.camera.setRoll(this.camera.getRoll() - (float)Math.toRadians(delta.x * this.player.getTurnSpeed()));
			Log.info("Change camera: " + this.camera.getYaw() + " " + this.camera.getPitch());
			if (this.camera.getRoll()>Math.PI) {
				this.camera.setRoll(this.camera.getRoll()-(float)Math.PI*2.0f);
			}
			if (this.camera.getRoll()<-Math.PI) {
				this.camera.setRoll(this.camera.getRoll()+(float)Math.PI*2.0f);
			}
			this.playerPosition.setAngles(new Vector3f(0,0,-this.camera.getRoll()));
			*/
			float tmpAngle = this.playerPosition.getAngles().z + (float)Math.toRadians(delta.x * this.player.getTurnSpeed());
			
			if (tmpAngle > Math.PI) {
				tmpAngle -= (float)Math.PI*2.0f;
			}
			if (tmpAngle < -Math.PI) {
				tmpAngle += (float)Math.PI*2.0f;
			}
			this.playerPosition.setAngles(new Vector3f(0,0,tmpAngle));
			this.camera.setRoll(-this.playerPosition.getAngles().z);
			Log.info("Change camera: " + this.camera.getYaw() + " " + this.camera.getPitch());
		}
		return false;
	}

	@Override
	public void periodicCall(EventTime event) {
		float speed = 0;
		float walkFactor = 1;
		if (this.walk == true) {
			walkFactor = this.player.getWalkFactor();
		}
		//distanceFromCenter = 6;
		if (moveUp != moveDown) {
			if (moveUp) {
				speed = this.player.getRunSpeed();
			} else {
				speed = -this.player.getRunSpeed();
			}
		}
		float distance = speed * walkFactor * event.getTimeDeltaCallSecond();
		float dx = -(float) (distance * Math.sin(this.playerPosition.getAngles().z));
		float dy = (float) (distance * Math.cos(this.playerPosition.getAngles().z));
		speed = 0;
		if (moveRight != moveLeft) {
			if (moveRight) {
				speed = this.player.getStrafSpeed();
			} else {
				speed = -this.player.getStrafSpeed();
			}
		}
		distance = speed * walkFactor * event.getTimeDeltaCallSecond();
		float dxStraf = (float) (distance * Math.sin((float)Math.PI*0.5f + this.playerPosition.getAngles().z));
		float dyStraf = -(float) (distance * Math.cos((float)Math.PI*0.5f + this.playerPosition.getAngles().z));
		//Log.error("update position ..." + dx + "   " + dy);
		this.playerPosition.getTransform().getPosition().x += dx + dxStraf;
		this.playerPosition.getTransform().getPosition().y += dy + dyStraf;
		// here the camera is behind the player, we need to move the camera ...
		//Log.info(" pitch: " + Math.toDegrees(this.camera.getPitch()) + "  " + Math.toDegrees(this.playerPosition.getAngles().z));
		float horinzontalDistance = (float) (distanceFromCenter * Math.sin(this.camera.getPitch()));
		float verticalDistance = (float) (distanceFromCenter * Math.cos(this.camera.getPitch()));
		//Log.info("     distanceFromCenter " + distanceFromCenter);
		float tmp = -horinzontalDistance;
		float theta = (float)Math.PI + this.playerPosition.getAngles().z;// - (float)Math.PI*0.5f;
		float offsetX = (float) (tmp * Math.sin(-theta));
		float offsetY = (float) (tmp * Math.cos(-theta));
		//Log.info("     res" + offsetX + "  " + offsetY);
		this.camera.getPosition().x = this.playerPosition.getTransform().getPosition().x + offsetX;
		this.camera.getPosition().y = this.playerPosition.getTransform().getPosition().y + offsetY;
		this.camera.getPosition().z = this.playerPosition.getTransform().getPosition().z + 1.6f + verticalDistance;
	}

}
