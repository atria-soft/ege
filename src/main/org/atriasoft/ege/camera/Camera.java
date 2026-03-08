package org.atriasoft.ege.camera;

import org.atriasoft.ege.camera.ProjectionInterface.ValueLine;
import org.atriasoft.ege.geometry.Ray;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.etk.math.Vector4f;

public class Camera {
	private float pitch = 0;
	private Vector3f position = new Vector3f(0, 0, 0);
	private float roll = 0;
	private float yaw = 0;
	private Matrix4f viewMatrix = null;

	public Camera() {

	}

	public Matrix4f getConvertionMatrix() {
		if (this.viewMatrix != null) {
			return this.viewMatrix;
		}
		Matrix4f matrix = Matrix4f.IDENTITY;
		matrix = matrix.rotate(new Vector3f(1, 0, 0), getPitch());
		matrix = matrix.rotate(new Vector3f(0, 1, 0), getYaw());
		matrix = matrix.rotate(new Vector3f(0, 0, 1), getRoll());
		matrix = matrix.translate(new Vector3f(-this.position.x(), -this.position.y(), -this.position.z()));
		return matrix;
	}

	/**
	 * Set a direct view matrix, bypassing Euler angle computation.
	 * When set (non-null), getConvertionMatrix() returns this matrix directly.
	 * Set to null to revert to Euler-angle-based computation.
	 * @param viewMatrix the view matrix, or null to use Euler angles
	 */
	public void setViewMatrix(final Matrix4f viewMatrix) {
		this.viewMatrix = viewMatrix;
	}
	
	public float getPitch() {
		return this.pitch;
	}
	
	public Vector3f getPosition() {
		return this.position;
	}
	
	public float getRoll() {
		return this.roll;
	}
	
	public float getYaw() {
		return this.yaw;
	}

	/**
	 * Get the camera's forward direction vector in world space.
	 * When a direct view matrix is set, extracts forward from it.
	 * Otherwise computed from Euler angles.
	 * @return Normalized forward direction
	 */
	public Vector3f getForward() {
		final Matrix4f m = getConvertionMatrix();
		// Row 3 of the view matrix = forward axis (OpenGL: camera looks along -Z).
		// World-space forward = negated row 3: (-a3, -b3, -c3)
		return new Vector3f(-m.a3(), -m.b3(), -m.c3());
	}

	/**
	 * Get the camera's right direction vector in world space.
	 * @return Normalized right direction
	 */
	public Vector3f getRight() {
		final Matrix4f m = getConvertionMatrix();
		// Row 1 of the view matrix = right axis: (a1, b1, c1)
		return new Vector3f(m.a1(), m.b1(), m.c1());
	}

	/**
	 * Get the camera's up direction vector in world space.
	 * @return Normalized up direction
	 */
	public Vector3f getUp() {
		final Matrix4f m = getConvertionMatrix();
		// Row 2 of the view matrix = up axis: (a2, b2, c2)
		return new Vector3f(m.a2(), m.b2(), m.c2());
	}
	
	public void setPitch(final float pitch) {
		this.pitch = pitch;
	}
	
	public void setPosition(final Vector3f position) {
		this.position = position;
	}
	
	public void setRoll(final float roll) {
		this.roll = roll;
	}
	
	public void setYaw(final float yaw) {
		this.yaw = yaw;
	}

	public ValueLine reverseTransform(ValueLine basicValues) {
		Matrix4f cameraMatrixInverted = getConvertionMatrix().invert();
		
		return new ValueLine(cameraMatrixInverted.multiply(basicValues.near()), // compute near
		                     cameraMatrixInverted.multiply(basicValues.far())); // compute far
	}
	public Ray getRayFromScreen(ProjectionInterface projection, Vector2f diplaySize, Vector2f mousePosition) {
		ValueLine elem = projection.reverseTransform(diplaySize, mousePosition);
		ValueLine result = reverseTransform(elem);
		return Ray.createFromPoint(result.near(), result.far());
	}
	public Ray getRayFromScreen(ProjectionInterface projection, Vector3f diplaySize, Vector3f mousePosition) {
		return getRayFromScreen(projection, new Vector2f(diplaySize.x(), diplaySize.y()), new Vector2f(mousePosition.x(), mousePosition.y()));
	}
}
