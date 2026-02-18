package sample.atriasoft.ege.shadowtest;

import org.atriasoft.ege.ControlCameraSimple;
import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.GameStatus;
import org.atriasoft.ege.Light;
import org.atriasoft.ege.Material;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.celestial.CelestialBody;
import org.atriasoft.ege.celestial.CelestialBodyType;
import org.atriasoft.ege.celestial.CelestialSystem;
import org.atriasoft.ege.components.ComponentLightSun;
import org.atriasoft.ege.components.ComponentMaterial;
import org.atriasoft.ege.components.ComponentMesh;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderColoredStaticMesh;
import org.atriasoft.ege.components.ComponentRenderMeshPalette;
import org.atriasoft.ege.components.ComponentRenderTexturedMaterialsStaticMesh;
import org.atriasoft.ege.components.ComponentStaticMesh;
import org.atriasoft.ege.components.ComponentTexture;
import org.atriasoft.ege.components.ComponentTexturePalette;
import org.atriasoft.ege.engines.EngineShadow;
import org.atriasoft.ege.tools.MeshGenerator;
import java.util.List;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.GaleApplication;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;
import org.atriasoft.gale.context.GaleContext;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeySpecial;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.gale.key.KeyType;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shadow test sample — Phase 2 CSM validation.
 * <p>
 * Sets up a celestial sun that orbits (fast, for visual testing),
 * a ground plane, cubes, and trees to verify cascaded shadow mapping.
 * <p>
 * Controls: same as lowPoly sample (WASD + mouse).
 */
