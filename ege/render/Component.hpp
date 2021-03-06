/** @file
 * @author Edouard DUPIN
 * @copyright 2011, Edouard DUPIN, all right reserved
 * @license MPL v2.0 (see license file)
 */
#pragma once

#include <ege/debug.hpp>

#include <ege/Component.hpp>
#include <etk/math/Transform3D.hpp>
#include <ege/resource/Mesh.hpp>

namespace ege {
	namespace render {
		class Component : public ege::Component {
			protected:
				etk::Transform3D m_transform; // keep it from the physic or the position component...
			public:
				/**
				 * @brief basic constructor
				 */
				Component();
				/**
				 * @brief contruct with a mesh filename
				 * @param[in] _fileName filename of the Mesh.
				 */
				Component(const etk::String& _fileName);
				/**
				 * @brief contruct with a prebuild mesh
				 * @param[in] _mesh The mesh pointer.
				 */
				Component(ememory::SharedPtr<ege::resource::Mesh> _mesh);
			protected:
				ememory::SharedPtr<ege::resource::Mesh> m_mesh; //!< Mesh of the Element (can be null)
			public:
				/**
				 * @brief Select a mesh with a specific name.
				 * @param[in] _fileName filename of the Mesh.
				 * @note Automaticly load the shape if it is specify in the mesh file
				 * @return true if no error occured
				 */
				virtual bool loadMesh(const etk::String& _fileName);
				/**
				 * @brief set the the Mesh properties.
				 * @param[in] _mesh The mesh pointer. (null to force the mesh remove ...)
				 * @note : this remove the shape and the mesh properties.
				 * @return true if no error occured
				 */
				virtual bool setMesh(ememory::SharedPtr<ege::resource::Mesh> _mesh);
				/**
				 * @brief get a pointer on the Mesh file.
				 * @return the mesh pointer.
				 */
				inline ememory::SharedPtr<ege::resource::Mesh> getMesh() {
					return m_mesh;
				};
				
				const etk::Transform3D& getTransform() {
					return m_transform;
				}
				/**
				 * @brief draw the curent element (can have multiple display)
				 * @param[in] pass Id of the current pass : [0..?]
				 */
				virtual void draw(int32_t _pass=0);
				/**
				 * @brief Debug display of the current element normal face
				 * @param[in,out] _draw Basic system to draw the debug shape and informations
				 */
				void drawNormalDebug(ememory::SharedPtr<ewol::resource::Colored3DObject> _draw);
			public:
				const etk::String& getType() const override;
				void addFriendComponent(const ememory::SharedPtr<ege::Component>& _component) override;
			private:
				void onSignalPositionChange(const etk::Transform3D& _transform);
		};
	}
}