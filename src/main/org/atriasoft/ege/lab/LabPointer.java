package org.atriasoft.ege.lab;

import org.atriasoft.etk.math.Vector3f;

/**
 * What a lab does with the mouse over its 3D view, before the camera ({@link LabView#setPointer}): the pointer
 * moving with no button, a button pressed, the pointer dragged with it, the button released. A press the lab takes
 * makes the drag and the release that follow its own; a press it leaves goes to the camera (turn, pan), and so does
 * the wheel, always. Each event carries the ray from the eye through the pointer, and so the point of the ground (or
 * of any level) under it ({@link Event#ground()}, {@link Event#at}). On the GUI thread; whatever it throws is reported
 * in the info panel.
 */
@FunctionalInterface
public interface LabPointer {

	/** What the pointer did. */
	enum Action {
		/** It moved with no button held. */
		HOVER,
		/** It left the view. */
		LEAVE,
		/** A button went down. */
		PRESS,
		/** It moved with the button pressed (a press the lab took). */
		DRAG,
		/** The button went up (a press the lab took). */
		RELEASE
	}

	/**
	 * An event of the pointer over the view.
	 *
	 * @param action what it did
	 * @param button the button: 1 left, 2 middle, 3 right; 0 for a hover or a leave
	 * @param x      pixels from the left of the view
	 * @param y      pixels from the bottom of the view
	 * @param eye    where the eye is (metres, X east, Y up, Z south)
	 * @param ray    the unit direction from the eye through the pointer
	 * @param shift  whether Shift is held
	 * @param ctrl   whether Control is held
	 */
	record Event(Action action, int button, float x, float y, Vector3f eye, Vector3f ray, boolean shift, boolean ctrl) {

		/** Where the ray meets the level {@code height} (metres), {@code null} when it never does (looking up). */
		public Vector3f at(final float height) {
			if (Math.abs(this.ray.y()) < 1.0e-6f) {
				return null;
			}
			final float t = (height - this.eye.y()) / this.ray.y();
			return t > 0.0f ? this.eye.add(this.ray.multiply(t)) : null;
		}

		/** The point of the ground under the pointer, {@code null} when it points above the horizon. */
		public Vector3f ground() {
			return at(0.0f);
		}
	}

	/**
	 * The pointer did something over the view.
	 *
	 * @return for a press, whether the lab takes it (the drag and the release that follow then come here, the camera
	 *         leaves them); otherwise ignored
	 */
	boolean pointer(Event event);
}
