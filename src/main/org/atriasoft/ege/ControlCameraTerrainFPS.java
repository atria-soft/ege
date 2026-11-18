package org.atriasoft.ege;

import org.atriasoft.ege.camera.Camera;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.event.EventTime;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeyStatus;

/**
 * First-person walk-through camera over a terrain (Y-up convention), driven by no entity:
 * WASD or arrows move on the XZ ground plane, a right-click drag looks around, and the eye
 * stays at a fixed height above the terrain given by a {@link TerrainHeight}. Meant for
 * previewing a map in an editor; the game's player camera is {@link ControlCameraPlayerFPS}.
 */
public class ControlCameraTerrainFPS implements ControlInterface {
	/** Height of the terrain at a world position on the XZ plane. */
	@FunctionalInterface
	public interface TerrainHeight {
		float heightAt(float worldX, float worldZ);
	}

	private static final float MOUSE_SENSITIVITY = 0.005f;
	private static final int LOOK_BUTTON = 3;

	private final Camera camera;
	private final TerrainHeight terrain;
	private final float worldWidth;
	private final float worldDepth;

	// Position on XZ ground plane and look angles
	private float posX;
	private float posZ;
	private float yaw = 0.0f; // horizontal look (radians, around Y axis)
	private float pitch = 0.0f; // vertical look (radians, around X axis)

	private float eyeHeight = 1.7f;
	private float moveSpeed = 10.0f;

	private boolean moveForward = false;
	private boolean moveBackward = false;
	private boolean moveLeft = false;
	private boolean moveRight = false;

	private Vector2f lastMousePosition = null;

	/**
	 * @param camera the camera to drive
	 * @param worldWidth size of the terrain on X, in world units (the walk is clamped to it)
	 * @param worldDepth size of the terrain on Z, in world units
	 * @param terrain height of the terrain under the eye
	 */
	public ControlCameraTerrainFPS(final Camera camera, final float worldWidth, final float worldDepth,
			final TerrainHeight terrain) {
		this.camera = camera;
		this.terrain = terrain;
		this.worldWidth = worldWidth;
		this.worldDepth = worldDepth;
		// Start at map center
		this.posX = worldWidth * 0.5f;
		this.posZ = worldDepth * 0.5f;
		updateCamera();
	}

	public void setEyeHeight(final float eyeHeight) {
		this.eyeHeight = eyeHeight;
		updateCamera();
	}

	public void setMoveSpeed(final float moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	/** Put the eye back at the right height after the terrain changed under it. */
	public void refreshHeight() {
		updateCamera();
	}

	private void updateCamera() {
		final float terrainH = this.terrain.heightAt(this.posX, this.posZ);
		this.camera.setPosition(new Vector3f(this.posX, terrainH + this.eyeHeight, this.posZ));
		// In Y-up, pitch=0 is horizontal, positive looks down
		this.camera.setPitch(this.pitch);
		this.camera.setYaw(this.yaw);
	}

	@Override
	public boolean onEventEntry(final EventEntry event) {
		if (event.status() == KeyStatus.down || event.status() == KeyStatus.up) {
			final boolean pressed = event.status() == KeyStatus.down;
			if (event.type() == KeyKeyboard.CHARACTER) {
				final Character ch = event.getChar();
				if (ch != null) {
					switch (ch) {
						case 'w', 'W':
							this.moveForward = pressed;
							return true;
						case 's', 'S':
							this.moveBackward = pressed;
							return true;
						case 'a', 'A':
							this.moveLeft = pressed;
							return true;
						case 'd', 'D':
							this.moveRight = pressed;
							return true;
						default:
							break;
					}
				}
			}
			// Also support arrow keys
			if (event.type() == KeyKeyboard.UP) {
				this.moveForward = pressed;
				return true;
			}
			if (event.type() == KeyKeyboard.DOWN) {
				this.moveBackward = pressed;
				return true;
			}
			if (event.type() == KeyKeyboard.LEFT) {
				this.moveLeft = pressed;
				return true;
			}
			if (event.type() == KeyKeyboard.RIGHT) {
				this.moveRight = pressed;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onEventInput(final EventInput event, final Vector2f relativePosition) {
		// Right-click drag for mouse look
		if (event.inputId() == LOOK_BUTTON) {
			if (event.status() == KeyStatus.down) {
				this.lastMousePosition = event.pos();
			} else if (event.status() == KeyStatus.move && this.lastMousePosition != null) {
				final Vector2f delta = event.pos().less(this.lastMousePosition);
				this.lastMousePosition = event.pos();
				this.yaw += delta.x() * MOUSE_SENSITIVITY;
				this.pitch += delta.y() * MOUSE_SENSITIVITY;
				// Clamp pitch to avoid flipping
				this.pitch = Math.max((float) -Math.PI * 0.45f, Math.min((float) Math.PI * 0.45f, this.pitch));
				updateCamera();
			} else if (event.status() == KeyStatus.up) {
				this.lastMousePosition = null;
			}
			return true;
		}
		return false;
	}

	@Override
	public void periodicCall(final EventTime event) {
		final float speed = this.moveSpeed * event.getTimeDeltaCallSecond();

		// Forward direction projected on XZ ground plane
		final float forwardX = (float) Math.sin(this.yaw);
		final float forwardZ = -(float) Math.cos(this.yaw);
		// Right direction
		final float rightX = -forwardZ;
		final float rightZ = forwardX;

		boolean moved = false;
		if (this.moveForward) {
			this.posX += forwardX * speed;
			this.posZ += forwardZ * speed;
			moved = true;
		}
		if (this.moveBackward) {
			this.posX -= forwardX * speed;
			this.posZ -= forwardZ * speed;
			moved = true;
		}
		if (this.moveLeft) {
			this.posX -= rightX * speed;
			this.posZ -= rightZ * speed;
			moved = true;
		}
		if (this.moveRight) {
			this.posX += rightX * speed;
			this.posZ += rightZ * speed;
			moved = true;
		}

		if (moved) {
			// Clamp to map bounds
			this.posX = Math.max(0.0f, Math.min(this.worldWidth, this.posX));
			this.posZ = Math.max(0.0f, Math.min(this.worldDepth, this.posZ));
			updateCamera();
		}
	}
}
