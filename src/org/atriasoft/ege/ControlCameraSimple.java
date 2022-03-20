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
		if (event.type() == KeyKeyboard.UP) {
			this.moveUp = getState(event.status(), this.moveUp);
		}
		if (event.type() == KeyKeyboard.LEFT) {
			this.moveLeft = getState(event.status(), this.moveLeft);
		}
		if (event.type() == KeyKeyboard.RIGHT) {
			this.moveRight = getState(event.status(), this.moveRight);
		}
		if (event.type() == KeyKeyboard.DOWN) {
			this.moveDown = getState(event.status(), this.moveDown);
		}
		this.ctrlIsSet = event.specialKey().getCtrl();
		return false;
	}
	
	@Override
	public boolean onEventInput(final EventInput event, final Vector2f relativePosition) {
		// TODO Auto-generated method stub
		if (event.inputId() == 4) {
			Vector3f delta = this.camera.getConvertionMatrix().transpose().multiply(new Vector3f(0,0,-1));
			if (event.status() == KeyStatus.down) {
				this.camera.setPosition(this.camera.getPosition().add(delta.multiply(1.0f)));
			}
		}
		if (event.inputId() == 5) {
			Vector3f delta = this.camera.getConvertionMatrix().transpose().multiply(new Vector3f(0,0,-1));
			if (event.status() == KeyStatus.down) {
				this.camera.setPosition(this.camera.getPosition().add(delta.multiply(-1.0f)));
			}
		}
		if (event.inputId() == 2) {
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
		float roll = this.camera.getRoll();
		if (this.moveLeft != this.moveRight) {
			Vector3f orientation = new Vector3f(-(float)Math.cos(roll), (float)Math.sin(roll), 0);
			if (this.moveRight) {
				this.camera.setPosition(this.camera.getPosition().add(orientation.multiply(-0.1f)));
			} else {
				this.camera.setPosition(this.camera.getPosition().add(orientation.multiply(0.1f)));
			}
		}
		if (!this.ctrlIsSet) {
			if (this.moveUp != this.moveDown) {
				Vector3f orientation = new Vector3f((float)Math.sin(roll), (float)Math.cos(roll), 0);
				if (this.moveUp) {
					this.camera.setPosition(this.camera.getPosition().add(orientation.multiply(0.1f)));
				} else {
					this.camera.setPosition(this.camera.getPosition().add(orientation.multiply(-0.1f)));
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
