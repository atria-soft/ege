package org.atriasoft.ege;

import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentPlayer;
import org.atriasoft.ege.components.ComponentPositionPlayer;
import org.atriasoft.etk.math.Transform3D;
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

public class ControlCameraPlayer implements ControlInterface {
	static final Logger LOGGER = LoggerFactory.getLogger(ControlCameraPlayer.class);
	private final Camera camera;
	private float distanceFromCenter = 2.5f;
	private boolean fpsMode = false;
	private final Entity playerEntity;
	private ComponentPositionPlayer playerPosition;
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
		if (event.type() == KeyKeyboard.SHIFT_LEFT || event.type() == KeyKeyboard.SHIFT_RIGHT) {
			this.walk = event.specialKey().getShift();
		}
		if (event.type() == KeyKeyboard.F10) {
			if (event.status() == KeyStatus.up) {
				if (!this.fpsMode) {
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
		// LOGGER.info("{}", event);
		// TODO Auto-generated method stub
		if (!this.fpsMode) {
			if (event.inputId() == 4) {
				if (event.status() == KeyStatus.down) {
					this.distanceFromCenter -= 0.2;
				}
				if (this.distanceFromCenter < 0.0) {
					this.distanceFromCenter = 0.0f;
				}
				return true;
			} else if (event.inputId() == 5) {
				if (event.status() == KeyStatus.down) {
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
			if (this.playerPosition != null) {
				final float playerYAngle = this.playerPosition.getAngles().y();
				float tmpAngle = playerYAngle - (float) Math.toRadians(delta.x() * this.player.getTurnSpeed());

				if (tmpAngle > Math.PI) {
					tmpAngle -= (float) Math.PI * 2.0f;
				}
				if (tmpAngle < -Math.PI) {
					tmpAngle += (float) Math.PI * 2.0f;
				}
				this.playerPosition.setAngles(new Vector3f(0, tmpAngle, 0));
				this.camera.setYaw(-tmpAngle);
				LOGGER.info("Change camera: {} {}", this.camera.getYaw(), this.camera.getPitch());
			}
		}
		return false;
	}
	
	@Override
	public void periodicCall(final EventTime event) {
		float speed = 0;
		float walkFactor = 1;
		if (this.walk) {
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
		float playerYAngle = 0;
		Transform3D playerTransform = null;
		if (this.playerPosition != null) {
			playerYAngle = this.playerPosition.getAngles().y();
			playerTransform = this.playerPosition.getTransform();
		}
		// Camera yaw = -playerYAngle, so world forward = (-sin(playerYAngle), 0, -cos(playerYAngle))
		final float dx = -(float) (distance * Math.sin(playerYAngle));
		final float dz = -(float) (distance * Math.cos(playerYAngle));
		speed = 0;
		if (this.moveRight != this.moveLeft) {
			if (this.moveRight) {
				speed = this.player.getStrafSpeed();
			} else {
				speed = -this.player.getStrafSpeed();
			}
		}
		distance = speed * walkFactor * event.getTimeDeltaCallSecond();
		// Camera right = (cos(playerYAngle), 0, -sin(playerYAngle))
		final float dxStraf = (float) (distance * Math.cos(playerYAngle));
		final float dzStraf = -(float) (distance * Math.sin(playerYAngle));
		//LOGGER.error("update position ... {}  {}", dx, dy);
		Vector3f tmpPos = playerTransform.getPosition();
		tmpPos = tmpPos.add(new Vector3f(dx + dxStraf, 0, dz + dzStraf));
		playerTransform = playerTransform.withPosition(tmpPos);
		if (this.playerPosition != null) {
			this.playerPosition.setTransform(playerTransform);
		}
		// here the camera is behind the player, we need to move the camera ...
		//LOGGER.info(" pitch: {}  {}", Math.toDegrees(this.camera.getPitch()), Math.toDegrees(playerYAngle));
		// pitch > 0 = looking down. horizontalDistance = how far behind, verticalDistance = how far above
		final float horizontalDistance = (float) (this.distanceFromCenter * Math.sin(this.camera.getPitch()));
		final float verticalDistance = (float) (this.distanceFromCenter * Math.cos(this.camera.getPitch()));
		// Camera behind player: offset in +Z when playerYAngle=0 (player faces -Z)
		final float offsetX = -(float) (horizontalDistance * Math.sin(playerYAngle));
		final float offsetZ = (float) (horizontalDistance * Math.cos(playerYAngle));
		//LOGGER.info("     res=({},{})", offsetX, offsetY);
		this.camera.setPosition(
				new Vector3f(playerTransform.getPosition().x() + offsetX,
						playerTransform.getPosition().y() + 1.6f + verticalDistance,
						playerTransform.getPosition().z() + offsetZ));
	}
	
}
