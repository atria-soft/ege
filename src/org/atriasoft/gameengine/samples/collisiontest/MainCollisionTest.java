package org.atriasoft.gameengine.samples.collisiontest;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;

public class MainCollisionTest {
	public static void main(final String[] args) {
		Uri.setGroup("DATA", "src/org/atriasoft/gameengine/samples/LoxelEngine/res/");
		Uri.setGroup("DATA_EGE", "src/org/atriasoft/gameengine/data/");
		Uri.setGroup("RES", "res");
		Gale.run(new CollisionTestApplication(), args);
	}
}
