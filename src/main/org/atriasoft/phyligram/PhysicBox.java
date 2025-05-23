package org.atriasoft.phyligram;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.phyligram.shape.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhysicBox extends PhysicShape {
	static final Logger LOGGER = LoggerFactory.getLogger(PhysicBox.class);
	// Box size property in X, Y and Z
	private Vector3f size = new Vector3f(1, 1, 1);

	// only needed for the narrow phase calculation ...
	public Vector3f narrowPhaseGlobalPos;

	public Vector3f narrowPhaseAxisX = new Vector3f(1, 0, 0);

	public Vector3f narrowPhaseAxisY = new Vector3f(1, 0, 0);

	public Vector3f narrowPhaseAxisZ = new Vector3f(1, 0, 0);

	public Vector3f narrowPhaseHalfSize;

	public PhysicBox() {}

	public Vector3f getSize() {
		return this.size;
	}

	@Override
	public void renderDebug(final Transform3D transformGlobal, final ResourceColored3DObject debugDrawProperty) {
		debugDrawProperty.drawSquare(this.size.multiply(0.5f),
				this.transform.getOpenGLMatrix().multiply(transformGlobal.getOpenGLMatrix()),
				new Color(0, 1, 0, 0.25f));
		final Vector3f dimention = this.size.multiply(0.5f);
		renderPoint2(new Vector3f(+dimention.x(), +dimention.y(), +dimention.z()), transformGlobal, debugDrawProperty);
		renderPoint(new Vector3f(-dimention.x(), +dimention.y(), +dimention.z()), transformGlobal, debugDrawProperty);
		renderPoint(new Vector3f(+dimention.x(), -dimention.y(), +dimention.z()), transformGlobal, debugDrawProperty);
		renderPoint(new Vector3f(-dimention.x(), -dimention.y(), +dimention.z()), transformGlobal, debugDrawProperty);
		renderPoint(new Vector3f(+dimention.x(), +dimention.y(), -dimention.z()), transformGlobal, debugDrawProperty);
		renderPoint(new Vector3f(-dimention.x(), +dimention.y(), -dimention.z()), transformGlobal, debugDrawProperty);
		renderPoint(new Vector3f(+dimention.x(), -dimention.y(), -dimention.z()), transformGlobal, debugDrawProperty);
		renderPoint3(new Vector3f(-dimention.x(), -dimention.y(), -dimention.z()), transformGlobal, debugDrawProperty);
		for (final Collision elem : this.colisionPoints) {
			if (elem != null) {
				if (elem.colisionPointLocal == null) {
					LOGGER.error("colision point must be set !!!");
					continue;
				}
				for (final ColisionPoint element : elem.colisionPointLocal) {
					renderPoint4(element.position, element.force, debugDrawProperty);
				}
			}
		}
	}

	private void renderPoint(
			final Vector3f subPosition,
			final Transform3D transformGlobal,
			final ResourceColored3DObject debugDrawProperty) {
		final Matrix4f transformation = transformGlobal.getOpenGLMatrix().multiply(this.transform.getOpenGLMatrix())
				.multiply(Matrix4f.createMatrixTranslate(subPosition));

		debugDrawProperty.drawSquare(new Vector3f(0.08f, 0.08f, 0.08f), transformation, new Color(0, 0, 1, 1));
	}

	private void renderPoint2(
			final Vector3f subPosition,
			final Transform3D transformGlobal,
			final ResourceColored3DObject debugDrawProperty) {
		final Matrix4f transformation = transformGlobal.getOpenGLMatrix().multiply(this.transform.getOpenGLMatrix())
				.multiply(Matrix4f.createMatrixTranslate(subPosition));
		debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), transformation, new Color(0, 1, 0, 1));

	}

	private void renderPoint3(
			final Vector3f subPosition,
			final Transform3D transformGlobal,
			final ResourceColored3DObject debugDrawProperty) {
		final Matrix4f transformation = transformGlobal.getOpenGLMatrix().multiply(this.transform.getOpenGLMatrix())
				.multiply(Matrix4f.createMatrixTranslate(subPosition));
		debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), transformation, new Color(1, 1, 0, 1));

	}

	private void renderPoint4(
			final Vector3f subPosition,
			final Vector3f force,
			final ResourceColored3DObject debugDrawProperty) {
		debugDrawProperty.drawSquare(new Vector3f(0.1f, 0.1f, 0.1f), Matrix4f.createMatrixTranslate(subPosition),
				new Color(1, 0, 0, 1));
		final List<Vector3f> tmp = new ArrayList<>();
		tmp.add(new Vector3f(0, 0, 0));
		tmp.add(force);
		debugDrawProperty.drawLine(tmp, new Color(1, 0, 0, 1), Matrix4f.createMatrixTranslate(subPosition), true,
				false);
	}

	public void setSize(final Vector3f size) {
		this.size = size;
	}

	@Override
	public void updateAABB(final Transform3D transformGlobal, final AABB aabb) {
		// store it, many time usefull...
		this.transformGlobal = transformGlobal;
		this.colisionPoints.clear();
		// TODO Auto-generated method stub
		aabb.update(transformGlobal.multiply(this.transform
				.multiply(new Vector3f(this.size.x() * 0.5f, this.size.y() * 0.5f, this.size.z() * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform
				.multiply(new Vector3f(-this.size.x() * 0.5f, this.size.y() * 0.5f, this.size.z() * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform
				.multiply(new Vector3f(-this.size.x() * 0.5f, -this.size.y() * 0.5f, this.size.z() * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform
				.multiply(new Vector3f(this.size.x() * 0.5f, -this.size.y() * 0.5f, this.size.z() * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform
				.multiply(new Vector3f(this.size.x() * 0.5f, this.size.y() * 0.5f, -this.size.z() * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform
				.multiply(new Vector3f(-this.size.x() * 0.5f, this.size.y() * 0.5f, -this.size.z() * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform
				.multiply(new Vector3f(-this.size.x() * 0.5f, -this.size.y() * 0.5f, -this.size.z() * 0.5f))));
		aabb.update(transformGlobal.multiply(this.transform
				.multiply(new Vector3f(this.size.x() * 0.5f, -this.size.y() * 0.5f, -this.size.z() * 0.5f))));
	}

	@Override
	public void updateForNarrowCollision(final Transform3D transformGlobal) {
		this.narrowPhaseGlobalPos = transformGlobal.multiply(this.transform.multiply(new Vector3f(0, 0, 0)));
		this.narrowPhaseAxisX = transformGlobal.multiply(this.transform.multiply(new Vector3f(1, 0, 0)))
				.less(this.narrowPhaseGlobalPos);
		this.narrowPhaseAxisY = transformGlobal.multiply(this.transform.multiply(new Vector3f(0, 1, 0)))
				.less(this.narrowPhaseGlobalPos);
		this.narrowPhaseAxisZ = transformGlobal.multiply(this.transform.multiply(new Vector3f(0, 0, 1)))
				.less(this.narrowPhaseGlobalPos);
		this.narrowPhaseHalfSize = this.size.multiply(0.5f);
	}

}
