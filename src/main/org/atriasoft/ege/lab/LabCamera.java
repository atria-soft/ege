package org.atriasoft.ege.lab;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;

/**
 * The camera of a lab: a target, a distance, and the direction of the eye
 * from the target (azimuth around Y, 0 for an eye south of the target,
 * growing towards the east; elevation over the ground). Two ways to drive it:
 * <ul>
 * <li>{@link Mode#ORBIT}: a drag turns the eye around the target
 * ({@link #turn}), the wheel comes closer or farther ({@link #zoom}), a pan
 * moves the target in the plane of the picture ({@link #pan});</li>
 * <li>{@link Mode#FLY}: a drag turns the look around the eye, the keys fly
 * the eye and the target together ({@link #drive}).</li>
 * </ul>
 * Changing mode keeps the eye and the look. Metres and radians; X east, Y up,
 * Z south. Pure Java, tested headless.
 */
public final class LabCamera {

	/** How a drag and the arrow keys drive the camera. */
	public enum Mode {
		/** Around the target. */
		ORBIT,
		/** Free flight. */
		FLY
	}

	/** Where the eye starts: south-south-east of the target, a little above it. */
	public static final float START_AZIMUTH = (float) Math.toRadians(35.0);
	public static final float START_ELEVATION = (float) Math.toRadians(18.0);
	/** Radians turned per pixel of a drag. */
	static final float TURN_PER_PIXEL = (float) Math.toRadians(0.4);
	/** Radians turned per second by a held arrow key (orbit). */
	static final float TURN_PER_SECOND = (float) Math.toRadians(60.0);
	/** Factor of the distance per step of the wheel. */
	static final float ZOOM_STEP = 1.15f;
	static final float MIN_DISTANCE = 0.5f;
	static final float MAX_DISTANCE = 3000.0f;
	/** Lowest and highest elevation of an orbit (the eye stays over the ground plane of the target). */
	static final float MIN_ORBIT_ELEVATION = (float) Math.toRadians(1.0);
	static final float MAX_ELEVATION = (float) Math.toRadians(89.0);
	/** A framed box fills this share of the narrower side of the picture. */
	static final float FRAME_FILL = 0.92f;

	private Mode mode = Mode.ORBIT;
	private Vector3f target = new Vector3f(0.0f, 1.0f, 0.0f);
	private float distance = 12.0f;
	private float azimuth = START_AZIMUTH;
	private float elevation = START_ELEVATION;

	public Mode mode() {
		return this.mode;
	}

	/** Orbit or fly from now on (the eye and the look stay). */
	public void setMode(final Mode next) {
		this.mode = next;
		if (next == Mode.ORBIT) {
			this.elevation = clampOrbit(this.elevation);
		}
	}

	public Vector3f target() {
		return this.target;
	}

	public float distance() {
		return this.distance;
	}

	public float azimuth() {
		return this.azimuth;
	}

	public float elevation() {
		return this.elevation;
	}

	/** Unit vector from the target to the eye. */
	public Vector3f back() {
		final float cosE = (float) Math.cos(this.elevation);
		return new Vector3f(cosE * (float) Math.sin(this.azimuth), (float) Math.sin(this.elevation),
				cosE * (float) Math.cos(this.azimuth));
	}

	/** Unit vector to the right of the picture, horizontal. */
	public Vector3f right() {
		return new Vector3f((float) Math.cos(this.azimuth), 0.0f, -(float) Math.sin(this.azimuth));
	}

	/** Unit vector to the top of the picture. */
	public Vector3f up() {
		return back().cross(right());
	}

	/** Where the eye is. */
	public Vector3f eye() {
		return this.target.add(back().multiply(this.distance));
	}

	/**
	 * The unit direction from the eye through the pixel {@code (x, y)} of a picture {@code width} x {@code height}
	 * pixels ({@code y} up from its bottom) of horizontal field of view {@code fovX}: what lies under the pointer.
	 */
	public Vector3f ray(final float x, final float y, final float width, final float height, final float fovX) {
		final double tanX = Math.tan(fovX * 0.5);
		final double tanY = height > 0.0f && width > 0.0f ? tanX * height / width : tanX;
		final float across = (float) ((width > 0.0f ? 2.0 * x / width - 1.0 : 0.0) * tanX);
		final float upward = (float) ((height > 0.0f ? 2.0 * y / height - 1.0 : 0.0) * tanY);
		return right().multiply(across).add(up().multiply(upward)).less(back()).normalize();
	}

