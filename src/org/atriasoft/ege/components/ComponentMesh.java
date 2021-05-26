package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.etk.Uri;
import org.atriasoft.loader3d.resources.ResourceMesh;
import org.atriasoft.loader3d.resources.ResourceStaticMesh;

public class ComponentMesh extends Component {
	private ResourceMesh mesh = null;
	
	public ComponentMesh(Uri objectFileName) {
		this.mesh = ResourceMesh.create(objectFileName);
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
		return "mesh";
	}
	
	public void render() {
		if (this.mesh == null) {
			return;
		}
		this.mesh.render();
	}
	public void renderArrays() {
		if (this.mesh == null) {
			return;
		}
		this.mesh.renderArrays();
	}
	
	public void unBindForRendering() {
		if (this.mesh == null) {
			return;
		}
		this.mesh.unBindForRendering();
	}
}
