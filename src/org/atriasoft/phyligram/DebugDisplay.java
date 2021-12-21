package org.atriasoft.phyligram;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceColored3DObject;

public class DebugDisplay {
	//	public static ComponentPosition relativeTestPos;
	//	public static PhysicBox boxTest;
	public static List<Vector3f> testPoints = new ArrayList<>();
	public static List<Vector3f> testPointsBox = new ArrayList<>();
	public static List<Boolean> testPointsCollide = new ArrayList<>();
	public static Vector3f testRpos;
	public static Quaternion testQTransfert;
	public static Vector3f box1HalfSize;
	public static Vector3f box2HalfSize;
	private static ResourceColored3DObject debugDrawProperty;
	
	static {
		if (debugDrawProperty == null) {
			debugDrawProperty = ResourceColored3DObject.create();
		}
		
	}
	
	public static void clear() {
		testPoints.clear();
		testPointsBox.clear();
		testPointsCollide.clear();
		
	}
	
	public static void onDraw() {
		if (debugDrawProperty == null) {
			debugDrawProperty = ResourceColored3DObject.create();
		}
		// now render the point test collision ...
		for (int iii = 0; iii < DebugDisplay.testPoints.size(); iii++) {
			Vector3f elem = DebugDisplay.testPoints.get(iii);
			boolean collide = DebugDisplay.testPointsCollide.get(iii);
			if (collide) {
				debugDrawProperty.drawSquare(new Vector3f(0.1f, 0.1f, 0.1f), Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14)), new Color(1, 0, 0, 1));
			} else {
				if (iii == 0) {
					debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14)), new Color(0, 1, 0, 1));
				} else if (iii == 7) {
					debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14)), new Color(1, 1, 0, 1));
				} else {
					debugDrawProperty.drawSquare(new Vector3f(0.1f, 0.1f, 0.1f), Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14)), new Color(1, 1, 1, 1));
				}
			}
		}
		for (int iii = 0; iii < DebugDisplay.testPointsBox.size(); iii++) {
			Vector3f elem = DebugDisplay.testPointsBox.get(iii);
			if (iii == 0) {
				debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14)), new Color(0, 1, 0, 1));
			} else if (iii == 7) {
				debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14)), new Color(1, 1, 0, 1));
			} else {
				debugDrawProperty.drawSquare(new Vector3f(0.1f, 0.1f, 0.1f), Matrix4f.createMatrixTranslate(new Vector3f(elem.x(), elem.y(), elem.z() + 14)), new Color(0, 0, 1, 1));
			}
		}
		
		if (testRpos != null) {
			//debugDrawProperty.drawSquare(box2HalfSize, testQTransfert.getMatrix4().multiplyNew(Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z+14))), new Color(0,1,0,0.5f));
			//Matrix4f transformation = Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z)).multiply(testQTransfert.getMatrix4()).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			//Matrix4f transformation = testQTransfert.getMatrix4().multiply(Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z))).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			//Matrix4f transformation = testQTransfert.getMatrix4().multiply(Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z))).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			Matrix4f trensformation = Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x(), testRpos.y(), testRpos.z())).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0, 0, 14)))
					.multiply(testQTransfert.getMatrix4());
			// OK sans la box1 orientation ...
			//Matrix4f transformation = Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z)).multiply(testQTransfert.getMatrix4()).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			//Matrix4f transformation = Matrix4f.createMatrixTranslate(new Vector3f(testRpos.x,testRpos.y,testRpos.z)).multiply(testQTransfert.getMatrix4()).multiply(Matrix4f.createMatrixTranslate(new Vector3f(0,0,14)));
			debugDrawProperty.drawSquare(box2HalfSize, trensformation, new Color(0, 1, 0, 0.5f));
			debugDrawProperty.drawSquare(box1HalfSize, Matrix4f.createMatrixTranslate(new Vector3f(0, 0, 14)), new Color(0, 0, 1, 0.5f));
		}
		
		// Restore context of matrix
		OpenGL.pop();
	}
}
