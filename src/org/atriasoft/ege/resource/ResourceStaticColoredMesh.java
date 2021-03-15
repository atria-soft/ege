package org.atriasoft.ege.resource;

import org.atriasoft.etk.Uri;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;


public class ResourceStaticColoredMesh extends ResourceStaticMesh {
	protected float[] vertices = null;
	protected float[] colors = null;
	protected float[] normals = null;
	protected int[] indices = null;

	protected ResourceStaticColoredMesh(Uri uriFile) {
		super(uriFile);
		addResourceType("ResourceStaticColoredMesh");
	}
	protected ResourceStaticColoredMesh(float[] vertices, float[] colors,
			float[] normals, int[] indices, RenderMode mode) {
		super(mode);
		addResourceType("ResourceStaticColoredMesh");
		this.vertices = vertices;
		this.colors = colors;
		this.normals = normals;
		this.indices = indices;
		flush();
	}
	/**
	 * @brief Send the data to the graphic card.
	 */
	public void flush() {
		// request to the manager to be call at the next update ...
		vao = ResourceVirtualArrayObject.create(this.vertices, this.colors, null, this.normals, this.indices);
		vao.flush();
	}
	
	public static ResourceStaticColoredMesh create(float[] vertices, float[] colors,
			float[] normals, int[] indices, RenderMode mode) {
		ResourceStaticColoredMesh resource = new ResourceStaticColoredMesh(vertices, colors, normals, indices, mode);
		if (resource.resourceHasBeenCorectlyInit() == false) {
			Log.critical("resource Is not correctly init: ResourceVirtualBufferObject");
		}
		getManager().localAdd(resource);
		return resource;
	}

}
