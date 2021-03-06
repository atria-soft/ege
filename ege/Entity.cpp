/** @file
 * @author Edouard DUPIN
 * @copyright 2011, Edouard DUPIN, all right reserved
 * @license MPL v2.0 (see license file)
 */

#include <etk/types.hpp>
#include <ege/debug.hpp>
#include <ege/Entity.hpp>
#include <ege/Environement.hpp>


const etk::String& ege::Entity::getType() const {
	static const etk::String nameType("----");
	return nameType;
}


ege::Entity::Entity(const ememory::SharedPtr<ege::Environement>& _env) :
  m_env(_env),
  m_uID(0),
  m_life(100),
  m_lifeMax(100),
  m_group(0),
  m_radius(0) {
	static uint32_t unique=0;
	m_uID = unique;
	EGE_DEBUG("Create Entity: uId=" << m_uID);
	//m_debugText.setFontSize(12);
	unique++;
}

ege::Entity::~Entity() {
	EGE_DEBUG("Destroy Entity: uId=" << m_uID);
}

void ege::Entity::addComponent(const ememory::SharedPtr<ege::Component>& _ref) {
	if (_ref == null) {
		EGE_ERROR("try to add an empty component");
		return;
	}
	EGE_PRINT("Entity: Add New component ... [START]");
	// Componenet to remove if it have the same type of the previous Component:
	ememory::SharedPtr<ege::Component> componentRemoved;
	int32_t findId = -1;
	// check if not exist
	for (size_t iii=0; iii<m_component.size(); ++iii) {
		if (m_component[iii] == null) {
			continue;
		}
		if (m_component[iii]->getType() == _ref->getType()) {
			componentRemoved = m_component[iii];
			m_component[iii] = _ref;
			findId = iii;
			break;
		}
	}
	// try to add in an empty slot
	if (findId == -1) {
		for (size_t iii=0; iii<m_component.size(); ++iii) {
			if (m_component[iii] != null) {
				continue;
			}
			findId = iii;
			m_component[iii] = _ref;
			break;
		}
	}
	// add it at the end ...
	if (findId == -1) {
		findId = m_component.size();
		m_component.pushBack(_ref);
	}
	for (size_t iii=0; iii<m_component.size(); ++iii) {
		if (m_component[iii] == null) {
			continue;
		}
		if (componentRemoved != null) {
			EGE_PRINT("Entity:     ==> remove previous component");
			m_env->engineComponentRemove(componentRemoved);
			m_component[iii]->removeFriendComponent(componentRemoved);
		}
		EGE_PRINT("Entity:     ==> add New component");
		m_env->engineComponentAdd(_ref);
		m_component[iii]->addFriendComponent(_ref);
		break;
	}
	// notify new component of all previously added component:
	componentRemoved = _ref;
	for (size_t iii=0; iii<m_component.size(); ++iii) {
		if (m_component[iii] == null) {
			continue;
		}
		if (m_component[iii] == _ref) {
			continue;
		}
		componentRemoved->addFriendComponent(m_component[iii]);
	}
	EGE_PRINT("Entity: Add New component ... [END]");
}

void ege::Entity::rmComponent(const ememory::SharedPtr<ege::Component>& _ref) {
	if (_ref == null) {
		EGE_ERROR("try to remove an empty component");
		return;
	}
	int32_t findId = -1;
	// check if not exist
	for (size_t iii=0; iii<m_component.size(); ++iii) {
		if (m_component[iii] == null) {
			continue;
		}
		if (m_component[iii] == _ref) {
			m_component[iii] = null;
			findId = iii;
			break;
		}
	}
	if (findId == -1) {
		EGE_ERROR("try to remove an unexisting component");
		return;
	}
	for (size_t iii=0; iii<m_component.size(); ++iii) {
		if (m_component[iii] == null) {
			continue;
		}
		m_env->engineComponentRemove(_ref);
		m_component[iii]->removeFriendComponent(_ref);
	}
}

void ege::Entity::rmComponent(const etk::String& _type) {
	int32_t findId = -1;
	ememory::SharedPtr<ege::Component> componentRemoved;
	// check if not exist
	for (size_t iii=0; iii<m_component.size(); ++iii) {
		if (m_component[iii] == null) {
			continue;
		}
		if (m_component[iii]->getType() == _type) {
			componentRemoved = m_component[iii];
			m_component[iii] = null;
			findId = iii;
			break;
		}
	}
	if (findId == -1) {
		EGE_ERROR("try to remove an unexisting component type : '" << _type << "'");
		return;
	}
	for (size_t iii=0; iii<m_component.size(); ++iii) {
		if (m_component[iii] == null) {
			continue;
		}
		m_env->engineComponentRemove(componentRemoved);
		m_component[iii]->removeFriendComponent(componentRemoved);
	}
}

ememory::SharedPtr<ege::Component> ege::Entity::getComponent(const etk::String& _type) {
	// check if not exist
	for (size_t iii=0; iii<m_component.size(); ++iii) {
		if (m_component[iii] == null) {
			continue;
		}
		if (m_component[iii]->getType() == _type) {
			return m_component[iii];
		}
	}
	return null;
}




