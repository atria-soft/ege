package org.atriasoft.ege;

import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.gale.event.EventInput;
import org.atriasoft.gale.event.EventTime;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.ege.camera.Camera;


public class ControlCameraSimple implements ControlInterface {
	private Camera camera;
	private float distanceFromCenter = 20;
	private float angleZ = 0;
	private float pitch = 0;
	private Vector2f lastMousePosition = null;
	private boolean moveUp = false;
	private boolean moveLeft = false;
	private boolean moveRight = false;
	private boolean moveDown = false;
	private boolean ctrlIsSet = false;

	public ControlCameraSimple(Camera camera) {
		this.camera = camera;
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
		if(event.getType() == KeyKeyboard.up) {
			moveUp = getState(event.getStatus(), moveUp);
		}
		if(event.getType() == KeyKeyboard.left) {
			moveLeft = getState(event.getStatus(), moveLeft);
		}
		if(event.getSpecialKey().getCtrl() == false
				&& event.getType() == KeyKeyboard.right) {
			moveRight = getState(event.getStatus(), moveRight);
		}
		if(event.getSpecialKey().getCtrl() == false
				&& event.getType() == KeyKeyboard.down) {
			moveDown = getState(event.getStatus(), moveDown);
		}
		ctrlIsSet = event.getSpecialKey().getCtrl();
		return false;
	}

	@Override
	public boolean onEventInput(EventInput event, Vector2f relativePosition) {
		Log.info("" + event);
		// TODO Auto-generated method stub
		if (event.getInputId() == 4) {
			if (event.getStatus() == KeyStatus.down) {
				distanceFromCenter -= 1;
			}
		} else if (event.getInputId() == 5) {
			if (event.getStatus() == KeyStatus.down) {
				distanceFromCenter += 1;
			}
		} else if (event.getInputId() == 2) {
			if (event.getStatus() == KeyStatus.down) {
				lastMousePosition = event.getPosition();
			} else if (event.getStatus() == KeyStatus.move) {
				Vector2f delta = event.getPosition().clone();
				delta.less(lastMousePosition);
				lastMousePosition = event.getPosition().clone();
				//angleZ += delta.x;
				//this.camera.setYaw(this.camera.getYaw() + (float)Math.toRadians(delta.x));
				this.camera.setPitch(this.camera.getPitch() - (float)Math.toRadians(delta.y));
				if (this.camera.getPitch()>0) {
					this.camera.setPitch(0);
				}
				if (this.camera.getPitch()<-Math.PI) {
					this.camera.setPitch((float)-Math.PI);
				}
				this.camera.setRoll(this.camera.getRoll() + (float)Math.toRadians(delta.x));
				Log.info("Change camera: " + this.camera.getYaw() + " " + this.camera.getPitch());
				if (this.camera.getRoll()>Math.PI) {
					this.camera.setRoll(this.camera.getRoll()-(float)Math.PI*2.0f);
				}
				if (this.camera.getRoll()<-Math.PI) {
					this.camera.setRoll(this.camera.getRoll()+(float)Math.PI*2.0f);
				}
			}
		}
		return false;
	}

	@Override
	public void periodicCall(EventTime event) {
		if (moveLeft != moveRight) {
			if (moveRight) {
				camera.getPosition().x += 0.1;
			} else {
				camera.getPosition().x -= 0.1;
			}
		}
		if (ctrlIsSet == false) {
			if (moveUp != moveDown) {
				if (moveUp) {
					camera.getPosition().y += 0.1;
				} else {
					camera.getPosition().y -= 0.1;
				}
			}
		} else {
			if (moveUp != moveDown) {
				if (moveUp) {
					camera.getPosition().z += 0.1;
				} else {
					camera.getPosition().z -= 0.1;
				}
			}
		}
	}

}
