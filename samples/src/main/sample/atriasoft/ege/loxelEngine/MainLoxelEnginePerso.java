package sample.atriasoft.ege.loxelEngine;

import java.util.logging.LogManager;

import org.atriasoft.ege.Ege;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class MainLoxelEnginePerso {
	public static void main(final String[] args) {
		// Loop-back of logger JDK logging API to SLF4J
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();
		Gale.init();
		Ege.init();
		Uri.setGroup("DATA", "data");
		Uri.setGroup("RES", "res");
		Uri.addLibrary("loxelEngine", MainLoxelEnginePerso.class, "resources/testDataLoxelEngine");
		Uri.setApplication(MainLoxelEnginePerso.class, "resources");
		Gale.run(new LoxelApplicationPerso(), args);
	}
}
