package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.etk.Uri;
import org.atriasoft.loader3d.resources.ResourceStaticMesh;
import org.atriasoft.loader3d.resources.ResourceStaticMeshObj;

public class ComponentStaticMesh extends Component {
	private ResourceStaticMesh mesh = null;
	
	public ComponentStaticMesh(ResourceStaticMesh mesh) {
		this.mesh = mesh;
	}
	
	public ComponentStaticMesh(Uri objectFileName) {
		// TODO check if it is OBJ ...
		this.mesh = ResourceStaticMeshObj.create(objectFileName);
	}
	
	public void bindForRendering() {
		if (this.mesh == null) {
			return;
		}
		this.mesh.bindForRendering();
	}
	
	public ResourceStaticMesh getMesh() {
		return this.mesh;
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "static-mesh";
	}
	
	public void render() {
		if (this.mesh == null) {
			return;
		}
		this.mesh.render();
	}
	
	public void unBindForRendering() {
		if (this.mesh == null) {
			return;
		}
		this.mesh.unBindForRendering();
	}
}
