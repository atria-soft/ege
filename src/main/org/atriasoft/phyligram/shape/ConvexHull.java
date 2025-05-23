/** @file
 * @author Edouard DUPIN
 * @copyright 2011, Edouard DUPIN, all right reserved
 * @license MPL v2.0 (see license file)
 */
package org.atriasoft.phyligram.shape;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.math.Vector3f;

public class ConvexHull extends Shape {
	private Vector3f scale = new Vector3f(1, 1, 1);
	private List<Vector3f> points = new ArrayList<>();
	@Override
	public boolean parse(String _line) {
		/*
		if (super.parse(_line) == true) {
			return true;
		}
		if(strncmp(_line, "points:", 6) == 0) {
			//EGE_DEBUG("convex hull point parsing " << _line);
			char* base = (char*)(&_line[6]);
			char* tmp= strchr(base, '|');
			vec3 pos(0,0,0);
			while (tmp != null) {
				*tmp = '\0';
				sscanf(base, "%f %f %f", &pos.m_floats[0], &pos.m_floats[1], &pos.m_floats[2] );
				m_points.pushBack(pos);
				base = tmp+1;
				tmp= strchr(base, '|');
			}
			sscanf(base, "%f %f %f", &pos.m_floats[0], &pos.m_floats[1], &pos.m_floats[2] );
			m_points.pushBack(pos);
			/ *
			for (int32_t iii=0; iii<m_points.size(); iii++) {
				EGE_VERBOSE("    parsed " << m_points[iii]);
			}
			* /
			
			return true;
		}
		if(strncmp(_line, "scale:", 6) == 0) {
			sscanf(&_line[6], "%f %f %f", &m_scale.m_floats[0], &m_scale.m_floats[1], &m_scale.m_floats[2] );
			EGE_VERBOSE("                scale=" << m_scale);
			return true;
		}
		*/
		return false;
	}
	public Vector3f getScale() {
		return scale;
	}
	public void setScale(Vector3f scale) {
		this.scale = scale;
	}
	public List<Vector3f> getPoints() {
		return points;
	}
	public void setPoints(List<Vector3f> points) {
		this.points = points;
	}
}

