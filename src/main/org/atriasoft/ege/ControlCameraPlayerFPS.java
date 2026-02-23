package org.atriasoft.ege;

import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentPlayer;
import org.atriasoft.ege.components.ComponentPositionPlayer;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.event.EventTime;
import org.atriasoft.gale.Gale;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlCameraPlayerFPS implements ControlInterface {
	static final Logger LOGGER = LoggerFactory.getLogger(ControlCameraPlayerFPS.class);
	private final Camera camera;
	private final Entity playerEntity;
	private final ComponentPositionPlayer playerPosition;
	private final ComponentPlayer player;
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
		if (event.type() == KeyKeyboard.UP
				|| (event.type() == KeyKeyboard.CHARACTER && (event.getChar() == 'z' || event.getChar() == 'Z'))) {
			this.moveUp = getState(event.status(), this.moveUp);
		}
		if (event.type() == KeyKeyboard.LEFT
				|| (event.type() == KeyKeyboard.CHARACTER && (event.getChar() == 'q' || event.getChar() == 'Q'))) {
			this.moveLeft = getState(event.status(), this.moveLeft);
		}
		if (event.type() == KeyKeyboard.RIGHT
				|| (event.type() == KeyKeyboard.CHARACTER && (event.getChar() == 'd' || event.getChar() == 'D'))) {
			this.moveRight = getState(event.status(), this.moveRight);
		}
		if (event.type() == KeyKeyboard.DOWN
				|| (event.type() == KeyKeyboard.CHARACTER && (event.getChar() == 's' || event.getChar() == 'S'))) {
			this.moveDown = getState(event.status(), this.moveDown);
		}
		return false;
	}

	@Override
	public boolean onEventInput(final EventInput event, final Vector2f relativePosition) {
		// LOGGER.info("{}", event);
		// in grabbing mouse only:
		if (!Gale.getContext().isGrabPointerEvents()) {
			return false;
		}
		if (event.status() == KeyStatus.move) {
			final Vector2f delta = event.pos();
			this.camera
					.setPitch(this.camera.getPitch() + (float) Math.toRadians(delta.y() * this.player.getTurnSpeed()));
			if (this.camera.getPitch() < 0) {
				this.camera.setPitch(0);
			}
			if (this.camera.getPitch() > Math.PI) {
				this.camera.setPitch((float) Math.PI);
			}
			this.camera.setYaw(this.camera.getYaw() + (float) Math.toRadians(delta.x() * this.player.getTurnSpeed()));
			LOGGER.info("Change camera: {} {}", this.camera.getYaw(), this.camera.getPitch());
			if (this.camera.getYaw() > Math.PI) {
				this.camera.setYaw(this.camera.getYaw() - (float) Math.PI * 2.0f);
			}
			if (this.camera.getYaw() < -Math.PI) {
				this.camera.setYaw(this.camera.getYaw() + (float) Math.PI * 2.0f);
			}
			this.playerPosition.setAngles(new Vector3f(0, this.camera.getYaw(), 0));
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
		// Forward = (sin(yaw), 0, -cos(yaw)) with camera yaw = playerAngles.y()
		final float dx = (float) (distance * Math.sin(this.playerPosition.getAngles().y()));
		final float dz = -(float) (distance * Math.cos(this.playerPosition.getAngles().y()));
		speed = 0;
		if (this.moveRight != this.moveLeft) {
			if (this.moveRight) {
				speed = this.player.getStrafSpeed();
			} else {
				speed = -this.player.getStrafSpeed();
			}
		}
		distance = speed * event.getTimeDeltaCallSecond();
		// Right = (cos(yaw), 0, sin(yaw))
		final float dxStraf = (float) (distance * Math.cos(this.playerPosition.getAngles().y()));
		final float dzStraf = (float) (distance * Math.sin(this.playerPosition.getAngles().y()));
		//LOGGER.error("update position ... {}   {}", dx, dy);
		this.playerPosition.setTransform(this.playerPosition.getTransform()
				.withPosition(this.playerPosition.getTransform().getPosition().add(dx + dxStraf, 0, dz + dzStraf)));
		this.camera.setPosition(this.playerPosition.getTransform().getPosition());
	}

}
