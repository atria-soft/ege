package org.atriasoft.gameengine.physics;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.gameengine.internal.Log;

public class PhysicBox extends PhysicShape {
	// Box size property in X, Y and Z
	private Vector3f size = new Vector3f(1, 1, 1);

	public PhysicBox() {
		super(PhysicShapeType.BOX);
	}

	public Vector3f getSize() {
		return size;
	}

	public void setSize(Vector3f size) {
		this.size = size;
	}

	@Override
	public void updateAABB(Transform3D transformGlobal, PhysicCollisionAABB aabb) {
		// store it, many time usefull...
		this.transformGlobal = transformGlobal;
		this.colisionPoints.clear();
		// TODO Auto-generated method stub
		aabb.update(transformGlobal.multiply(this.transform.multiply(new Vector3f(this.size.x * 0.5f, this.size.y * 0.5f, this.size.z * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform.multiply(new Vector3f(-this.size.x * 0.5f, this.size.y * 0.5f, this.size.z * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform.multiply(new Vector3f(-this.size.x * 0.5f, -this.size.y * 0.5f, this.size.z * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform.multiply(new Vector3f(this.size.x * 0.5f, -this.size.y * 0.5f, this.size.z * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform.multiply(new Vector3f(this.size.x * 0.5f, this.size.y * 0.5f, -this.size.z * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform.multiply(new Vector3f(-this.size.x * 0.5f, this.size.y * 0.5f, -this.size.z * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform.multiply(new Vector3f(-this.size.x * 0.5f, -this.size.y * 0.5f, -this.size.z * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform.multiply(new Vector3f(this.size.x * 0.5f, -this.size.y * 0.5f, -this.size.z * 0.5f))));
	}

	// only needed for the narrow phase calculation ...
	public Vector3f narrowPhaseGlobalPos;
	public Vector3f narrowPhaseAxisX = new Vector3f(1, 0, 0);
	public Vector3f narrowPhaseAxisY = new Vector3f(1, 0, 0);
	public Vector3f narrowPhaseAxisZ = new Vector3f(1, 0, 0);
	public Vector3f narrowPhaseHalfSize;

	@Override
	public void updateForNarrowCollision(Transform3D transformGlobal) {
		this.narrowPhaseGlobalPos = transformGlobal.multiply(this.transform.multiply(new Vector3f(0, 0, 0)));
		this.narrowPhaseAxisX = transformGlobal.multiply(this.transform.multiply(new Vector3f(1, 0, 0))).less(this.narrowPhaseGlobalPos);
		this.narrowPhaseAxisY = transformGlobal.multiply(this.transform.multiply(new Vector3f(0, 1, 0))).less(this.narrowPhaseGlobalPos);
		this.narrowPhaseAxisZ = transformGlobal.multiply(this.transform.multiply(new Vector3f(0, 0, 1))).less(this.narrowPhaseGlobalPos);
		this.narrowPhaseHalfSize = this.size.multiplyNew(0.5f);
	}

	private void renderPoint(Vector3f subPosition, Transform3D transformGlobal, ResourceColored3DObject debugDrawProperty) {
		Matrix4f transformation = transformGlobal.getOpenGLMatrix().multiplyNew(this.transform.getOpenGLMatrix()).multiply(Matrix4f.createMatrixTranslate(subPosition));

		debugDrawProperty.drawSquare(new Vector3f(0.08f, 0.08f, 0.08f), transformation, new Color(0, 0, 1, 1));
	}

	private void renderPoint2(Vector3f subPosition, Transform3D transformGlobal, ResourceColored3DObject debugDrawProperty) {
		Matrix4f transformation = transformGlobal.getOpenGLMatrix().multiplyNew(this.transform.getOpenGLMatrix()).multiply(Matrix4f.createMatrixTranslate(subPosition));
		debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), transformation, new Color(0, 1, 0, 1));

	}

	private void renderPoint3(Vector3f subPosition, Transform3D transformGlobal, ResourceColored3DObject debugDrawProperty) {
		Matrix4f transformation = transformGlobal.getOpenGLMatrix().multiplyNew(this.transform.getOpenGLMatrix()).multiply(Matrix4f.createMatrixTranslate(subPosition));
		debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), transformation, new Color(1, 1, 0, 1));

	}

	private void renderPoint4(Vector3f subPosition, Vector3f force, ResourceColored3DObject debugDrawProperty) {
		debugDrawProperty.drawSquare(new Vector3f(0.1f, 0.1f, 0.1f), Matrix4f.identity().multiply(Matrix4f.createMatrixTranslate(subPosition)), new Color(1, 0, 0, 1));
		List<Vector3f> tmp = new ArrayList<>();
		tmp.add(new Vector3f(0,0,0));
		tmp.add(force); 
		debugDrawProperty.drawLine(tmp, new Color(1, 0, 0, 1), Matrix4f.identity().multiply(Matrix4f.createMatrixTranslate(subPosition)), true, false);
	}

	@Override
	public void renderDebug(Transform3D transformGlobal, ResourceColored3DObject debugDrawProperty) {
		debugDrawProperty.drawSquare(this.size.multiplyNew(0.5f), this.transform.getOpenGLMatrix().multiplyNew(transformGlobal.getOpenGLMatrix()), new Color(0, 1, 0, 0.25f));
		Vector3f dimention = this.size.multiplyNew(0.5f);
		renderPoint2(new Vector3f(+dimention.x, +dimention.y, +dimention.z), transformGlobal, debugDrawProperty);
		renderPoint(new Vector3f(-dimention.x, +dimention.y, +dimention.z), transformGlobal, debugDrawProperty);
		renderPoint(new Vector3f(+dimention.x, -dimention.y, +dimention.z), transformGlobal, debugDrawProperty);
		renderPoint(new Vector3f(-dimention.x, -dimention.y, +dimention.z), transformGlobal, debugDrawProperty);
		renderPoint(new Vector3f(+dimention.x, +dimention.y, -dimention.z), transformGlobal, debugDrawProperty);
		renderPoint(new Vector3f(-dimention.x, +dimention.y, -dimention.z), transformGlobal, debugDrawProperty);
		renderPoint(new Vector3f(+dimention.x, -dimention.y, -dimention.z), transformGlobal, debugDrawProperty);
		renderPoint3(new Vector3f(-dimention.x, -dimention.y, -dimention.z), transformGlobal, debugDrawProperty);
		for (Collision elem: this.colisionPoints) {
			if (elem != null) {
				if (elem.colisionPointLocal == null) {
					Log.error("colision point must be set !!!");
					continue;
				}
				for (int iii = 0; iii < elem.colisionPointLocal.length; iii++) {
					renderPoint4(elem.colisionPointLocal[iii].position, elem.colisionPointLocal[iii].force, debugDrawProperty);
				}
			}
		}
	}

}
