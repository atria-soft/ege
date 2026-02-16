package sample.atriasoft.ege.shadowtest;

import java.util.logging.LogManager;

import org.atriasoft.ege.Ege;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class MainShadowTest {
	public static void main(final String[] args) {
		// Loop-back of logger JDK logging API to SLF4J
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();
		Gale.init();
		Ege.init();
		Uri.setGroup("DATA", "data");
		Uri.setApplication(MainShadowTest.class, "resources/shadowTest");
		Gale.run(new ShadowTestApplication(), args);
	}
}
