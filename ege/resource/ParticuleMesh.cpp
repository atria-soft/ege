/**
 * @author Edouard DUPIN
 * 
 * @copyright 2011, Edouard DUPIN, all right reserved
 * 
 * @license BSD v3 (see license file)
 */

#include <ege/debug.h>
#include <ege/resource/ParticuleMesh.h>
#include <ewol/resources/ResourceManager.h>

ege::resource::ParticuleMesh::ParticuleMesh(const etk::UString& _fileName, const etk::UString& _shaderName) : 
	ewol::Mesh(_fileName, _shaderName)
{
	if (m_GLprogram != NULL) {
		m_GLMainColor = m_GLprogram->getUniform("EW_mainColor");
	}
}

ege::resource::ParticuleMesh::~ParticuleMesh(void)
{
	
}

void ege::resource::ParticuleMesh::draw(mat4& _positionMatrix,
                                        const etk::Color<float>& _mainColor,
                                        bool _enableDepthTest,
                                        bool _enableDepthUpdate)
{
	if (m_GLprogram == NULL) {
		EWOL_ERROR("No shader ...");
		return;
	}
	//EWOL_DEBUG(m_name << "  " << m_light);
	if (_enableDepthTest == true) {
		ewol::openGL::enable(ewol::openGL::FLAG_DEPTH_TEST);
		if (false == _enableDepthUpdate) {
			glDepthMask(GL_FALSE);
		}
	} else {
		ewol::openGL::disable(ewol::openGL::FLAG_DEPTH_TEST);
	}
	//EWOL_DEBUG("    display " << m_coord.size() << " elements" );
	m_GLprogram->use();
	// set Matrix : translation/positionMatrix
	mat4 projMatrix = ewol::openGL::getMatrix();
	mat4 camMatrix = ewol::openGL::getCameraMatrix();
	mat4 tmpMatrix = projMatrix * camMatrix;
	m_GLprogram->uniformMatrix4fv(m_GLMatrix, 1, tmpMatrix.m_mat);
	m_GLprogram->uniformMatrix4fv(m_GLMatrixPosition, 1, _positionMatrix.m_mat);
	vec4 tmpColor(_mainColor.r(), _mainColor.g(), _mainColor.b(), _mainColor.a());
	m_GLprogram->uniform4(m_GLMainColor, tmpColor);
	// position :
	m_GLprogram->sendAttributePointer(m_GLPosition, 3/*x,y,z*/, m_verticesVBO, MESH_VBO_VERTICES);
	// Texture :
	m_GLprogram->sendAttributePointer(m_GLtexture, 2/*u,v*/, m_verticesVBO, MESH_VBO_TEXTURE);
	// position :
	m_GLprogram->sendAttributePointer(m_GLNormal, 3/*x,y,z*/, m_verticesVBO, MESH_VBO_VERTICES_NORMAL);
	// draw lights :
	m_light.draw(m_GLprogram);
	#ifdef DISPLAY_NB_VERTEX_DISPLAYED
	int32_t nbElementDrawTheoric = 0;
	int32_t nbElementDraw = 0;
	#endif
	for (esize_t kkk=0; kkk<m_listFaces.size(); kkk++) {
		if (false == m_materials.exist(m_listFaces.getKey(kkk))) {
			EWOL_WARNING("missing materials : '" << m_listFaces.getKey(kkk) << "'");
			continue;
		}
		m_materials[m_listFaces.getKey(kkk)]->draw(m_GLprogram, m_GLMaterial);
		if (m_checkNormal == false) {
			ewol::openGL::drawElements(GL_TRIANGLES, m_listFaces.getValue(kkk).m_index);
			#ifdef DISPLAY_NB_VERTEX_DISPLAYED
				nbElementDraw += m_listFaces.getValue(kkk).m_index.size();
				nbElementDrawTheoric += m_listFaces.getValue(kkk).m_index.size();
			#endif
		} else {
			mat4 mattttt = (projMatrix * camMatrix) * _positionMatrix;
			mattttt.m_mat[3] = 0;
			mattttt.m_mat[7] = 0;
			mattttt.m_mat[11] = 0;
			//vec3 cameraNormal = vec3(-mattttt.m_mat[2], -mattttt.m_mat[6], -mattttt.m_mat[10]);
			vec3 cameraNormal = vec3(0,0,-1);
			cameraNormal.normalized();
			// remove face that is notin the view ...
			etk::Vector<uint32_t> tmpIndexResult;
			etk::Vector<ewol::Face>& tmppFaces = m_listFaces.getValue(kkk).m_faces;
			etk::Vector<uint32_t>& tmppIndex = m_listFaces.getValue(kkk).m_index;
			if (normalModeFace == m_normalMode) {
				for(int32_t iii=0; iii<tmppFaces.size() ; ++iii) {
					if(btDot(mattttt * m_listFacesNormal[tmppFaces[iii].m_normal[0]], cameraNormal) >= 0.0f) {
						tmpIndexResult.pushBack(iii*3);
						tmpIndexResult.pushBack(iii*3+1);
						tmpIndexResult.pushBack(iii*3+2);
					}
				}
			} else {
				for(int32_t iii=0; iii<tmppFaces.size() ; ++iii) {
					if(    (btDot(mattttt * m_listVertexNormal[tmppFaces[iii].m_normal[0]], cameraNormal) >= -0.2f)
					    || (btDot(mattttt * m_listVertexNormal[tmppFaces[iii].m_normal[1]], cameraNormal) >= -0.2f)
					    || (btDot(mattttt * m_listVertexNormal[tmppFaces[iii].m_normal[2]], cameraNormal) >= -0.2f) ) {
						tmpIndexResult.pushBack(iii*3);
						tmpIndexResult.pushBack(iii*3+1);
						tmpIndexResult.pushBack(iii*3+2);
					}
				}
			}
			ewol::openGL::drawElements(GL_TRIANGLES, tmpIndexResult);
			#ifdef DISPLAY_NB_VERTEX_DISPLAYED
				nbElementDraw += tmpIndexResult.size();
				nbElementDrawTheoric += m_listFaces.getValue(kkk).m_index.size();
			#endif
		}
	}
	#ifdef DISPLAY_NB_VERTEX_DISPLAYED
		EWOL_DEBUG(((float)nbElementDraw/(float)nbElementDrawTheoric*100.0f) << "% Request draw : " << m_listFaces.size() << ":" << nbElementDraw << "/" << nbElementDrawTheoric << " elements [" << m_name << "]");
	#endif
	m_GLprogram->unUse();
	if (_enableDepthTest == true){
		if (false == _enableDepthUpdate) {
			glDepthMask(GL_TRUE);
		}
		ewol::openGL::disable(ewol::openGL::FLAG_DEPTH_TEST);
	}
	glBindBuffer(GL_ARRAY_BUFFER,0);
}

ege::resource::ParticuleMesh* ege::resource::ParticuleMesh::keep(const etk::UString& _meshName, const etk::UString& _shaderName)
{
	ege::resource::ParticuleMesh* object = static_cast<ege::resource::ParticuleMesh*>(getManager().localKeep(_meshName));
	if (NULL != object) {
		return object;
	}
	object = new ege::resource::ParticuleMesh(_meshName, _shaderName);
	if (object == NULL) {
		EWOL_ERROR("allocation error of a resource : ??Mesh??" << _meshName);
		return NULL;
	}
	getManager().localAdd(object);
	return object;
}

void ege::resource::ParticuleMesh::release(ege::resource::ParticuleMesh*& _object)
{
	if (_object == NULL) {
		return;
	}
	ewol::Resource* object2 = static_cast<ewol::Resource*>(_object);
	getManager().release(object2);
	_object = NULL;
}