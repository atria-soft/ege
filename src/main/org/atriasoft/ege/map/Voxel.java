package org.atriasoft.ege.map;

public class Voxel {
	public int type = VoxelType.NATIVE_UNKNOWN;
	public boolean active = true;
	public Voxel(int type, boolean active) {
		this.type = type;
		this.active = active;
	}
	
}
