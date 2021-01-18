/** @file
 * @author Edouard DUPIN
 * @copyright 2011, Edouard DUPIN, all right reserved
 * @license MPL v2.0 (see license file)
 */
package org.atriasoft.gameengine.physics.shape;

import org.atriasoft.etk.math.Vector3f;

public class Box extends Shape {
	private Vector3f size; // Box size property in X, Y and Z
	
	@Override
	public boolean parse(String _line) {
		/*
		if (super.parse(_line) == true) {
			return true;
		}
		if(strncmp(_line, "half-extents:", 13) == 0) {
			sscanf(&_line[13], "%f %f %f", &size.m_floats[0], &size.m_floats[1], &size.m_floats[2] );
			EGE_VERBOSE("                halfSize=" << size);
			return true;
		}
		*/
		return false;
	}

	public Vector3f getSize() {
		return size;
	}
	
	public void setSize(Vector3f size) {
		this.size = size;
	}
}

