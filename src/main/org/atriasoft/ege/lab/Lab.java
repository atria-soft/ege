package org.atriasoft.ege.lab;

/**
 * A lab: a standalone window to look at what a library builds (trees,
 * buildings...) without the game, run by {@link LabApplication#run}. The kit
 * gives the window, the 3D view and its camera, the control panel, the info
 * panel and the help; the lab declares its controls, builds its content
 * (away from the picture: {@link LabWorkshop}) and says what to show.
 * <p>
 * Every method runs on the GUI thread; whatever they throw is shown in the
 * info panel and the window stays.
 */
public interface Lab {

	/** The title of the window. */
	String title();

	/**
	 * The window is open: declare the controls ({@link LabView#controls()}),
	 * the files to watch ({@link LabView#watch}), ask the first build.
	 */
	void start(LabView view);

	/**
	 * Each frame, {@code seconds} after the one before: take what was built,
	 * give the content ({@link LabView#setContent}, only when it changed) and
	 * the lines of the info panel ({@link LabView#setInfo}).
	 */
	void update(LabView view, float seconds);

	/** The window closes: stop the threads. */
	void close();
}
