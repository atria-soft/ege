/** @file
 * @author Edouard DUPIN
 * @copyright 2011, Edouard DUPIN, all right reserved
 * @license MPL v2.0 (see license file)
 */
package org.atriasoft.ege.physics.shape;

import org.atriasoft.etk.math.Vector3f;

public class Capsule extends Shape {
	private float radius = 1;
	private float size = 1;
	@Override
	public boolean parse(String _line) {
		/*
		if (super.parse(_line) == true) {
			return true;
		}
		if(strncmp(_line, "radius:", 7) == 0) {
			sscanf(&_line[7], "%f", &m_radius );
			EGE_VERBOSE("                radius=" << m_radius);
			return true;
		}
		if(strncmp(_line, "size:", 5) == 0) {
			sscanf(&_line[5], "%f", &m_size );
			EGE_VERBOSE("                height=" << m_size);
			return true;
		}
		*/
		return false;
	}
	public float getRadius() {
		return radius;
	}
	public void setRadius(float radius) {
		this.radius = radius;
	}

	public float getSize() {
		return size;
	}
	public void setSize(float size) {
		this.size = size;
	}
}

