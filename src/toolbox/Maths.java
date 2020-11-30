package toolbox;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;

import entities.Camera;

public class Maths {
	
	public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
		float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
		float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
		float l3 = 1.0f - l1 - l2;
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}
	
	public static Matrix4f createTransformationMatrix(Vector3f translation, Vector3f rotation, float scale) {
		// Need to rework all of this this is really not optimum ...
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		matrix.translate(translation);
		matrix.rotate(new Vector3f(1,0,0), rotation.x);
		matrix.rotate(new Vector3f(0,1,0), rotation.y);
		matrix.rotate(new Vector3f(0,0,1), rotation.z);
		matrix.scale(scale);
		return matrix;
	}
	public static Matrix4f createViewMatrixNoTranslate(Camera camera) {
		// Need to rework all of this this is really not optimum ...
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		matrix.rotate(new Vector3f(1,0,0), camera.getPitch());
		matrix.rotate(new Vector3f(0,1,0), camera.getYaw());
		return matrix;
	}
	public static Matrix4f createViewMatrix(Camera camera) {
		// Need to rework all of this this is really not optimum ...
		Matrix4f matrix = createViewMatrixNoTranslate(camera);
		Vector3f camarePos = camera.getPosition();
		matrix.translate(new Vector3f(-camarePos.x,-camarePos.y,-camarePos.z));
		return matrix;
	}
	public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		matrix.translate(new Vector3f(translation.x, translation.y, 0));
		matrix.scale(new Vector3f(scale.x, scale.y, 1f));
		return matrix;
	}

}
