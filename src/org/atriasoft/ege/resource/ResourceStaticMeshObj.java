package org.atriasoft.ege.resource;

import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.Resource;

import objConverter.ModelData;
import objConverter.OBJFileLoader;

public class ResourceStaticMeshObj extends ResourceStaticTexturedMesh {
	public static ResourceStaticMeshObj create(final Uri uriObj) {
		ResourceStaticMeshObj resource;
		Resource resource2;
		final String name = uriObj.getValue();
		if (name.isEmpty() || name.equals("---")) {
			Log.error("Can not create a shader without a filaname");
			return null;
		}
		resource2 = Resource.getManager().localKeep(name);
		if (resource2 != null) {
			if (resource2 instanceof ResourceStaticMeshObj) {
				resource2.keep();
				return (ResourceStaticMeshObj) resource2;
			}
			Log.critical("Request resource file : '" + name + "' With the wrong type (dynamic cast error)");
			return null;
		}
		resource = new ResourceStaticMeshObj(uriObj);
		Resource.getManager().localAdd(resource);
		return resource;
	}
	
	private final Uri uriFile;
	
	public ResourceStaticMeshObj(final Uri uriFile) {
		super(uriFile);
		this.uriFile = uriFile;
		System.out.println("Load file " + uriFile);
		final ModelData data = OBJFileLoader.loadOBJ(uriFile);
		this.vertices = data.getVertices();
		this.textureCoords = data.getTextureCoords();
		this.normals = data.getNormals();
		this.indices = data.getIndices();
		this.mode = RenderMode.triangle;
		flush();
	}
	
	public Uri getUriFile() {
		return this.uriFile;
	}
}
