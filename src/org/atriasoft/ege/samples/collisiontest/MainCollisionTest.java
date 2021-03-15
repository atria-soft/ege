package org.atriasoft.ege.samples.collisiontest;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;

public class MainCollisionTest {
	public static void main(final String[] args) {
		Uri.setGroup("DATA", "src/org/atriasoft/ege/samples/LoxelEngine/res/");
		Uri.setGroup("DATA_EGE", "src/org/atriasoft/ege/data/");
		Uri.setGroup("RES", "res");
		Gale.run(new CollisionTestApplication(), args);
	}
}
