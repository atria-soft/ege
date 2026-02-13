package sample.atriasoft.ege.collisiontest;

import org.atriasoft.ege.Ege;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;

public class MainCollisionTest {
	public static void main(final String[] args) {
		Gale.init();
		Ege.init();
		Uri.setGroup("DATA", "data/");
		Uri.setGroup("RES", "res");
		Uri.addLibrary("loxelEngine", MainCollisionTest.class, "resources/testDataLoxelEngine");
		Uri.setApplication(MainCollisionTest.class, "resources");
		Gale.run(new CollisionTestApplication(), args);
	}
}
