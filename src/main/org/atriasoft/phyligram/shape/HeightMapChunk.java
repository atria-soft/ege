package org.atriasoft.phyligram.shape;

import org.atriasoft.ege.geometry.Triangle;

public class HeightMapChunk extends Shape {
	private Triangle[] triangles;
	
	public HeightMapChunk(Triangle[] triangles) {
		this.triangles = triangles;
	}
	
	@Override
	public void display() {
		super.display();
	}
	
}
