package sample.atriasoft.ege.shadowtest;

import org.atriasoft.ege.celestial.CelestialBody;
import org.atriasoft.ege.shadow.ShadowConfig;
import org.atriasoft.etk.math.Vector2b;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.widget.Button;
import org.atriasoft.ewol.widget.CheckBox;
import org.atriasoft.ewol.widget.Label;
import org.atriasoft.ewol.widget.Sizer;
import org.atriasoft.ewol.widget.Sizer.DisplayMode;
import org.atriasoft.ewol.widget.Slider;
import org.atriasoft.ewol.widget.SplitPane;
import org.atriasoft.ewol.widget.Windows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main window for the shadow test application.
 * Layout: 3D scene on the left, control panel on the right.
 */
public class ShadowWindows extends Windows {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShadowWindows.class);

	private final ShadowScene scene;
	private Button pauseButton;
	private boolean sunPaused = false;
	private float savedAngularSpeed = 0.3f;
	private Label angleLabel;
	private Label inclinationLabel;
	private Label infoLabel;
	private Slider angleSlider;
	private Slider inclinationSlider;
	private Label cascadeLabel;
	private Button pcfButton;
	private Label lampIntensityLabel;

	// --- Static callbacks for connectAuto (prevent GC via weak ref) ---

	private static void onPauseClicked(final ShadowWindows self) {
		final CelestialBody sun = self.scene.getSun();
		final CelestialBody moon = self.scene.getMoon();
		if (self.sunPaused) {
			sun.setAngularSpeed(self.savedAngularSpeed);
			moon.setAngularSpeed(self.savedAngularSpeed);
			self.sunPaused = false;
			self.pauseButton.label("Pause");
			LOGGER.info("Sun/Moon RESUMED (speed={})", self.savedAngularSpeed);
		} else {
			self.savedAngularSpeed = sun.getAngularSpeed();
			sun.setAngularSpeed(0.0f);
			moon.setAngularSpeed(0.0f);
			self.sunPaused = true;
			self.pauseButton.label("Resume");
			LOGGER.info("Sun/Moon PAUSED at angle={}", sun.getCurrentAngle());
		}
	}

	private static void onAngleChanged(final ShadowWindows self, final Float degrees) {
		final float radians = (float) (degrees * Math.PI / 180.0);
		self.scene.getSun().setCurrentAngle(radians);
		// Keep moon in opposition
		self.scene.getMoon().setCurrentAngle(radians + (float) Math.PI);
		self.angleLabel.setPropertyValue(String.format("Angle: %.1f", degrees));
	}

	private static void onInclinationChanged(final ShadowWindows self, final Float degrees) {
		final float radians = (float) (degrees * Math.PI / 180.0);
		self.scene.getSun().setOrbitalInclination(radians);
		// Sync moon inclination
		self.scene.getMoon().setOrbitalInclination(radians);
		self.inclinationLabel.setPropertyValue(String.format("Inclination: %.1f", degrees));
	}

	private static void onFrustumToggled(final ShadowWindows self, final Boolean value) {
		self.scene.setDrawFrustumWireframe(value);
	}

	private static void onLightAABBToggled(final ShadowWindows self, final Boolean value) {
		self.scene.setDrawLightAABB(value);
	}

	private static void onSunDirectionToggled(final ShadowWindows self, final Boolean value) {
		self.scene.setDrawSunDirection(value);
	}

	private static void onCascadeCountChanged(final ShadowWindows self, final Float value) {
		final int count = Math.round(value);
		self.scene.getEngineShadow().getConfig().setCascadeCount(count);
		self.cascadeLabel.setPropertyValue("Cascades: " + count);
	}

	private static void onPcfCycleClicked(final ShadowWindows self) {
		final ShadowConfig config = self.scene.getEngineShadow().getConfig();
		final int current = config.getPcfKernelSize();
		final int next;
		if (current <= 1) {
			next = 3;
		} else if (current <= 3) {
			next = 5;
		} else {
			next = 1;
		}
		config.setPcfKernelSize(next);
		final String label = next == 1 ? "hard (1x1)" : next == 3 ? "medium (3x3)" : "soft (5x5)";
		self.pcfButton.label("PCF: " + label);
	}

	private static void onThumbnailsToggled(final ShadowWindows self, final Boolean value) {
		self.scene.getEngineShadow().setDebugThumbnailEnabled(value);
	}

	private static void onLampsToggled(final ShadowWindows self, final Boolean value) {
		self.scene.setLampsEnabled(value);
	}

	private static void onLampIntensityChanged(final ShadowWindows self, final Float value) {
		self.scene.setLampIntensity(value);
		self.lampIntensityLabel.setPropertyValue(String.format("Intensity: %.1f", value));
	}

	public ShadowWindows() {
		setPropertyTitle("Shadow Test - Phase 2 CSM (ewol)");

		// Create the 3D scene
		this.scene = new ShadowScene();
		this.scene.setPropertyExpand(Vector2b.TRUE);
		this.scene.setPropertyFill(Vector2b.TRUE);

		// Create the control panel
		final Sizer controlPanel = createControlPanel();

		// SplitPane: 3D scene (left, 78%) + controls (right, 22%)
		final SplitPane splitPane = SplitPane.horizontal()
				.splitPosition(0.78f)
				.separatorSize(4)
				.minSizes(300, 180)
				.expand(true, true)
				.fill(true, true);
		splitPane.setFirstWidget(this.scene);
		splitPane.setSecondWidget(controlPanel);

		setSubWidget(splitPane);
	}

	private Sizer createControlPanel() {
		final Sizer panel = new Sizer(DisplayMode.VERTICAL);
		panel.setPropertyExpand(Vector2b.TRUE);
		panel.setPropertyFill(Vector2b.TRUE);

		// --- Title ---
		final Label title = new Label("<b>Shadow Debug Controls</b>");
		panel.subWidgetAdd(title);

		// --- Sun/Moon control section ---
		final Label sunSectionLabel = new Label("<b>Sun / Moon</b>");
		panel.subWidgetAdd(sunSectionLabel);

		// Pause/Resume button
		this.pauseButton = Button.create("Pause");
		this.pauseButton.setPropertyExpand(new Vector2b(true, false));
		this.pauseButton.setPropertyFill(new Vector2b(true, false));
		this.pauseButton.signalClick.connectAuto(this, ShadowWindows::onPauseClicked);
		panel.subWidgetAdd(this.pauseButton);

		// Angle slider (0-360 degrees)
		this.angleLabel = new Label("Angle: 45.0");
		panel.subWidgetAdd(this.angleLabel);

		this.angleSlider = Slider.create()
				.range(0, 360)
				.value(45.0f)
				.step(1.0f);
		this.angleSlider.setPropertyExpand(new Vector2b(true, false));
		this.angleSlider.setPropertyFill(new Vector2b(true, false));
		this.angleSlider.signalValue.connectAuto(this, ShadowWindows::onAngleChanged);
		panel.subWidgetAdd(this.angleSlider);

		// Inclination slider (0-90 degrees) — orbital tilt (summer/winter)
		final float initialInclinationDeg = (float) (0.1 * 180.0 / Math.PI);
		this.inclinationLabel = new Label(String.format("Inclination: %.1f", initialInclinationDeg));
		panel.subWidgetAdd(this.inclinationLabel);

		this.inclinationSlider = Slider.create()
				.range(0, 90)
				.value(initialInclinationDeg)
				.step(1.0f);
		this.inclinationSlider.setPropertyExpand(new Vector2b(true, false));
		this.inclinationSlider.setPropertyFill(new Vector2b(true, false));
		this.inclinationSlider.signalValue.connectAuto(this, ShadowWindows::onInclinationChanged);
		panel.subWidgetAdd(this.inclinationSlider);

		// --- Wireframe section ---
		final Label wireSectionLabel = new Label("<b>Wireframe</b>");
		panel.subWidgetAdd(wireSectionLabel);

		// Camera frustum wireframe
		final CheckBox frustumCheck = CheckBox.create("Camera Frustum");
		frustumCheck.setPropertyExpand(new Vector2b(true, false));
		frustumCheck.setPropertyFill(new Vector2b(true, false));
		frustumCheck.signalValue.connectAuto(this, ShadowWindows::onFrustumToggled);
		panel.subWidgetAdd(frustumCheck);

		// Light AABB wireframe
		final CheckBox aabbCheck = CheckBox.create("Light AABB");
		aabbCheck.setPropertyExpand(new Vector2b(true, false));
		aabbCheck.setPropertyFill(new Vector2b(true, false));
		aabbCheck.signalValue.connectAuto(this, ShadowWindows::onLightAABBToggled);
		panel.subWidgetAdd(aabbCheck);

		// Sun direction line
		final CheckBox sunDirCheck = CheckBox.create("Sun Direction")
				.checked(true);
		sunDirCheck.setPropertyExpand(new Vector2b(true, false));
		sunDirCheck.setPropertyFill(new Vector2b(true, false));
		sunDirCheck.signalValue.connectAuto(this, ShadowWindows::onSunDirectionToggled);
		panel.subWidgetAdd(sunDirCheck);

		// --- Shadow Quality section ---
		final Label shadowSectionLabel = new Label("<b>Shadow Quality</b>");
		panel.subWidgetAdd(shadowSectionLabel);

		// Cascade count slider (1-4)
		this.cascadeLabel = new Label("Cascades: 3");
		panel.subWidgetAdd(this.cascadeLabel);

		final Slider cascadeSlider = Slider.create()
				.range(1, 4)
				.value(3.0f)
				.step(1.0f);
		cascadeSlider.setPropertyExpand(new Vector2b(true, false));
		cascadeSlider.setPropertyFill(new Vector2b(true, false));
		cascadeSlider.signalValue.connectAuto(this, ShadowWindows::onCascadeCountChanged);
		panel.subWidgetAdd(cascadeSlider);

		// PCF cycle button
		this.pcfButton = Button.create("PCF: medium (3x3)");
		this.pcfButton.setPropertyExpand(new Vector2b(true, false));
		this.pcfButton.setPropertyFill(new Vector2b(true, false));
		this.pcfButton.signalClick.connectAuto(this, ShadowWindows::onPcfCycleClicked);
		panel.subWidgetAdd(this.pcfButton);

		// Debug thumbnails checkbox
		final CheckBox thumbnailsCheck = CheckBox.create("Debug Thumbnails")
				.checked(true);
		thumbnailsCheck.setPropertyExpand(new Vector2b(true, false));
		thumbnailsCheck.setPropertyFill(new Vector2b(true, false));
		thumbnailsCheck.signalValue.connectAuto(this, ShadowWindows::onThumbnailsToggled);
		panel.subWidgetAdd(thumbnailsCheck);

		// --- Street Lamps section ---
		final Label lampSectionLabel = new Label("<b>Street Lamps</b>");
		panel.subWidgetAdd(lampSectionLabel);

		final CheckBox lampsCheck = CheckBox.create("Lamps On")
				.checked(true);
		lampsCheck.setPropertyExpand(new Vector2b(true, false));
		lampsCheck.setPropertyFill(new Vector2b(true, false));
		lampsCheck.signalValue.connectAuto(this, ShadowWindows::onLampsToggled);
		panel.subWidgetAdd(lampsCheck);

		this.lampIntensityLabel = new Label("Intensity: 1.0");
		panel.subWidgetAdd(this.lampIntensityLabel);

		final Slider intensitySlider = Slider.create()
				.range(0, 2)
				.value(1.0f)
				.step(0.1f);
		intensitySlider.setPropertyExpand(new Vector2b(true, false));
		intensitySlider.setPropertyFill(new Vector2b(true, false));
		intensitySlider.signalValue.connectAuto(this, ShadowWindows::onLampIntensityChanged);
		panel.subWidgetAdd(intensitySlider);

		// --- Info section ---
		final Label infoSectionLabel = new Label("<b>Info</b>");
		panel.subWidgetAdd(infoSectionLabel);

		this.infoLabel = new Label("Sun: ...");
		this.infoLabel.setPropertyExpand(new Vector2b(true, false));
		this.infoLabel.setPropertyFill(new Vector2b(true, false));
		panel.subWidgetAdd(this.infoLabel);

		return panel;
	}

	@Override
	public void onRegenerateDisplay() {
		super.onRegenerateDisplay();
		// Update info label with sun/moon state
		if (this.infoLabel != null && this.scene != null) {
			final CelestialBody sun = this.scene.getSun();
			final CelestialBody moon = this.scene.getMoon();
			final float angleDeg = (float) (sun.getCurrentAngle() * 180.0 / Math.PI);
			final float inclDeg = (float) (sun.getOrbitalInclination() * 180.0 / Math.PI);
			final Vector3f sunDir = sun.getDirection();
			final Vector3f moonDir = moon.getDirection();
			this.infoLabel.setPropertyValue(String.format(
					"A:%.0f I:%.0f\nSun h=%.2f %s\nMoon h=%.2f %s",
					angleDeg, inclDeg,
					sunDir.y(), sun.isAboveHorizon() ? "above" : "BELOW",
					moonDir.y(), moon.isAboveHorizon() ? "above" : "BELOW"));
			// Sync slider position when animating
			if (!this.sunPaused && this.angleSlider != null) {
				this.angleSlider.setPropertyValue(angleDeg);
			}
		}
	}
}
