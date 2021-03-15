package org.atriasoft.ege.map;

import org.atriasoft.etk.math.Vector2i;
import org.atriasoft.etk.math.Vector3i;
import org.atriasoft.ege.components.ComponentMap;

// This chunk in designed in 2D no upper chunk, and no lower chunk...
public class VoxelChunk extends ComponentMap {
	
	public final static int VOXEL_CHUNK_SIZE = 32;
	private Voxel[][][] data = new Voxel[VOXEL_CHUNK_SIZE][VOXEL_CHUNK_SIZE][VOXEL_CHUNK_SIZE];
	private final Vector3i position;
	//private VoxelChunk[] neibours = new VoxelChunk[8];
	private final MapVoxel globalMap;
	
	private boolean haveChange = false;
	
	public VoxelChunk(MapVoxel globalMap, Vector3i position) {
		this.globalMap = globalMap;
		this.position = position;
		basicFill();
		haveChange = true;
	}
	// this is a default fill tho have a usable map ==> only 3 layers ...
	private void basicFill() {
		for (int xxx=0; xxx < VOXEL_CHUNK_SIZE; xxx++) {
			for (int yyy=0; yyy < VOXEL_CHUNK_SIZE; yyy++) {
				for (int zzz=0; zzz < VOXEL_CHUNK_SIZE; zzz++) {
					if(zzz < 1) {
						data[xxx][yyy][zzz] = new Voxel(VoxelType.NATIVE_UNBREAKABLE, true);
					} else if(zzz < 8) {
						data[xxx][yyy][zzz] = new Voxel(VoxelType.NATIVE_STONE, true);
					} else if(zzz < 12) {
						data[xxx][yyy][zzz] = new Voxel(VoxelType.NATIVE_DIRT, true);
					} else if(zzz < 13) {
						data[xxx][yyy][zzz] = new Voxel(VoxelType.NATIVE_GRASS, true);
					} else {
						data[xxx][yyy][zzz] = new Voxel(VoxelType.NATIVE_UNKNOWN, false);
					}
				}
			}
		}
	}
	
	public Voxel[][][] getData() {
		return data;
	}
	public boolean haveChange() {
		boolean tmp = haveChange;
		haveChange = false;
		return tmp;
	}
	public Voxel getVoxel(int xxx, int yyy, int zzz) {
		// TODO Auto-generated method stub
		if (xxx < 0) {
			return null;
		}if (xxx >= VOXEL_CHUNK_SIZE) {
			return null;
		}
		if (yyy < 0) {
			return null;
		}if (yyy >= VOXEL_CHUNK_SIZE) {
			return null;
		}
		if (zzz < 0) {
			return null;
		}if (zzz >= VOXEL_CHUNK_SIZE) {
			return null;
		}
		return data[xxx][yyy][zzz];
	}
	public Vector3i getPosition() {
		return position;
	}
}
