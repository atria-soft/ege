package org.atriasoft.gameengine.physics;

import org.atriasoft.etk.math.Vector3f;

public abstract class GravityMap {
	public abstract Vector3f getGravityAtPosition(Vector3f position);
}