bool ege::Entity::init() {
	EGE_WARNING("init() not implemented: uId=" << m_uID);
	return false;
}
bool ege::Entity::initString(const etk::String& _description) {
	EGE_WARNING("String Init not implemented: uId=" << m_uID);
	return false;
}
bool ege::Entity::initXML(const exml::Node& _node) {
	EGE_WARNING("xml Init not implemented: uId=" << m_uID);
	return false;
}
bool ege::Entity::initJSON(const ejson::Value& _value) {
	EGE_WARNING("JSON Init not implemented: uId=" << m_uID);
	return false;
}
bool ege::Entity::initVoid(void* _value) {
	EGE_WARNING("joid* Init not implemented: uId=" << m_uID);
	return false;
}
bool ege::Entity::unInit() {
	return true;
}



float ege::Entity::getLifeRatio() {
	if (0 >= m_life) {
		return 0;
	}
	return m_life/m_lifeMax;
}

void ege::Entity::setFireOn(int32_t _groupIdSource, int32_t _type, float _power, const vec3& _center) {
	float previousLife = m_life;
	m_life += _power;
	m_life = etk::avg(0.0f, m_life, m_lifeMax);
	if (m_life <= 0) {
		EGE_DEBUG("[" << getUID() << "] Entity is killed ..." << getType());
	}
	if (m_life != previousLife) {
		onLifeChange();
	}
}

const float lifeBorder = 0.1f;
const float lifeHeight = 0.3f;
const float lifeWidth = 2.0f;
const float lifeYPos = 1.7f;

#if 0
void ege::Entity::drawLife(ememory::SharedPtr<ewol::resource::Colored3DObject> _draw, ememory::SharedPtr<ege::Camera> _camera) {
	if (_draw == null) {
		return;
	}
	float ratio = getLifeRatio();
	if (ratio == 1.0f) {
		return;
	}
	#if 0
	mat4 transformationMatrix =   etk::matTranslate(getPosition())
	                            * etk::matRotate(vec3(0,0,1),_camera.getAngleZ())
	                            * etk::matRotate(vec3(1,0,0),(M_PI/2.0f-_camera.getAngleTeta()));
	etk::Vector<vec3> localVertices;
	localVertices.pushBack(vec3(-lifeWidth/2.0-lifeBorder,lifeYPos           -lifeBorder,0));
	localVertices.pushBack(vec3(-lifeWidth/2.0-lifeBorder,lifeYPos+lifeHeight+lifeBorder,0));
	localVertices.pushBack(vec3( lifeWidth/2.0+lifeBorder,lifeYPos+lifeHeight+lifeBorder,0));
	localVertices.pushBack(vec3(-lifeWidth/2.0-lifeBorder,lifeYPos           -lifeBorder,0));
	localVertices.pushBack(vec3( lifeWidth/2.0+lifeBorder,lifeYPos+lifeHeight+lifeBorder,0));
	localVertices.pushBack(vec3( lifeWidth/2.0+lifeBorder,lifeYPos           -lifeBorder,0));
	etk::Color<float> myColor(0x0000FF99);
	_draw->draw(localVertices, myColor, transformationMatrix, false, false);
	localVertices.clear();
	/** Bounding box  == > model shape **/
	localVertices.pushBack(vec3(-lifeWidth/2.0                ,lifeYPos,0));
	localVertices.pushBack(vec3(-lifeWidth/2.0                ,lifeYPos + lifeHeight,0));
	localVertices.pushBack(vec3(-lifeWidth/2.0+lifeWidth*ratio,lifeYPos + lifeHeight,0));
	localVertices.pushBack(vec3(-lifeWidth/2.0                ,lifeYPos,0));
	localVertices.pushBack(vec3(-lifeWidth/2.0+lifeWidth*ratio,lifeYPos + lifeHeight,0));
	localVertices.pushBack(vec3(-lifeWidth/2.0+lifeWidth*ratio,lifeYPos,0));
	myColor =0x00FF00FF;
	if (ratio < 0.2f) {
		myColor = 0xFF0000FF;
	} else if (ratio < 0.4f) {
		myColor = 0xDA7B00FF;
	}
	_draw->draw(localVertices, myColor, transformationMatrix, false, false);
	#endif
}
#endif

void ege::Entity::drawDebug(ememory::SharedPtr<ewol::resource::Colored3DObject> _draw, ememory::SharedPtr<ege::Camera> _camera) {
	/*
	m_debugText.clear();
	m_debugText.setColor(etk::Color<>(0x00, 0xFF, 0x00, 0xFF));
	m_debugText.setPos(vec3(-20,32,0));
	m_debugText.print(getType());
	m_debugText.setPos(vec3(-20,20,0));
	m_debugText.print("life=("+etk::toString(getLifeRatio()));
	*/
	//m_debugText.print(etk::String("Axe=(")+etk::String(m_tmpAxe.x())+etk::String(",")+etk::UString(m_tmpAxe.y())+etk::UString(",")+etk::UString(m_tmpAxe.z())+etk::UString(")"));
	/*
	// TODO : Keep this it can be usefull to print something in direction of the camera ...
	m_debugText.draw(   etk::matTranslate(getPosition())
	                  * etk::matRotate(vec3(0,0,1),_camera.getAngleZ())
	                  * etk::matRotate(vec3(1,0,0),(M_PI/2.0f-_camera.getAngleTeta()))
	                  * etk::matScale(vec3(0.05,0.05,0.05)));
	*/
}




