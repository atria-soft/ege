package sample.atriasoft.ege.loxelEngine;

import org.atriasoft.ege.Ege;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;

public class MainLoxelEnginePerso {
	public static void main(final String[] args) {
		Gale.init();
		Ege.init();
		Uri.setGroup("DATA", "data");
		Uri.setGroup("RES", "res");
		Uri.addLibrary("loxelEngine", MainLoxelEnginePerso.class, "resources/testDataLoxelEngine");
		Uri.setApplication(MainLoxelEnginePerso.class, "resources");
		Gale.run(new LoxelApplicationPerso(), args);
	}
}