public class ShadowTestApplication extends GaleApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShadowTestApplication.class);

	private static final float SUN_LIGHT_DISTANCE = 1000.0f;

	private Environement env;
	private ControlCameraSimple simpleControl;
	private CelestialBody sun;
	private ComponentPosition sunPosition;
	private ComponentLightSun sunLightComponent;
	private EngineShadow engineShadow;
	private boolean sunPaused = false;
	private float savedAngularSpeed = 0.0f;
	private ResourceColored3DObject debugDraw;

	public ShadowTestApplication() {
	}

	@Override
	public void onCreate(final GaleContext context) {
		try {
			Thread.sleep(500);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		this.env = new Environement();
		setSize(new Vector2f(1024, 768));
		setTitle("Shadow Test - Phase 2 CSM");

		// --- Celestial system: add a fast-orbiting sun for testing ---
		final CelestialSystem celestialSystem = this.env.getCelestialSystem();
		// Fast angular speed (full orbit in ~20 seconds) for visual testing
		// Start at angle PI/4 so sun is above horizon
		this.sun = new CelestialBody(
				CelestialBodyType.SUN,
				0.3f,                                   // angular speed (rad/s) — fast for testing
				0.1f,                                    // slight orbital inclination
				(float) (Math.PI * 0.25),                // initial angle — sun starts above horizon
				new Color(1.0f, 1.0f, 0.9f, 1.0f),      // warm white light
				1.0f,                                     // full intensity
				true);                                    // casts shadow
		celestialSystem.addBody(this.sun);

		// --- Engine references ---
		this.engineShadow = this.env.getEngineShadow();
		// Configure CSM shadow parameters
		this.engineShadow.setShadowMapResolution(2048);
		this.engineShadow.setShadowDistance(50.0f);
		// Use 3 cascades to test cascade blending
		this.engineShadow.getConfig().setCascadeCount(3);
		// Match camera FOV and aspect ratio for accurate cascade frustum fitting
		this.engineShadow.setCameraFovY(3.14f * 0.5f);
		this.engineShadow.setCameraAspectRatio(1024.0f / 768.0f);
		// Enable debug thumbnails to visualize shadow maps
		this.engineShadow.setDebugThumbnailEnabled(true);

		// --- Shader URIs (local to sample resources) ---
		final Uri shadowVert = new Uri("DATA", "shadowMaterial.vert");
		final Uri shadowFrag = new Uri("DATA", "shadowMaterial.frag");

		// --- Sun light entity (for EngineLight — provides diffuse light in shader) ---
		// Position is updated every frame in onRegenerateDisplay to follow CelestialBody
		final Vector3f initialDir = this.sun.getDirection();
		this.sunPosition = new ComponentPosition(new Transform3D(new Vector3f(
				initialDir.x() * SUN_LIGHT_DISTANCE,
				initialDir.y() * SUN_LIGHT_DISTANCE,
				initialDir.z() * SUN_LIGHT_DISTANCE)));
		final Entity sunEntity = new Entity(this.env);
		sunEntity.addComponent(this.sunPosition);
		this.sunLightComponent = new ComponentLightSun(
				new Light(new Color(1.0f, 1.0f, 0.9f), new Vector3f(0, 0, 0), new Vector3f(1.0f, 0, 0)));
		sunEntity.addComponent(this.sunLightComponent);
		this.env.addEntity(sunEntity);

		// --- Reference grid ---
		final Entity grid = new Entity(this.env);
		grid.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 0, 0))));
		grid.addComponent(new ComponentStaticMesh(MeshGenerator.createGrid(10)));
		grid.addComponent(new ComponentRenderColoredStaticMesh(
				new Uri("DATA", "wireColor.vert", "ege"),
				new Uri("DATA", "wireColor.frag", "ege")));
		this.env.addEntity(grid);

		// --- Ground plane (receives shadows) ---
		final Entity ground = new Entity(this.env);
		ground.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 0, -0.01f))));
		ground.addComponent(new ComponentStaticMesh(new Uri("DATA", "ground.obj")));
		ground.addComponent(new ComponentTexture(new Uri("DATA", "dirt.png")));
		ground.addComponent(new ComponentMaterial(new Material()));
		ground.addComponent(new ComponentRenderTexturedMaterialsStaticMesh(
				shadowVert, shadowFrag));
		this.env.addEntity(ground);

		// --- Shadow casting cubes ---
		createCube(new Vector3f(0, 0, 0.5f), shadowVert, shadowFrag);
		createCube(new Vector3f(3, 2, 0.5f), shadowVert, shadowFrag);
		createCube(new Vector3f(-2, 3, 0.5f), shadowVert, shadowFrag);
		createCube(new Vector3f(-3, -2, 1.0f), shadowVert, shadowFrag);
		createCube(new Vector3f(4, -3, 0.5f), shadowVert, shadowFrag);
		// A taller cube to see longer shadows
		createCube(new Vector3f(0, 4, 1.5f), shadowVert, shadowFrag);

		// --- Low-poly trees (EMF format with palette rendering) ---
		final Uri paletteVert = new Uri("DATA", "basicPalette.vert");
		final Uri paletteFrag = new Uri("DATA", "basicPalette.frag");
		// tree1 variants scattered around
		createTree(new Vector3f(-6, 5, 0), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(8, -4, 0), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(-10, -8, 0), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(12, 7, 0), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(-4, 12, 0), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(6, -10, 0), "tree1.emf", paletteVert, paletteFrag);
		// tree2 variants
		createTree(new Vector3f(5, 8, 0), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(-8, -3, 0), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(10, 3, 0), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(-12, 6, 0), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(2, -12, 0), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(-7, -10, 0), "tree2.emf", paletteVert, paletteFrag);

		// --- Camera ---
		final Camera mainView = new Camera();
		this.env.addCamera("default", mainView);
		mainView.setPitch((float) Math.PI * -0.3f);
		mainView.setPosition(new Vector3f(0, -12, 10));

		this.simpleControl = new ControlCameraSimple(mainView);
		this.env.addControlInterface(this.simpleControl);

		// Start engine
		this.env.setPropertyStatus(GameStatus.gameStart);

		LOGGER.info("==> Shadow Test Init (END)");
	}

	private void createCube(
			final Vector3f position,
			final Uri vertShader,
			final Uri fragShader) {
		final Entity cube = new Entity(this.env);
		cube.addComponent(new ComponentPosition(new Transform3D(position)));
		cube.addComponent(new ComponentStaticMesh(new Uri("DATA", "cube-one.obj")));
		cube.addComponent(new ComponentTexture(new Uri("DATA", "grass.png")));
		cube.addComponent(new ComponentMaterial(new Material()));
		cube.addComponent(new ComponentRenderTexturedMaterialsStaticMesh(
				vertShader, fragShader));
		this.env.addEntity(cube);
	}

	private void createTree(
			final Vector3f position,
			final String emfFile,
			final Uri vertShader,
			final Uri fragShader) {
		final Entity tree = new Entity(this.env);
		tree.addComponent(new ComponentPosition(new Transform3D(position)));
		tree.addComponent(new ComponentMesh(new Uri("DATA", emfFile)));
		tree.addComponent(new ComponentTexturePalette(new Uri("DATA", emfFile)));
		tree.addComponent(new ComponentRenderMeshPalette(vertShader, fragShader));
		this.env.addEntity(tree);
	}

	@Override
	public void onDraw(final GaleContext context) {
		final Vector2f size = getSize();
		// Store openGl context.
		OpenGL.push();
		// set projection matrix:
		final Matrix4f tmpProjection = Matrix4f.createMatrixPerspective(3.14f * 0.5f, getAspectRatio(), 0.1f, 50000);
		OpenGL.setMatrix(tmpProjection);

		// set the basic openGL view port
		OpenGL.setViewPort(new Vector2f(0, 0), size);

		// clear background with sky-ish color
		final Color bgColor = this.env.getCelestialSystem().getSkyColor();
		OpenGL.clearColor(bgColor);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_colorBuffer);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.enable(Flag.flag_depthTest);

		this.env.render(20, "default");

		// Draw sun light direction line (yellow line from origin towards sun)
		drawLightDirectionLine();

		// Render shadow map debug thumbnails in bottom-right corner
		this.engineShadow.renderDebugThumbnails();

		// Restore context of matrix
		OpenGL.pop();
	}

	@Override
	public void onKeyboard(
			final KeySpecial special,
			final KeyKeyboard type,
			final Character value,
			final KeyStatus state) {
		if (value != null && state == KeyStatus.down) {
			// P = pause/resume sun orbit
			if (value == 'p' || value == 'P') {
				if (this.sunPaused) {
					this.sun.setAngularSpeed(this.savedAngularSpeed);
					this.sunPaused = false;
					LOGGER.info("Sun RESUMED (speed={})", this.savedAngularSpeed);
				} else {
					this.savedAngularSpeed = this.sun.getAngularSpeed();
					this.sun.setAngularSpeed(0.0f);
					this.sunPaused = true;
					LOGGER.info("Sun PAUSED at angle={}", this.sun.getCurrentAngle());
				}
			}
			// C = cycle cascade count (1 → 2 → 3 → 4 → 1)
			if (value == 'c' || value == 'C') {
				final int current = this.engineShadow.getConfig().getCascadeCount();
				final int next = (current % 4) + 1;
				this.engineShadow.getConfig().setCascadeCount(next);
				LOGGER.info("Cascade count: {} -> {}", current, next);
			}
			// F = cycle PCF kernel size (1 → 3 → 5 → 1)
			if (value == 'f' || value == 'F') {
				final int current = this.engineShadow.getConfig().getPcfKernelSize();
				final int next;
				if (current <= 1) {
					next = 3;
				} else if (current <= 3) {
					next = 5;
				} else {
					next = 1;
				}
				this.engineShadow.getConfig().setPcfKernelSize(next);
				LOGGER.info("PCF kernel: {} -> {} ({})", current, next,
						next == 1 ? "hard" : next == 3 ? "medium" : "soft");
			}
			// T = toggle debug thumbnails
			if (value == 't' || value == 'T') {
				final boolean current = this.engineShadow.isDebugThumbnailEnabled();
				this.engineShadow.setDebugThumbnailEnabled(!current);
				LOGGER.info("Debug thumbnails: {}", !current ? "ON" : "OFF");
			}
		}
		this.env.onKeyboard(special, type, value, state);
	}

	@Override
	public void onPointer(
			final KeySpecial special,
			final KeyType type,
			final int pointerID,
			final Vector2f pos,
			final KeyStatus state) {
		this.env.onPointer(special, type, pointerID, pos, state);
	}

	@Override
	public void onRegenerateDisplay(final GaleContext context) {
		this.env.periodicCall();
		// Sync the EngineLight sun position with the CelestialBody direction
		final Vector3f dir = this.sun.getDirection();
		this.sunPosition.setTransform(this.sunPosition.getTransform().withPosition(new Vector3f(
				dir.x() * SUN_LIGHT_DISTANCE,
				dir.y() * SUN_LIGHT_DISTANCE,
				dir.z() * SUN_LIGHT_DISTANCE)));
		// Sync the sun light color based on height (twilight = warm orange/red)
		final Color sunColor = computeSunLightColor(dir.z());
		this.sunLightComponent.getLight().setColor(sunColor);
		markDrawingIsNeeded();
	}

	/**
	 * Draw a line showing the sun light direction.
	 * Yellow line from (0,0,0) towards the sun, length 20.
	 * Red line from (0,0,0) in the opposite direction (shadow direction), length 10.
	 */
	private void drawLightDirectionLine() {
		if (this.debugDraw == null) {
			this.debugDraw = ResourceColored3DObject.create();
		}
		if (this.debugDraw == null) {
			return;
		}
		final Vector3f dir = this.sun.getDirection();
		final float lineLen = 20.0f;
		final float shadowLen = 10.0f;

		// Yellow line: origin → sun direction
		final List<Vector3f> sunLine = List.of(
				new Vector3f(0, 0, 0.1f),
				new Vector3f(dir.x() * lineLen, dir.y() * lineLen, dir.z() * lineLen + 0.1f));
		this.debugDraw.drawLine(sunLine, new Color(1.0f, 1.0f, 0.0f, 1.0f), Matrix4f.IDENTITY, false, true);

		// Red line: origin → opposite direction (where shadow falls)
		final List<Vector3f> shadowLine = List.of(
				new Vector3f(0, 0, 0.1f),
				new Vector3f(-dir.x() * shadowLen, -dir.y() * shadowLen, -dir.z() * shadowLen + 0.1f));
		this.debugDraw.drawLine(shadowLine, new Color(1.0f, 0.0f, 0.0f, 1.0f), Matrix4f.IDENTITY, false, true);
	}

	/**
	 * Compute the sun light color based on its height above the horizon.
	 * <ul>
	 *   <li>Below horizon (z &lt; 0): black — no light</li>
	 *   <li>Near horizon (0 &lt; z &lt; 0.1): deep orange/red (twilight)</li>
	 *   <li>Low sun (0.1 &lt; z &lt; 0.4): orange → warm white transition</li>
	 *   <li>High sun (z &gt; 0.4): warm white (1.0, 1.0, 0.9)</li>
	 * </ul>
	 * @param height The Z component of the sun direction (height above horizon)
	 * @return The computed light color
	 */
	private static Color computeSunLightColor(final float height) {
		if (height <= 0.0f) {
			// Below horizon — no light
			return new Color(0.0f, 0.0f, 0.0f, 1.0f);
		}
		if (height < 0.1f) {
			// Twilight: dark red/orange
			final float t = height / 0.1f; // 0..1
			return new Color(
					0.8f * t,
					0.2f * t,
					0.05f * t,
					1.0f);
		}
		if (height < 0.4f) {
			// Sunrise/sunset transition: orange → warm white
			final float t = (height - 0.1f) / 0.3f; // 0..1
			return new Color(
					0.8f + 0.2f * t,           // 0.8 → 1.0
					0.2f + 0.8f * t,           // 0.2 → 1.0
					0.05f + 0.85f * t,         // 0.05 → 0.9
					1.0f);
		}
		// Full day — warm white
		return new Color(1.0f, 1.0f, 0.9f, 1.0f);
	}
}
