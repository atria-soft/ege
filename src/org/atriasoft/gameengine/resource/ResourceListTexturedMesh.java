package org.atriasoft.gameengine.resource;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;

import org.atriasoft.gameengine.internal.Log;


public class ResourceListTexturedMesh extends ResourceStaticMesh {
	protected List<Vector3f> vertices = new ArrayList<Vector3f>();
	protected List<Vector2f> textureCoords = new ArrayList<Vector2f>();
	protected List<Vector3f> normals = new ArrayList<Vector3f>();
	protected List<Integer> indices = new ArrayList<Integer>();

	protected ResourceListTexturedMesh(Uri uriFile) {
		super(uriFile);
		addResourceType("ResourceListTexturedMesh");
	}
	protected ResourceListTexturedMesh(RenderMode mode) {
		super(mode);
		addResourceType("ResourceListTexturedMesh");
	}

	private float[] toFloatArray(List<Float> values) {
		float[] out = new float[values.size()];
		for (int iii=0; iii<values.size(); iii++) {
			out[iii] = values.get(iii);
		}
		return out;
	}
	private int[] toIntArray(List<Integer> values) {
		int[] out = new int[values.size()];
		for (int iii=0; iii<values.size(); iii++) {
			out[iii] = values.get(iii);
		}
		return out;
	}
	private float[] toFloatV3(List<Vector3f> values) {
		float[] out = new float[values.size()*3];
		for (int iii=0; iii<values.size(); iii++) {
			Vector3f tmp = values.get(iii);
			out[iii*3] = tmp.x;
			out[iii*3+1] = tmp.y;
			out[iii*3+2] = tmp.z;
		}
		return out;
	}
	private float[] toFloatV2(List<Vector2f> values) {
		float[] out = new float[values.size()*2];
		for (int iii=0; iii<values.size(); iii++) {
			Vector2f tmp = values.get(iii);
			out[iii*2] = tmp.x;
			out[iii*2+1] = tmp.y;
		}
		return out;
	}
	/**
	 * @brief Send the data to the graphic card.
	 */
	public void flush() {
		// request to the manager to be call at the next update ...
		vao = ResourceVirtualArrayObject.create(toFloatV3(this.vertices), toFloatV2(this.textureCoords), toFloatV3(this.normals), toIntArray(this.indices));
		vao.flush();
	}

	public void addTriangle(Vector3f v1, Vector3f v2, Vector3f v3, Vector2f t1, Vector2f t2, Vector2f t3, Vector3f n1, Vector3f n2, Vector3f n3) {
		vertices.add(v1);
		vertices.add(v2);
		vertices.add(v3);
		textureCoords.add(t1);
		textureCoords.add(t2);
		textureCoords.add(t3);
		normals.add(n1);
		normals.add(n2);
		normals.add(n3);
		indices.add(vertices.size()-3);
		indices.add(vertices.size()-2);
		indices.add(vertices.size()-1);
	}

	public void addQuad(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, Vector2f t1, Vector2f t2, Vector2f t3, Vector2f t4, Vector3f n1, Vector3f n2, Vector3f n3, Vector3f n4) {
		addTriangle(v1, v2, v3, t1, t2, t3, n1, n2, n3);
		addTriangle(v1, v3, v4, t1, t3, t4, n1, n3, n4);
	}
	public void addQuad(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, Vector2f t1, Vector2f t2, Vector2f t3, Vector2f t4, Vector3f n1) {
		addTriangle(v1, v2, v3, t1, t2, t3, n1, n1, n1);
		addTriangle(v1, v3, v4, t1, t3, t4, n1, n1, n1);
	}
	public void clear() {
		vertices.clear();
		textureCoords.clear();
		normals.clear();
		indices.clear();
	}
	
	public static ResourceListTexturedMesh create(RenderMode mode) {
		ResourceListTexturedMesh resource = new ResourceListTexturedMesh(mode);
		if (resource.resourceHasBeenCorectlyInit() == false) {
			Log.critical("resource Is not correctly init: ResourceVirtualBufferObject");
		}
		getManager().localAdd(resource);
		return resource;
	}

}
