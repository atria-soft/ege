package terrains;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;

import models.RawModel;
import renderEngine.Loader;
import toolbox.Maths;

public class Terrain {
	private static final float SIZE = 800;
	private static final int MAX_HEIGHT = 40;
	private static final int MAX_PIXEL_COLOUR = 256 * 256 * 256;
	
	private float x;
	private float z;
	private RawModel model;
	private TerrainTexturePack texturePack;
	private TerrainTexture blendMap;
	
	private float[][] heights;
	
	public Terrain(final int gridX, final int gridZ, final Loader loader, final TerrainTexturePack texturePack, final TerrainTexture blendMap, final String heightMap) {
		this.texturePack = texturePack;
		this.blendMap = blendMap;
		this.x = gridX * SIZE;
		this.z = gridZ * SIZE;
		this.model = generateTerrain(loader, heightMap);
	}
	
	private Vector3f calculateNormal(final int x, final int z, final BufferedImage image) {
		float heightL = getHeight(x - 1, z, image);
		float heightR = getHeight(x + 1, z, image);
		float heightD = getHeight(x, z - 1, image);
		float heightU = getHeight(x, z + 1, image);
		Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
		normal = normal.normalize();
		return normal;
	}
	
	private RawModel generateTerrain(final Loader loader, final String heightMap) {
		
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File("res/" + heightMap + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int VERTEX_COUNT = image.getHeight();
		this.heights = new float[VERTEX_COUNT][VERTEX_COUNT];
		int count = VERTEX_COUNT * VERTEX_COUNT;
		float[] vertices = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count * 2];
		int[] indices = new int[6 * (VERTEX_COUNT - 1) * (VERTEX_COUNT - 1)];
		int vertexPointer = 0;
		for (int i = 0; i < VERTEX_COUNT; i++) {
			for (int j = 0; j < VERTEX_COUNT; j++) {
				vertices[vertexPointer * 3] = j / ((float) VERTEX_COUNT - 1) * SIZE;
				float height = getHeight(j, i, image);
				this.heights[j][i] = height;
				vertices[vertexPointer * 3 + 1] = height;
				vertices[vertexPointer * 3 + 2] = i / ((float) VERTEX_COUNT - 1) * SIZE;
				Vector3f normal = calculateNormal(j, i, image);
				normals[vertexPointer * 3] = normal.x();
				normals[vertexPointer * 3 + 1] = normal.y();
				normals[vertexPointer * 3 + 2] = normal.z();
				textureCoords[vertexPointer * 2] = j / ((float) VERTEX_COUNT - 1);
				textureCoords[vertexPointer * 2 + 1] = i / ((float) VERTEX_COUNT - 1);
				vertexPointer++;
			}
		}
		int pointer = 0;
		for (int gz = 0; gz < VERTEX_COUNT - 1; gz++) {
			for (int gx = 0; gx < VERTEX_COUNT - 1; gx++) {
				int topLeft = (gz * VERTEX_COUNT) + gx;
				int topRight = topLeft + 1;
				int bottomLeft = ((gz + 1) * VERTEX_COUNT) + gx;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
		return loader.loadToVAO(vertices, textureCoords, normals, indices);
	}
	
	public TerrainTexture getBlendMap() {
		return this.blendMap;
	}
	
	private float getHeight(final int x, final int z, final BufferedImage image) {
		if (x < 0 || x >= image.getWidth() || z < 0 || z >= image.getHeight()) {
			return 0;
		}
		float height = image.getRGB(x, z);
		height += MAX_PIXEL_COLOUR / 2.0f;
		height /= MAX_PIXEL_COLOUR / 2.0f;
		height *= MAX_HEIGHT;
		return height;
	}
	
	public float getHeightOfTerrain(final float worldX, final float worldZ) {
		float terrainX = worldX - this.x;
		float terrainZ = worldZ - this.z;
		float gridSquareSize = SIZE / ((float) this.heights.length - 1);
		int gridX = (int) Math.floor(terrainX / gridSquareSize);
		int gridZ = (int) Math.floor(terrainZ / gridSquareSize);
		if (gridX >= this.heights.length - 1 || gridZ >= this.heights.length - 1 || gridX < 0 || gridZ < 0) {
			return 0;
		}
		float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
		float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;
		float answer;
		if (xCoord <= (1 - zCoord)) {
			answer = Maths.barryCentric(new Vector3f(0, this.heights[gridX][gridZ], 0), new Vector3f(1, this.heights[gridX + 1][gridZ], 0), new Vector3f(0, this.heights[gridX][gridZ + 1], 1),
					new Vector2f(xCoord, zCoord));
		} else {
			answer = Maths.barryCentric(new Vector3f(1, this.heights[gridX + 1][gridZ], 0), new Vector3f(1, this.heights[gridX + 1][gridZ + 1], 1), new Vector3f(0, this.heights[gridX][gridZ + 1], 1),
					new Vector2f(xCoord, zCoord));
		}
		return answer;
	}
	
	public RawModel getModel() {
		return this.model;
	}
	
	public TerrainTexturePack getTexturePack() {
		return this.texturePack;
	}
	
	public float getX() {
		return this.x;
	}
	
	public float getZ() {
		return this.z;
	}
	
	public void setModel(final RawModel model) {
		this.model = model;
	}
	
	public void setX(final float x) {
		this.x = x;
	}
	
	public void setZ(final float z) {
		this.z = z;
	}
}
