package org.atriasoft.phyligram.math;

import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.phyligram.ColisionPoint;
import org.atriasoft.phyligram.PhysicSphere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolCollisionSphereWithSphere {
	static final Logger LOGGER = LoggerFactory.getLogger(ToolCollisionSphereWithSphere.class);
	
	// Note sphere 2 is the reference ...
	public static ColisionPoint getCollisionPoint(final PhysicSphere sphere1, final PhysicSphere shapeReference) {
		if (sphere1.getSize() > shapeReference.getSize()) {
			LOGGER.info("[TODO] implement then reference is smaller than moving");
		}
		Vector3f force = sphere1.narrowPhaseGlobalPos.less(shapeReference.narrowPhaseGlobalPos);
		final float distance = shapeReference.getSize() + sphere1.getSize() - force.length();
		force = force.safeNormalize();
		final Vector3f impact = force.multiply(shapeReference.getSize());
		force = force.multiply(distance);
		force = force.multiply(sphere1.getSize() + distance);
		// set relative impact position:
		//return new ColisionPoint(impact, force);
		// set global position
		return new ColisionPoint(shapeReference.narrowPhaseGlobalPos.add(impact), force);
	}

	// Note sphere 2 is the reference ...
	public static boolean testCollide(final PhysicSphere sphere1, final PhysicSphere shapeReference) {
		final float distance1 = sphere1.narrowPhaseGlobalPos.distance2(shapeReference.narrowPhaseGlobalPos);
		float distance2 = sphere1.getSize() + shapeReference.getSize();
		distance2 = distance2 * distance2;
		return distance1 <= distance2;
	}

	private ToolCollisionSphereWithSphere() {}
}
