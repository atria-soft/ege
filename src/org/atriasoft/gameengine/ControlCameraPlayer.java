package org.atriasoft.gameengine;

import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.Gale;
import org.atriasoft.gale.event.EventEntry;
import org.atriasoft.gale.event.EventInput;
import org.atriasoft.gale.event.EventTime;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.gameengine.camera.Camera;
import org.atriasoft.gameengine.components.ComponentPhysics;
import org.atriasoft.gameengine.components.ComponentPlayer;
import org.atriasoft.gameengine.components.ComponentPositionPlayer;
import org.atriasoft.gameengine.internal.Log;

public class ControlCameraPlayer implements ControlInterface {
	private final Camera camera;
	private float distanceFromCenter = 2.5f;
	private boolean fpsMode = false;
	private final Entity playerEntity;
	private ComponentPositionPlayer playerPosition;
	private ComponentPhysics playerPhysics;
	private final ComponentPlayer player;
	private boolean moveUp = false;
	private boolean moveDown = false;
	private boolean moveLeft = false;
	private boolean moveRight = false;
	private boolean walk = false;
	
	public ControlCameraPlayer(final Camera camera, final Entity playerEntity) {
		this.camera = camera;
		this.playerEntity = playerEntity;
		if (this.playerEntity.exist("position")) {
			this.playerPosition = (ComponentPositionPlayer) this.playerEntity.getComponent("position");
		} else if (this.playerEntity.exist("physics")) {
			this.playerPhysics = (ComponentPhysics) this.playerEntity.getComponent("physics");
		}
		this.player = (ComponentPlayer) this.playerEntity.getComponent("player");
	}
	
	private boolean getState(final KeyStatus state, final boolean previousState) {
		if (state == KeyStatus.down) {
			return true;
		}
		if (state == KeyStatus.up) {
			return false;
		}
		return previousState;
	}
	
