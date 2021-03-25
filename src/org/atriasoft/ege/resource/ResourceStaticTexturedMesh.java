package org.atriasoft.ege.resource;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;

public class ResourceStaticTexturedMesh extends ResourceStaticMesh {
	public static ResourceStaticTexturedMesh create(final float[] vertices, final float[] textureCoordinates, final float[] normals, final int[] indices, final RenderMode mode) {
		ResourceStaticTexturedMesh resource = new ResourceStaticTexturedMesh(vertices, textureCoordinates, normals, indices, mode);
		getManager().localAdd(resource);
		return resource;
	}
	
	protected float[] vertices = null;
	protected float[] textureCoords = null;
	protected float[] normals = null;
	
	protected int[] indices = null;
	
	protected ResourceStaticTexturedMesh(final float[] vertices, final float[] textureCoordinates, final float[] normals, final int[] indices, final RenderMode mode) {
		super(mode);
		this.vertices = vertices;
		this.textureCoords = textureCoordinates;
		this.normals = normals;
		this.indices = indices;
	}
	
	protected ResourceStaticTexturedMesh(final Uri uriFile) {
		super(uriFile);
	}
	
	/**
	 * Send the data to the graphic card.
	 */
	public void flush() {
		// request to the manager to be call at the next update ...
		this.vao = ResourceVirtualArrayObject.create(this.vertices, this.textureCoords, this.normals, this.indices);
		this.vao.flush();
	}
	
}
