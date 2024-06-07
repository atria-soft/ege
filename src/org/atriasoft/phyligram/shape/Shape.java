/** @file
 * @author Edouard DUPIN
 * @copyright 2011, Edouard DUPIN, all right reserved
 * @license MPL v2.0 (see license file)
 */
package org.atriasoft.phyligram.shape;

import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Vector3f;

public class Shape {
	private Quaternion orientation = Quaternion.IDENTITY;
	private float mass = 1; //!< element mass in "g" then 1000 for 1kg
	private Vector3f origin = Vector3f.ZERO;
	
	public void display() {
		
	}
	
	public float getMass() {
		return this.mass;
	}
	
	public Quaternion getOrientation() {
		return this.orientation;
	}
	
	public Vector3f getOrigin() {
		return this.origin;
	}
	
	public boolean parse(final String _line) {
		LOGGER.error("dfgdfg");
		/*
		if(strncmp(_line, "origin:", 7) == 0) {
			sscanf(&_line[7], "%f %f %f", &m_origin.m_floats[0], &m_origin.m_floats[1], &m_origin.m_floats[2] );
			EGE_VERBOSE("                Origin=" << m_origin);
			return true;
		}
		if(strncmp(_line, "rotate:", 7) == 0) {
			sscanf(&_line[7], "%f %f %f %f", &m_quaternion.m_floats[0], &m_quaternion.m_floats[1], &m_quaternion.m_floats[2], &m_quaternion.m_floats[3] );
			EGE_VERBOSE("                rotate=" << m_quaternion);
			return true;
		}
		if(strncmp(_line, "mass:", 5) == 0) {
			sscanf(&_line[5], "%f", &m_mass );
			EGE_VERBOSE("                mass=" << m_mass);
			return true;
		}
		*/
		return false;
	};
	
	public void setMass(final float mass) {
		this.mass = mass;
	};
	
	public void setOrientation(final Quaternion orientation) {
		this.orientation = orientation;
	};
	
	public void setOrigin(final Vector3f origin) {
		this.origin = origin;
	};
}
/*
 ememory::SharedPtr<ege::physics::Shape> ege::physics::Shape::create(const etk::String& _name) {
	ememory::SharedPtr<ege::physics::Shape> tmpp = null;
	etk::String name = etk::toLower(_name);
	if (name == "box") {
		tmpp = ememory::makeShared<ege::physics::shape::Box>();
	} else if (name == "sphere") {
		tmpp = ememory::makeShared<ege::physics::shape::Sphere>();
	} else if (name == "cone") {
		tmpp = ememory::makeShared<ege::physics::shape::Cone>();
	} else if (name == "cylinder") {
		tmpp = ememory::makeShared<ege::physics::shape::Cylinder>();
	} else if (name == "capsule") {
		tmpp = ememory::makeShared<ege::physics::shape::Capsule>();
	} else if (name == "convexhull") {
		tmpp = ememory::makeShared<ege::physics::shape::ConvexHull>();
	} else if (name == "autoconcave") {
		tmpp = ememory::makeShared<ege::physics::shape::Concave>();
	} else {
		EGE_ERROR("Create an unknow element : '" << _name << "' availlable : [BOX,SPHERE,CONE,CYLINDER,CAPSULE,CONVEXHULL,autoConcave]");
		return null;
	}
	if (tmpp == null) {
		EGE_ERROR("Allocation error for physical element : '" << _name << "'");
	}
	return tmpp;
}
*/
