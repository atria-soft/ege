package org.atriasoft.ege.map;

import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.etk.math.Vector3i;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.ege.components.ComponentDynamicMeshs;
import org.atriasoft.ege.resource.ResourceListTexturedMesh;

public class ComponentDynamicMeshsVoxelMap extends ComponentDynamicMeshs {
	private VoxelChunk chunk;
	ResourceListTexturedMesh unbreakable = ResourceListTexturedMesh.create(RenderMode.triangle);
	ResourceListTexturedMesh stone = ResourceListTexturedMesh.create(RenderMode.triangle);
	ResourceListTexturedMesh dirt = ResourceListTexturedMesh.create(RenderMode.triangle);
	ResourceListTexturedMesh grass = ResourceListTexturedMesh.create(RenderMode.triangle);
	
	public ComponentDynamicMeshsVoxelMap(VoxelChunk chunk) {
		super();
		this.chunk = chunk;
		this.setMesh("unbreakable", unbreakable);
		this.setMesh("stone", stone);
		this.setMesh("dirt", dirt);
		this.setMesh("grass", grass);
	}

	private void drawPlane(Vector3i base, int xxx, int yyy, int zzz, int type) {
		//Log.warning("Add plane Z : " + (base.x + xxx) + ", " + (base.y + yyy) + ", " + (base.z + zzz));
		Vector3f v1 = new Vector3f(base.x + xxx  , base.y + yyy  , base.z + zzz);
		Vector3f v2 = new Vector3f(base.x + xxx  , base.y + yyy+1, base.z + zzz);
		Vector3f v3 = new Vector3f(base.x + xxx+1, base.y + yyy+1, base.z + zzz);
		Vector3f v4 = new Vector3f(base.x + xxx+1, base.y + yyy  , base.z + zzz);
		Vector2f t1 = new Vector2f(0, 0);
		Vector2f t2 = new Vector2f(0, 1);
		Vector2f t3 = new Vector2f(1, 1);
		Vector2f t4 = new Vector2f(1, 0);
		Vector3f n1 = new Vector3f(0, 0, -1);
		if (type == VoxelType.NATIVE_UNBREAKABLE) {
			unbreakable.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_DIRT) {
			dirt.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_STONE) {
			stone.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_GRASS) {
			grass.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		}
	}
	private void drawPlane_anti(Vector3i base, int xxx, int yyy, int zzz, int type) {
		//Log.warning("Add plane Z : " + (base.x + xxx) + ", " + (base.y + yyy) + ", " + (base.z + zzz));
		Vector3f v1 = new Vector3f(base.x + xxx  , base.y + yyy  , base.z + zzz);
		Vector3f v2 = new Vector3f(base.x + xxx  , base.y + yyy+1, base.z + zzz);
		Vector3f v3 = new Vector3f(base.x + xxx+1, base.y + yyy+1, base.z + zzz);
		Vector3f v4 = new Vector3f(base.x + xxx+1, base.y + yyy  , base.z + zzz);
		Vector2f t1 = new Vector2f(0, 0);
		Vector2f t2 = new Vector2f(0, 1);
		Vector2f t3 = new Vector2f(1, 1);
		Vector2f t4 = new Vector2f(1, 0);
		Vector3f n1 = new Vector3f(0, 0, 1);
		if (type == VoxelType.NATIVE_UNBREAKABLE) {
			unbreakable.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_DIRT) {
			dirt.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_STONE) {
			stone.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_GRASS) {
			grass.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		}
	}
	private void drawPlaneVerticalX(Vector3i base, int xxx, int yyy, int zzz, int type) {
		//Log.warning("Add plane X : " + (base.x + xxx) + ", " + (base.y + yyy) + ", " + (base.z + zzz));
		Vector3f v1 = new Vector3f(base.x + xxx  , base.y + yyy  , base.z + zzz);
		Vector3f v2 = new Vector3f(base.x + xxx  , base.y + yyy  , base.z + zzz+1);
		Vector3f v3 = new Vector3f(base.x + xxx  , base.y + yyy+1, base.z + zzz+1);
		Vector3f v4 = new Vector3f(base.x + xxx  , base.y + yyy+1, base.z + zzz);
		Vector2f t1 = new Vector2f(0, 0);
		Vector2f t2 = new Vector2f(0, 1);
		Vector2f t3 = new Vector2f(1, 1);
		Vector2f t4 = new Vector2f(1, 0);
		Vector3f n1 = new Vector3f(-1, 0, 0);
		if (type == VoxelType.NATIVE_UNBREAKABLE) {
			unbreakable.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_DIRT) {
			dirt.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_STONE) {
			stone.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_GRASS) {
			grass.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		}
	}
	private void drawPlaneVerticalX_anti(Vector3i base, int xxx, int yyy, int zzz, int type) {
		//Log.warning("Add plane X : " + (base.x + xxx) + ", " + (base.y + yyy) + ", " + (base.z + zzz));
		Vector3f v1 = new Vector3f(base.x + xxx  , base.y + yyy  , base.z + zzz);
		Vector3f v2 = new Vector3f(base.x + xxx  , base.y + yyy  , base.z + zzz+1);
		Vector3f v3 = new Vector3f(base.x + xxx  , base.y + yyy+1, base.z + zzz+1);
		Vector3f v4 = new Vector3f(base.x + xxx  , base.y + yyy+1, base.z + zzz);
		Vector2f t1 = new Vector2f(0, 0);
		Vector2f t2 = new Vector2f(0, 1);
		Vector2f t3 = new Vector2f(1, 1);
		Vector2f t4 = new Vector2f(1, 0);
		Vector3f n1 = new Vector3f(1, 0, 0);
		if (type == VoxelType.NATIVE_UNBREAKABLE) {
			unbreakable.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_DIRT) {
			dirt.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_STONE) {
			stone.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_GRASS) {
			grass.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		}
	}
	private void drawPlaneVerticalY(Vector3i base, int xxx, int yyy, int zzz, int type) {
		//Log.warning("Add plane Y : " + (base.x + xxx) + ", " + (base.y + yyy) + ", " + (base.z + zzz));
		Vector3f v1 = new Vector3f(base.x + xxx  , base.y + yyy  , base.z + zzz);
		Vector3f v2 = new Vector3f(base.x + xxx  , base.y + yyy  , base.z + zzz+1);
		Vector3f v3 = new Vector3f(base.x + xxx+1, base.y + yyy  , base.z + zzz+1);
		Vector3f v4 = new Vector3f(base.x + xxx+1, base.y + yyy  , base.z + zzz);
		Vector2f t1 = new Vector2f(0, 0);
		Vector2f t2 = new Vector2f(0, 1);
		Vector2f t3 = new Vector2f(1, 1);
		Vector2f t4 = new Vector2f(1, 0);
		Vector3f n1 = new Vector3f(0, 1, 0);
		if (type == VoxelType.NATIVE_UNBREAKABLE) {
			unbreakable.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_DIRT) {
			dirt.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_STONE) {
			stone.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_GRASS) {
			grass.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		}
	}
	private void drawPlaneVerticalY_anti(Vector3i base, int xxx, int yyy, int zzz, int type) {
		//Log.warning("Add plane Y : " + (base.x + xxx) + ", " + (base.y + yyy) + ", " + (base.z + zzz));
		Vector3f v1 = new Vector3f(base.x + xxx  , base.y + yyy  , base.z + zzz);
		Vector3f v2 = new Vector3f(base.x + xxx  , base.y + yyy  , base.z + zzz+1);
		Vector3f v3 = new Vector3f(base.x + xxx+1, base.y + yyy  , base.z + zzz+1);
		Vector3f v4 = new Vector3f(base.x + xxx+1, base.y + yyy  , base.z + zzz);
		Vector2f t1 = new Vector2f(0, 0);
		Vector2f t2 = new Vector2f(0, 1);
		Vector2f t3 = new Vector2f(1, 1);
		Vector2f t4 = new Vector2f(1, 0);
		Vector3f n1 = new Vector3f(0, -1, 0);
		if (type == VoxelType.NATIVE_UNBREAKABLE) {
			unbreakable.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_DIRT) {
			dirt.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_STONE) {
			stone.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_GRASS) {
			grass.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		}
	}
	
