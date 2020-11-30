package org.atriasoft.gameengine.resource;

import org.atriasoft.etk.Uri;
import org.atriasoft.gameengine.internal.Log;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;


public class ResourceStaticTexturedMesh extends ResourceStaticMesh {
	protected float[] vertices = null;
	protected float[] textureCoords = null;
	protected float[] normals = null;
	protected int[] indices = null;

	protected ResourceStaticTexturedMesh(Uri uriFile) {
		super(uriFile);
		addResourceType("ResourceStaticTexturedMesh");
	}
	protected ResourceStaticTexturedMesh(float[] vertices, float[] textureCoordinates,
			float[] normals, int[] indices, RenderMode mode) {
		super(mode);
		addResourceType("ResourceStaticTexturedMesh");
		this.vertices = vertices;
		this.textureCoords = textureCoordinates;
		this.normals = normals;
		this.indices = indices;
	}
	/**
	 * @brief Send the data to the graphic card.
	 */
	public void flush() {
		// request to the manager to be call at the next update ...
		vao = ResourceVirtualArrayObject.create(this.vertices, this.textureCoords, this.normals, this.indices);
		vao.flush();
	}
	
	public static ResourceStaticTexturedMesh create(float[] vertices, float[] textureCoordinates,
			float[] normals, int[] indices, RenderMode mode) {
		ResourceStaticTexturedMesh resource = new ResourceStaticTexturedMesh(vertices, textureCoordinates, normals, indices, mode);
		if (resource.resourceHasBeenCorectlyInit() == false) {
			Log.critical("resource Is not correctly init: ResourceVirtualBufferObject");
		}
		getManager().localAdd(resource);
		return resource;
	}

}