	@Override
	public boolean onEventEntry(final EventEntry event) {
		if (event.getType() == KeyKeyboard.up || (event.getType() == KeyKeyboard.character && (event.getChar() == 'z' || event.getChar() == 'Z'))) {
			this.moveUp = getState(event.getStatus(), this.moveUp);
		}
		if (event.getType() == KeyKeyboard.left || (event.getType() == KeyKeyboard.character && (event.getChar() == 'q' || event.getChar() == 'Q'))) {
			this.moveLeft = getState(event.getStatus(), this.moveLeft);
		}
		if (event.getType() == KeyKeyboard.right || (event.getType() == KeyKeyboard.character && (event.getChar() == 'd' || event.getChar() == 'D'))) {
			this.moveRight = getState(event.getStatus(), this.moveRight);
		}
		if (event.getType() == KeyKeyboard.down || (event.getType() == KeyKeyboard.character && (event.getChar() == 's' || event.getChar() == 'S'))) {
			this.moveDown = getState(event.getStatus(), this.moveDown);
		}
		if (event.getType() == KeyKeyboard.shiftLeft || event.getType() == KeyKeyboard.shiftRight) {
			this.walk = event.getSpecialKey().getShift();
		}
		if (event.getType() == KeyKeyboard.f10) {
			if (event.getStatus() == KeyStatus.up) {
				if (this.fpsMode == false) {
					this.fpsMode = true;
					this.distanceFromCenter = 0;
				} else {
					this.fpsMode = false;
					this.distanceFromCenter = 2.5f;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onEventInput(final EventInput event, final Vector2f relativePosition) {
		// Log.info("" + event);
		// TODO Auto-generated method stub
		if (this.fpsMode == false) {
			if (event.getInputId() == 4) {
				if (event.getStatus() == KeyStatus.down) {
					this.distanceFromCenter -= 0.2;
				}
				if (this.distanceFromCenter < 0.0) {
					this.distanceFromCenter = 0.0f;
				}
				return true;
			} else if (event.getInputId() == 5) {
				if (event.getStatus() == KeyStatus.down) {
					this.distanceFromCenter += 0.2;
				}
				if (this.distanceFromCenter < 0.3) {
					this.distanceFromCenter = 0.3f;
				}
				return true;
			}
		} else {
			this.distanceFromCenter = 0;
		}
		// TODO check if grabbing is enable ...
		// in grabbing mouse only:
		if (Gale.getContext().isGrabPointerEvents() == false) {
			return false;
		}
		if (event.getStatus() == KeyStatus.move) {
			final Vector2f delta = event.getPosition().clone();
			//angleZ += delta.x;
			//this.camera.setYaw(this.camera.getYaw() + (float)Math.toRadians(delta.x));
			this.camera.setPitch(this.camera.getPitch() + (float) Math.toRadians(delta.y * this.player.getTurnSpeed()));
			if (this.camera.getPitch() > 0) {
				this.camera.setPitch(0);
			}
			if (this.camera.getPitch() < -Math.PI) {
				this.camera.setPitch((float) -Math.PI);
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
			if (this.playerPosition != null) {
				final float playerZAngle = this.playerPosition.getAngles().z;
				float tmpAngle = playerZAngle + (float) Math.toRadians(delta.x * this.player.getTurnSpeed());
				
				if (tmpAngle > Math.PI) {
					tmpAngle -= (float) Math.PI * 2.0f;
				}
				if (tmpAngle < -Math.PI) {
					tmpAngle += (float) Math.PI * 2.0f;
				}
				this.playerPosition.setAngles(new Vector3f(0, 0, tmpAngle));
				this.camera.setRoll(-playerZAngle);
				Log.info("Change camera: " + this.camera.getYaw() + " " + this.camera.getPitch());
			} else if (this.playerPhysics != null) {
				//this.playerPhysics.applyTorque(new Vector3f(0, 0, (float) Math.toRadians(delta.x * this.player.getTurnSpeed())));
			}
		}
		return false;
	}
	
	@Override
	public void periodicCall(final EventTime event) {
		if (this.playerPhysics != null) {
			//this.camera.setRoll(-this.playerPhysics.getAngles().z);
		}
		float speed = 0;
		float walkFactor = 1;
		if (this.walk == true) {
			walkFactor = this.player.getWalkFactor();
		}
		//distanceFromCenter = 6;
		if (this.moveUp != this.moveDown) {
			if (this.moveUp) {
				speed = this.player.getRunSpeed();
			} else {
				speed = -this.player.getRunSpeed();
			}
		}
		float distance = speed * walkFactor * event.getTimeDeltaCallSecond();
		float playerZAngle = 0;
		Transform3D playerTransform = null;
		if (this.playerPosition != null) {
			playerZAngle = this.playerPosition.getAngles().z;
			playerTransform = this.playerPosition.getTransform();
		} else if (this.playerPhysics != null) {
			playerZAngle = 0; // TODO ...
			playerTransform = this.playerPhysics.getTransform();
		}
		final float dx = -(float) (distance * Math.sin(playerZAngle));
		final float dy = (float) (distance * Math.cos(playerZAngle));
		speed = 0;
		if (this.moveRight != this.moveLeft) {
			if (this.moveRight) {
				speed = this.player.getStrafSpeed();
			} else {
				speed = -this.player.getStrafSpeed();
			}
		}
		distance = speed * walkFactor * event.getTimeDeltaCallSecond();
		final float dxStraf = (float) (distance * Math.sin((float) Math.PI * 0.5f + playerZAngle));
		final float dyStraf = -(float) (distance * Math.cos((float) Math.PI * 0.5f + playerZAngle));
		//Log.error("update position ..." + dx + "   " + dy);
		playerTransform.getPosition().x += dx + dxStraf;
		playerTransform.getPosition().y += dy + dyStraf;
		// here the camera is behind the player, we need to move the camera ...
		//Log.info(" pitch: " + Math.toDegrees(this.camera.getPitch()) + "  " + Math.toDegrees(playerZAngle));
		final float horinzontalDistance = (float) (this.distanceFromCenter * Math.sin(this.camera.getPitch()));
		final float verticalDistance = (float) (this.distanceFromCenter * Math.cos(this.camera.getPitch()));
		//Log.info("     distanceFromCenter " + distanceFromCenter);
		final float tmp = -horinzontalDistance;
		final float theta = (float) Math.PI + playerZAngle;// - (float)Math.PI*0.5f;
		final float offsetX = (float) (tmp * Math.sin(-theta));
		final float offsetY = (float) (tmp * Math.cos(-theta));
		//Log.info("     res" + offsetX + "  " + offsetY);
		this.camera.getPosition().x = playerTransform.getPosition().x + offsetX;
		this.camera.getPosition().y = playerTransform.getPosition().y + offsetY;
		this.camera.getPosition().z = playerTransform.getPosition().z + 1.6f + verticalDistance;
	}
	
}
