package sample.atriasoft.ege.mapFactory;

import org.atriasoft.ege.Ege;
import org.atriasoft.etk.Uri;
import org.atriasoft.ewol.Ewol;
import org.atriasoft.gale.Gale;

public class MapFactoryMain {
	public static void main(final String[] args) {
		Gale.init();
		Ewol.init();
		Ege.init();
		Uri.setGroup("DATA", "data/");
		//Uri.setGroup("RES", "res");
		//Uri.addLibrary("loxelEngine", MapFactoryMain.class, "testDataLoxelEngine/");
		//Uri.addLibrary("plop", Appl.class, "resources/mapFactory/");
		Uri.setApplication(Appl.class, "mapFactory");//, "resources/mapFactory/");
		Ewol.run(new Appl(), args);
	}
	
	private MapFactoryMain() {}
}
