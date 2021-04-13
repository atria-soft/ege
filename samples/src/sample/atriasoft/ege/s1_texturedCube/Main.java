package sample.atriasoft.ege.s1_texturedCube;

import org.atriasoft.ege.Ege;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;

import sample.atriasoft.ege.collisiontest.MainCollisionTest;

public class Main {
	public static void main(String[] args) {
		Gale.init();
		Ege.init();
		Uri.setGroup("DATA", "data/");
		Uri.setGroup("RES", "res");
		Uri.addLibrary("sample", MainCollisionTest.class, "s1_textured_cube/");
		Uri.setApplication(MainCollisionTest.class, "");
		Uri.addLibrary("loxelEngine", MainCollisionTest.class, "testDataLoxelEngine/");
		Gale.run(new S1Application(), args);
	}
	
	public Main() {}
}
