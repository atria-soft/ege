/**
 * @author Edouard DUPIN
 * 
 * @copyright 2011, Edouard DUPIN, all right reserved
 * 
 * @license BSD v3 (see license file)
 */

#include <etk/types.h>
#include <ege/debug.h>
#include <ege/elements/Element.h>
#include <ege/Environement.h>
#include <BulletDynamics/Dynamics/btRigidBody.h>
#include <LinearMath/btDefaultMotionState.h>
#include <BulletDynamics/Dynamics/btDynamicsWorld.h>
#include <BulletCollision/CollisionShapes/btCollisionShape.h>
#include <LinearMath/btIDebugDraw.h>
#include <btBulletCollisionCommon.h>
#include <BulletCollision/CollisionShapes/btConvexPolyhedron.h>
#include <BulletCollision/CollisionShapes/btShapeHull.h>
#include <LinearMath/btTransformUtil.h>
#include <LinearMath/btIDebugDraw.h>
#include <btBulletDynamicsCommon.h>
#include <BulletCollision/CollisionDispatch/btCollisionObject.h>

#include <ege/CollisionShapeCreator.h>

#undef __class__
#define __class__	"Element"

const std::string& ege::Element::getType() const {
	static const std::string nameType("----");
	return nameType;
}


ege::Element::Element(const std::shared_ptr<ege::Environement>& _env) :
  m_env(_env),
  m_uID(0),
  m_mesh(),
  m_life(100),
  m_lifeMax(100),
  m_group(0),
  m_fixe(true),
  m_radius(0) {
	static uint32_t unique=0;
	m_uID = unique;
	EGE_DEBUG("Create element : uId=" << m_uID);
	m_debugText.setFontSize(12);
	unique++;
}

ege::Element::~Element() {
	EGE_DEBUG("Destroy element : uId=" << m_uID);
}

bool ege::Element::loadMesh(const std::string& _meshFileName) {
	std::shared_ptr<ege::resource::Mesh> tmpMesh = ege::resource::Mesh::create(_meshFileName);
	if(nullptr == tmpMesh) {
		EGE_ERROR("can not load the resources : " << _meshFileName);
		return false;
	}
	return setMesh(tmpMesh);
}

bool ege::Element::setMesh(const std::shared_ptr<ege::resource::Mesh>& _mesh) {
	if (nullptr!=m_mesh) {
		m_mesh.reset();
	}
	m_mesh = _mesh;
	// auto load the shape :
	if (m_mesh == nullptr) {
		return true;
	}
	return true;
}


float ege::Element::getLifeRatio() {
	if (0 >= m_life) {
		return 0;
	}
	return m_life/m_lifeMax;
}

void ege::Element::setFireOn(int32_t _groupIdSource, int32_t _type, float _power, const vec3& _center) {
	float previousLife = m_life;
	m_life += _power;
	m_life = std::avg(0.0f, m_life, m_lifeMax);
	if (m_life <= 0) {
		EGE_DEBUG("[" << getUID() << "] element is killed ..." << getType());
	}
	if (m_life!=previousLife) {
		onLifeChange();
	}
}

const vec3& ege::Element::getPosition() {
	// this is to prevent error like segmentation fault ...
	static vec3 emptyPosition(-1000000,-1000000,-1000000);
	return emptyPosition;
};

static void drawSphere(const std::shared_ptr<ewol::resource::Colored3DObject>& _draw,
                       btScalar _radius,
                       int _lats,
                       int _longs,
                       mat4& _transformationMatrix,
                       etk::Color<float>& _tmpColor) {
	int i, j;
	std::vector<vec3> EwolVertices;
	for(i = 0; i <= _lats; i++) {
		btScalar lat0 = SIMD_PI * (-btScalar(0.5) + (btScalar) (i - 1) / _lats);
		btScalar z0  = _radius*sin(lat0);
		btScalar zr0 = _radius*cos(lat0);
		
		btScalar lat1 = SIMD_PI * (-btScalar(0.5) + (btScalar) i / _lats);
		btScalar z1 = _radius*sin(lat1);
		btScalar zr1 = _radius*cos(lat1);
		
		//glBegin(GL_QUAD_STRIP);
		for(j = 0; j < _longs; j++) {
			btScalar lng = 2 * SIMD_PI * (btScalar) (j - 1) / _longs;
			btScalar x = cos(lng);
			btScalar y = sin(lng);
			vec3 v1 = vec3(x * zr1, y * zr1, z1);
			vec3 v4 = vec3(x * zr0, y * zr0, z0);
			
			lng = 2 * SIMD_PI * (btScalar) (j) / _longs;
			x = cos(lng);
			y = sin(lng);
			vec3 v2 = vec3(x * zr1, y * zr1, z1);
			vec3 v3 = vec3(x * zr0, y * zr0, z0);
			
			EwolVertices.push_back(v1);
			EwolVertices.push_back(v2);
			EwolVertices.push_back(v3);
			
			EwolVertices.push_back(v1);
			EwolVertices.push_back(v3);
			EwolVertices.push_back(v4);
		}
	}
	_draw->draw(EwolVertices, _tmpColor, _transformationMatrix);
}

