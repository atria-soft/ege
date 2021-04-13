package objConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;

public class OBJFileLoader {
	private static float convertDataToArrays(final List<Vertex> vertices, final List<Vector2f> textures, final List<Vector3f> normals, final float[] verticesArray, final float[] texturesArray,
			final float[] normalsArray) {
		float furthestPoint = 0;
		for (int i = 0; i < vertices.size(); i++) {
			final Vertex currentVertex = vertices.get(i);
			if (currentVertex.getLength() > furthestPoint) {
				furthestPoint = currentVertex.getLength();
			}
			final Vector3f position = currentVertex.getPosition();
			final Vector2f textureCoord = textures.get(currentVertex.getTextureIndex());
			final Vector3f normalVector = normals.get(currentVertex.getNormalIndex());
			verticesArray[i * 3] = position.x();
			verticesArray[i * 3 + 1] = position.y();
			verticesArray[i * 3 + 2] = position.z();
			texturesArray[i * 2] = textureCoord.x();
			texturesArray[i * 2 + 1] = 1 - textureCoord.y();
			normalsArray[i * 3] = normalVector.x();
			normalsArray[i * 3 + 1] = normalVector.y();
			normalsArray[i * 3 + 2] = normalVector.z();
		}
		return furthestPoint;
	}
	
	private static int[] convertIndicesListToArray(final List<Integer> indices) {
		final int[] indicesArray = new int[indices.size()];
		for (int i = 0; i < indicesArray.length; i++) {
			indicesArray[i] = indices.get(i);
		}
		return indicesArray;
	}
	
	private static void dealWithAlreadyProcessedVertex(final Vertex previousVertex, final int newTextureIndex, final int newNormalIndex, final List<Integer> indices, final List<Vertex> vertices) {
		if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
			indices.add(previousVertex.getIndex());
		} else {
			final Vertex anotherVertex = previousVertex.getDuplicateVertex();
			if (anotherVertex != null) {
				OBJFileLoader.dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex, newNormalIndex, indices, vertices);
			} else {
				final Vertex duplicateVertex = new Vertex(vertices.size(), previousVertex.getPosition());
				duplicateVertex.setTextureIndex(newTextureIndex);
				duplicateVertex.setNormalIndex(newNormalIndex);
				previousVertex.setDuplicateVertex(duplicateVertex);
				vertices.add(duplicateVertex);
				indices.add(duplicateVertex.getIndex());
			}
			
		}
	}
	
	public static ModelData loadOBJ(final Uri objFileName) {
		final InputStream stream = Uri.getStream(objFileName);
		if (stream == null) {
			Log.error("Can not read file: " + objFileName + " ==> load OBJ");
			return null;
		}
		/*
		final FileReader isr = null;
		final File objFile = new File(objFileName);
		try {
			isr = new FileReader(objFile);
		} catch (final FileNotFoundException e) {
			System.err.println("File not found in res; don't use any extention");
			return null;
		}
		*/
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		} catch (final UnsupportedEncodingException e1) {
			Log.error("Error in loading file: " + objFileName + " ==> load OBJ");
			e1.printStackTrace();
			return null;
		}
		String line;
		final List<Vertex> vertices = new ArrayList<>();
		final List<Vector2f> textures = new ArrayList<>();
		final List<Vector3f> normals = new ArrayList<>();
		final List<Integer> indices = new ArrayList<>();
		try {
			while (true) {
				line = reader.readLine();
				if (line.startsWith("v ")) {
					final String[] currentLine = line.split(" ");
					final Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]));
					final Vertex newVertex = new Vertex(vertices.size(), vertex);
					vertices.add(newVertex);
					
				} else if (line.startsWith("vt ")) {
					final String[] currentLine = line.split(" ");
					final Vector2f texture = new Vector2f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]));
					textures.add(texture);
				} else if (line.startsWith("vn ")) {
					final String[] currentLine = line.split(" ");
					final Vector3f normal = new Vector3f(Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]));
					normals.add(normal);
				} else if (line.startsWith("f ")) {
					break;
				}
			}
			while (line != null && line.startsWith("f ")) {
				final String[] currentLine = line.split(" ");
				final String[] vertex1 = currentLine[1].split("/");
				final String[] vertex2 = currentLine[2].split("/");
				final String[] vertex3 = currentLine[3].split("/");
				OBJFileLoader.processVertex(vertex1, vertices, indices);
				OBJFileLoader.processVertex(vertex2, vertices, indices);
				OBJFileLoader.processVertex(vertex3, vertices, indices);
				line = reader.readLine();
			}
			reader.close();
		} catch (final IOException e) {
			System.err.println("Error reading the file");
		}
		OBJFileLoader.removeUnusedVertices(vertices);
		final float[] verticesArray = new float[vertices.size() * 3];
		final float[] texturesArray = new float[vertices.size() * 2];
		final float[] normalsArray = new float[vertices.size() * 3];
		final float furthest = OBJFileLoader.convertDataToArrays(vertices, textures, normals, verticesArray, texturesArray, normalsArray);
		final int[] indicesArray = OBJFileLoader.convertIndicesListToArray(indices);
		final ModelData data = new ModelData(verticesArray, texturesArray, normalsArray, indicesArray, furthest);
		return data;
	}
	
	private static void processVertex(final String[] vertex, final List<Vertex> vertices, final List<Integer> indices) {
		final int index = Integer.parseInt(vertex[0]) - 1;
		final Vertex currentVertex = vertices.get(index);
		final int textureIndex = Integer.parseInt(vertex[1]) - 1;
		final int normalIndex = Integer.parseInt(vertex[2]) - 1;
		if (!currentVertex.isSet()) {
			currentVertex.setTextureIndex(textureIndex);
			currentVertex.setNormalIndex(normalIndex);
			indices.add(index);
		} else {
			OBJFileLoader.dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices, vertices);
		}
	}
	
	private static void removeUnusedVertices(final List<Vertex> vertices) {
		for (final Vertex vertex : vertices) {
			if (!vertex.isSet()) {
				vertex.setTextureIndex(0);
				vertex.setNormalIndex(0);
			}
		}
	}
	
}