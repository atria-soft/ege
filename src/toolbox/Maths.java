package toolbox;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;

import entities.Camera;

public class Maths {
	
	public static float barryCentric(final Vector3f p1, final Vector3f p2, final Vector3f p3, final Vector2f pos) {
		float det = (p2.z() - p3.z()) * (p1.x() - p3.x()) + (p3.x() - p2.x()) * (p1.z() - p3.z());
		float l1 = ((p2.z() - p3.z()) * (pos.x() - p3.x()) + (p3.x() - p2.x()) * (pos.y() - p3.z())) / det;
		float l2 = ((p3.z() - p1.z()) * (pos.x() - p3.x()) + (p1.x() - p3.x()) * (pos.y() - p3.z())) / det;
		float l3 = 1.0f - l1 - l2;
		return l1 * p1.y() + l2 * p2.y() + l3 * p3.y();
	}
	
	public static Matrix4f createTransformationMatrix(final Vector2f translation, final Vector2f scale) {
		Matrix4f matrix = Matrix4f.IDENTITY;
		matrix = matrix.translate(new Vector3f(translation.x(), translation.y(), 0));
		matrix = matrix.scale(new Vector3f(scale.x(), scale.y(), 1f));
		return matrix;
	}
	
	public static Matrix4f createTransformationMatrix(final Vector3f translation, final Vector3f rotation, final float scale) {
		// Need to rework all of this this is really not optimum ...
		Matrix4f matrix = Matrix4f.IDENTITY;
		matrix = matrix.translate(translation);
		matrix = matrix.rotate(new Vector3f(1, 0, 0), rotation.x());
		matrix = matrix.rotate(new Vector3f(0, 1, 0), rotation.y());
		matrix = matrix.rotate(new Vector3f(0, 0, 1), rotation.z());
		matrix = matrix.scale(scale);
		return matrix;
	}
	
	public static Matrix4f createViewMatrix(final Camera camera) {
		// Need to rework all of this this is really not optimum ...
		Matrix4f matrix = createViewMatrixNoTranslate(camera);
		Vector3f camarePos = camera.getPosition();
		matrix = matrix.translate(new Vector3f(-camarePos.x(), -camarePos.y(), -camarePos.z()));
		return matrix;
	}
	
	public static Matrix4f createViewMatrixNoTranslate(final Camera camera) {
		// Need to rework all of this this is really not optimum ...
		Matrix4f matrix = Matrix4f.IDENTITY;
		matrix = matrix.rotate(new Vector3f(1, 0, 0), camera.getPitch());
		matrix = matrix.rotate(new Vector3f(0, 1, 0), camera.getYaw());
		return matrix;
	}
	
}
