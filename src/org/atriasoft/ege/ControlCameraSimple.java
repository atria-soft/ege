package org.atriasoft.ege;

import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.event.EventTime;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeyStatus;

public class ControlCameraSimple implements ControlInterface {
	private final Camera camera;
	private float distanceFromCenter = 20;
	private float angleZ = 0;
	private float pitch = 0;
	private Vector2f lastMousePosition = null;
	private boolean moveUp = false;
	private boolean moveLeft = false;
	private boolean moveRight = false;
	private boolean moveDown = false;
	private boolean ctrlIsSet = false;
	
	public ControlCameraSimple(final Camera camera) {
		this.camera = camera;
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
		if (event.type() == KeyKeyboard.up) {
			this.moveUp = getState(event.status(), this.moveUp);
		}
		if (event.type() == KeyKeyboard.left) {
			this.moveLeft = getState(event.status(), this.moveLeft);
		}
		if (!event.specialKey().getCtrl() && event.type() == KeyKeyboard.right) {
			this.moveRight = getState(event.status(), this.moveRight);
		}
		if (!event.specialKey().getCtrl() && event.type() == KeyKeyboard.down) {
			this.moveDown = getState(event.status(), this.moveDown);
		}
		this.ctrlIsSet = event.specialKey().getCtrl();
		return false;
	}
	
	@Override
	public boolean onEventInput(final EventInput event, final Vector2f relativePosition) {
		Log.info("" + event);
		// TODO Auto-generated method stub
		if (event.inputId() == 4) {
			if (event.status() == KeyStatus.down) {
				this.distanceFromCenter -= 1;
			}
		} else if (event.inputId() == 5) {
			if (event.status() == KeyStatus.down) {
				this.distanceFromCenter += 1;
			}
		} else if (event.inputId() == 2) {
			if (event.status() == KeyStatus.down) {
				this.lastMousePosition = event.pos();
			} else if (event.status() == KeyStatus.move) {
				Vector2f delta = event.pos();
				delta = delta.less(this.lastMousePosition);
				this.lastMousePosition = event.pos();
				//angleZ += delta.x;
				//this.camera.setYaw(this.camera.getYaw() + (float)Math.toRadians(delta.x));
				this.camera.setPitch(this.camera.getPitch() - (float) Math.toRadians(delta.y()));
				if (this.camera.getPitch() > 0) {
					this.camera.setPitch(0);
				}
				if (this.camera.getPitch() < -Math.PI) {
					this.camera.setPitch((float) -Math.PI);
				}
				this.camera.setRoll(this.camera.getRoll() + (float) Math.toRadians(delta.x()));
				Log.info("Change camera: " + this.camera.getYaw() + " " + this.camera.getPitch());
				if (this.camera.getRoll() > Math.PI) {
					this.camera.setRoll(this.camera.getRoll() - (float) Math.PI * 2.0f);
				}
				if (this.camera.getRoll() < -Math.PI) {
					this.camera.setRoll(this.camera.getRoll() + (float) Math.PI * 2.0f);
				}
			}
		}
		return false;
	}
	
	@Override
	public void periodicCall(final EventTime event) {
		if (this.moveLeft != this.moveRight) {
			if (this.moveRight) {
				this.camera.setPosition(this.camera.getPosition().add(new Vector3f(0.1f, 0, 0)));
			} else {
				this.camera.setPosition(this.camera.getPosition().add(new Vector3f(-0.1f, 0, 0)));
			}
		}
		if (!this.ctrlIsSet) {
			if (this.moveUp != this.moveDown) {
				if (this.moveUp) {
					this.camera.setPosition(this.camera.getPosition().add(new Vector3f(0, 0.1f, 0)));
				} else {
					this.camera.setPosition(this.camera.getPosition().add(new Vector3f(0, -0.1f, 0)));
				}
			}
		} else if (this.moveUp != this.moveDown) {
			if (this.moveUp) {
				
				this.camera.setPosition(this.camera.getPosition().add(new Vector3f(0, 0, 0.1f)));
			} else {
				this.camera.setPosition(this.camera.getPosition().add(new Vector3f(0, 0, -0.1f)));
			}
		}
	}
	
}
