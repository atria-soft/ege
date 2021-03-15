package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.etk.Uri;
import org.atriasoft.ege.resource.ResourceStaticMesh;
import org.atriasoft.ege.resource.ResourceStaticMeshObj;

public class ComponentStaticMesh extends Component {
	private ResourceStaticMesh mesh = null;
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "static-mesh";
	}
	
	public ComponentStaticMesh(Uri objectFileName) {
		// TODO check if it is OBJ ...
		mesh = ResourceStaticMeshObj.create(objectFileName);
	}
	public ComponentStaticMesh(ResourceStaticMesh mesh) {
		this.mesh = mesh;
	}
	
	public ResourceStaticMesh getMesh() {
		return mesh;
	}
	
	public void bindForRendering() {
		if (mesh == null) {
			return;
		}
		mesh.bindForRendering();
	}

	public void unBindForRendering() {
		if (mesh == null) {
			return;
		}
		mesh.unBindForRendering();
	}

	public void render() {
		if (mesh == null) {
			return;
		}
		mesh.render();
	}
}
