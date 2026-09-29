package sample.atriasoft.ege.skyboxSample;

import java.util.logging.LogManager;

import org.atriasoft.ege.Ege;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Entry point for the skybox sample.
 * <p>
 * Displays a 3D scene with a cubemap skybox, a ground plane and
 * a few cubes. Camera controls: WASD + mouse drag.
 * Press 'R' to toggle skybox rotation.
 */
public class MainSkybox {
	public static void main(final String[] args) {
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();
		Gale.init();
		Ege.init();
		Uri.setGroup("DATA", "data");
		Uri.setGroup("RES", "res");
		Uri.addLibrary("sample", MainSkybox.class, "/resources/s1_textured_cube");
		Uri.addLibrary("loxelEngine", MainSkybox.class, "/resources/testDataLoxelEngine");
		Uri.setApplication(MainSkybox.class, "/resources");
		Gale.run(new SkyboxSampleApplication(), args);
	}

	private MainSkybox() {}
}
