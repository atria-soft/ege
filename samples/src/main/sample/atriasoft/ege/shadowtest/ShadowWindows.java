package sample.atriasoft.ege.shadowtest;

import org.atriasoft.ege.celestial.CelestialBody;
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
	private Label elevationLabel;
	private Label infoLabel;
	private Slider angleSlider;
	private Slider inclinationSlider;
	private Slider elevationSlider;

	// --- Static callbacks for connectAuto (prevent GC via weak ref) ---

	private static void onPauseClicked(final ShadowWindows self) {
		final CelestialBody sun = self.scene.getSun();
		if (self.sunPaused) {
			sun.setAngularSpeed(self.savedAngularSpeed);
			self.sunPaused = false;
			self.pauseButton.label("Pause Sun");
			LOGGER.info("Sun RESUMED (speed={})", self.savedAngularSpeed);
		} else {
			self.savedAngularSpeed = sun.getAngularSpeed();
			sun.setAngularSpeed(0.0f);
			self.sunPaused = true;
			self.pauseButton.label("Resume Sun");
			LOGGER.info("Sun PAUSED at angle={}", sun.getCurrentAngle());
		}
	}

	private static void onAngleChanged(final ShadowWindows self, final Float degrees) {
		final float radians = (float) (degrees * Math.PI / 180.0);
		self.scene.getSun().setCurrentAngle(radians);
		self.angleLabel.setPropertyValue(String.format("Angle: %.1f", degrees));
	}

	private static void onInclinationChanged(final ShadowWindows self, final Float degrees) {
		final float radians = (float) (degrees * Math.PI / 180.0);
		self.scene.getSun().setOrbitalInclination(radians);
		self.inclinationLabel.setPropertyValue(String.format("Inclination: %.1f", degrees));
	}

	private static void onElevationChanged(final ShadowWindows self, final Float degrees) {
		final float radians = (float) (degrees * Math.PI / 180.0);
		self.scene.getSun().setOrbitalElevation(radians);
		self.elevationLabel.setPropertyValue(String.format("Elevation: %.1f", degrees));
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

		// --- Sun control section ---
		final Label sunSectionLabel = new Label("<b>Sun</b>");
		panel.subWidgetAdd(sunSectionLabel);

		// Pause/Resume button
		this.pauseButton = Button.create("Pause Sun");
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

		// Elevation slider (-90 to +90 degrees) — raises orbit above horizon
		this.elevationLabel = new Label("Elevation: 0.0");
		panel.subWidgetAdd(this.elevationLabel);

		this.elevationSlider = Slider.create()
				.range(-90, 90)
				.value(0.0f)
				.step(1.0f);
		this.elevationSlider.setPropertyExpand(new Vector2b(true, false));
		this.elevationSlider.setPropertyFill(new Vector2b(true, false));
		this.elevationSlider.signalValue.connectAuto(this, ShadowWindows::onElevationChanged);
		panel.subWidgetAdd(this.elevationSlider);

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
		// Update info label with sun state
		if (this.infoLabel != null && this.scene != null) {
			final CelestialBody sun = this.scene.getSun();
			final float angleDeg = (float) (sun.getCurrentAngle() * 180.0 / Math.PI);
			final float inclDeg = (float) (sun.getOrbitalInclination() * 180.0 / Math.PI);
			final float elevDeg = (float) (sun.getOrbitalElevation() * 180.0 / Math.PI);
			final Vector3f dir = sun.getDirection();
			this.infoLabel.setPropertyValue(String.format(
					"A:%.0f I:%.0f E:%.0f\nh=%.2f %s",
					angleDeg, inclDeg, elevDeg, dir.z(),
					sun.isAboveHorizon() ? "above" : "BELOW"));
			// Sync slider position when sun is animating
			if (!this.sunPaused && this.angleSlider != null) {
				this.angleSlider.setPropertyValue(angleDeg);
			}
		}
	}
}
