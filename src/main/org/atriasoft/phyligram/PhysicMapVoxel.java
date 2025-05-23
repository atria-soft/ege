package org.atriasoft.phyligram;

import org.atriasoft.ege.map.VoxelChunk;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.phyligram.shape.AABB;

public class PhysicMapVoxel extends PhysicShape {
	// Box size property in X, Y and Z
	private VoxelChunk chunk;
	// only needed for the narrow phase calculation ...
	private Vector3f narrowPhaseGlobalPos;
	private Vector3f narrowPhaseAxisX = new Vector3f(1, 0, 0);
	private Vector3f narrowPhaseAxisY = new Vector3f(1, 0, 0);
	private Vector3f narrowPhaseAxisZ = new Vector3f(1, 0, 0);
	private Vector3f narrowPhaseHalfSize = new Vector3f(0.5f, 0.5f, 0.5f);
	
	public PhysicMapVoxel(VoxelChunk chunk) {
		this.chunk = chunk;
	}
	
	@Override
	public void renderDebug(Transform3D transform, ResourceColored3DObject debugDrawProperty) {
		
	}
	
	@Override
	public void updateAABB(Transform3D transform, AABB aabb) {
		if (this.chunk == null) {
			return;
		}
		this.colisionPoints.clear();
		aabb.update(new Vector3f(this.chunk.getPosition().x(), this.chunk.getPosition().y(), this.chunk.getPosition().z()));
		aabb.update(new Vector3f(this.chunk.getPosition().x() + VoxelChunk.VOXEL_CHUNK_SIZE, this.chunk.getPosition().y() + VoxelChunk.VOXEL_CHUNK_SIZE,
				this.chunk.getPosition().z() + VoxelChunk.VOXEL_CHUNK_SIZE));
	}
	
	@Override
	public void updateForNarrowCollision(Transform3D transform) {
		this.narrowPhaseGlobalPos = transform.multiply(this.transform.multiply(new Vector3f(0, 0, 0)));
	}
}
