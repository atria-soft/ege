package org.atriasoft.ege.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.atriasoft.ege.Component;
import org.atriasoft.etk.Uri;
import org.atriasoft.loader3d.resources.ResourceStaticMesh;
import org.atriasoft.loader3d.resources.ResourceStaticMeshObj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentStaticMeshs extends Component {
	static final Logger LOGGER = LoggerFactory.getLogger(ComponentStaticMeshs.class);
	private final Map<String, ResourceStaticMesh> meshs = new HashMap<>();
	
	public ComponentStaticMeshs() {
		// nothing to do ...
	}
	
	public ComponentStaticMeshs(final Uri meshUrl) {
		// TODO load Mesh
		LOGGER.error("[CRITICAL]Can not Load the Mesh for now ... {}", meshUrl);
		System.exit(-1);
		final ResourceStaticMeshObj mesh = ResourceStaticMeshObj.create(meshUrl);
		setMesh("default", mesh);
	}
	
	public void bindForRendering(final String name) {
		final ResourceStaticMesh mesh = this.meshs.get(name);
		if (mesh == null) {
			return;
		}
		mesh.bindForRendering();
	}
	
	public Set<String> getKeys() {
		return this.meshs.keySet();
	}
	
	public ResourceStaticMesh getMesh(final String name) {
		return this.meshs.get(name);
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "static-meshs";
	}
	
	public void render(final String name) {
		final ResourceStaticMesh mesh = this.meshs.get(name);
		if (mesh == null) {
			return;
		}
		mesh.render();
	}
	
	public void setMesh(final String name, final ResourceStaticMesh mesh) {
		this.meshs.put(name, mesh);
	}
	
	public void unBindForRendering(final String name) {
		final ResourceStaticMesh mesh = this.meshs.get(name);
		if (mesh == null) {
			return;
		}
		mesh.unBindForRendering();
	}
}
