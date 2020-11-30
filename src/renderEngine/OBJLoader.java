package renderEngine;

import models.RawModel;
import objConverter.ModelData;
import objConverter.OBJFileLoader;

public class OBJLoader {
	public static RawModel loadObjModel(String fileName, Loader loader) {
		System.out.println("Load file " + fileName);
		ModelData data = OBJFileLoader.loadOBJ(fileName);
		return loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices());
	}
}
