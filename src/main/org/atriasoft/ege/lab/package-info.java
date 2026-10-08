/**
 * A small kit to write a lab: a standalone window to look at what a library
 * builds (the trees of eFlora, the buildings of eArchi) without the game.
 * Nothing here knows about trees or buildings.
 * <p>
 * A lab implements {@link org.atriasoft.ege.lab.Lab} and runs with
 * {@link org.atriasoft.ege.lab.LabApplication#run}. The kit gives:
 * <ul>
 * <li>the window ({@code LabWindow}): the 3D view on the left, the control
 * panel docked on the right;</li>
 * <li>the 3D view ({@link org.atriasoft.ege.lab.LabView}): a flat ground ruled
 * every 1, 5 and 25 m (X east red, Z south blue), a sky colour, a sun casting
 * cascaded shadows, a human figure of 1.80 m, an orbit camera that can fly
 * ({@link org.atriasoft.ege.lab.LabCamera}), the info panel and the F1
 * help drawn over the picture;</li>
 * <li>the controls ({@link org.atriasoft.ege.lab.LabControls}): one
 * declaration per action (label, key, kind, callback) makes its widget in the
 * panel (button, check box, drop-down list, minus/plus), its key and its line
 * in the help; a key the kit keeps or never receives is reported and dropped,
 * the control kept ({@link org.atriasoft.ege.lab.LabControls#refusal});</li>
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
 * window stays.
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
