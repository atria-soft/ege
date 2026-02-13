package sample.atriasoft.ege.mapFactory;

import org.atriasoft.ege.ControlInterface;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.event.EventTime;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orbit-style camera controller for the map editor.
 * Rotates around a target point (focus), with zoom, pan, and numpad view shortcuts.
 */
public class ControlCameraMapEditor implements ControlInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ControlCameraMapEditor.class);

	private final Camera camera;

	// Orbit parameters
	private Vector3f targetPoint;
	private float distance;
	private float azimuth;
	private float elevation;

	// Mouse tracking
	private Vector2f lastMousePosition = null;
	private boolean shiftHeld = false;

	// Smooth animation for elevation transitions (numpad 8/5)
	private Float targetElevation = null;
	private static final float ANIMATION_SPEED = 8.0f;

	public ControlCameraMapEditor(final Camera camera) {
		this.camera = camera;
		this.targetPoint = new Vector3f(32, 32, 0);
		this.distance = 50.0f;
		this.azimuth = 0.0f;
		this.elevation = (float) Math.PI * -0.25f;
		updateCamera();
	}

	private void updateCamera() {
		final float cosElev = (float) Math.cos(this.elevation);
		final float sinElev = (float) Math.sin(this.elevation);
		final float cosAz = (float) Math.cos(this.azimuth);
		final float sinAz = (float) Math.sin(this.azimuth);

		final Vector3f offset = new Vector3f(
				this.distance * cosElev * sinAz,
				-this.distance * cosElev * cosAz,
				-this.distance * sinElev);

		this.camera.setPosition(this.targetPoint.add(offset));
		this.camera.setPitch(this.elevation);
		this.camera.setRoll(this.azimuth);
	}

	private void normalizeAzimuth() {
		while (this.azimuth > Math.PI) {
			this.azimuth -= (float) Math.PI * 2.0f;
		}
		while (this.azimuth < -Math.PI) {
			this.azimuth += (float) Math.PI * 2.0f;
		}
	}

	@Override
	public boolean onEventEntry(final EventEntry event) {
		this.shiftHeld = event.specialKey().getShift();

		if (event.type() == KeyKeyboard.CHARACTER && event.status() == KeyStatus.down) {
			final Character ch = event.getChar();
			if (ch != null) {
				switch (ch) {
					case '8': // Top-down view
						this.targetElevation = (float) -Math.PI * 0.499f;
						return true;
					case '5': // 45-degree view
						this.targetElevation = (float) -Math.PI * 0.25f;
						return true;
					case '6': // Rotate 45 degrees right
						this.azimuth += (float) Math.PI * 0.25f;
						normalizeAzimuth();
						updateCamera();
						return true;
					case '4': // Rotate 45 degrees left
						this.azimuth -= (float) Math.PI * 0.25f;
						normalizeAzimuth();
						updateCamera();
						return true;
					default:
						break;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEventInput(final EventInput event, final Vector2f relativePosition) {
		// Scroll wheel: zoom in/out (only without modifiers to avoid conflicts with tools)
		if ((event.inputId() == 4 || event.inputId() == 5) && event.status() == KeyStatus.down) {
			if (event.specialKey() != null && (event.specialKey().getCtrl()
					|| event.specialKey().getShift() || event.specialKey().getAlt())) {
				return false;
			}
			if (event.inputId() == 4) {
				this.distance = Math.max(1.0f, this.distance * 0.9f);
			} else {
				this.distance = Math.min(200.0f, this.distance * 1.1f);
			}
			updateCamera();
		}

		// Middle mouse button (inputId == 2)
		if (event.inputId() == 2) {
			if (event.status() == KeyStatus.down) {
				this.lastMousePosition = event.pos();
			} else if (event.status() == KeyStatus.move && this.lastMousePosition != null) {
				final Vector2f delta = event.pos().less(this.lastMousePosition);
				this.lastMousePosition = event.pos();

				if (this.shiftHeld) {
					// Pan: move target point in screen-aligned plane
					final float panSpeed = this.distance * 0.002f;
					final float cosAz = (float) Math.cos(this.azimuth);
					final float sinAz = (float) Math.sin(this.azimuth);

					// Screen right direction projected on XY plane
					final Vector3f right = new Vector3f(cosAz, sinAz, 0);
					// Screen up direction
					final float cosElev = (float) Math.cos(this.elevation);
					final float sinElev = (float) Math.sin(this.elevation);
					final Vector3f up = new Vector3f(
							-sinElev * sinAz,
							sinElev * cosAz,
							-cosElev);

					this.targetPoint = this.targetPoint
							.add(right.multiply(-delta.x() * panSpeed))
							.add(up.multiply(delta.y() * panSpeed));
				} else {
					// Orbit: rotate around target
					this.azimuth += (float) Math.toRadians(delta.x());
					this.elevation -= (float) Math.toRadians(delta.y());
					this.elevation = Math.max((float) -Math.PI * 0.499f,
							Math.min(-0.01f, this.elevation));
					normalizeAzimuth();
				}
				updateCamera();
			} else if (event.status() == KeyStatus.up) {
				this.lastMousePosition = null;
			}
		}
		return false;
	}

	@Override
	public void periodicCall(final EventTime event) {
		if (this.targetElevation != null) {
			final float dt = event.getTimeDeltaCallSecond();
			final float diff = this.targetElevation - this.elevation;
			if (Math.abs(diff) < 0.01f) {
				this.elevation = this.targetElevation;
				this.targetElevation = null;
			} else {
				this.elevation += Math.signum(diff) * Math.min(Math.abs(diff), ANIMATION_SPEED * dt);
			}
			updateCamera();
		}
	}
}
