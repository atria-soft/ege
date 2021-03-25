package org.atriasoft.ege.resource;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;

public class ResourceListTexturedMesh extends ResourceStaticMesh {
	public static ResourceListTexturedMesh create(final RenderMode mode) {
		ResourceListTexturedMesh resource = new ResourceListTexturedMesh(mode);
		getManager().localAdd(resource);
		return resource;
	}
	
	protected List<Vector3f> vertices = new ArrayList<>();
	protected List<Vector2f> textureCoords = new ArrayList<>();
	protected List<Vector3f> normals = new ArrayList<>();
	
	protected List<Integer> indices = new ArrayList<>();
	
	protected ResourceListTexturedMesh(final RenderMode mode) {
		super(mode);
	}
	
	protected ResourceListTexturedMesh(final Uri uriFile) {
		super(uriFile);
	}
	
	public void addQuad(final Vector3f v1, final Vector3f v2, final Vector3f v3, final Vector3f v4, final Vector2f t1, final Vector2f t2, final Vector2f t3, final Vector2f t4, final Vector3f n1) {
		addTriangle(v1, v2, v3, t1, t2, t3, n1, n1, n1);
		addTriangle(v1, v3, v4, t1, t3, t4, n1, n1, n1);
	}
	
	public void addQuad(final Vector3f v1, final Vector3f v2, final Vector3f v3, final Vector3f v4, final Vector2f t1, final Vector2f t2, final Vector2f t3, final Vector2f t4, final Vector3f n1,
			final Vector3f n2, final Vector3f n3, final Vector3f n4) {
		addTriangle(v1, v2, v3, t1, t2, t3, n1, n2, n3);
		addTriangle(v1, v3, v4, t1, t3, t4, n1, n3, n4);
	}
	
	public void addTriangle(final Vector3f v1, final Vector3f v2, final Vector3f v3, final Vector2f t1, final Vector2f t2, final Vector2f t3, final Vector3f n1, final Vector3f n2, final Vector3f n3) {
		this.vertices.add(v1);
		this.vertices.add(v2);
		this.vertices.add(v3);
		this.textureCoords.add(t1);
		this.textureCoords.add(t2);
		this.textureCoords.add(t3);
		this.normals.add(n1);
		this.normals.add(n2);
		this.normals.add(n3);
		this.indices.add(this.vertices.size() - 3);
		this.indices.add(this.vertices.size() - 2);
		this.indices.add(this.vertices.size() - 1);
	}
	
	public void clear() {
		this.vertices.clear();
		this.textureCoords.clear();
		this.normals.clear();
		this.indices.clear();
	}
	
	/**
	 * Send the data to the graphic card.
	 */
	public void flush() {
		// request to the manager to be call at the next update ...
		this.vao = ResourceVirtualArrayObject.create(toFloatV3(this.vertices), toFloatV2(this.textureCoords), toFloatV3(this.normals), toIntArray(this.indices));
		this.vao.flush();
	}
	
	private float[] toFloatArray(final List<Float> values) {
		float[] out = new float[values.size()];
		for (int iii = 0; iii < values.size(); iii++) {
			out[iii] = values.get(iii);
		}
		return out;
	}
	
	private float[] toFloatV2(final List<Vector2f> values) {
		float[] out = new float[values.size() * 2];
		for (int iii = 0; iii < values.size(); iii++) {
			Vector2f tmp = values.get(iii);
			out[iii * 2] = tmp.x();
			out[iii * 2 + 1] = tmp.y();
		}
		return out;
	}
	
	private float[] toFloatV3(final List<Vector3f> values) {
		float[] out = new float[values.size() * 3];
		for (int iii = 0; iii < values.size(); iii++) {
			Vector3f tmp = values.get(iii);
			out[iii * 3] = tmp.x();
			out[iii * 3 + 1] = tmp.y();
			out[iii * 3 + 2] = tmp.z();
		}
		return out;
	}
	
	private int[] toIntArray(final List<Integer> values) {
		int[] out = new int[values.size()];
		for (int iii = 0; iii < values.size(); iii++) {
			out[iii] = values.get(iii);
		}
		return out;
	}
	
}
