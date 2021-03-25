package org.atriasoft.ege;

import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentPlayer;
import org.atriasoft.ege.components.ComponentPositionPlayer;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.event.EventTime;
import org.atriasoft.gale.Gale;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeyStatus;

public class ControlCameraPlayerFPS implements ControlInterface {
	private Camera camera;
	private Entity playerEntity;
	private ComponentPositionPlayer playerPosition;
	private ComponentPlayer player;
	private boolean moveUp = false;
	private boolean moveDown = false;
	private boolean moveLeft = false;
	private boolean moveRight = false;
	
	public ControlCameraPlayerFPS(final Camera camera, final Entity playerEntity) {
		this.camera = camera;
		this.playerEntity = playerEntity;
		this.playerPosition = (ComponentPositionPlayer) this.playerEntity.getComponent("position");
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
		if (event.type() == KeyKeyboard.up || (event.type() == KeyKeyboard.character && (event.getChar() == 'z' || event.getChar() == 'Z'))) {
			this.moveUp = getState(event.status(), this.moveUp);
		}
		if (event.type() == KeyKeyboard.left || (event.type() == KeyKeyboard.character && (event.getChar() == 'q' || event.getChar() == 'Q'))) {
			this.moveLeft = getState(event.status(), this.moveLeft);
		}
		if (event.type() == KeyKeyboard.right || (event.type() == KeyKeyboard.character && (event.getChar() == 'd' || event.getChar() == 'D'))) {
			this.moveRight = getState(event.status(), this.moveRight);
		}
		if (event.type() == KeyKeyboard.down || (event.type() == KeyKeyboard.character && (event.getChar() == 's' || event.getChar() == 'S'))) {
			this.moveDown = getState(event.status(), this.moveDown);
		}
		return false;
	}
	
	@Override
	public boolean onEventInput(final EventInput event, final Vector2f relativePosition) {
		// Log.info("" + event);
		// in grabbing mouse only:
		if (!Gale.getContext().isGrabPointerEvents()) {
			return false;
		}
		if (event.status() == KeyStatus.move) {
			Vector2f delta = event.pos();
			//angleZ += delta.x;
			//this.camera.setYaw(this.camera.getYaw() + (float)Math.toRadians(delta.x));
			this.camera.setPitch(this.camera.getPitch() + (float) Math.toRadians(delta.y() * this.player.getTurnSpeed()));
			if (this.camera.getPitch() > 0) {
				this.camera.setPitch(0);
			}
			if (this.camera.getPitch() < -Math.PI) {
				this.camera.setPitch((float) -Math.PI);
			}
			this.camera.setRoll(this.camera.getRoll() - (float) Math.toRadians(delta.x() * this.player.getTurnSpeed()));
			Log.info("Change camera: " + this.camera.getYaw() + " " + this.camera.getPitch());
			if (this.camera.getRoll() > Math.PI) {
				this.camera.setRoll(this.camera.getRoll() - (float) Math.PI * 2.0f);
			}
			if (this.camera.getRoll() < -Math.PI) {
				this.camera.setRoll(this.camera.getRoll() + (float) Math.PI * 2.0f);
			}
			this.playerPosition.setAngles(new Vector3f(0, 0, this.camera.getRoll()));
		}
		return false;
	}
	
	@Override
	public void periodicCall(final EventTime event) {
		float speed = 0;
		if (this.moveUp != this.moveDown) {
			if (this.moveUp) {
				speed = this.player.getRunSpeed();
			} else {
				speed = -this.player.getRunSpeed();
			}
		}
		float distance = speed * event.getTimeDeltaCallSecond();
		float dx = (float) (distance * Math.sin(this.playerPosition.getAngles().z()));
		float dy = (float) (distance * Math.cos(this.playerPosition.getAngles().z()));
		speed = 0;
		if (this.moveRight != this.moveLeft) {
			if (this.moveRight) {
				speed = this.player.getStrafSpeed();
			} else {
				speed = -this.player.getStrafSpeed();
			}
		}
		distance = speed * event.getTimeDeltaCallSecond();
		float dxStraf = (float) (distance * Math.sin((float) Math.PI * 0.5f + this.playerPosition.getAngles().z()));
		float dyStraf = (float) (distance * Math.cos((float) Math.PI * 0.5f + this.playerPosition.getAngles().z()));
		//Log.error("update position ..." + dx + "   " + dy);
		this.playerPosition.setTransform(this.playerPosition.getTransform().withPosition(this.playerPosition.getTransform().getPosition().add(dx + dxStraf, dy + dyStraf, 0)));
		this.camera.setPosition(this.playerPosition.getTransform().getPosition());
	}
	
}
