/**
 * @author Edouard DUPIN
 * 
 * @copyright 2011, Edouard DUPIN, all right reserved
 * 
 * @license BSD v3 (see license file)
 */


#include <ege/elements/ElementBase.h>
#include <ege/debug.h>

ege::ElementBase::ElementBase(const std::shared_ptr<ege::Environement>& _env) : 
  ege::Element(_env),
  m_position(0,0,0) {
	
}

ege::ElementBase::~ElementBase() {
	EGE_WARNING("Remove ... ");
}

const std::string& ege::ElementBase::getType() const {
	return ege::Element::getType();
}

void ege::ElementBase::draw(int32_t _pass) {
	mat4 transformationMatrix(0);
	transformationMatrix.identity();
	transformationMatrix.translate(m_position);
	//transformationMatrix.transpose();
	m_mesh->draw(transformationMatrix);
	EGE_VERBOSE("draw ... " << transformationMatrix);
}

const vec3& ege::ElementBase::getPosition() {
	return m_position;
}

void ege::ElementBase::setPosition(const vec3& _pos) {
	m_position = _pos;
}

