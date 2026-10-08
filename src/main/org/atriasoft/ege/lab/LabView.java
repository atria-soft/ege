package org.atriasoft.ege.lab;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.camera.ProjectionPerspective;
import org.atriasoft.ege.celestial.CelestialBody;
import org.atriasoft.ege.celestial.CelestialBodyType;
import org.atriasoft.ege.engines.EngineShadow;
import org.atriasoft.ege.shadow.ShadowConfig;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector2i;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.Ewol;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.widget.Widget;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeyStatus;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The 3D view of a lab, and what a {@link Lab} drives: the content it shows
 * ({@link #setContent}), the lines of its info panel ({@link #setInfo}), its
 * controls ({@link #controls()}), the files it watches ({@link #watch}),
 * the problems it reports ({@link #report}).
 * <p>
 * It draws a flat ground ruled every 1, 5 and 25 m (the axes X east in red,
 * Z south in blue) under a sky colour, lit by a sun casting cascaded shadows
 * (ege's {@link EngineShadow}), the content, a human figure of 1.80 m beside
 * it (a toggle), then the info panel and the help of the keys over the
 * picture. The mouse drives a {@link LabCamera}: left drag turns (around the
 * target, or the look in a flight), right or middle drag (or Shift and left)
 * pans, the wheel zooms; the arrows and Page up/down drive it too (held keys:
 * {@link #holdKey}).
 * <p>
 * Every throwable of the lab (an update, a control, a watcher) is caught and
 * shown in red at the top of the info panel until the same thing succeeds
 * again, never past the view. On the GUI thread.
 */
public final class LabView extends Widget implements LabReporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(LabView.class);
	/** Horizontal field of view, radians. */
	static final float FOV_X = (float) Math.toRadians(70.0);
	private static final Color SKY = new Color(0.64f, 0.77f, 0.90f, 1.0f);
	private static final Vector2f MIN_SIZE = new Vector2f(320, 240);
	/** Seconds between two looks at the watched files. */
	static final double POLL_SECONDS = 1.0;
	/** The human figure stands this far west of the content, metres. */
	static final float HUMAN_GAP = 1.0f;
	/** The group of the kit's own controls, after those of the lab. */
	public static final String VIEW_GROUP = "View";
	/** The labels of the kit's own controls that take a key. */
	static final String FRAME = "Frame the model";
	static final String HUMAN = "Human figure 1.80 m";
	static final String FLIGHT = "Free flight";
	static final String HELP = "Key help";
	/** What the problems are reported under. */
	static final String UPDATE = "update";
	static final String PANEL = "panel";
	static final String READING = "reading the data";
	static final String RENDERING = "3D view";

	/** Files watched and what their change runs. */
	private record Watch(LabWatcher watcher, Runnable onChange) {}

	private final Environement env = new Environement();
	/** The ege camera the environment and its shadows take; {@link #labCamera} drives it. */
	private final Camera egeCamera = new Camera();
	private final ProjectionPerspective projection = new ProjectionPerspective();
	private final LabCamera labCamera = new LabCamera();
	private final LabControls controls = new LabControls();
	private final Map<String, String> errors = new LinkedHashMap<>();
	private final List<Watch> watches = new ArrayList<>();
	private Lab lab;
	private List<LabText.Line> info = List.of();
	private LabMesh content;
	private LabMesh human;
	/** Where the human figure stands, {@code null}: beside the content. */
	private Vector3f humanSpot;
	private boolean humanShown = true;
	private boolean helpShown;
	private boolean frameNext = true;
	/** Held keys of the camera: ahead (Up, Down), side (Right, Left), rise (Page up, Page down). */
	private boolean up;
	private boolean down;
	private boolean left;
	private boolean right;
	private boolean pageUp;
	private boolean pageDown;
	/** A drag: the button held and where the pointer was. */
	private int dragButton;
	private boolean dragPans;
	private Vector2f dragFrom;
	private long lastNanos;
	private double sincePoll;
	private Runnable afterUpdate;
	private LabRenderer renderer;
	/** Whether the renderer could not be made (told once, never tried again). */
	private boolean rendererFailed;
	private LabInfoPanel panel;
	private boolean detached;

	public LabView() {
		this.propertyCanFocus = true;
		setMouseLimit(2);
		this.env.addCamera("default", this.egeCamera);
		this.projection.setAngleViewRad(FOV_X);
		final ShadowConfig config = this.env.getEngineShadow().getConfig();
		config.setCascadeCount(3);
		config.setShadowMapResolution(2048);
		config.setShadowDistance(160.0f);
		config.setPcfKernelSize(3);
		final Vector3f toSun = LabRenderer.TO_SUN;
		// CelestialBody: x = cos(angle), y = sin(angle) cos(inclination), z = sin(angle) sin(inclination).
		final CelestialBody sun = new CelestialBody(CelestialBodyType.SUN, 0.0f, (float) Math.atan2(toSun.z(), toSun.y()),
				(float) Math.acos(toSun.x()), Color.WHITE, 1.0f, true);
		this.env.getCelestialSystem().addBody(sun);
		this.env.getCelestialSystem().update(0L);
		this.controls.setReporter(this);
		reserveKitKeys(this.controls);
		this.human = humanBeside(null);
		markToRedraw();
	}

	/** The keys of the kit's own controls, kept before a lab declares its controls. */
	static void reserveKitKeys(final LabControls controls) {
		controls.reserve(LabKey.of('f'), FRAME);
		controls.reserve(LabKey.of(KeyKeyboard.F1), HELP);
		controls.reserve(LabKey.of(KeyKeyboard.F2), HUMAN);
		controls.reserve(LabKey.of(KeyKeyboard.F3), FLIGHT);
	}

	// ---- What a lab drives -----------------------------------------------------------------------------

	/** The controls of the lab: declare them in {@link Lab#start}; the panel, the keys and the help follow. */
	public LabControls controls() {
		return this.controls;
	}

	/** Show {@code mesh} from the next frame on (the one before is given back); framed when asked so. */
	public void setContent(final LabMesh mesh) {
		this.content = mesh;
		this.human = humanBeside(mesh != null ? mesh.bounds() : null);
		if (this.frameNext && mesh != null && !mesh.isEmpty()) {
			this.frameNext = false;
			frame();
		}
	}

	/** The content shown, {@code null} before the first one. */
	public LabMesh content() {
		return this.content;
	}

	/** Frame the next content given (a new subject, another size of picture). */
	public void frameNextContent() {
		this.frameNext = true;
	}

	/**
	 * Put the camera where it sees the whole content, from where it looks
	 * now, in the part of the picture right of the info panel.
	 */
	public void frame() {
		final LabMesh shown = this.content;
		float[] box = shown != null ? shown.bounds() : null;
		if (box == null) {
			box = new float[] { -2.0f, 0.0f, -2.0f, 2.0f, 2.0f, 2.0f };
		}
		final Vector2f size = getSize();
		if (!(size.x() > 0.0f) || !(size.y() > 0.0f)) {
			this.labCamera.frame(box, FOV_X, 1.0f, 0.0f);
			return;
		}
		final float covered = this.info.isEmpty() ? 0.0f : LabInfoPanel.width(size) + 2.0f * LabInfoPanel.MARGIN;
		this.labCamera.frame(box, FOV_X, aspect(), Math.min(0.6f, covered / size.x()));
	}

	/**
	 * Where the human figure stands, {@code null}: west of the content, half
	 * way along it (the default).
	 */
	public void placeHuman(final Vector3f spot) {
		this.humanSpot = spot;
		this.human = humanBeside(this.content != null ? this.content.bounds() : null);
	}

	/** The lines of the info panel (the problems reported come first). */
	public void setInfo(final List<LabText.Line> lines) {
		this.info = lines != null ? List.copyOf(lines) : List.of();
	}

	/** Where the eye is (a level of detail by distance). */
	public Vector3f eye() {
		return this.labCamera.eye();
	}

	/** The camera: to frame a part of the content, to change its mode. */
	public LabCamera camera() {
		return this.labCamera;
	}

	/**
	 * Watch {@code files} (asked at every poll): once one of them changed and
	 * stayed the same for a poll of a second, {@code onChange} runs (read
	 * them again). A throwable of {@code onChange} is reported until a reading
	 * succeeds.
	 *
	 * @return the watcher: {@link LabWatcher#markRead()} after reading them on demand (F5)
	 */
	public LabWatcher watch(final Supplier<List<Path>> files, final Runnable onChange) {
		final LabWatcher watcher = new LabWatcher(files);
		this.watches.add(new Watch(watcher, onChange));
		return watcher;
	}

	/**
	 * Show {@code error} in red in the info panel, as what went wrong in
	 * {@code what}, until {@link #clear}; logged once while it stays the same.
	 */
	@Override
	public void report(final String what, final Throwable error) {
		final String message = what + ": " + describe(error);
		if (!message.equals(this.errors.put(what, message))) {
			LOGGER.error("Lab: {} failed: {}", what, error.toString(), error);
		}
	}

	/** Show {@code message} in red in the info panel under {@code what}, until {@link #clear}. */
	@Override
	public void report(final String what, final String message) {
		final String line = what + ": " + message;
		if (!line.equals(this.errors.put(what, line))) {
			LOGGER.warn("Lab: {}", line);
		}
	}

	/** Forget the problem reported under {@code what}. */
	@Override
	public void clear(final String what) {
		this.errors.remove(what);
	}

	/** The problems shown now, each {@code what: message}. */
	public List<String> problems() {
		return List.copyOf(this.errors.values());
	}

	/** {@code IllegalStateException: no species} or the class and message of the cause that says most. */
	static String describe(final Throwable error) {
		Throwable shown = error;
		while (shown.getMessage() == null && shown.getCause() != null && shown.getCause() != shown) {
			shown = shown.getCause();
		}
		final String name = shown.getClass().getSimpleName();
		return shown.getMessage() != null ? name + ": " + shown.getMessage() : name;
	}

	// ---- Wiring with the window ------------------------------------------------------------------------

	/** Drive {@code next} from now on (started by the window). */
	void attach(final Lab next) {
		this.lab = next;
	}

	/** Run after each update of the lab (the panel takes the state of the controls). */
	void setAfterUpdate(final Runnable run) {
		this.afterUpdate = run;
	}

	/** Declare the controls of the view itself, after those of the lab: they close the panel. */
	void addViewControls() {
		this.controls.declareAsKit(() -> {
			this.controls.group(VIEW_GROUP);
			this.controls.action(FRAME, LabKey.of('f'), this::frame);
			this.controls.toggle(HUMAN, LabKey.of(KeyKeyboard.F2), () -> this.humanShown,
					value -> this.humanShown = value);
			this.controls.toggle(FLIGHT, LabKey.of(KeyKeyboard.F3), () -> this.labCamera.mode() == LabCamera.Mode.FLY,
					value -> this.labCamera.setMode(value ? LabCamera.Mode.FLY : LabCamera.Mode.ORBIT));
			this.controls.toggle(HELP, LabKey.of(KeyKeyboard.F1), () -> this.helpShown,
					value -> this.helpShown = value);
			this.controls.action("Quit", null, () -> Ewol.getContext().exit(0));
		});
	}

	/** Whether {@code type} is a key of the camera: the arrows and Page up/down. */
	static boolean isCameraKey(final KeyKeyboard type) {
		return switch (type) {
			case UP, DOWN, LEFT, RIGHT, PAGE_UP, PAGE_DOWN -> true;
			default -> false;
		};
	}

	/**
	 * A camera key going down or up: the arrows and Page up/down, held.
	 *
	 * @return whether it is one of them
	 */
	boolean holdKey(final KeyKeyboard type, final boolean isDown) {
		switch (type) {
			case UP -> this.up = isDown;
			case DOWN -> this.down = isDown;
			case LEFT -> this.left = isDown;
			case RIGHT -> this.right = isDown;
			case PAGE_UP -> this.pageUp = isDown;
			case PAGE_DOWN -> this.pageDown = isDown;
			default -> {
				return false;
			}
		}
		return true;
	}

	/** The human figure where it was placed, else west of {@code box}, half way along it (or beside the origin). */
	private LabMesh humanBeside(final float[] box) {
		final LabMesh figure = new LabMesh();
		if (this.humanSpot != null) {
			LabShapes.human(figure, this.humanSpot.x(), this.humanSpot.y(), this.humanSpot.z());
		} else if (box == null) {
			LabShapes.human(figure, -1.5f, 0.0f, 0.0f);
		} else {
			LabShapes.human(figure, box[0] - HUMAN_GAP, 0.0f, (box[2] + box[5]) * 0.5f);
		}
		return figure;
	}

	private float aspect() {
		final Vector2f size = getSize();
		return size.y() > 0.0f ? size.x() / size.y() : 1.0f;
	}

	private float fovY() {
		return (float) (2.0 * Math.atan(Math.tan(FOV_X * 0.5) / aspect()));
	}

	/** The lines over the picture: the problems first (never cut off by a long panel), then those of the lab. */
	private List<LabText.Line> infoLines() {
		if (this.errors.isEmpty()) {
			return this.info;
		}
		final List<LabText.Line> lines = new ArrayList<>();
		lines.add(LabText.Line.bad("Problems"));
		for (final String error : this.errors.values()) {
			lines.add(LabText.Line.bad(error));
		}
		lines.add(LabText.Line.gap());
		lines.addAll(this.info);
		return lines;
	}

	private List<LabText.Line> helpLines() {
		if (!this.helpShown) {
			return List.of();
		}
		final List<LabText.Line> lines = new ArrayList<>();
		lines.add(LabText.Line.title("Keys and mouse"));
		for (final String line : this.controls.help()) {
			lines.add(line.startsWith(" ") ? LabText.Line.normal(line) : LabText.Line.warn(line));
		}
		lines.add(LabText.Line.warn("Camera"));
		lines.add(LabText.Line.normal("  Left drag: turn around (the look in a flight)"));
		lines.add(LabText.Line.normal("  Right or middle drag, Shift + left drag: pan"));
		lines.add(LabText.Line.normal("  Wheel: closer, farther (ahead in a flight)"));
		lines.add(LabText.Line.normal("  Arrows: turn and tilt (fly in a flight)"));
		lines.add(LabText.Line.normal("  Page up / down: zoom (rise and sink in a flight)"));
		return lines;
	}

	// ---- Widget ------------------------------------------------------------------------------------------

	@Override
	public void calculateMinMaxSize() {
		super.calculateMinMaxSize();
		this.minSize = MIN_SIZE;
		checkMinSize();
	}

	@Override
	public void onChangeSize() {
		super.onChangeSize();
		this.projection.updateMatrix(getSize());
	}

	/** Each frame: the camera keys, the update of the lab, the watched files, the panels; asks the next frame. */
	@Override
	public void onRegenerateDisplay() {
		needRedraw();
		if (!this.detached) {
			final long now = System.nanoTime();
			final float seconds = this.lastNanos == 0L ? 0.0f : (float) Math.min(0.25, (now - this.lastNanos) * 1.0e-9);
			this.lastNanos = now;
			this.env.periodicCall();
			this.labCamera.drive((this.up ? 1 : 0) - (this.down ? 1 : 0), (this.right ? 1 : 0) - (this.left ? 1 : 0),
					(this.pageUp ? 1 : 0) - (this.pageDown ? 1 : 0), seconds);
			final Lab driven = this.lab;
			if (driven != null) {
				try {
					driven.update(this, seconds);
					clear(UPDATE);
				} catch (final Throwable e) {
					report(UPDATE, e);
				}
			}
			poll(seconds);
			if (this.afterUpdate != null) {
				try {
					this.afterUpdate.run();
					clear(PANEL);
				} catch (final Throwable e) {
					report(PANEL, e);
				}
			}
			if (this.panel == null) {
				this.panel = new LabInfoPanel();
			}
			this.panel.build(getSize(), infoLines(), helpLines());
		}
		// The next frame (the picture moves: a growing model, a held key, a build that comes).
		markToRedraw();
	}

	/** Once a second, the watched files. */
	private void poll(final float seconds) {
		this.sincePoll += seconds;
		if (this.sincePoll < POLL_SECONDS) {
			return;
		}
		this.sincePoll = 0.0;
		for (final Watch watch : this.watches) {
			try {
				if (watch.watcher().poll()) {
					watch.onChange().run();
					clear(READING);
				}
			} catch (final Throwable e) {
				report(READING, e);
			}
		}
	}

	@Override
	protected void onDraw() {
		if (this.detached) {
			return;
		}
		final Matrix4f widgetCamera = OpenGL.getCameraMatrix();
		if (this.renderer == null && !this.rendererFailed) {
			try {
				this.renderer = new LabRenderer(this.env.getEngineShadow());
			} catch (final Throwable e) {
				// Never tried again: the panels still work.
				this.rendererFailed = true;
				report(RENDERING, e);
			}
		}
		if (this.renderer != null) {
			this.renderer.show(this.content, this.humanShown ? this.human : null);
		}
		final Matrix4f viewMatrix = this.labCamera.view();
		final Vector3f eyePosition = this.labCamera.eye();
		this.egeCamera.setViewMatrix(viewMatrix);
		this.egeCamera.setPosition(eyePosition);
		final EngineShadow shadow = this.env.getEngineShadow();
		shadow.setCameraAspectRatio(aspect());
		shadow.setCameraFovY(fovY());
		final Vector2f origin = getOrigin();
		final Vector2f size = getSize();
		OpenGL.push();
		OpenGL.setMatrix(this.projection.getMatrix());
		OpenGL.setViewPort(origin, size);
		// The sky over this view alone (glClear ignores the viewport).
		OpenGL.enable(Flag.flag_scissorTest);
		OpenGL.updateAllFlags();
		GL11.glScissor((int) origin.x(), (int) origin.y(), (int) size.x(), (int) size.y());
		OpenGL.clearColor(SKY);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_colorBuffer);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.disable(Flag.flag_scissorTest);
		OpenGL.enable(Flag.flag_depthTest);
		OpenGL.updateAllFlags();
		try {
			if (this.renderer != null) {
				// The depth passes of the shadows (the content casts through LabRenderer), then the ege entities (none).
				this.env.render(20, "default");
				OpenGL.setViewPort(new Vector2i((int) origin.x(), (int) origin.y()),
						new Vector2i((int) size.x(), (int) size.y()));
				this.renderer.draw(this.projection.getMatrix(), viewMatrix, eyePosition);
			}
			clear(RENDERING);
		} catch (final Throwable e) {
			report(RENDERING, e);
		}
		OpenGL.disable(Flag.flag_depthTest);
		OpenGL.updateAllFlags();
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.pop();
		OpenGL.setCameraMatrix(widgetCamera);
		if (this.panel != null) {
			this.panel.draw();
		}
		OpenGL.enable(Flag.flag_blend);
		OpenGL.updateAllFlags();
		OpenGL.blendFuncAuto();
	}

	@Override
	public boolean onEventInput(final EventInput event) {
		final Vector2f position = relativePosition(event.pos());
		final int button = event.inputId();
		if ((button == 4 || button == 5) && event.status() == KeyStatus.down) {
			// gale: 5 for the wheel turned away from the user (closer), 4 towards (farther).
			this.labCamera.zoom(button == 5 ? 1.0f : -1.0f);
			return true;
		}
		if (button < 1 || button > 3) {
			return false;
		}
		switch (event.status()) {
			case down -> {
				this.dragButton = button;
				this.dragFrom = position;
				this.dragPans = button != 1 || event.specialKey() != null && event.specialKey().getShift();
			}
			case move -> {
				if (this.dragFrom != null && button == this.dragButton) {
					final float dx = position.x() - this.dragFrom.x();
					final float dy = position.y() - this.dragFrom.y();
					this.dragFrom = position;
					if (this.dragPans) {
						this.labCamera.pan(dx, dy, getSize().y(), fovY());
					} else {
						this.labCamera.turn(dx, dy);
					}
				}
			}
			case up, abort -> {
				if (button == this.dragButton) {
					this.dragFrom = null;
					this.dragButton = 0;
				}
			}
			default -> {
				// Clicks, enter, leave: nothing.
			}
		}
		return true;
	}

	/**
	 * Stop drawing and give everything back: the renderer, the panels, the
	 * shadow maps (the OpenGL objects on the rendering thread). Idempotent.
	 */
	void release() {
		if (this.detached) {
			return;
		}
		this.detached = true;
		this.lab = null;
		if (this.renderer != null) {
			this.renderer.release();
			this.renderer = null;
		}
		if (this.panel != null) {
			this.panel.release();
			this.panel = null;
		}
		this.env.getEngineShadow().release();
	}
}
