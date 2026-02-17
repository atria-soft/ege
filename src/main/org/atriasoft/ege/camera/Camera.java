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
	
	public Camera() {
		
	}
	
	public Matrix4f getConvertionMatrix() {
		Matrix4f matrix = Matrix4f.IDENTITY;
		matrix = matrix.rotate(new Vector3f(1, 0, 0), getPitch());
		matrix = matrix.rotate(new Vector3f(0, 1, 0), getYaw());
		matrix = matrix.rotate(new Vector3f(0, 0, 1), getRoll());
		matrix = matrix.translate(new Vector3f(-this.position.x(), -this.position.y(), -this.position.z()));
		return matrix;
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
	 * Computed by transforming the -Z view-space direction through the inverse
	 * of the rotation part of the view matrix (= its transpose, since rotations
	 * are orthogonal).
	 * @return Normalized forward direction
	 */
	public Vector3f getForward() {
		// Build rotation matrix then transpose it, then multiply by (0,0,-1)
		Matrix4f rotation = Matrix4f.IDENTITY;
		rotation = rotation.rotate(new Vector3f(1, 0, 0), this.pitch);
		rotation = rotation.rotate(new Vector3f(0, 1, 0), this.yaw);
		rotation = rotation.rotate(new Vector3f(0, 0, 1), this.roll);
		// Inverse = transpose for rotation. Forward = R^T * (0,0,-1)
		// Row 3 of R = column 3 of R^T. Forward = -column3 of R^T = -row3 of R.
		// R row3 = (a3, b3, c3) in the record layout
		final Matrix4f inv = rotation.transpose();
		// forward = inv * (0,0,-1) = (-inv.c1, -inv.c2, -inv.c3)
		return new Vector3f(-inv.c1(), -inv.c2(), -inv.c3());
	}

	/**
	 * Get the camera's right direction vector in world space.
	 * @return Normalized right direction
	 */
	public Vector3f getRight() {
		Matrix4f rotation = Matrix4f.IDENTITY;
		rotation = rotation.rotate(new Vector3f(1, 0, 0), this.pitch);
		rotation = rotation.rotate(new Vector3f(0, 1, 0), this.yaw);
		rotation = rotation.rotate(new Vector3f(0, 0, 1), this.roll);
		final Matrix4f inv = rotation.transpose();
		// right = inv * (1,0,0) = (inv.a1, inv.a2, inv.a3)
		return new Vector3f(inv.a1(), inv.a2(), inv.a3());
	}

	/**
	 * Get the camera's up direction vector in world space.
	 * @return Normalized up direction
	 */
	public Vector3f getUp() {
		Matrix4f rotation = Matrix4f.IDENTITY;
		rotation = rotation.rotate(new Vector3f(1, 0, 0), this.pitch);
		rotation = rotation.rotate(new Vector3f(0, 1, 0), this.yaw);
		rotation = rotation.rotate(new Vector3f(0, 0, 1), this.roll);
		final Matrix4f inv = rotation.transpose();
		// up = inv * (0,1,0) = (inv.b1, inv.b2, inv.b3)
		return new Vector3f(inv.b1(), inv.b2(), inv.b3());
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
		Matrix4f cameraMatrix = getConvertionMatrix().transpose();
		// invert Matrix:
		Matrix4f cameraMatrixInverted = cameraMatrix.invert();
		
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
