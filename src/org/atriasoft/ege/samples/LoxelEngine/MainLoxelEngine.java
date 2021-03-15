package org.atriasoft.ege.samples.LoxelEngine;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;

public class MainLoxelEngine {
	public static void main(final String[] args) {
		Uri.setGroup("DATA", "src/org/atriasoft/ege/samples/LoxelEngine/res/");
		Uri.setGroup("DATA_EGE", "src/org/atriasoft/ege/data/");
		Uri.setGroup("RES", "res");
		Gale.run(new LoxelApplication(), args);
	}
}