const float lifeBorder = 0.1f;
const float lifeHeight = 0.3f;
const float lifeWidth = 2.0f;
const float lifeYPos = 1.7f;

void ege::Element::drawLife(const std::shared_ptr<ewol::resource::Colored3DObject>& _draw, const ege::Camera& _camera) {
	if (nullptr == _draw) {
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
	std::vector<vec3> localVertices;
	localVertices.push_back(vec3(-lifeWidth/2.0-lifeBorder,lifeYPos           -lifeBorder,0));
	localVertices.push_back(vec3(-lifeWidth/2.0-lifeBorder,lifeYPos+lifeHeight+lifeBorder,0));
	localVertices.push_back(vec3( lifeWidth/2.0+lifeBorder,lifeYPos+lifeHeight+lifeBorder,0));
	localVertices.push_back(vec3(-lifeWidth/2.0-lifeBorder,lifeYPos           -lifeBorder,0));
	localVertices.push_back(vec3( lifeWidth/2.0+lifeBorder,lifeYPos+lifeHeight+lifeBorder,0));
	localVertices.push_back(vec3( lifeWidth/2.0+lifeBorder,lifeYPos           -lifeBorder,0));
	etk::Color<float> myColor(0x0000FF99);
	_draw->draw(localVertices, myColor, transformationMatrix, false, false);
	localVertices.clear();
	/** Bounding box  == > model shape **/
	localVertices.push_back(vec3(-lifeWidth/2.0                ,lifeYPos,0));
	localVertices.push_back(vec3(-lifeWidth/2.0                ,lifeYPos + lifeHeight,0));
	localVertices.push_back(vec3(-lifeWidth/2.0+lifeWidth*ratio,lifeYPos + lifeHeight,0));
	localVertices.push_back(vec3(-lifeWidth/2.0                ,lifeYPos,0));
	localVertices.push_back(vec3(-lifeWidth/2.0+lifeWidth*ratio,lifeYPos + lifeHeight,0));
	localVertices.push_back(vec3(-lifeWidth/2.0+lifeWidth*ratio,lifeYPos,0));
	myColor =0x00FF00FF;
	if (ratio < 0.2f) {
		myColor = 0xFF0000FF;
	} else if (ratio < 0.4f) {
		myColor = 0xDA7B00FF;
	}
	_draw->draw(localVertices, myColor, transformationMatrix, false, false);
	#endif
}

void ege::Element::drawDebug(const std::shared_ptr<ewol::resource::Colored3DObject>& _draw, const ege::Camera& _camera) {
	m_debugText.clear();
	m_debugText.setColor(etk::Color<>(0x00, 0xFF, 0x00, 0xFF));
	m_debugText.setPos(vec3(-20,32,0));
	m_debugText.print(getType());
	m_debugText.setPos(vec3(-20,20,0));
	m_debugText.print("life=("+etk::to_string(getLifeRatio()));
	//m_debugText.print(std::string("Axe=(")+std::string(m_tmpAxe.x())+std::string(",")+etk::UString(m_tmpAxe.y())+etk::UString(",")+etk::UString(m_tmpAxe.z())+etk::UString(")"));
	/*
	m_debugText.draw(   etk::matTranslate(getPosition())
	                  * etk::matRotate(vec3(0,0,1),_camera.getAngleZ())
	                  * etk::matRotate(vec3(1,0,0),(M_PI/2.0f-_camera.getAngleTeta()))
	                  * etk::matScale(vec3(0.05,0.05,0.05)));
	*/
}



