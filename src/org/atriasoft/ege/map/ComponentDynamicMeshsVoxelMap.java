package org.atriasoft.ege.map;

import org.atriasoft.ege.components.ComponentDynamicMeshs;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.etk.math.Vector3i;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.loader3d.resources.ResourceListTexturedMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentDynamicMeshsVoxelMap extends ComponentDynamicMeshs {
	static final Logger LOGGER = LoggerFactory.getLogger(ComponentDynamicMeshsVoxelMap.class);
	private final VoxelChunk chunk;
	ResourceListTexturedMesh unbreakable = ResourceListTexturedMesh.create(RenderMode.TRIANGLE);
	ResourceListTexturedMesh stone = ResourceListTexturedMesh.create(RenderMode.TRIANGLE);
	ResourceListTexturedMesh dirt = ResourceListTexturedMesh.create(RenderMode.TRIANGLE);
	ResourceListTexturedMesh grass = ResourceListTexturedMesh.create(RenderMode.TRIANGLE);

	public ComponentDynamicMeshsVoxelMap(final VoxelChunk chunk) {
		this.chunk = chunk;
		setMesh("unbreakable", this.unbreakable);
		setMesh("stone", this.stone);
		setMesh("dirt", this.dirt);
		setMesh("grass", this.grass);
	}

	private void drawPlane(final Vector3i base, final int xxx, final int yyy, final int zzz, final int type) {
		//LOGGER.warn("Add plane Z : {}, {}, {}", (base.x + xxx), (base.y + yyy), (base.z + zzz));
		final Vector3f v1 = new Vector3f(base.x() + xxx, base.y() + yyy, base.z() + zzz);
		final Vector3f v2 = new Vector3f(base.x() + xxx, base.y() + yyy + 1, base.z() + zzz);
		final Vector3f v3 = new Vector3f(base.x() + xxx + 1, base.y() + yyy + 1, base.z() + zzz);
		final Vector3f v4 = new Vector3f(base.x() + xxx + 1, base.y() + yyy, base.z() + zzz);
		final Vector2f t1 = new Vector2f(0, 0);
		final Vector2f t2 = new Vector2f(0, 1);
		final Vector2f t3 = new Vector2f(1, 1);
		final Vector2f t4 = new Vector2f(1, 0);
		final Vector3f n1 = new Vector3f(0, 0, -1);
		if (type == VoxelType.NATIVE_UNBREAKABLE) {
			this.unbreakable.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_DIRT) {
			this.dirt.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_STONE) {
			this.stone.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_GRASS) {
			this.grass.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		}
	}

	private void drawPlane_anti(final Vector3i base, final int xxx, final int yyy, final int zzz, final int type) {
		//LOGGER.warn("Add plane Z : {}, {}, {}", (base.x + xxx), (base.y + yyy), (base.z + zzz));
		final Vector3f v1 = new Vector3f(base.x() + xxx, base.y() + yyy, base.z() + zzz);
		final Vector3f v2 = new Vector3f(base.x() + xxx, base.y() + yyy + 1, base.z() + zzz);
		final Vector3f v3 = new Vector3f(base.x() + xxx + 1, base.y() + yyy + 1, base.z() + zzz);
		final Vector3f v4 = new Vector3f(base.x() + xxx + 1, base.y() + yyy, base.z() + zzz);
		final Vector2f t1 = new Vector2f(0, 0);
		final Vector2f t2 = new Vector2f(0, 1);
		final Vector2f t3 = new Vector2f(1, 1);
		final Vector2f t4 = new Vector2f(1, 0);
		final Vector3f n1 = new Vector3f(0, 0, 1);
		if (type == VoxelType.NATIVE_UNBREAKABLE) {
			this.unbreakable.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_DIRT) {
			this.dirt.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_STONE) {
			this.stone.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_GRASS) {
			this.grass.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		}
	}

	private void drawPlaneVerticalX(final Vector3i base, final int xxx, final int yyy, final int zzz, final int type) {
		//LOGGER.warn("Add plane X : {}, {}, {}", (base.x + xxx), (base.y + yyy), (base.z + zzz));
		final Vector3f v1 = new Vector3f(base.x() + xxx, base.y() + yyy, base.z() + zzz);
		final Vector3f v2 = new Vector3f(base.x() + xxx, base.y() + yyy, base.z() + zzz + 1);
		final Vector3f v3 = new Vector3f(base.x() + xxx, base.y() + yyy + 1, base.z() + zzz + 1);
		final Vector3f v4 = new Vector3f(base.x() + xxx, base.y() + yyy + 1, base.z() + zzz);
		final Vector2f t1 = new Vector2f(0, 0);
		final Vector2f t2 = new Vector2f(0, 1);
		final Vector2f t3 = new Vector2f(1, 1);
		final Vector2f t4 = new Vector2f(1, 0);
		final Vector3f n1 = new Vector3f(-1, 0, 0);
		if (type == VoxelType.NATIVE_UNBREAKABLE) {
			this.unbreakable.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_DIRT) {
			this.dirt.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_STONE) {
			this.stone.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_GRASS) {
			this.grass.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		}
	}

	private void drawPlaneVerticalX_anti(
			final Vector3i base,
			final int xxx,
			final int yyy,
			final int zzz,
			final int type) {
		//LOGGER.warn("Add plane X : {}, {}, {}", (base.x + xxx), (base.y + yyy), (base.z + zzz));
		final Vector3f v1 = new Vector3f(base.x() + xxx, base.y() + yyy, base.z() + zzz);
		final Vector3f v2 = new Vector3f(base.x() + xxx, base.y() + yyy, base.z() + zzz + 1);
		final Vector3f v3 = new Vector3f(base.x() + xxx, base.y() + yyy + 1, base.z() + zzz + 1);
		final Vector3f v4 = new Vector3f(base.x() + xxx, base.y() + yyy + 1, base.z() + zzz);
		final Vector2f t1 = new Vector2f(0, 0);
		final Vector2f t2 = new Vector2f(0, 1);
		final Vector2f t3 = new Vector2f(1, 1);
		final Vector2f t4 = new Vector2f(1, 0);
		final Vector3f n1 = new Vector3f(1, 0, 0);
		if (type == VoxelType.NATIVE_UNBREAKABLE) {
			this.unbreakable.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_DIRT) {
			this.dirt.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_STONE) {
			this.stone.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_GRASS) {
			this.grass.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		}
	}

	private void drawPlaneVerticalY(final Vector3i base, final int xxx, final int yyy, final int zzz, final int type) {
		//LOGGER.warn("Add plane Y : {}, {}, {}", (base.x + xxx), (base.y + yyy), (base.z + zzz));
		final Vector3f v1 = new Vector3f(base.x() + xxx, base.y() + yyy, base.z() + zzz);
		final Vector3f v2 = new Vector3f(base.x() + xxx, base.y() + yyy, base.z() + zzz + 1);
		final Vector3f v3 = new Vector3f(base.x() + xxx + 1, base.y() + yyy, base.z() + zzz + 1);
		final Vector3f v4 = new Vector3f(base.x() + xxx + 1, base.y() + yyy, base.z() + zzz);
		final Vector2f t1 = new Vector2f(0, 0);
		final Vector2f t2 = new Vector2f(0, 1);
		final Vector2f t3 = new Vector2f(1, 1);
		final Vector2f t4 = new Vector2f(1, 0);
		final Vector3f n1 = new Vector3f(0, 1, 0);
		if (type == VoxelType.NATIVE_UNBREAKABLE) {
			this.unbreakable.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_DIRT) {
			this.dirt.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_STONE) {
			this.stone.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		} else if (type == VoxelType.NATIVE_GRASS) {
			this.grass.addQuad(v1, v2, v3, v4, t1, t2, t3, t4, n1);
		}
	}

	private void drawPlaneVerticalY_anti(
			final Vector3i base,
			final int xxx,
			final int yyy,
			final int zzz,
			final int type) {
		//LOGGER.warn("Add plane Y : {}, {}, {}", (base.x + xxx), (base.y + yyy), (base.z + zzz));
		final Vector3f v1 = new Vector3f(base.x() + xxx, base.y() + yyy, base.z() + zzz);
		final Vector3f v2 = new Vector3f(base.x() + xxx, base.y() + yyy, base.z() + zzz + 1);
		final Vector3f v3 = new Vector3f(base.x() + xxx + 1, base.y() + yyy, base.z() + zzz + 1);
		final Vector3f v4 = new Vector3f(base.x() + xxx + 1, base.y() + yyy, base.z() + zzz);
		final Vector2f t1 = new Vector2f(0, 0);
		final Vector2f t2 = new Vector2f(0, 1);
		final Vector2f t3 = new Vector2f(1, 1);
		final Vector2f t4 = new Vector2f(1, 0);
		final Vector3f n1 = new Vector3f(0, -1, 0);
		if (type == VoxelType.NATIVE_UNBREAKABLE) {
			this.unbreakable.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_DIRT) {
			this.dirt.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_STONE) {
			this.stone.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		} else if (type == VoxelType.NATIVE_GRASS) {
			this.grass.addQuad(v1, v4, v3, v2, t1, t4, t3, t2, n1);
		}
	}

	@Override
	public void render(final String name) {
		//LOGGER.warn("Render : {}", name);
		super.render(name);
	}

	@Override
	public void update(final float timeStep) {
		LOGGER.warn("update : {}", timeStep);
		if (!this.chunk.haveChange()) {
			return;
		}
		LOGGER.warn("    ==> YES");
		final Voxel[][][] data = this.chunk.getData();
		this.unbreakable.clear();
		this.stone.clear();
		this.dirt.clear();
		this.grass.clear();
		for (int xxx = 0; xxx < VoxelChunk.VOXEL_CHUNK_SIZE; xxx++) {
			for (int yyy = 0; yyy < VoxelChunk.VOXEL_CHUNK_SIZE; yyy++) {
				for (int zzz = 0; zzz < VoxelChunk.VOXEL_CHUNK_SIZE; zzz++) {
					final Voxel current = data[xxx][yyy][zzz];
					if (!current.active) {
						continue;
					}
					final Voxel bottom = this.chunk.getVoxel(xxx, yyy, zzz - 1);
					if (bottom == null || !bottom.active) {
						drawPlane(this.chunk.getPosition(), xxx, yyy, zzz, current.type);
					}
					final Voxel up = this.chunk.getVoxel(xxx, yyy, zzz + 1);
					if (up == null || !up.active) {
						drawPlane_anti(this.chunk.getPosition(), xxx, yyy, zzz + 1, current.type);
					}
					final Voxel left = this.chunk.getVoxel(xxx - 1, yyy, zzz);
					if (left == null || !left.active) {
						drawPlaneVerticalX(this.chunk.getPosition(), xxx, yyy, zzz, current.type);
					}
					final Voxel right = this.chunk.getVoxel(xxx + 1, yyy, zzz);
					if (right == null || !right.active) {
						drawPlaneVerticalX_anti(this.chunk.getPosition(), xxx + 1, yyy, zzz, current.type);
					}
					final Voxel front = this.chunk.getVoxel(xxx, yyy - 1, zzz);
					if (front == null || !front.active) {
						drawPlaneVerticalY_anti(this.chunk.getPosition(), xxx, yyy, zzz, current.type);
					}
					final Voxel back = this.chunk.getVoxel(xxx, yyy + 1, zzz);
					if (back == null || !back.active) {
						drawPlaneVerticalY(this.chunk.getPosition(), xxx, yyy + 1, zzz, current.type);
					}
				}
			}
		}
		this.unbreakable.flush();
		this.stone.flush();
		this.dirt.flush();
		this.grass.flush();
	}

}
