package org.atriasoft.ege.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.Uri;
import org.atriasoft.loader3d.resources.ResourceStaticMesh;
import org.atriasoft.loader3d.resources.ResourceStaticMeshObj;

public class ComponentStaticMeshs extends Component {
	private final Map<String, ResourceStaticMesh> meshs = new HashMap<String, ResourceStaticMesh>();
	
	public ComponentStaticMeshs() {
		// nothing to do ...
	}
	
	public ComponentStaticMeshs(Uri meshUrl) {
		// TODO load Mesh
		Log.critical("Can not Load the Mesh for now ... {}", meshUrl);
		final ResourceStaticMeshObj mesh = ResourceStaticMeshObj.create(meshUrl);
		setMesh("default", mesh);
	}
	
	public void bindForRendering(String name) {
		final ResourceStaticMesh mesh = this.meshs.get(name);
		if (mesh == null) {
			return;
		}
		mesh.bindForRendering();
	}
	
	public Set<String> getKeys() {
		return this.meshs.keySet();
	}
	
	public ResourceStaticMesh getMesh(String name) {
		return this.meshs.get(name);
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "static-meshs";
	}
	
	public void render(String name) {
		final ResourceStaticMesh mesh = this.meshs.get(name);
		if (mesh == null) {
			return;
		}
		mesh.render();
	}
	
	public void setMesh(String name, ResourceStaticMesh mesh) {
		this.meshs.put(name, mesh);
	}
	
	public void unBindForRendering(String name) {
		final ResourceStaticMesh mesh = this.meshs.get(name);
		if (mesh == null) {
			return;
		}
		mesh.unBindForRendering();
	}
}
