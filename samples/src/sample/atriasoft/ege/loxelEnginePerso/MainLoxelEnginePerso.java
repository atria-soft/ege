package sample.atriasoft.ege.loxelEnginePerso;

import org.atriasoft.ege.Ege;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;

import sample.atriasoft.ege.collisiontest.MainCollisionTest;

public class MainLoxelEnginePerso {
	public static void main(final String[] args) {
		Gale.init();
		Ege.init();
		Uri.setGroup("DATA", "data/");
		Uri.setGroup("RES", "res");
		Uri.addLibrary("loxelEngine", MainLoxelEnginePerso.class, "testDataLoxelEngine/");
		Uri.setApplication(MainLoxelEnginePerso.class, "");
		Gale.run(new LoxelApplicationPerso(), args);
	}
}
