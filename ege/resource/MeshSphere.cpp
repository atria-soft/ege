/** @file
 * @author Edouard DUPIN
 * @copyright 2011, Edouard DUPIN, all right reserved
 * @license MPL v2.0 (see license file)
 */

#include <ege/debug.hpp>
#include <ege/resource/Mesh.hpp>

ememory::SharedPtr<ege::resource::Mesh> ege::resource::Mesh::createSphere(float _radius, const etk::String& _materialName, const etk::Color<float>& _color, int32_t _lats, int32_t _longs) {
	EGE_VERBOSE(" create a sphere _size=" << _radius << " _materialName=" << _materialName << " _color=" << _color);
	ememory::SharedPtr<ege::resource::Mesh> out = ege::resource::Mesh::create("---", "DATA:///color3.prog");
	if (out != null) {
		ememory::SharedPtr<ege::Material> material = ememory::makeShared<ege::Material>();
		// set the element material properties :
		material->setAmbientFactor(vec4(1,1,1,1));
		material->setDiffuseFactor(vec4(0,0,0,1));
		material->setSpecularFactor(vec4(0,0,0,1));
		material->setShininess(1);
		material->setRenderMode(gale::openGL::renderMode::triangle);
		out->addMaterial(_materialName, material);
		
		out->addFaceIndexing(_materialName);
		for(int32_t iii=0; iii<=_lats; ++iii) {
			float lat0 = M_PI * (-0.5f + float(iii - 1) / _lats);
			float z0  = _radius*sin(lat0);
			float zr0 = _radius*cos(lat0);
			
			float lat1 = M_PI * (-0.5f + float(iii) / _lats);
			float z1 = _radius*sin(lat1);
			float zr1 = _radius*cos(lat1);
			
			for(int32_t jjj=0; jjj<_longs; ++jjj) {
				float lng = 2.0f * M_PI * float(jjj - 1) / _longs;
				float x = cos(lng);
				float y = sin(lng);
				vec3 v1 = vec3(x * zr1, y * zr1, z1);
				vec3 v4 = vec3(x * zr0, y * zr0, z0);
				
				lng = 2 * M_PI * float(jjj) / _longs;
				x = cos(lng);
				y = sin(lng);
				vec3 v2 = vec3(x * zr1, y * zr1, z1);
				vec3 v3 = vec3(x * zr0, y * zr0, z0);
				
				out->addTriangle(_materialName, v1, v3, v2, _color);
				out->addTriangle(_materialName, v1, v4, v3, _color);
			}
		}
		out->setNormalMode(ege::resource::Mesh::normalMode::face);
		out->calculateNormaleFace(_materialName);
		// generate the VBO
		out->generateVBO();
	} else {
		EGE_ERROR("can not create the basic mesh interface");
	}
	return out;
}