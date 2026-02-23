package org.atriasoft.ege.tools;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.loader3d.resources.ResourceStaticColoredMesh;

class MeshData {
	public List<Vector3f> vertices = new ArrayList<>();
	public List<Color> colors = new ArrayList<>();
	public List<Integer> indices = new ArrayList<>();
	
	public void addLine(final Vector3f pos1, final Vector3f pos2, final Color color) {
		this.vertices.add(pos1);
		this.vertices.add(pos2);
		this.colors.add(color);
		this.colors.add(color);
		this.indices.add(this.vertices.size() - 2);
		this.indices.add(this.vertices.size() - 1);
	}
	
	public void addLines(final List<Vector3f> pos, final Color color) {
		for (int iii = 1; iii < pos.size(); iii++) {
			addLine(pos.get(iii - 1), pos.get(iii), color);
		}
	}
	
	public float[] getListOfColors() {
		float[] out = new float[this.colors.size() * 4];
		for (int iii = 0; iii < this.colors.size(); iii++) {
			out[iii * 4] = this.colors.get(iii).r();
			out[iii * 4 + 1] = this.colors.get(iii).g();
			out[iii * 4 + 2] = this.colors.get(iii).b();
			out[iii * 4 + 3] = this.colors.get(iii).a();
		}
		return out;
	}
	
	public int[] getListOfIndices() {
		int[] out = new int[this.indices.size()];
		for (int iii = 0; iii < this.indices.size(); iii++) {
			out[iii] = this.indices.get(iii);
		}
		return out;
	}
	
	public float[] getListOfVertices() {
		float[] out = new float[this.vertices.size() * 3];
		for (int iii = 0; iii < this.vertices.size(); iii++) {
			out[iii * 3] = this.vertices.get(iii).x();
			out[iii * 3 + 1] = this.vertices.get(iii).y();
			out[iii * 3 + 2] = this.vertices.get(iii).z();
		}
		return out;
	}
}

