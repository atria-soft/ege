package org.atriasoft.eagle.model;

import org.atriasoft.etk.math.Vector3i;

public class MapChunk {
	public final Vector3i dimention;
	public final Vector3i position;
	private final VoxelMap[] element;
	
	public MapChunk(Vector3i size, Vector3i pos) {
		this.dimention = size;
		this.position = pos;
		this.element = new VoxelMap[this.dimention.x() * this.dimention.y()];
	}
	
	public VoxelMap getValue(int xxx, int yyy) {
		return getValueInternal(xxx - this.position.x(), yyy - this.position.y());
	}
	
	public VoxelMap getValue(Vector3i position) {
		return getValue(position.x(), position.y());
	}
	
	public VoxelMap getValueCreate(int xxx, int yyy) {
		return getValueCreateInternal(xxx - this.position.x(), yyy - this.position.y());
	}
	
	public VoxelMap getValueCreate(Vector3i position) {
		return getValueCreate(position.x(), position.y());
	}
	
	private VoxelMap getValueCreateInternal(int xxx, int yyy) {
		VoxelMap val = this.element[yyy * this.dimention.x() + xxx];
		if (val == null) {
			val = new VoxelMap();
			this.element[this.dimention.y() + yyy * this.dimention.x() + xxx] = val;
		}
		return val;
	}
	
	private VoxelMap getValueInternal(int xxx, int yyy) {
		return this.element[yyy * this.dimention.x() + xxx];
	}
	
	public boolean isStartPosition(int xxx, int yyy) {
		return this.position.x() == xxx & this.position.y() == yyy;
	}
	
}
