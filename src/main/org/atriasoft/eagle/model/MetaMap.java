package org.atriasoft.eagle.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.atriasoft.etk.math.Vector3i;

public class MetaMap {
	private final long originalSeed;
	private Vector3i size = Vector3i.VALUE_128;
	private final Random randGenerator;
	private final List<MapChunk> elementChunk = new ArrayList<>();
	
	public MetaMap(long seed) {
		this.originalSeed = seed;
		this.randGenerator = new Random(seed);
		this.elementChunk.add(new MapChunk(this.size, Vector3i.ZERO));
	}
	
	public Random getRand() {
		return this.randGenerator;
	}
	
	public VoxelMap getValue(int xxx, int yyy) {
		//find start
		int startX = (xxx / this.size.x()) * this.size.x();
		int startY = (yyy / this.size.y()) * this.size.y();
		for (MapChunk elem : this.elementChunk) {
			if (elem.isStartPosition(startX, startY)) {
				return elem.getValue(xxx, yyy);
			}
		}
		return null;
	}
	
	public VoxelMap getValue(Vector3i pos) {
		// TODO Auto-generated method stub
		return getValue(pos.x(), pos.y());
	}
	
	public VoxelMap getValueCreate(int xxx, int yyy) {
		//find start
		int startX = (xxx / this.size.x()) * this.size.x();
		int startY = (yyy / this.size.y()) * this.size.y();
		for (MapChunk elem : this.elementChunk) {
			if (elem.isStartPosition(startX, startY)) {
				return elem.getValueCreate(xxx, yyy);
			}
		}
		MapChunk newElem = new MapChunk(this.size, new Vector3i(startX, startY, 0));
		this.elementChunk.add(newElem);
		return newElem.getValueCreate(xxx, yyy);
	}
	
	public VoxelMap getValueCreate(Vector3i pos) {
		// TODO Auto-generated method stub
		return getValue(pos.x(), pos.y());
	}
}