public class MeshGenerator {
	public static ResourceStaticColoredMesh createGrid(final int lineCount) {
		final MeshData meshData = new MeshData();
		final Color colorRed = new Color(1, 0, 0, 1);
		final Color colorGreen = new Color(0, 1, 0, 1);
		final Color colorBlue = new Color(0, 0, 1, 1);
		final Color colorGray = new Color(0.5f, 0.5f, 0.5f, 1);
		// create X axis lines (red) — runs along X on the XZ ground plane
		for (int iii = -lineCount; iii <= lineCount; ++iii) {
			if (iii == 0) {
				meshData.addLine(new Vector3f(-lineCount, 0, 0), new Vector3f(lineCount + 1, 0, 0), colorRed);
				// arrow
				meshData.addLine(new Vector3f(lineCount + 1.0f, 0, 0), new Vector3f(lineCount + 0.5f, 0.5f, 0), colorRed);
				meshData.addLine(new Vector3f(lineCount + 1.0f, 0, 0), new Vector3f(lineCount + 0.5f, -0.5f, 0), colorRed);
				meshData.addLine(new Vector3f(lineCount + 1.0f, 0, 0), new Vector3f(lineCount + 0.5f, 0, 0.5f), colorRed);
				meshData.addLine(new Vector3f(lineCount + 1.0f, 0, 0), new Vector3f(lineCount + 0.5f, 0, -0.5f), colorRed);
				// X letter (vertical plane at end of X axis)
				meshData.addLine(new Vector3f(lineCount + 2.0f, 1.0f, -1.0f), new Vector3f(lineCount + 2.0f, -1.0f, 1.0f), colorRed);
				meshData.addLine(new Vector3f(lineCount + 2.0f, -1.0f, -1.0f), new Vector3f(lineCount + 2.0f, 1.0f, 1.0f), colorRed);
			} else {
				// gray grid line along X at Z=iii, Y=0
				meshData.addLine(new Vector3f(-lineCount, 0, iii), new Vector3f(lineCount, 0, iii), colorGray);
			}
		}
		// create Y axis (green) — vertical axis, goes up
		for (int iii = -lineCount; iii <= lineCount; ++iii) {
			if (iii == 0) {
				meshData.addLine(new Vector3f(0, -lineCount, 0), new Vector3f(0, lineCount + 1, 0), colorGreen);
				// arrow
				meshData.addLine(new Vector3f(0, lineCount + 1.0f, 0), new Vector3f(0.5f, lineCount + 0.5f, 0), colorGreen);
				meshData.addLine(new Vector3f(0, lineCount + 1.0f, 0), new Vector3f(-0.5f, lineCount + 0.5f, 0), colorGreen);
				meshData.addLine(new Vector3f(0, lineCount + 1.0f, 0), new Vector3f(0, lineCount + 0.5f, 0.5f), colorGreen);
				meshData.addLine(new Vector3f(0, lineCount + 1.0f, 0), new Vector3f(0, lineCount + 0.5f, -0.5f), colorGreen);
				// Y letter (at top of Y axis)
				meshData.addLine(new Vector3f(0, lineCount + 2.0f, 0), new Vector3f(0.7f, lineCount + 2.0f, 1.0f), colorGreen);
				meshData.addLine(new Vector3f(0, lineCount + 2.0f, 0), new Vector3f(-0.7f, lineCount + 2.0f, 1.0f), colorGreen);
				meshData.addLine(new Vector3f(0, lineCount + 2.0f, 0), new Vector3f(0, lineCount + 2.0f, -1.0f), colorGreen);
			} else {
				List<Vector3f> list = new ArrayList<>();
				list.add(new Vector3f(-1, iii, -1));
				list.add(new Vector3f(1, iii, -1));
				list.add(new Vector3f(1, iii, 1));
				list.add(new Vector3f(-1, iii, 1));
				list.add(new Vector3f(-1, iii, -1));
				meshData.addLines(list, colorGray);
			}
		}
		// create Z axis lines (blue) — runs along Z on the XZ ground plane
		for (int iii = -lineCount; iii <= lineCount; ++iii) {
			if (iii == 0) {
				meshData.addLine(new Vector3f(0, 0, -lineCount), new Vector3f(0, 0, lineCount + 1), colorBlue);
				// arrow
				meshData.addLine(new Vector3f(0, 0, lineCount + 1), new Vector3f(0.5f, 0, lineCount + 0.5f), colorBlue);
				meshData.addLine(new Vector3f(0, 0, lineCount + 1), new Vector3f(-0.5f, 0, lineCount + 0.5f), colorBlue);
				meshData.addLine(new Vector3f(0, 0, lineCount + 1), new Vector3f(0, 0.5f, lineCount + 0.5f), colorBlue);
				meshData.addLine(new Vector3f(0, 0, lineCount + 1), new Vector3f(0, -0.5f, lineCount + 0.5f), colorBlue);
				// Z letter (vertical plane at end of Z axis)
				meshData.addLine(new Vector3f(-1, 1, lineCount + 2.0f), new Vector3f(1, 1, lineCount + 2.0f), colorBlue);
				meshData.addLine(new Vector3f(1, 1, lineCount + 2.0f), new Vector3f(-1, -1, lineCount + 2.0f), colorBlue);
				meshData.addLine(new Vector3f(-1, -1, lineCount + 2.0f), new Vector3f(1, -1, lineCount + 2.0f), colorBlue);
			} else {
				// gray grid line along Z at X=iii, Y=0
				meshData.addLine(new Vector3f(iii, 0, -lineCount), new Vector3f(iii, 0, lineCount), colorGray);
			}
		}
		return ResourceStaticColoredMesh.create(meshData.getListOfVertices(), meshData.getListOfColors(), null, meshData.getListOfIndices(), RenderMode.LINE);
	}
	
	private MeshGenerator() {}
	
