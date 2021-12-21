package org.atriasoft.phyligram.shape;

import org.atriasoft.ephysics.collision.Triangle;

public class HeightMapChunk extends Shape {
	private Triangle[] triangles;
	public HeightMapChunk(Triangle[] triangles) {
		this.triangles = triangles;
	}
	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
	}
	
}
