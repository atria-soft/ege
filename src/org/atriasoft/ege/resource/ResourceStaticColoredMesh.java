package org.atriasoft.ege.resource;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;

public class ResourceStaticColoredMesh extends ResourceStaticMesh {
	public static ResourceStaticColoredMesh create(final float[] vertices, final float[] colors, final float[] normals, final int[] indices, final RenderMode mode) {
		ResourceStaticColoredMesh resource = new ResourceStaticColoredMesh(vertices, colors, normals, indices, mode);
		getManager().localAdd(resource);
		return resource;
	}
	
	protected float[] vertices = null;
	protected float[] colors = null;
	protected float[] normals = null;
	
	protected int[] indices = null;
	
	protected ResourceStaticColoredMesh(final float[] vertices, final float[] colors, final float[] normals, final int[] indices, final RenderMode mode) {
		super(mode);
		this.vertices = vertices;
		this.colors = colors;
		this.normals = normals;
		this.indices = indices;
		flush();
	}
	
	protected ResourceStaticColoredMesh(final Uri uriFile) {
		super(uriFile);
	}
	
	/**
	 * Send the data to the graphic card.
	 */
	public void flush() {
		// request to the manager to be call at the next update ...
		this.vao = ResourceVirtualArrayObject.create(this.vertices, this.colors, null, this.normals, this.indices);
		this.vao.flush();
	}
	
}
