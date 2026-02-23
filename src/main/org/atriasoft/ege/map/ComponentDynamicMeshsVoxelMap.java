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

	// Y-up convention: worldX = horizontal, worldY = height (up), worldZ = depth
	// Horizontal plane (floor/ceiling) at constant worldY
	private void drawPlaneHorizontal(final float wx, final float wy, final float wz, final int type) {
		final Vector3f v1 = new Vector3f(wx, wy, wz);
		final Vector3f v2 = new Vector3f(wx + 1, wy, wz);
		final Vector3f v3 = new Vector3f(wx + 1, wy, wz + 1);
		final Vector3f v4 = new Vector3f(wx, wy, wz + 1);
		final Vector2f t1 = new Vector2f(0, 0);
		final Vector2f t2 = new Vector2f(0, 1);
		final Vector2f t3 = new Vector2f(1, 1);
		final Vector2f t4 = new Vector2f(1, 0);
		final Vector3f n1 = new Vector3f(0, -1, 0);
		addQuadToMesh(type, v1, v2, v3, v4, t1, t2, t3, t4, n1);
	}

	private void drawPlaneHorizontal_anti(final float wx, final float wy, final float wz, final int type) {
		final Vector3f v1 = new Vector3f(wx, wy, wz);
		final Vector3f v2 = new Vector3f(wx + 1, wy, wz);
		final Vector3f v3 = new Vector3f(wx + 1, wy, wz + 1);
		final Vector3f v4 = new Vector3f(wx, wy, wz + 1);
		final Vector2f t1 = new Vector2f(0, 0);
		final Vector2f t2 = new Vector2f(0, 1);
		final Vector2f t3 = new Vector2f(1, 1);
		final Vector2f t4 = new Vector2f(1, 0);
		final Vector3f n1 = new Vector3f(0, 1, 0);
		addQuadToMeshReverse(type, v1, v2, v3, v4, t1, t2, t3, t4, n1);
	}

	// Vertical wall perpendicular to X axis (constant worldX)
	private void drawPlaneVerticalX(final float wx, final float wy, final float wz, final int type) {
		final Vector3f v1 = new Vector3f(wx, wy, wz);
		final Vector3f v2 = new Vector3f(wx, wy, wz + 1);
		final Vector3f v3 = new Vector3f(wx, wy + 1, wz + 1);
		final Vector3f v4 = new Vector3f(wx, wy + 1, wz);
		final Vector2f t1 = new Vector2f(0, 0);
		final Vector2f t2 = new Vector2f(0, 1);
		final Vector2f t3 = new Vector2f(1, 1);
		final Vector2f t4 = new Vector2f(1, 0);
		final Vector3f n1 = new Vector3f(-1, 0, 0);
		addQuadToMesh(type, v1, v2, v3, v4, t1, t2, t3, t4, n1);
	}

	private void drawPlaneVerticalX_anti(final float wx, final float wy, final float wz, final int type) {
		final Vector3f v1 = new Vector3f(wx, wy, wz);
		final Vector3f v2 = new Vector3f(wx, wy, wz + 1);
		final Vector3f v3 = new Vector3f(wx, wy + 1, wz + 1);
		final Vector3f v4 = new Vector3f(wx, wy + 1, wz);
		final Vector2f t1 = new Vector2f(0, 0);
		final Vector2f t2 = new Vector2f(0, 1);
		final Vector2f t3 = new Vector2f(1, 1);
		final Vector2f t4 = new Vector2f(1, 0);
		final Vector3f n1 = new Vector3f(1, 0, 0);
		addQuadToMeshReverse(type, v1, v2, v3, v4, t1, t2, t3, t4, n1);
	}

	// Vertical wall perpendicular to Z axis (constant worldZ)
	private void drawPlaneVerticalZ(final float wx, final float wy, final float wz, final int type) {
		final Vector3f v1 = new Vector3f(wx, wy, wz);
		final Vector3f v2 = new Vector3f(wx, wy + 1, wz);
		final Vector3f v3 = new Vector3f(wx + 1, wy + 1, wz);
		final Vector3f v4 = new Vector3f(wx + 1, wy, wz);
		final Vector2f t1 = new Vector2f(0, 0);
		final Vector2f t2 = new Vector2f(0, 1);
		final Vector2f t3 = new Vector2f(1, 1);
		final Vector2f t4 = new Vector2f(1, 0);
		final Vector3f n1 = new Vector3f(0, 0, -1);
		addQuadToMesh(type, v1, v2, v3, v4, t1, t2, t3, t4, n1);
	}

	private void drawPlaneVerticalZ_anti(final float wx, final float wy, final float wz, final int type) {
		final Vector3f v1 = new Vector3f(wx, wy, wz);
		final Vector3f v2 = new Vector3f(wx, wy + 1, wz);
		final Vector3f v3 = new Vector3f(wx + 1, wy + 1, wz);
		final Vector3f v4 = new Vector3f(wx + 1, wy, wz);
		final Vector2f t1 = new Vector2f(0, 0);
		final Vector2f t2 = new Vector2f(0, 1);
		final Vector2f t3 = new Vector2f(1, 1);
		final Vector2f t4 = new Vector2f(1, 0);
		final Vector3f n1 = new Vector3f(0, 0, 1);
		addQuadToMeshReverse(type, v1, v2, v3, v4, t1, t2, t3, t4, n1);
	}

	private void addQuadToMesh(final int type,
			final Vector3f v1, final Vector3f v2, final Vector3f v3, final Vector3f v4,
			final Vector2f t1, final Vector2f t2, final Vector2f t3, final Vector2f t4,
			final Vector3f n1) {
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

	private void addQuadToMeshReverse(final int type,
			final Vector3f v1, final Vector3f v2, final Vector3f v3, final Vector3f v4,
			final Vector2f t1, final Vector2f t2, final Vector2f t3, final Vector2f t4,
			final Vector3f n1) {
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
		final Vector3i base = this.chunk.getPosition();
		// Voxel array indices: xxx = X (horizontal), yyy = Z (depth), zzz = Y (height)
		// World coords (Y-up): worldX = base.x + xxx, worldY = base.y + zzz, worldZ = base.z + yyy
		for (int xxx = 0; xxx < VoxelChunk.VOXEL_CHUNK_SIZE; xxx++) {
			for (int yyy = 0; yyy < VoxelChunk.VOXEL_CHUNK_SIZE; yyy++) {
				for (int zzz = 0; zzz < VoxelChunk.VOXEL_CHUNK_SIZE; zzz++) {
					final Voxel current = data[xxx][yyy][zzz];
					if (!current.active) {
						continue;
					}
					final float worldX = base.x() + xxx;
					final float worldY = base.y() + zzz;
					final float worldZ = base.z() + yyy;
					// Bottom face (zzz-1 = below in height)
					final Voxel bottom = this.chunk.getVoxel(xxx, yyy, zzz - 1);
					if (bottom == null || !bottom.active) {
						drawPlaneHorizontal(worldX, worldY, worldZ, current.type);
					}
					// Top face (zzz+1 = above in height)
					final Voxel up = this.chunk.getVoxel(xxx, yyy, zzz + 1);
					if (up == null || !up.active) {
						drawPlaneHorizontal_anti(worldX, worldY + 1, worldZ, current.type);
					}
					// Left face (xxx-1)
					final Voxel left = this.chunk.getVoxel(xxx - 1, yyy, zzz);
					if (left == null || !left.active) {
						drawPlaneVerticalX(worldX, worldY, worldZ, current.type);
					}
					// Right face (xxx+1)
					final Voxel right = this.chunk.getVoxel(xxx + 1, yyy, zzz);
					if (right == null || !right.active) {
						drawPlaneVerticalX_anti(worldX + 1, worldY, worldZ, current.type);
					}
					// Front face (yyy-1 = Z- in world)
					final Voxel front = this.chunk.getVoxel(xxx, yyy - 1, zzz);
					if (front == null || !front.active) {
						drawPlaneVerticalZ(worldX, worldY, worldZ, current.type);
					}
					// Back face (yyy+1 = Z+ in world)
					final Voxel back = this.chunk.getVoxel(xxx, yyy + 1, zzz);
					if (back == null || !back.active) {
						drawPlaneVerticalZ_anti(worldX, worldY, worldZ + 1, current.type);
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