	/**
	 * Look from the direction {@code azimuth} (radians, 0: the eye south of
	 * the target, {@code PI / 2}: east of it) and {@code elevation} (radians
	 * above the horizon): around the target in an orbit (the elevation kept
	 * over its ground, as a drag does), the look around the eye in a flight.
	 */
	public void setDirection(final float azimuth, final float elevation) {
		if (this.mode == Mode.ORBIT) {
			this.azimuth = wrap(azimuth);
			this.elevation = clampOrbit(elevation);
			return;
		}
		final Vector3f eye = eye();
		this.azimuth = wrap(azimuth);
		this.elevation = clamp(elevation, -MAX_ELEVATION, MAX_ELEVATION);
		this.target = eye.less(back().multiply(this.distance));
	}

	/** Look at {@code target} from {@code distance} metres, the direction kept. */
	public void lookAt(final Vector3f target, final float distance) {
		this.target = target;
		this.distance = clamp(distance, MIN_DISTANCE, MAX_DISTANCE);
	}

	/**
	 * Look at {@code box} ({@code {minX, minY, minZ, maxX, maxY, maxZ}}) from
	 * as close as its eight corners stay in a picture of horizontal field of
	 * view {@code fovX} and {@code aspect} (width / height), with a small
	 * margin; the direction kept. Nothing changes for no box.
	 */
	public void frame(final float[] box, final float fovX, final float aspect) {
		frame(box, fovX, aspect, 0.0f);
	}

