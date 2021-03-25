package org.atriasoft.ege.resource;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.Resource;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;

public class ResourceStaticMesh extends Resource {
	protected ResourceVirtualArrayObject vao = null;
	protected RenderMode mode = RenderMode.quadStrip;
	
	protected ResourceStaticMesh(final RenderMode mode) {
		super();
		this.mode = mode;
	}
	
	protected ResourceStaticMesh(final Uri uriFile) {
		super(uriFile);
	}
	
	public void bindForRendering() {
		if (this.vao == null) {
			return;
		}
		this.vao.bindForRendering();
	}
	
	@Override
	public void cleanUp() {
		this.vao.cleanUp();
	}
	
	public RenderMode getMode() {
		return this.mode;
	}
	
	public void render() {
		if (this.vao == null) {
			return;
		}
		this.vao.render(this.mode);
	}
	
	public void setMode(final RenderMode mode) {
		this.mode = mode;
	}
	
	public void unBindForRendering() {
		if (this.vao == null) {
			return;
		}
		this.vao.unBindForRendering();
	}
	
}
