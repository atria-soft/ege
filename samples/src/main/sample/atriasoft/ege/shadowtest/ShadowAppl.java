package sample.atriasoft.ege.shadowtest;

import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.context.EwolApplication;
import org.atriasoft.ewol.context.EwolContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EwolApplication for the shadow test sample.
 * Creates a ShadowWindows with 3D scene + debug controls.
 */
public class ShadowAppl implements EwolApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShadowAppl.class);

	@Override
	public void onCreate(final EwolContext context) {
		LOGGER.info("ShadowAppl onCreate: [BEGIN]");
		context.setSize(new Vector2f(1280, 800));
		final ShadowWindows windows = new ShadowWindows();
		context.setWindows(windows);
		LOGGER.info("ShadowAppl onCreate: [ END ]");
	}

	@Override
	public void onDestroy(final EwolContext context) {
		LOGGER.info("ShadowAppl onDestroy");
	}

	@Override
	public void onPause(final EwolContext context) {
	}

	@Override
	public void onResume(final EwolContext context) {
	}

	@Override
	public void onStart(final EwolContext context) {
	}

	@Override
	public void onStop(final EwolContext context) {
	}
}
