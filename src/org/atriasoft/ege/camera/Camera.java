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
