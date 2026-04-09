package org.atriasoft.ege.camera;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.etk.math.Vector4f;

public class ProjectionPerspective implements ProjectionInterface {
	Matrix4f lastMatrix = Matrix4f.IDENTITY;
	float angleViewRad = 3.14f * 0.5f;
	float nearView = 0.1f;
	float farView = 100000.0f;

	protected float getAspectRatio(Vector2f size) {
		return size.x() / size.y();
	}
	protected float getAspectRatio(Vector3f size) {
		return size.x() / size.y();
	}
	public void setAngleViewRad(float angle) {
		this.angleViewRad = angle;
	}
	public float getAngleViewRad() {
		return this.angleViewRad;
	}
	@Override
	public Matrix4f getMatrix() {
		return lastMatrix;
	}
	@Override
	public Matrix4f updateMatrix(Vector2f diplaySize) {
		lastMatrix = Matrix4f.createMatrixPerspective(getAngleViewRad(), getAspectRatio(diplaySize), nearView, farView);;
		return lastMatrix;
	}
	@Override
	public Matrix4f updateMatrix(Vector3f diplaySize) {
		lastMatrix = Matrix4f.createMatrixPerspective(getAngleViewRad(), getAspectRatio(diplaySize), nearView, farView);;
		return lastMatrix;
	}
	@Override
	public ValueLine reverseTransform(Vector2f diplaySize, Vector2f mousePosition) {

		float mouse_pos_x_clip = mousePosition.x() / diplaySize.x() * 2.0f - 1.0f;
		float mouse_pos_y_clip = mousePosition.y() / diplaySize.y() * 2.0f - 1.0f;
		Vector4f mouse_pos_near_clip = new Vector4f(mouse_pos_x_clip, mouse_pos_y_clip, -1.0f, 1.0f);
		Vector4f mouse_pos_far_clip = new Vector4f(mouse_pos_x_clip, mouse_pos_y_clip, 1.0f, 1.0f);
		
		Matrix4f projectionMatrixInverted = getMatrix().invert();
		
		Vector4f mouse_pos_near_view = projectionMatrixInverted.multiply(mouse_pos_near_clip);
		Vector4f mouse_pos_far_view = projectionMatrixInverted.multiply(mouse_pos_far_clip);
		// only for perspective
		mouse_pos_near_view = mouse_pos_near_view.divide(mouse_pos_near_view.w());
		mouse_pos_far_view = mouse_pos_far_view.divide(mouse_pos_far_view.w());

		return new ValueLine(new Vector3f(mouse_pos_near_view.x(), mouse_pos_near_view.y(), mouse_pos_near_view.z()),
				             new Vector3f(mouse_pos_far_view.x(), mouse_pos_far_view.y(), mouse_pos_far_view.z()));
		
	}
	
}
