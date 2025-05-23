package org.atriasoft.eagle;

import org.atriasoft.etk.math.Vector3i;

public class CirclePointGenerator {
	private Vector3i dataList[];
	private int maxIndex = 0;
	private int currentIndex = -1;
	
	public CirclePointGenerator(Vector3i position, int distance) {
		Vector3i startPosition = new Vector3i(position.x() - distance / 2, position.y() - distance / 2, position.z());
		this.dataList = new Vector3i[distance * distance + 1];
		int square = distance * distance;
		for (int xxx = 0; xxx < distance; xxx++) {
			for (int yyy = 0; yyy < distance; yyy++) {
				if (position.distance2(startPosition.x() + xxx, startPosition.y() + yyy, position.z()) < square) {
					this.dataList[this.maxIndex] = new Vector3i(startPosition.x() + xxx, startPosition.y() + yyy, position.z());
					this.maxIndex++;
				}
			}
		}
		this.maxIndex--;
	}
	
	public Vector3i getValue() {
		return this.dataList[this.currentIndex];
	}
	
	public boolean hasNext() {
		if (this.currentIndex < this.maxIndex) {
			this.currentIndex++;
			return true;
		}
		return false;
	}
	
}
