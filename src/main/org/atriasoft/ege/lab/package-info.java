/**
 * A small kit to write a lab: a standalone window to look at what a library
 * builds (the trees of eFlora, the buildings of eArchi) without the game.
 * Nothing here knows about trees or buildings.
 * <p>
 * A lab implements {@link org.atriasoft.ege.lab.Lab} and runs with
 * {@link org.atriasoft.ege.lab.LabApplication#run}. The kit gives:
 * <ul>
 * <li>the window ({@code LabWindow}): the 3D view on the left, the control
 * panel docked on the right, its groups scrolling over a footer that keeps
 * the controls of the view and Quit always in sight;</li>
 * <li>the 3D view ({@link org.atriasoft.ege.lab.LabView}): a flat ground ruled
 * every 1, 5 and 25 m (X east red, Z south blue), a sky colour, a sun casting
 * cascaded shadows, a human figure of 1.80 m (placed and shown by the lab
 * too: {@code placeHuman}, {@code showHuman}), an orbit camera that can fly
 * and be turned by the lab ({@link org.atriasoft.ege.lab.LabCamera#setDirection}),
 * the info panel (its lines proportional or monospace:
 * {@link org.atriasoft.ege.lab.LabText.Line#mono(String)}) and the F1 help
 * drawn over the picture; an overlay of the lab's tools over the content
 * ({@code setOverlay}: a grid, a cursor, never framed, casting no shadow); the
 * mouse given to the lab first when it asks
 * ({@link org.atriasoft.ege.lab.LabPointer}, {@code setPointer}: the ground
 * point under the pointer, a press taken with its drag and release, the rest
 * to the camera);</li>
 * <li>the controls ({@link org.atriasoft.ege.lab.LabControls}): one
 * declaration per action (label, key, kind, callback) makes its widget in the
 * panel (button, check box, drop-down list, minus/plus, a text field taking
 * what is typed on Enter, a palette of buttons each with its key), its key
 * (alone or with Control: {@link org.atriasoft.ege.lab.LabKey#ctrl}) and its
 * line in the help; a key the kit keeps or never receives is reported and
 * dropped, the control kept ({@link org.atriasoft.ege.lab.LabControls#refusal});</li>
 * <li>the content ({@link org.atriasoft.ege.lab.LabMesh}, built with
 * {@link org.atriasoft.ege.lab.LabShapes}): opaque flat-shaded triangles
 * (casting shadows), translucent ones, lines hidden or on top;</li>
 * <li>a build thread where the newest request wins
 * ({@link org.atriasoft.ege.lab.LabWorkshop}) and a watcher of data files
 * ({@link org.atriasoft.ege.lab.LabWatcher}, through
 * {@link org.atriasoft.ege.lab.LabView#watch}).</li>
 * </ul>
 * Whatever a lab throws (its factory, its start, a build, a control, an
 * update, a reading) is shown in red at the top of the info panel until the
 * same thing succeeds again ({@link org.atriasoft.ege.lab.LabReporter}); the
 * window stays. A lab reports there what must never fall off the panel (a
 * data file that broke): {@code view.report(what, message)}, then
 * {@code view.clear(what)}.
 * <p>
 * A lab is seen by an automated session only in a virtual X server:
 * {@code ege/tools/lab-smoke.sh LAB_SCRIPT OUTDIR "ARGS" STEP...} (each lab
 * keeps a wrapper naming its launcher).
 *
 * <pre>
 * public final class MyLab implements Lab {
 * 	private final LabWorkshop&lt;Long, Thing&gt; workshop = new LabWorkshop&lt;&gt;("my-lab", Things::build);
 * 	private long seed = 1;
 * 	private boolean helpers;
 * 	private LabWorkshop.Result&lt;Long, Thing&gt; shown;
 * 	private boolean shownHelpers;
 *
 * 	public String title() { return "My lab"; }
 *
 * 	public void start(final LabView view) {
 * 		view.controls().group("Subject")
 * 				.stepper("Seed", LabKey.of('u'), LabKey.of('i'), () -&gt; Long.toString(this.seed),
 * 						() -&gt; this.workshop.request(--this.seed), () -&gt; this.workshop.request(++this.seed))
 * 				.group("Debug")
 * 				.toggle("Helpers", LabKey.of('h'), () -&gt; this.helpers, value -&gt; this.helpers = value);
 * 		this.workshop.request(this.seed);
 * 	}
 *
 * 	public void update(final LabView view, final float seconds) {
 * 		final LabWorkshop.Result&lt;Long, Thing&gt; latest = this.workshop.latest();
 * 		if (latest != null &amp;&amp; (latest != this.shown || this.helpers != this.shownHelpers)) {
 * 			this.shown = latest;
 * 			this.shownHelpers = this.helpers;
 * 			view.setContent(latest.failed() ? new LabMesh() : latest.model().mesh(this.helpers));
 * 		}
 * 		view.setInfo(List.of(LabText.Line.title("Seed " + this.seed)));
 * 	}
 *
 * 	public void close() { this.workshop.close(); }
 * }
 * </pre>
 */
package org.atriasoft.ege.lab;
