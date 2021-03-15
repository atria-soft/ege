package org.atriasoft.ege.resource;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.Resource;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;


public class ResourceStaticMesh extends Resource {
	protected ResourceVirtualArrayObject vao = null;
	protected RenderMode mode = RenderMode.quadStrip;

	protected ResourceStaticMesh(Uri uriFile) {
		super(uriFile);
		addResourceType("ResourceStaticMesh");
	}
	protected ResourceStaticMesh(RenderMode mode) {
		super();
		addResourceType("ResourceStaticMesh");
		this.mode = mode;
	}
	
	public void bindForRendering() {
		if (vao == null) {
			return;
		}
		vao.bindForRendering();
	}

	public void unBindForRendering() {
		if (vao == null) {
			return;
		}
		vao.unBindForRendering();
	}

	public void render() {
		if (vao == null) {
			return;
		}
		vao.render(mode);
	}
	
	@Override
	public void cleanUp() {
		vao.cleanUp();
	}

	public RenderMode getMode() {
		return mode;
	}

	public void setMode(RenderMode mode) {
		this.mode = mode;
	}

}