	@Override
	public void update(float timeStep) {
		Log.warning("update : " + timeStep);
		if (chunk.haveChange() == false) {
			return;
		}
		Log.warning("    ==> YES");
		Voxel[][][] data = chunk.getData();
		unbreakable.clear();
		stone.clear();
		dirt.clear();
		grass.clear();
		for (int xxx=0; xxx < VoxelChunk.VOXEL_CHUNK_SIZE; xxx++) {
			for (int yyy=0; yyy < VoxelChunk.VOXEL_CHUNK_SIZE; yyy++) {
				for (int zzz=0; zzz < VoxelChunk.VOXEL_CHUNK_SIZE; zzz++) {
					Voxel current = data[xxx][yyy][zzz];
					if (current.active == false) {
						continue;
					}
					Voxel bottom = chunk.getVoxel(xxx, yyy, zzz-1);
					if (bottom == null || bottom.active == false) {
						drawPlane(chunk.getPosition(), xxx, yyy, zzz, current.type);
					}
					Voxel up = chunk.getVoxel(xxx, yyy, zzz+1);
					if (up == null || up.active == false) {
						drawPlane_anti(chunk.getPosition(), xxx, yyy, zzz+1, current.type);
					}
					Voxel left = chunk.getVoxel(xxx-1, yyy, zzz);
					if (left == null || left.active == false) {
						drawPlaneVerticalX(chunk.getPosition(), xxx, yyy, zzz, current.type);
					}
					Voxel right = chunk.getVoxel(xxx+1, yyy, zzz);
					if (right == null || right.active == false) {
						drawPlaneVerticalX_anti(chunk.getPosition(), xxx+1, yyy, zzz, current.type);
					}
					Voxel front = chunk.getVoxel(xxx, yyy-1, zzz);
					if (front == null || front.active == false) {
						drawPlaneVerticalY_anti(chunk.getPosition(), xxx, yyy, zzz, current.type);
					}
					Voxel back = chunk.getVoxel(xxx, yyy+1, zzz);
					if (back == null || back.active == false) {
						drawPlaneVerticalY(chunk.getPosition(), xxx, yyy+1, zzz, current.type);
					}
				}
			}
		}
		unbreakable.flush();
		stone.flush();
		dirt.flush();
		grass.flush();
	}

	@Override
	public void render(String name) {
		//Log.warning("Render : " + name);
		super.render(name);
	}
	
}