	//	public static ResourceMesh createCube(float _size=1.0f,
	//                                          const etk::String& _materialName="basics",
	//                                          Color _color=etk::color::green) {
	//		return createCube(new Vector3f(_size, _size, _size), _materialName, _color);
	//	}
	//	public static ResourceMesh createCube(const new Vector3f& _size=new Vector3f(1.0f, 1.0f, 1.0f),
	//                                          const etk::String& _materialName="basics",
	//                                          Color _color=etk::color::green) {
	//		EGE_VERBOSE(" create a cube _size=" << _size << " _materialName=" << _materialName << " _color=" << _color);
	//		ememory::SharedPtr<ege::resource::Mesh> out = ege::resource::Mesh::create("---", "DATA:///color3.prog");
	//		if (out != null) {
	//			ememory::SharedPtr<ege::Material> material = ememory::makeShared<ege::Material>();
	//			// set the element material properties :
	//			material.setAmbientFactor(vec4(1,1,1,1));
	//			material.setDiffuseFactor(vec4(0,0,0,1));
	//			material.setSpecularFactor(vec4(0,0,0,1));
	//			material.setShininess(1);
	//			material.setRenderMode(gale::openGL::renderMode::triangle);
	//			out.addMaterial(_materialName, material);
	//			
	//			out.addFaceIndexing(_materialName);
	//			
	//			out.addQuad(_materialName, new Vector3f(-1,-1, 1)*_size, new Vector3f(-1,-1,-1)*_size, new Vector3f( 1,-1,-1)*_size, new Vector3f( 1,-1, 1)*_size, _color);
	//			out.addQuad(_materialName, new Vector3f(-1, 1,-1)*_size, new Vector3f(-1, 1, 1)*_size, new Vector3f( 1, 1, 1)*_size, new Vector3f( 1, 1,-1)*_size, _color);
	//			out.addQuad(_materialName, new Vector3f(-1, 1,-1)*_size, new Vector3f(-1,-1,-1)*_size, new Vector3f(-1,-1, 1)*_size, new Vector3f(-1, 1, 1)*_size, _color);
	//			out.addQuad(_materialName, new Vector3f( 1,-1,-1)*_size, new Vector3f( 1, 1,-1)*_size, new Vector3f( 1, 1, 1)*_size, new Vector3f( 1,-1, 1)*_size, _color);
	//			out.addQuad(_materialName, new Vector3f(-1,-1,-1)*_size, new Vector3f(-1, 1,-1)*_size, new Vector3f( 1, 1,-1)*_size, new Vector3f( 1,-1,-1)*_size, _color);
	//			out.addQuad(_materialName, new Vector3f(-1, 1, 1)*_size, new Vector3f(-1,-1, 1)*_size, new Vector3f( 1,-1, 1)*_size, new Vector3f( 1, 1, 1)*_size, _color);
	//			out.setNormalMode(ege::resource::Mesh::normalMode::face);
	//			out.calculateNormaleFace(_materialName);
	//			// generate the VBO
	//			out.generateVBO();
	//		} else {
	//			EGE_ERROR("can not create the basic mesh interface");
	//		}
	//		return out;
	//	}
	//	public static ResourceMesh createSphere(float _size=1.0f,
	//                                            const etk::String& _materialName="basics",
	//                                            Color _color=etk::color::green,
	//                                            int _lats = 10,
	//                                            int _longs = 10) {
	//		EGE_VERBOSE(" create a sphere _size=" << _radius << " _materialName=" << _materialName << " _color=" << _color);
	//		ememory::SharedPtr<ege::resource::Mesh> out = ege::resource::Mesh::create("---", "DATA:///color3.prog");
	//		if (out != null) {
	//			ememory::SharedPtr<ege::Material> material = ememory::makeShared<ege::Material>();
	//			// set the element material properties :
	//			material.setAmbientFactor(vec4(1,1,1,1));
	//			material.setDiffuseFactor(vec4(0,0,0,1));
	//			material.setSpecularFactor(vec4(0,0,0,1));
	//			material.setShininess(1);
	//			material.setRenderMode(gale::openGL::renderMode::triangle);
	//			out.addMaterial(_materialName, material);
	//			
	//			out.addFaceIndexing(_materialName);
	//			for(int iii=0; iii<=_lats; ++iii) {
	//				float lat0 = PI * (-0.5f + float(iii - 1) / _lats);
	//				float z0  = _radius*sin(lat0);
	//				float zr0 = _radius*cos(lat0);
	//				
	//				float lat1 = PI * (-0.5f + float(iii) / _lats);
	//				float z1 = _radius*sin(lat1);
	//				float zr1 = _radius*cos(lat1);
	//				
	//				for(int jjj=0; jjj<_longs; ++jjj) {
	//					float lng = 2.0f * PI * float(jjj - 1) / _longs;
	//					float x = cos(lng);
	//					float y = sin(lng);
	//					new Vector3f v1 = new Vector3f(x * zr1, y * zr1, z1);
	//					new Vector3f v4 = new Vector3f(x * zr0, y * zr0, z0);
	//					
	//					lng = 2 * PI * float(jjj) / _longs;
	//					x = cos(lng);
	//					y = sin(lng);
	//					new Vector3f v2 = new Vector3f(x * zr1, y * zr1, z1);
	//					new Vector3f v3 = new Vector3f(x * zr0, y * zr0, z0);
	//					
	//					out.addTriangle(_materialName, v1, v3, v2, _color);
	//					out.addTriangle(_materialName, v1, v4, v3, _color);
	//				}
	//			}
	//			out.setNormalMode(ege::resource::Mesh::normalMode::face);
	//			out.calculateNormaleFace(_materialName);
	//			// generate the VBO
	//			out.generateVBO();
	//		} else {
	//			EGE_ERROR("can not create the basic mesh interface");
	//		}
	//		return out;
	//	}
	//	public static ResourceMesh createCylinder(float _radius = 1.0f,
	//                                              float _size = 1.0f, 
	//                                              const etk::String& _materialName="basics",
	//                                              Color _color=etk::color::green,
	//                                              int _lats = 10,
	//                                              int _longs = 10) {
	//		EGE_VERBOSE(" create a cylinder _size=" << _size << " _materialName=" << _materialName << " _color=" << _color);
	//		ememory::SharedPtr<ege::resource::Mesh> out = ege::resource::Mesh::create("---", "DATA:///color3.prog");
	//		if (out != null) {
	//			ememory::SharedPtr<ege::Material> material = ememory::makeShared<ege::Material>();
	//			// set the element material properties :
	//			material.setAmbientFactor(vec4(1,1,1,1));
	//			material.setDiffuseFactor(vec4(0,0,0,1));
	//			material.setSpecularFactor(vec4(0,0,0,1));
	//			material.setShininess(1);
	//			material.setRenderMode(gale::openGL::renderMode::triangle);
	//			out.addMaterial(_materialName, material);
	//			
	//			out.addFaceIndexing(_materialName);
	//			
	//			// center to border (TOP)
	//			for(int jjj=0; jjj<_longs; ++jjj) {
	//				float lng = 2.0f * PI * float(jjj - 1) / _longs;
	//				
	//				float z = _size*0.5f;
	//				new Vector3f v1 = new Vector3f(0.0f, 0.0f, z);
	//				
	//				float x = cos(lng)*_radius;
	//				float y = sin(lng)*_radius;
	//				new Vector3f v2 = new Vector3f(x, y, z);
	//				
	//				lng = 2.0f * PI * float(jjj) / _longs;
	//				x = cos(lng)*_radius;
	//				y = sin(lng)*_radius;
	//				new Vector3f v3 = new Vector3f(x, y, z);
	//				out.addTriangle(_materialName, v1, v2, v3, _color);
	//			}
	//			// Cylinder
	//			for(int jjj=0; jjj<_longs; ++jjj) {
	//				float lng = 2.0f * PI * float(jjj - 1) / _longs;
	//				
	//				float z = _size*0.5f;
	//				
	//				float x = cos(lng)*_radius;
	//				float y = sin(lng)*_radius;
	//				new Vector3f v2  = new Vector3f(x, y, z);
	//				new Vector3f v2b = new Vector3f(x, y, -z);
	//				
	//				lng = 2.0f * PI * float(jjj) / _longs;
	//				x = cos(lng)*_radius;
	//				y = sin(lng)*_radius;
	//				new Vector3f v3  = new Vector3f(x, y, z);
	//				new Vector3f v3b = new Vector3f(x, y, -z);
	//				
	//				out.addQuad(_materialName, v3, v2, v2b, v3b, _color);
	//			}
	//			// center to border (BUTTOM)
	//			for(int jjj=0; jjj<_longs; ++jjj) {
	//				float lng = 2.0f * PI * float(jjj - 1) / _longs;
	//				
	//				float z = _size*-0.5f;
	//				new Vector3f v1 = new Vector3f(0.0f, 0.0f, z);
	//				
	//				float x = cos(lng)*_radius;
	//				float y = sin(lng)*_radius;
	//				new Vector3f v2 = new Vector3f(x, y, z);
	//				
	//				lng = 2.0f * PI * float(jjj) / _longs;
	//				x = cos(lng)*_radius;
	//				y = sin(lng)*_radius;
	//				new Vector3f v3 = new Vector3f(x, y, z);
	//				out.addTriangle(_materialName, v1, v3, v2, _color);
	//			}
	//			out.setNormalMode(ege::resource::Mesh::normalMode::face);
	//			out.calculateNormaleFace(_materialName);
	//			// generate the VBO
	//			out.generateVBO();
	//		} else {
	//			EGE_ERROR("can not create the basic mesh interface");
	//		}
	//		return out;
	//	}
	//	public static ResourceMesh createCapsule(float _radius = 1.0f,
	//                                             float _size = 1.0f, 
	//                                             const etk::String& _materialName="basics",
	//                                             Color _color=etk::color::green,
	//                                             int _lats = 10,
	//                                             int _longs = 10)  {
	//		EGE_VERBOSE(" create a capsule _size=" << _size << " _materialName=" << _materialName << " _color=" << _color);
	//		ememory::SharedPtr<ege::resource::Mesh> out = ege::resource::Mesh::create("---", "DATA:///color3.prog");
	//		if (out != null) {
	//			ememory::SharedPtr<ege::Material> material = ememory::makeShared<ege::Material>();
	//			// set the element material properties :
	//			material.setAmbientFactor(vec4(1,1,1,1));
	//			material.setDiffuseFactor(vec4(0,0,0,1));
	//			material.setSpecularFactor(vec4(0,0,0,1));
	//			material.setShininess(1);
	//			material.setRenderMode(gale::openGL::renderMode::triangle);
	//			out.addMaterial(_materialName, material);
	//			
	//			out.addFaceIndexing(_materialName);
	//			
	//			// center to border (TOP)
	//			float offset = _size*0.5f;
	//			for(int iii=_lats/2+1; iii<=_lats; ++iii) {
	//				float lat0 = PI * (-0.5f + float(iii - 1) / _lats);
	//				float z0  = _radius*sin(lat0);
	//				float zr0 = _radius*cos(lat0);
	//				
	//				float lat1 = PI * (-0.5f + float(iii) / _lats);
	//				float z1 = _radius*sin(lat1);
	//				float zr1 = _radius*cos(lat1);
	//				
	//				for(int jjj=0; jjj<_longs; ++jjj) {
	//					float lng = 2.0f * PI * float(jjj - 1) / _longs;
	//					float x = cos(lng);
	//					float y = sin(lng);
	//					new Vector3f v1 = new Vector3f(x * zr1, y * zr1, z1+offset);
	//					new Vector3f v4 = new Vector3f(x * zr0, y * zr0, z0+offset);
	//					
	//					lng = 2 * PI * float(jjj) / _longs;
	//					x = cos(lng);
	//					y = sin(lng);
	//					new Vector3f v2 = new Vector3f(x * zr1, y * zr1, z1+offset);
	//					new Vector3f v3 = new Vector3f(x * zr0, y * zr0, z0+offset);
	//					out.addQuad(_materialName, v2, v1, v4, v3, _color);
	//				}
	//			}
	//			// Cylinder
	//			for(int jjj=0; jjj<_longs; ++jjj) {
	//				float lng = 2.0f * PI * float(jjj - 1) / _longs;
	//				
	//				float z = _size*0.5f;
	//				
	//				float x = cos(lng)*_radius;
	//				float y = sin(lng)*_radius;
	//				new Vector3f v2  = new Vector3f(x, y, z);
	//				new Vector3f v2b = new Vector3f(x, y, -z);
	//				
	//				lng = 2.0f * PI * float(jjj) / _longs;
	//				x = cos(lng)*_radius;
	//				y = sin(lng)*_radius;
	//				new Vector3f v3  = new Vector3f(x, y, z);
	//				new Vector3f v3b = new Vector3f(x, y, -z);
	//				
	//				out.addQuad(_materialName, v3, v2, v2b, v3b, _color);
	//			}
	//			// center to border (BUTTOM)
	//			offset = -_size*0.5f;
	//			for(int iii=0; iii<=_lats/2; ++iii) {
	//				float lat0 = PI * (-0.5f + float(iii - 1) / _lats);
	//				float z0  = _radius*sin(lat0);
	//				float zr0 = _radius*cos(lat0);
	//				
	//				float lat1 = PI * (-0.5f + float(iii) / _lats);
	//				float z1 = _radius*sin(lat1);
	//				float zr1 = _radius*cos(lat1);
	//				
	//				for(int jjj=0; jjj<_longs; ++jjj) {
	//					float lng = 2.0f * PI * float(jjj - 1) / _longs;
	//					float x = cos(lng);
	//					float y = sin(lng);
	//					new Vector3f v1 = new Vector3f(x * zr1, y * zr1, z1+offset);
	//					new Vector3f v4 = new Vector3f(x * zr0, y * zr0, z0+offset);
	//					
	//					lng = 2 * PI * float(jjj) / _longs;
	//					x = cos(lng);
	//					y = sin(lng);
	//					new Vector3f v2 = new Vector3f(x * zr1, y * zr1, z1+offset);
	//					new Vector3f v3 = new Vector3f(x * zr0, y * zr0, z0+offset);
	//					out.addQuad(_materialName, v2, v1, v4, v3, _color);
	//				}
	//			}
	//			out.setNormalMode(ege::resource::Mesh::normalMode::face);
	//			out.calculateNormaleFace(_materialName);
	//			// generate the VBO
	//			out.generateVBO();
	//		} else {
	//			EGE_ERROR("can not create the basic mesh interface");
	//		}
	//		return out;
	//	}
	//	public static ResourceMesh createCone(float _radius = 1.0f,
	//                                          float _size = 1.0f, 
	//                                          const etk::String& _materialName="basics",
	//                                          Color _color=etk::color::green,
	//                                          int _lats = 10,
	//                                          int _longs = 10) {
	//		EGE_VERBOSE(" create a cylinder _size=" << _size << " _materialName=" << _materialName << " _color=" << _color);
	//		ememory::SharedPtr<ege::resource::Mesh> out = ege::resource::Mesh::create("---", "DATA:///color3.prog");
	//		if (out != null) {
	//			ememory::SharedPtr<ege::Material> material = ememory::makeShared<ege::Material>();
	//			// set the element material properties :
	//			material.setAmbientFactor(vec4(1,1,1,1));
	//			material.setDiffuseFactor(vec4(0,0,0,1));
	//			material.setSpecularFactor(vec4(0,0,0,1));
	//			material.setShininess(1);
	//			material.setRenderMode(gale::openGL::renderMode::triangle);
	//			out.addMaterial(_materialName, material);
	//			
	//			out.addFaceIndexing(_materialName);
	//			
	//			// center to border (TOP)
	//			for(int jjj=0; jjj<_longs; ++jjj) {
	//				float lng = 2.0f * PI * float(jjj - 1) / _longs;
	//				new Vector3f v1 = new Vector3f(0.0f, 0.0f, _size/2);
	//				
	//				float x = cos(lng)*_radius;
	//				float y = sin(lng)*_radius;
	//				new Vector3f v2 = new Vector3f(x, y, -_size/2);
	//				
	//				lng = 2.0f * PI * float(jjj) / _longs;
	//				x = cos(lng)*_radius;
	//				y = sin(lng)*_radius;
	//				new Vector3f v3 = new Vector3f(x, y, -_size/2);
	//				out.addTriangle(_materialName, v1, v2, v3, _color);
	//			}
	//			// center to border (BUTTOM)
	//			for(int jjj=0; jjj<_longs; ++jjj) {
	//				float lng = 2.0f * PI * float(jjj - 1) / _longs;
	//				
	//				new Vector3f v1 = new Vector3f(0.0f, 0.0f, -_size/2);
	//				
	//				float x = cos(lng)*_radius;
	//				float y = sin(lng)*_radius;
	//				new Vector3f v2 = new Vector3f(x, y, -_size/2);
	//				
	//				lng = 2.0f * PI * float(jjj) / _longs;
	//				x = cos(lng)*_radius;
	//				y = sin(lng)*_radius;
	//				new Vector3f v3 = new Vector3f(x, y, -_size/2);
	//				out.addTriangle(_materialName, v1, v3, v2, _color);
	//			}
	//			out.setNormalMode(ege::resource::Mesh::normalMode::face);
	//			out.calculateNormaleFace(_materialName);
	//			// generate the VBO
	//			out.generateVBO();
	//		} else {
	//			EGE_ERROR("can not create the basic mesh interface");
	//		}
	//		return out;
	//	}
}
