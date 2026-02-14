package sample.atriasoft.ege.mapFactory;

import java.util.logging.LogManager;

import org.atriasoft.ege.Ege;
import org.atriasoft.etk.Uri;
import org.atriasoft.ewol.Ewol;
import org.atriasoft.gale.Gale;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class MapFactoryMain {
	public static void main(final String[] args) {
		// Loop-back of logger JDK logging API to SLF4J
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();
		Gale.init();
		Ewol.init();
		Ege.init();
		Uri.setGroup("DATA", "data");
		//Uri.setGroup("RES", "res");
		//Uri.addLibrary("loxelEngine", MapFactoryMain.class, "/resources/testDataLoxelEngine");
		//Uri.addLibrary("plop", Appl.class, "/resources/mapFactory");
		Uri.setApplication(Appl.class, "resources/mapFactory");
		Ewol.run(new Appl(), args);
	}

	private MapFactoryMain() {}
}
