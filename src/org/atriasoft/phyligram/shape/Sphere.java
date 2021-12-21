/** @file
 * @author Edouard DUPIN
 * @copyright 2011, Edouard DUPIN, all right reserved
 * @license MPL v2.0 (see license file)
 */
package org.atriasoft.phyligram.shape;

import org.atriasoft.etk.math.Vector3f;

public class Sphere extends Shape {
	private float radius = 1;
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
		*/
		return false;
	}
	public float getRadius() {
		return radius;
	}
	public void setRadius(float radius) {
		this.radius = radius;
	}

}

