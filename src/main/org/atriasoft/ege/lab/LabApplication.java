package org.atriasoft.ege.lab;

import java.util.function.Supplier;

import org.atriasoft.ege.Ege;
import org.atriasoft.etk.Configs;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.Ewol;
import org.atriasoft.ewol.context.EwolApplication;
import org.atriasoft.ewol.context.EwolContext;
import org.atriasoft.gale.Gale;

/**
 * Runs a {@link Lab} in a window of its own: the engine layers started (Gale,
 * Ewol, Ege), the window of the lab opened ({@link LabWindow}), the lab
 * closed with the window.
 *
 * <pre>
 * public static void main(final String[] args) {
 * 	LabApplication.run(args, () -&gt; new FloraLab(FloraOptions.parse(args)));
 * }
 * </pre>
 */
public final class LabApplication implements EwolApplication {

	/** Size of the window, pixels. */
	static final Vector2f SIZE = new Vector2f(1280, 760);

	private final Supplier<Lab> factory;
	private LabWindow window;

	private LabApplication(final Supplier<Lab> factory) {
		this.factory = factory;
	}

	/** Open the window of the lab made by {@code factory} (on the GUI thread) and run until it closes. */
	public static int run(final String[] args, final Supplier<Lab> factory) {
		Gale.init();
		Ewol.init();
		Ege.init();
		Uri.setGroup("DATA", "data");
		Uri.setApplication(LabApplication.class, "resources/ege");
		return Ewol.run(new LabApplication(factory), args);
	}

	@Override
	public void onCreate(final EwolContext context) {
		context.setSize(SIZE);
		Configs.getConfigFonts().set("FreeSherif", 13);
		this.window = new LabWindow(this.factory.get());
		context.setWindows(this.window);
	}

	@Override
	public void onDestroy(final EwolContext context) {
		if (this.window != null) {
			this.window.close();
		}
	}

	@Override
	public void onPause(final EwolContext context) {
		// Nothing: the lab keeps its state.
	}

	@Override
	public void onResume(final EwolContext context) {
		// Nothing.
	}

	@Override
	public void onStart(final EwolContext context) {
		// Nothing.
	}

	@Override
	public void onStop(final EwolContext context) {
		// Nothing.
	}
}
