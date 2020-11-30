package org.atriasoft.gameengine.resource;

import org.atriasoft.etk.Uri;
import org.atriasoft.gameengine.internal.Log;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.Resource;

import objConverter.ModelData;
import objConverter.OBJFileLoader;

public class ResourceStaticMeshObj extends ResourceStaticTexturedMesh {
	private final Uri uriFile;
	
	public ResourceStaticMeshObj(Uri uriFile) {
		super(uriFile);
		addResourceType("ResourceStaticMeshObj");
		this.uriFile = uriFile;
		System.out.println("Load file " + uriFile);
		ModelData data = OBJFileLoader.loadOBJ(uriFile.get());
		this.vertices = data.getVertices();
		this.textureCoords = data.getTextureCoords();
		this.normals = data.getNormals();
		this.indices = data.getIndices();
		mode = RenderMode.triangle;
		flush();
	}

	public Uri getUriFile() {
		return uriFile;
	}
	

	public static ResourceStaticMeshObj create(Uri uriObj) {
		ResourceStaticMeshObj resource;
		Resource resource2;
		String name = uriObj.getValue();
		if (name.isEmpty() == false && name != "---") {
			resource2 = getManager().localKeep(name);
		} else {
			Log.error("Can not create a shader without a filaname");
			return null;
		}
		if (resource2 != null) {
			if (resource2 instanceof ResourceStaticMeshObj) {
				resource2.keep();
				return (ResourceStaticMeshObj)resource2;
			}
			Log.critical("Request resource file : '" + name + "' With the wrong type (dynamic cast error)");
			return null;
		}
		resource = new ResourceStaticMeshObj(uriObj);
		if (resource.resourceHasBeenCorectlyInit() == false) {
			Log.critical("resource Is not correctly init : ResourceProgram" );
			return null;
		}
		getManager().localAdd(resource);
		return resource;
	}
}
