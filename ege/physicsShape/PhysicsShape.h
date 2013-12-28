/**
 * @author Edouard DUPIN
 * 
 * @copyright 2011, Edouard DUPIN, all right reserved
 * 
 * @license BSD v3 (see license file)
 */

#ifndef __EGE_PHYSICS_SHAPE_H__
#define __EGE_PHYSICS_SHAPE_H__


#include <etk/types.h>
#include <etk/types.h>
#include <etk/math/Vector4D.h>
#include <etk/math/Vector3D.h>


namespace ege {
	class PhysicsBox;
	class PhysicsCylinder;
	class PhysicsCapsule;
	class PhysicsCone;
	class PhysicsConvexHull;
	class PhysicsSphere;
	
	class PhysicsShape {
		public:
			static ege::PhysicsShape* create(const std::string& _name);
		public:
			enum type {
				unknow,
				box,
				capsule,
				cone,
				convexHull,
				cylinder,
				sphere
			};
		public:
			PhysicsShape(void) :
			  m_quaternion(1,0,0,0),
			  m_origin(0,0,0) {
				
			};
			virtual ~PhysicsShape(void) {
				
			};
		public:
			virtual enum ege::PhysicsShape::type getType(void) {
				return ege::PhysicsShape::unknow;
			};
			
		public:
			virtual bool parse(const char* _line);
			virtual void display(void) {
				
			};
		private:
			vec4 m_quaternion;
		public:
			const vec4& getQuaternion(void) const {
				return m_quaternion;
			};
		private:
			vec3 m_origin;
		public:
			const vec3& getOrigin(void) const {
				return m_origin;
			};
		public:
			bool isBox(void) {
				return getType() == ege::PhysicsShape::box;
			};
			bool isCylinder(void) {
				return getType() == ege::PhysicsShape::cylinder;
			};
			bool isCapsule(void) {
				return getType() == ege::PhysicsShape::capsule;
			};
			bool isCone(void) {
				return getType() == ege::PhysicsShape::cone;
			};
			bool isConvexHull(void) {
				return getType() == ege::PhysicsShape::convexHull;
			};
			bool isSphere(void) {
				return getType() == ege::PhysicsShape::sphere;
			};
			
			virtual const ege::PhysicsBox* toBox(void) const {
				return NULL;
			};
			virtual ege::PhysicsBox* toBox(void) {
				return NULL;
			};
			
			virtual const ege::PhysicsCylinder* toCylinder(void) const {
				return NULL;
			};
			virtual ege::PhysicsCylinder* toCylinder(void) {
				return NULL;
			};
			
			virtual const ege::PhysicsCapsule* toCapsule(void) const {
				return NULL;
			};
			virtual ege::PhysicsCapsule* toCapsule(void) {
				return NULL;
			};
			
			virtual const ege::PhysicsCone* toCone(void) const {
				return NULL;
			};
			virtual ege::PhysicsCone* toCone(void) {
				return NULL;
			};
			
			virtual const ege::PhysicsConvexHull* toConvexHull(void) const {
				return NULL;
			};
			virtual ege::PhysicsConvexHull* toConvexHull(void) {
				return NULL;
			};
			
			virtual const ege::PhysicsSphere* toSphere(void) const {
				return NULL;
			};
			virtual ege::PhysicsSphere* toSphere(void) {
				return NULL;
			};
	};
};


#endif
