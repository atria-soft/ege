/**
 * @author Edouard DUPIN
 * 
 * @copyright 2011, Edouard DUPIN, all right reserved
 * 
 * @license BSD v3 (see license file)
 */

#include <ege/debug.h>
#include <ege/resource/Mesh.h>

std::shared_ptr<ege::resource::Mesh> ege::resource::Mesh::createCube(float _size, const std::string& _materialName, const etk::Color<float>& _color) {
	EGE_ERROR(" create a cube _size=" << _size << " _materialName=" << _materialName << " _color=" << _color);
	std::shared_ptr<ege::resource::Mesh> out = ege::resource::Mesh::create("---", "DATA:color3.prog");
	if (out != nullptr) {
		std::shared_ptr<ege::Material> material = std::make_shared<ege::Material>();
		// set the element material properties :
		material->setAmbientFactor(vec4(1,1,1,1));
		material->setDiffuseFactor(vec4(0,0,0,1));
		material->setSpecularFactor(vec4(0,0,0,1));
		material->setShininess(1);
		material->setRenderMode(gale::openGL::render_triangle);
		out->addMaterial(_materialName, material);
		
		out->addFaceIndexing(_materialName);
		
		out->addQuad(_materialName, vec3(-1,-1,-1)*_size, vec3(-1, 1,-1)*_size, vec3( 1, 1,-1)*_size, vec3( 1,-1,-1)*_size, _color);
		out->addQuad(_materialName, vec3(-1,-1, 1)*_size, vec3(-1, 1, 1)*_size, vec3( 1, 1, 1)*_size, vec3( 1,-1, 1)*_size, _color);
		out->addQuad(_materialName, vec3(-1,-1,-1)*_size, vec3(-1,-1, 1)*_size, vec3(-1, 1, 1)*_size, vec3(-1, 1,-1)*_size, _color);
		out->addQuad(_materialName, vec3( 1,-1,-1)*_size, vec3( 1,-1, 1)*_size, vec3( 1, 1, 1)*_size, vec3( 1, 1,-1)*_size, _color);
		out->addQuad(_materialName, vec3(-1,-1,-1)*_size, vec3(-1,-1, 1)*_size, vec3( 1,-1, 1)*_size, vec3( 1,-1,-1)*_size, _color);
		out->addQuad(_materialName, vec3(-1, 1,-1)*_size, vec3(-1, 1, 1)*_size, vec3( 1, 1, 1)*_size, vec3( 1, 1,-1)*_size, _color);
		// generate the VBO
		out->generateVBO();
	} else {
		EGE_ERROR("can not create the basic mesh interface");
	}
	return out;
}