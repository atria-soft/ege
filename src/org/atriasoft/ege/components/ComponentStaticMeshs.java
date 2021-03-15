package org.atriasoft.ege.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.atriasoft.ege.Component;
import org.atriasoft.etk.Uri;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.ege.resource.ResourceStaticMesh;
import org.atriasoft.ege.resource.ResourceStaticMeshObj;

public class ComponentStaticMeshs extends Component {
	private Map<String, ResourceStaticMesh> meshs = new HashMap<String, ResourceStaticMesh>();
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "static-meshs";
	}

	public ComponentStaticMeshs(Uri meshUrl) {
		// TODO load Mesh
		Log.critical("Can not Load the Mesh for now ... " + meshUrl);
		ResourceStaticMeshObj mesh = ResourceStaticMeshObj.create(meshUrl);
		setMesh("default", mesh);
	}
	public ComponentStaticMeshs() {
		// nothing to do ...
	}
	
	public Set<String> getKeys() {
		return meshs.keySet();
	}
	
	public void setMesh(String name, ResourceStaticMesh mesh) {
		this.meshs.put(name, mesh);
	}
	
	public ResourceStaticMesh getMesh(String name) {
		return meshs.get(name);
	}
	
	public void bindForRendering(String name) {
		ResourceStaticMesh mesh = meshs.get(name);
		if (mesh == null) {
			return;
		}
		mesh.bindForRendering();
	}

	public void unBindForRendering(String name) {
		ResourceStaticMesh mesh = meshs.get(name);
		if (mesh == null) {
			return;
		}
		mesh.unBindForRendering();
	}

	public void render(String name) {
		ResourceStaticMesh mesh = meshs.get(name);
		if (mesh == null) {
			return;
		}
		mesh.render();
	}
}
