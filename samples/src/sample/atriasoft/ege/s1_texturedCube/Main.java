package sample.atriasoft.ege.s1_texturedCube;

import org.atriasoft.ege.Ege;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;

import sample.atriasoft.ege.collisiontest.MainCollisionTest;

public class Main {
	public static void main(final String[] args) {
		Gale.init();
		Ege.init();
		Uri.setGroup("DATA", "data");
		Uri.setGroup("RES", "res");
		// Add some base path of the providers:
		Uri.addLibrary("sample", MainCollisionTest.class, "/resources/s1_textured_cube");
		Uri.addLibrary("loxelEngine", MainCollisionTest.class, "/resources/testDataLoxelEngine");
		Uri.setApplication(MainCollisionTest.class, "/resources");
		Gale.run(new S1Application(), args);
	}
	
	public Main() {}
}
