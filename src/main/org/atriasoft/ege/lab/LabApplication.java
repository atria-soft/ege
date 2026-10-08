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
 *
 * The kit's own files are found by their library ({@code new Uri("DATA",
 * "lab/labLit.vert", "ege")}): a lab with files of its own names its
 * application ({@link #run(String[], Class, String, Supplier)}), and
 * {@code new Uri("DATA", "file")} finds them.
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
		return run(args, LabApplication.class, "resources/ege", factory);
	}

	/**
	 * {@link #run(String[], Supplier)} for a lab with files of its own:
	 * {@code DATA:} names the folder {@code data} under {@code resources} of
	 * the classpath of {@code application} ({@code new Uri("DATA",
	 * "theme.json")} is {@code /<resources>/data/theme.json}).
	 */
	public static int run(final String[] args, final Class<?> application, final String resources,
			final Supplier<Lab> factory) {
		Gale.init();
		Ewol.init();
		Ege.init();
		Uri.setGroup("DATA", "data");
		Uri.setApplication(application, resources);
		return Ewol.run(new LabApplication(factory), args);
	}

	@Override
	public void onCreate(final EwolContext context) {
		context.setSize(SIZE);
		Configs.getConfigFonts().set("FreeSherif", 13);
		// The lab is made by the window: a factory that throws is shown there, the window stays.
		this.window = new LabWindow(this.factory);
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
