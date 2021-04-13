package sample.atriasoft.ege.lowPoly;

import org.atriasoft.ege.Ege;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;

import sample.atriasoft.ege.collisiontest.MainCollisionTest;

public class MainMowPoly {
	public static void main(String[] args) {
		Gale.init();
		Ege.init();
		Uri.setGroup("DATA", "data/");
		Uri.setGroup("RES", "res");
		Uri.addLibrary("loxelEngine", MainCollisionTest.class, "testDataLoxelEngine/");
		Uri.setApplication(MainCollisionTest.class, "");
		Gale.run(new LowPolyApplication(), args);
	}
}