	/**
	 * {@link #frame(float[], float, float)} in the part of the picture right
	 * of its left {@code leftShare} (0 to 1: a panel drawn over the picture
	 * there): the box in the middle of that part.
	 */
	public void frame(final float[] box, final float fovX, final float aspect, final float leftShare) {
		if (box == null || box.length < 6) {
			return;
		}
		final Vector3f centre = new Vector3f((box[0] + box[3]) * 0.5f, (box[1] + box[4]) * 0.5f,
				(box[2] + box[5]) * 0.5f);
		final double tanX = Math.tan(fovX * 0.5);
		final double tanY = tanX / (aspect > 0.0f ? aspect : 1.0f) * FRAME_FILL;
		// The free part of the picture, in tangents of the angle from the axis of the view: [low, high].
		final double share = Math.max(0.0, Math.min(0.9, leftShare));
		final double middle = tanX * share;
		final double half = tanX * (1.0 - share) * FRAME_FILL;
		final double low = middle - half;
		final double high = middle + half;
		final Vector3f r = right();
		final Vector3f u = up();
		final Vector3f b = back();
		final double[] across = new double[8];
		final double[] depth = new double[8];
		double distance = MIN_DISTANCE;
		for (int i = 0; i < 8; i++) {
			final Vector3f corner = new Vector3f((i & 1) == 0 ? box[0] : box[3], (i & 2) == 0 ? box[1] : box[4],
					(i & 4) == 0 ? box[2] : box[5]).less(centre);
			across[i] = corner.dot(r);
			depth[i] = corner.dot(b);
			// Up and down: |c.u| <= (d - c.b) tanY.
			distance = Math.max(distance, depth[i] + Math.abs(corner.dot(u)) / tanY);
		}
		// Across, with the target moved by k along the right: low <= (c.r - k) / (d - c.b) <= high for every corner.
		// A k exists when c_i.r - high (d - c_i.b) <= c_j.r - low (d - c_j.b) for every pair.
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				distance = Math.max(distance,
						(across[i] - across[j] + high * depth[i] - low * depth[j]) / (high - low));
			}
		}
		double fromK = Double.NEGATIVE_INFINITY;
		double toK = Double.POSITIVE_INFINITY;
		for (int i = 0; i < 8; i++) {
			fromK = Math.max(fromK, across[i] - high * (distance - depth[i]));
			toK = Math.min(toK, across[i] - low * (distance - depth[i]));
		}
		final double k = (fromK + toK) * 0.5;
		lookAt(centre.add(r.multiply((float) k)), (float) distance);
	}

	/**
	 * A drag of {@code dx, dy} pixels (y up): around the target in an orbit,
	 * the look around the eye in a flight.
	 */
	public void turn(final float dx, final float dy) {
		if (this.mode == Mode.ORBIT) {
			this.azimuth = wrap(this.azimuth - dx * TURN_PER_PIXEL);
			this.elevation = clampOrbit(this.elevation + dy * TURN_PER_PIXEL);
			return;
		}
		final Vector3f eye = eye();
		this.azimuth = wrap(this.azimuth - dx * TURN_PER_PIXEL);
		this.elevation = clamp(this.elevation - dy * TURN_PER_PIXEL, -MAX_ELEVATION, MAX_ELEVATION);
		this.target = eye.less(back().multiply(this.distance));
	}

	/** {@code steps} of the wheel: closer for a positive number (an orbit), forward (a flight). */
	public void zoom(final float steps) {
		if (this.mode == Mode.ORBIT) {
			this.distance = clamp(this.distance * (float) Math.pow(ZOOM_STEP, -steps), MIN_DISTANCE, MAX_DISTANCE);
			return;
		}
		this.target = this.target.less(back().multiply(steps * flySpeed() * 0.25f));
	}

	/**
	 * Move the target in the plane of the picture by a drag of {@code dx, dy}
	 * pixels (y up) in a view {@code height} pixels high of vertical field
	 * of view {@code fovY}: what is under the pointer follows it.
	 */
	public void pan(final float dx, final float dy, final float height, final float fovY) {
		if (!(height > 0.0f)) {
			return;
		}
		final float metresPerPixel = 2.0f * this.distance * (float) Math.tan(fovY * 0.5) / height;
		this.target = this.target.less(right().multiply(dx * metresPerPixel)).less(up().multiply(dy * metresPerPixel));
	}

	/**
	 * Held keys for {@code seconds}, each -1, 0 or 1. An orbit turns
	 * ({@code side}), tilts ({@code ahead}) and zooms ({@code rise}); a flight
	 * moves ahead along the look, sideways and up.
	 */
	public void drive(final int ahead, final int side, final int rise, final float seconds) {
		if (this.mode == Mode.ORBIT) {
			this.azimuth = wrap(this.azimuth - side * TURN_PER_SECOND * seconds);
			this.elevation = clampOrbit(this.elevation + ahead * TURN_PER_SECOND * seconds);
			this.distance = clamp(this.distance * (float) Math.pow(ZOOM_STEP, -rise * 4.0f * seconds), MIN_DISTANCE,
					MAX_DISTANCE);
			return;
		}
		final float step = flySpeed() * seconds;
		final Vector3f move = back().multiply(-ahead * step).add(right().multiply(side * step))
				.add(new Vector3f(0.0f, rise * step, 0.0f));
		this.target = this.target.add(move);
	}

	/** Metres per second of a flight: faster far from what is looked at. */
	private float flySpeed() {
		return clamp(this.distance * 0.6f, 3.0f, 60.0f);
	}

	/** The view matrix (world to camera, the camera looking along -Z). */
	public Matrix4f view() {
		final Vector3f eye = eye();
		final Vector3f f = back();
		final Vector3f r = right();
		final Vector3f u = f.cross(r);
		final float tx = -(r.x() * eye.x() + r.y() * eye.y() + r.z() * eye.z());
		final float ty = -(u.x() * eye.x() + u.y() * eye.y() + u.z() * eye.z());
		final float tz = -(f.x() * eye.x() + f.y() * eye.y() + f.z() * eye.z());
		return new Matrix4f(r.x(), r.y(), r.z(), tx, u.x(), u.y(), u.z(), ty, f.x(), f.y(), f.z(), tz, 0.0f, 0.0f,
				0.0f, 1.0f);
	}

	private static float clampOrbit(final float elevation) {
		return clamp(elevation, MIN_ORBIT_ELEVATION, MAX_ELEVATION);
	}

	private static float clamp(final float value, final float min, final float max) {
		if (!(value >= min)) {
			return min;
		}
		return Math.min(value, max);
	}

	private static float wrap(final float angle) {
		final double turn = 2.0 * Math.PI;
		return (float) (angle - turn * Math.floor((angle + Math.PI) / turn));
	}
}
