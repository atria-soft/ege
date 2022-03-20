package org.atriasoft.ege.camera;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;


public interface ProjectionInterface {
	public record ValueLine(Vector3f near, Vector3f far) {};
	Matrix4f getMatrix();
	Matrix4f updateMatrix(Vector2f diplaySize);
	ValueLine reverseTransform(Vector2f diplaySize, Vector2f mousePosition);
	
}
