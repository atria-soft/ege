package renderEngine;

import org.atriasoft.etk.Uri;

import models.RawModel;
import objConverter.ModelData;
import objConverter.OBJFileLoader;

public class OBJLoader {
	public static RawModel loadObjModel(Uri fileName, Loader loader) {
		System.out.println("Load file " + fileName);
		final ModelData data = OBJFileLoader.loadOBJ(fileName);
		return loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices());
	}
}
