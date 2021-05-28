package org.atriasoft.ege.components.part;

import org.atriasoft.etk.math.Transform3D;

public interface PositionningInterface {
	/**
	 * Get the current positionning on an object
	 * @return
	 */
	Transform3D getTransform();
}
