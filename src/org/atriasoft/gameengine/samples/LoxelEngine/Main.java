package org.atriasoft.gameengine.samples.LoxelEngine;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;

public class Main {
	public static void main(String[] args) {
		Uri.setGroup("DATA", "src/org/atriasoft/gameengine/samples/LoxelEngine/res/");
		Uri.setGroup("DATA_EGE", "src/org/atriasoft/gameengine/data/");
		Uri.setGroup("RES", "res");
		Gale.run(new LoxelApplication(), args);
	}
}
