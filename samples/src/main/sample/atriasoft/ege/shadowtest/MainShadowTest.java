package sample.atriasoft.ege.shadowtest;

import java.util.logging.LogManager;

import org.atriasoft.ege.Ege;
import org.atriasoft.etk.Uri;
import org.atriasoft.ewol.Ewol;
import org.atriasoft.gale.Gale;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class MainShadowTest {
	public static void main(final String[] args) {
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();
		Gale.init();
		Ewol.init();
		Ege.init();
		Uri.setGroup("DATA", "data");
		Uri.setApplication(MainShadowTest.class, "resources/shadowTest");
		Ewol.run(new ShadowAppl(), args);
	}
}
