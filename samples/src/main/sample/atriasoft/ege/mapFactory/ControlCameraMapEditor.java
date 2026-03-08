package sample.atriasoft.ege.mapFactory;

import org.atriasoft.ege.ControlInterface;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.etk.math.Matrix4f;
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
		this.targetPoint = new Vector3f(0, 0, 0);
		this.distance = 20.0f;
		this.azimuth = 0.0f;
		this.elevation = (float) Math.PI * 0.25f;
		updateCamera();
	}

	private void updateCamera() {
		final float cosElev = (float) Math.cos(this.elevation);
		final float sinElev = (float) Math.sin(this.elevation);
		final float cosAz = (float) Math.cos(this.azimuth);
		final float sinAz = (float) Math.sin(this.azimuth);

		final Vector3f offset = new Vector3f(
				this.distance * cosElev * sinAz,
				this.distance * sinElev,
				this.distance * cosElev * cosAz);

		final Vector3f eye = this.targetPoint.add(offset);
		this.camera.setPosition(eye);
		this.camera.setViewMatrix(buildOrbitViewMatrix(eye, this.targetPoint, this.azimuth));
	}

	/**
	 * Build a view matrix for an orbit camera around Y axis.
	 * Uses world-up (0,1,0) for general cases. When the camera is near the
	 * pole (looking straight down/up), the cross product with world-up degenerates,
	 * so we derive the right vector from the azimuth angle instead.
	 */
	private static Matrix4f buildOrbitViewMatrix(final Vector3f eye, final Vector3f target, final float azimuth) {
		// Forward = normalize(eye - target) — OpenGL camera looks in -Z
		float fx = eye.x() - target.x();
		float fy = eye.y() - target.y();
		float fz = eye.z() - target.z();
		final float fLen = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
		if (fLen > 0.0001f) {
			fx /= fLen;
			fy /= fLen;
			fz /= fLen;
		}

		// Right = normalize(worldUp x forward) with worldUp = (0,1,0)
		float rx = fz;  // 1*fz - 0*fy
		float ry = 0;   // 0*fx - 0*fz
		float rz = -fx; // 0*fy - 1*fx
		final float rLen = (float) Math.sqrt(rx * rx + rz * rz);
		if (rLen > 0.0001f) {
			// Normal case: forward is not vertical
			rx /= rLen;
			rz /= rLen;
		} else {
			// Degenerate case: forward is vertical (top-down view).
			// Derive right from azimuth to keep orientation consistent.
			rx = (float) Math.cos(azimuth);
			ry = 0.0f;
			rz = -(float) Math.sin(azimuth);
		}

		// Recompute up = forward x right (ensures orthonormality)
		final float ux = fy * rz - fz * ry;
		final float uy = fz * rx - fx * rz;
		final float uz = fx * ry - fy * rx;

		// View matrix = rotation * translation
		final float tx = -(rx * eye.x() + ry * eye.y() + rz * eye.z());
		final float ty = -(ux * eye.x() + uy * eye.y() + uz * eye.z());
		final float tz = -(fx * eye.x() + fy * eye.y() + fz * eye.z());

		return new Matrix4f(
				rx, ry, rz, tx,
				ux, uy, uz, ty,
				fx, fy, fz, tz,
				0.0f, 0.0f, 0.0f, 1.0f);
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
						this.targetElevation = (float) Math.PI * 0.5f;
						return true;
					case '5': // 45-degree view
						this.targetElevation = (float) Math.PI * 0.25f;
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
					// Pan: move target point on XZ ground plane
					final float panSpeed = this.distance * 0.002f;
					final float cosAz = (float) Math.cos(this.azimuth);
					final float sinAz = (float) Math.sin(this.azimuth);

					// Camera right projected on XZ ground plane
					final Vector3f right = new Vector3f(cosAz, 0, -sinAz);
					// Camera forward projected on XZ ground plane
					final Vector3f forward = new Vector3f(-sinAz, 0, -cosAz);

					this.targetPoint = this.targetPoint
							.add(right.multiply(-delta.x() * panSpeed))
							.add(forward.multiply(-delta.y() * panSpeed));
				} else {
					// Orbit: rotate around target
					this.azimuth -= (float) Math.toRadians(delta.x());
					this.elevation += (float) Math.toRadians(delta.y());
					this.elevation = Math.max(0.01f,
							Math.min((float) Math.PI * 0.5f, this.elevation));
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
