package org.atriasoft.ege.map;

public class VoxelType {
	public static final int NATIVE_UNKNOWN  = 0;
	public static final int NATIVE_BASIC_ID = 10000;
	public static final int NATIVE_UNBREAKABLE = NATIVE_BASIC_ID;
	public static final int NATIVE_DIRT        = NATIVE_BASIC_ID + 1;
	public static final int NATIVE_GRASS       = NATIVE_BASIC_ID + 2;
	public static final int NATIVE_STONE       = NATIVE_BASIC_ID + 3;
	public static final int NATIVE_SAND        = NATIVE_BASIC_ID + 4;
	public static final int NATIVE_WOOD        = NATIVE_BASIC_ID + 101;
	public static final int NATIVE_LEAF        = NATIVE_BASIC_ID + 102;
	public static final int NATIVE_WATTER      = NATIVE_BASIC_ID + 201;
	public static final int NATIVE_MAGMA       = NATIVE_BASIC_ID + 202;
	private VoxelType() {};
}
