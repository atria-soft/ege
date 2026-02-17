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
	private boolean sunPaused = false;
	private float savedAngularSpeed = 0.3f;
	private Label angleLabel;
	private Label infoLabel;
	private Slider angleSlider;

	public ShadowWindows() {
		setPropertyTitle("Shadow Test - Phase 2 CSM (ewol)");

		// Create the 3D scene
		this.scene = new ShadowScene();
		this.scene.setPropertyExpand(Vector2b.TRUE);
		this.scene.setPropertyFill(Vector2b.TRUE);

		// Create the control panel
		final Sizer controlPanel = createControlPanel();

		// SplitPane: 3D scene (left, 75%) + controls (right, 25%)
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
		final Button pauseButton = Button.create("Pause Sun");
		pauseButton.setPropertyExpand(new Vector2b(true, false));
		pauseButton.setPropertyFill(new Vector2b(true, false));
		pauseButton.signalClick.connect(() -> {
			final CelestialBody sun = this.scene.getSun();
			if (this.sunPaused) {
				sun.setAngularSpeed(this.savedAngularSpeed);
				this.sunPaused = false;
				pauseButton.label("Pause Sun");
				LOGGER.info("Sun RESUMED (speed={})", this.savedAngularSpeed);
			} else {
				this.savedAngularSpeed = sun.getAngularSpeed();
				sun.setAngularSpeed(0.0f);
				this.sunPaused = true;
				pauseButton.label("Resume Sun");
				LOGGER.info("Sun PAUSED at angle={}", sun.getCurrentAngle());
			}
		});
		panel.subWidgetAdd(pauseButton);

		// Angle slider (0-360 degrees)
		this.angleLabel = new Label("Angle: 45.0");
		panel.subWidgetAdd(this.angleLabel);

		this.angleSlider = Slider.create()
				.range(0, 360)
				.value((float) (Math.PI * 0.25 * 180.0 / Math.PI))
				.step(1.0f)
				.onValueChange(this::onAngleChanged);
		this.angleSlider.setPropertyExpand(new Vector2b(true, false));
		this.angleSlider.setPropertyFill(new Vector2b(true, false));
		panel.subWidgetAdd(this.angleSlider);

		// --- Wireframe section ---
		final Label wireSectionLabel = new Label("<b>Wireframe</b>");
		panel.subWidgetAdd(wireSectionLabel);

		// Camera frustum wireframe
		final CheckBox frustumCheck = CheckBox.create("Camera Frustum")
				.checked(false)
				.onValueChange(value -> this.scene.setDrawFrustumWireframe(value));
		frustumCheck.setPropertyExpand(new Vector2b(true, false));
		frustumCheck.setPropertyFill(new Vector2b(true, false));
		panel.subWidgetAdd(frustumCheck);

		// Light AABB wireframe
		final CheckBox aabbCheck = CheckBox.create("Light AABB")
				.checked(false)
				.onValueChange(value -> this.scene.setDrawLightAABB(value));
		aabbCheck.setPropertyExpand(new Vector2b(true, false));
		aabbCheck.setPropertyFill(new Vector2b(true, false));
		panel.subWidgetAdd(aabbCheck);

		// Sun direction line
		final CheckBox sunDirCheck = CheckBox.create("Sun Direction")
				.checked(true)
				.onValueChange(value -> this.scene.setDrawSunDirection(value));
		sunDirCheck.setPropertyExpand(new Vector2b(true, false));
		sunDirCheck.setPropertyFill(new Vector2b(true, false));
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

	private void onAngleChanged(final Float degrees) {
		final float radians = (float) (degrees * Math.PI / 180.0);
		this.scene.getSun().setCurrentAngle(radians);
		this.angleLabel.setPropertyValue(String.format("Angle: %.1f", degrees));
	}

	@Override
	public void onRegenerateDisplay() {
		super.onRegenerateDisplay();
		// Update info label with sun state
		if (this.infoLabel != null && this.scene != null) {
			final CelestialBody sun = this.scene.getSun();
			final float angleDeg = (float) (sun.getCurrentAngle() * 180.0 / Math.PI);
			final Vector3f dir = sun.getDirection();
			this.infoLabel.setPropertyValue(String.format(
					"Sun: %.0f  h=%.2f\n%s",
					angleDeg, dir.z(),
					sun.isAboveHorizon() ? "above horizon" : "BELOW horizon"));
			// Sync slider position when sun is animating
			if (!this.sunPaused && this.angleSlider != null) {
				this.angleSlider.setPropertyValue(angleDeg);
			}
		}
	}
}
