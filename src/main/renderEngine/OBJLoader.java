package renderEngine;

import org.atriasoft.etk.Uri;
import org.atriasoft.loader3d.OBJFileLoader;
import org.atriasoft.loader3d.model.ModelData;

import models.RawModel;

public class OBJLoader {
	public static RawModel loadObjModel(Uri fileName, Loader loader) {
		System.out.println("Load file " + fileName);
		final ModelData data = OBJFileLoader.loadOBJ(fileName);
		return loader.loadToVAO(data.vertices(), data.textureCoords(), data.normals(), data.indices());
	}
}
