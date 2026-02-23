package sample.atriasoft.ege.shadowtest;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.ege.ControlCameraSimple;
import org.atriasoft.ege.Entity;
import org.atriasoft.ege.GameStatus;
import org.atriasoft.ege.Light;
import org.atriasoft.ege.Material;
import org.atriasoft.ege.celestial.CelestialBody;
import org.atriasoft.ege.celestial.CelestialBodyType;
import org.atriasoft.ege.celestial.CelestialSystem;
import org.atriasoft.ege.components.ComponentLight;
import org.atriasoft.ege.components.ComponentLightSun;
import org.atriasoft.ege.components.ComponentMaterial;
import org.atriasoft.ege.components.ComponentMesh;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderMeshPalette;
import org.atriasoft.ege.components.ComponentRenderTexturedMaterialsStaticMesh;
import org.atriasoft.ege.components.ComponentStaticMesh;
import org.atriasoft.ege.components.ComponentTexture;
import org.atriasoft.ege.components.ComponentTexturePalette;
import org.atriasoft.ege.engines.EngineShadow;
import org.atriasoft.ege.shadow.ShadowCascade;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.loader3d.model.LightData;
import org.atriasoft.loader3d.resources.ResourceMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sample.atriasoft.ege.mapFactory.EgeScene;

/**
 * 3D scene widget for shadow test — extends EgeScene to integrate
 * into an ewol widget tree alongside UI controls.
 */
public class ShadowScene extends EgeScene {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShadowScene.class);
	private static final float SUN_LIGHT_DISTANCE = 1000.0f;

	private CelestialBody sun;
	private ComponentPosition sunPosition;
	private ComponentLightSun sunLightComponent;
	private CelestialBody moon;
	private ComponentPosition moonPosition;
	private ComponentLightSun moonLightComponent;
	private EngineShadow engineShadow;
	private ResourceColored3DObject debugDraw;

	// Debug toggles (controlled from ShadowWindows)
	private boolean drawFrustumWireframe = false;
	private boolean drawLightAABB = false;
	private boolean drawSunDirection = true;

	// Street lamps
	private final List<ComponentLight> lampLights = new ArrayList<>();
	private final List<Color> lampOriginalColors = new ArrayList<>();
	private boolean lampsEnabled = true;
	private float lampIntensity = 1.0f;

	public ShadowScene() {
		// Override default camera position for shadow test
		this.mainView.setPitch((float) Math.PI * 0.3f);
		this.mainView.setPosition(new Vector3f(0, 10, 12));

		addGenericGird();

		// --- Celestial system ---
		final CelestialSystem celestialSystem = this.env.getCelestialSystem();
		this.sun = new CelestialBody(
				CelestialBodyType.SUN,
				0.3f,
				0.1f,
				(float) (Math.PI * 0.25),
				new Color(1.0f, 1.0f, 0.9f, 1.0f),
				1.0f,
				true);
		celestialSystem.addBody(this.sun);

		// --- Engines ---
		this.engineShadow = this.env.getEngineShadow();
		this.engineShadow.setShadowMapResolution(2048);
		this.engineShadow.setShadowDistance(50.0f);
		this.engineShadow.getConfig().setCascadeCount(3);
		this.engineShadow.setCameraFovY(3.14f * 0.5f);
		this.engineShadow.setCameraAspectRatio(1024.0f / 768.0f);
		this.engineShadow.setDebugThumbnailEnabled(true);

		// --- Shaders ---
		final Uri shadowVert = new Uri("DATA", "shadowMaterial.vert");
		final Uri shadowFrag = new Uri("DATA", "shadowMaterial.frag");

		// --- Sun light entity ---
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

		// --- Moon (opposite the sun) ---
		this.moon = new CelestialBody(
				CelestialBodyType.MOON,
				0.3f,
				0.1f,
				(float) (Math.PI * 0.25 + Math.PI),
				new Color(0.7f, 0.7f, 1.0f, 1.0f),
				0.15f,
				false);
		celestialSystem.addBody(this.moon);

		final Vector3f moonDir = this.moon.getDirection();
		this.moonPosition = new ComponentPosition(new Transform3D(new Vector3f(
				moonDir.x() * SUN_LIGHT_DISTANCE,
				moonDir.y() * SUN_LIGHT_DISTANCE,
				moonDir.z() * SUN_LIGHT_DISTANCE)));
		final Entity moonEntity = new Entity(this.env);
		moonEntity.addComponent(this.moonPosition);
		this.moonLightComponent = new ComponentLightSun(
				new Light(new Color(0.7f, 0.7f, 1.0f), new Vector3f(0, 0, 0), new Vector3f(1.0f, 0, 0)));
		moonEntity.addComponent(this.moonLightComponent);
		this.env.addEntity(moonEntity);

		// --- Ground plane ---
		final Entity ground = new Entity(this.env);
		ground.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, -0.01f, 0))));
		ground.addComponent(new ComponentStaticMesh(new Uri("DATA", "ground.obj")));
		ground.addComponent(new ComponentTexture(new Uri("DATA", "dirt.png")));
		ground.addComponent(new ComponentMaterial(new Material()));
		ground.addComponent(new ComponentRenderTexturedMaterialsStaticMesh(
				shadowVert, shadowFrag));
		this.env.addEntity(ground);

		// --- Cubes ---
		createCube(new Vector3f(0, 0.5f, 0), shadowVert, shadowFrag);
		createCube(new Vector3f(3, 0.5f, 2), shadowVert, shadowFrag);
		createCube(new Vector3f(-2, 0.5f, 3), shadowVert, shadowFrag);
		createCube(new Vector3f(-3, 1.0f, -2), shadowVert, shadowFrag);
		createCube(new Vector3f(4, 0.5f, -3), shadowVert, shadowFrag);
		createCube(new Vector3f(0, 1.5f, 4), shadowVert, shadowFrag);

		// --- Trees (spread over a larger area) ---
		final Uri paletteVert = new Uri("DATA", "basicPalette.vert");
		final Uri paletteFrag = new Uri("DATA", "basicPalette.frag");
		// Inner ring
		createTree(new Vector3f(-4, 0, 3), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(5, 0, -2), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(-2, 0, -5), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(3, 0, 5), "tree2.emf", paletteVert, paletteFrag);
		// Mid ring
		createTree(new Vector3f(-8, 0, 6), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(9, 0, -5), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(-6, 0, -9), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(7, 0, 8), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(-10, 0, -3), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(10, 0, 3), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(0, 0, 10), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(0, 0, -10), "tree2.emf", paletteVert, paletteFrag);
		// Outer ring
		createTree(new Vector3f(-14, 0, 10), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(15, 0, -8), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(-12, 0, -12), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(13, 0, 11), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(-16, 0, -5), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(16, 0, 2), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(-5, 0, 15), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(4, 0, -15), "tree2.emf", paletteVert, paletteFrag);
		// Far corners
		createTree(new Vector3f(-18, 0, 14), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(18, 0, -13), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(-17, 0, -15), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(17, 0, 14), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(-20, 0, 0), "tree1.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(20, 0, 0), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(0, 0, 18), "tree2.emf", paletteVert, paletteFrag);
		createTree(new Vector3f(0, 0, -18), "tree1.emf", paletteVert, paletteFrag);

		// --- Lampadaires (4 lamps around the scene, not too far from objects) ---
		addLamp(new Vector3f(-8, 0, 8), "roadLamp.emf", paletteVert, paletteFrag);
		addLamp(new Vector3f(8, 0, 8), "roadLamp2.emf", paletteVert, paletteFrag);
		addLamp(new Vector3f(-8, 0, -8), "roadLamp.emf", paletteVert, paletteFrag);
		addLamp(new Vector3f(8, 0, -8), "roadLamp2.emf", paletteVert, paletteFrag);

		// --- Camera control ---
		final ControlCameraSimple simpleControl = new ControlCameraSimple(this.mainView);
		this.env.addControlInterface(simpleControl);

		// Start engine
		this.env.setPropertyStatus(GameStatus.gameStart);
		LOGGER.info("==> ShadowScene Init (END)");
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

	private List<ComponentLight> createLamp(
			final Vector3f position,
			final String emfFile,
			final Uri vertShader,
			final Uri fragShader) {
		final Uri meshUri = new Uri("DATA", emfFile);
		final Entity lamp = new Entity(this.env);
		lamp.addComponent(new ComponentPosition(new Transform3D(position)));
		lamp.addComponent(new ComponentMesh(meshUri));
		lamp.addComponent(new ComponentTexturePalette(meshUri));
		lamp.addComponent(new ComponentRenderMeshPalette(vertShader, fragShader));
		// Auto-load lights from the .emf file
		final List<ComponentLight> lights = new ArrayList<>();
		final ResourceMesh resourceMesh = ResourceMesh.create(meshUri);
		if (resourceMesh != null) {
			for (final LightData lightData : resourceMesh.getLights()) {
				if (lightData.getType() == LightData.LightType.SUN) {
					continue; // Skip sun lights — only local lights
				}
				final Vector3f col = lightData.getColor();
				final Light light;
				if (lightData.getType() == LightData.LightType.SPOT) {
					light = new Light(
							new Color(col.x(), col.y(), col.z()),
							lightData.getPosition(),
							lightData.getAttenuation(),
							lightData.getDirection(),
							lightData.getAngle(),
							lightData.getBlend());
				} else {
					light = new Light(
							new Color(col.x(), col.y(), col.z()),
							lightData.getPosition(),
							lightData.getAttenuation());
				}
				light.setRadius(lightData.getRadius());
				final ComponentLight componentLight = new ComponentLight(light);
				lamp.addComponent(componentLight);
				lights.add(componentLight);
				LOGGER.info("Loaded light from {}: type={} pos={} dir={} angle={} blend={} radius={} atten={}",
						emfFile, lightData.getType(), lightData.getPosition(),
						lightData.getDirection(), lightData.getAngle(), lightData.getBlend(),
						lightData.getRadius(), lightData.getAttenuation());
			}
		}
		if (lights.isEmpty()) {
			LOGGER.warn("No lights found in {}, adding default point light", emfFile);
			final ComponentLight defaultLight = new ComponentLight(
					new Light(new Color(1.0f, 0.9f, 0.7f),
							new Vector3f(0, 3.0f, 0),
							new Vector3f(1.0f, 0.09f, 0.032f)));
			lamp.addComponent(defaultLight);
			lights.add(defaultLight);
		}
		this.env.addEntity(lamp);
		return lights;
	}

	private void addLamp(
			final Vector3f position,
			final String emfFile,
			final Uri vertShader,
			final Uri fragShader) {
		final List<ComponentLight> lights = createLamp(position, emfFile, vertShader, fragShader);
		for (final ComponentLight light : lights) {
			this.lampLights.add(light);
			this.lampOriginalColors.add(light.getLight().getColor());
		}
	}

	@Override
	public void onChangeSize() {
		super.onChangeSize();
		// Update shadow engine aspect ratio when widget is resized
		if (this.engineShadow != null && this.size.x() > 0 && this.size.y() > 0) {
			this.engineShadow.setCameraAspectRatio(this.size.x() / this.size.y());
		}
	}

	@Override
	public void onRegenerateDisplay() {
		super.onRegenerateDisplay();
		// Sync the EngineLight sun position with the CelestialBody direction
		final Vector3f dir = this.sun.getDirection();
		this.sunPosition.setTransform(this.sunPosition.getTransform().withPosition(new Vector3f(
				dir.x() * SUN_LIGHT_DISTANCE,
				dir.y() * SUN_LIGHT_DISTANCE,
				dir.z() * SUN_LIGHT_DISTANCE)));
		// Sync the sun light color based on height
		final Color sunColor = computeSunLightColor(dir.y());
		this.sunLightComponent.getLight().setColor(sunColor);
		// Sync moon position and light
		final Vector3f moonDir = this.moon.getDirection();
		this.moonPosition.setTransform(this.moonPosition.getTransform().withPosition(new Vector3f(
				moonDir.x() * SUN_LIGHT_DISTANCE,
				moonDir.y() * SUN_LIGHT_DISTANCE,
				moonDir.z() * SUN_LIGHT_DISTANCE)));
		final Color moonColor = computeMoonLightColor(moonDir.y());
		this.moonLightComponent.getLight().setColor(moonColor);
	}

	@Override
	protected void onDrawScene() {
		if (this.drawSunDirection) {
			drawLightDirectionLine();
		}
		if (this.drawFrustumWireframe) {
			drawFrustumWireframe();
		}
		if (this.drawLightAABB) {
			drawLightAABBWireframe();
		}
		// Shadow map debug thumbnails
		this.engineShadow.renderDebugThumbnails();
	}

	// --- Debug drawing ---

	private ResourceColored3DObject getDebugDraw() {
		if (this.debugDraw == null) {
			this.debugDraw = ResourceColored3DObject.create();
		}
		return this.debugDraw;
	}

	private void drawLightDirectionLine() {
		final ResourceColored3DObject dd = getDebugDraw();
		if (dd == null) {
			return;
		}
		final Vector3f dir = this.sun.getDirection();
		final float lineLen = 20.0f;
		final float shadowLen = 10.0f;

		final List<Vector3f> sunLine = List.of(
				new Vector3f(0, 0.1f, 0),
				new Vector3f(dir.x() * lineLen, dir.y() * lineLen + 0.1f, dir.z() * lineLen));
		dd.drawLine(sunLine, new Color(1.0f, 1.0f, 0.0f, 1.0f), Matrix4f.IDENTITY, false, true);

		final List<Vector3f> shadowLine = List.of(
				new Vector3f(0, 0.1f, 0),
				new Vector3f(-dir.x() * shadowLen, -dir.y() * shadowLen + 0.1f, -dir.z() * shadowLen));
		dd.drawLine(shadowLine, new Color(1.0f, 0.0f, 0.0f, 1.0f), Matrix4f.IDENTITY, false, true);
	}

	private void drawFrustumWireframe() {
		final ResourceColored3DObject dd = getDebugDraw();
		if (dd == null) {
			return;
		}
		final ShadowCascade cascade = this.engineShadow.getCascade(0, 0);
		if (cascade == null) {
			return;
		}
		final Vector3f[] corners = cascade.getDebugFrustumCorners();
		if (corners == null || corners.length < 8) {
			return;
		}
		final Color color = new Color(0.0f, 1.0f, 1.0f, 1.0f); // cyan
		// Near plane (corners 0-3)
		drawWireframeQuad(dd, corners[0], corners[1], corners[2], corners[3], color);
		// Far plane (corners 4-7)
		drawWireframeQuad(dd, corners[4], corners[5], corners[6], corners[7], color);
		// Connecting edges
		drawEdge(dd, corners[0], corners[4], color);
		drawEdge(dd, corners[1], corners[5], color);
		drawEdge(dd, corners[2], corners[6], color);
		drawEdge(dd, corners[3], corners[7], color);
	}

	private void drawLightAABBWireframe() {
		final ResourceColored3DObject dd = getDebugDraw();
		if (dd == null) {
			return;
		}
		final ShadowCascade cascade = this.engineShadow.getCascade(0, 0);
		if (cascade == null) {
			return;
		}
		final Vector3f[] corners = cascade.getDebugLightAABBCorners();
		if (corners == null || corners.length < 8) {
			return;
		}
		final Color color = new Color(1.0f, 0.5f, 0.0f, 1.0f); // orange
		// Bottom face (corners 0-3, z=minZ)
		drawWireframeQuad(dd, corners[0], corners[1], corners[2], corners[3], color);
		// Top face (corners 4-7, z=maxZ)
		drawWireframeQuad(dd, corners[4], corners[5], corners[6], corners[7], color);
		// Connecting edges
		drawEdge(dd, corners[0], corners[4], color);
		drawEdge(dd, corners[1], corners[5], color);
		drawEdge(dd, corners[2], corners[6], color);
		drawEdge(dd, corners[3], corners[7], color);
	}

	private void drawWireframeQuad(
			final ResourceColored3DObject dd,
			final Vector3f a, final Vector3f b,
			final Vector3f c, final Vector3f d,
			final Color color) {
		drawEdge(dd, a, b, color);
		drawEdge(dd, b, c, color);
		drawEdge(dd, c, d, color);
		drawEdge(dd, d, a, color);
	}

	private void drawEdge(
			final ResourceColored3DObject dd,
			final Vector3f from, final Vector3f to,
			final Color color) {
		dd.drawLine(List.of(from, to), color, Matrix4f.IDENTITY, false, true);
	}

	// --- Public API for controls ---

	public CelestialBody getSun() {
		return this.sun;
	}

	public CelestialBody getMoon() {
		return this.moon;
	}

	public EngineShadow getEngineShadow() {
		return this.engineShadow;
	}

	public void setDrawFrustumWireframe(final boolean draw) {
		this.drawFrustumWireframe = draw;
	}

	public boolean isDrawFrustumWireframe() {
		return this.drawFrustumWireframe;
	}

	public void setDrawLightAABB(final boolean draw) {
		this.drawLightAABB = draw;
	}

	public boolean isDrawLightAABB() {
		return this.drawLightAABB;
	}

	public void setDrawSunDirection(final boolean draw) {
		this.drawSunDirection = draw;
	}

	public boolean isDrawSunDirection() {
		return this.drawSunDirection;
	}

	public void setLampsEnabled(final boolean enabled) {
		this.lampsEnabled = enabled;
		updateLampColors();
	}

	public boolean isLampsEnabled() {
		return this.lampsEnabled;
	}

	public void setLampIntensity(final float intensity) {
		this.lampIntensity = intensity;
		updateLampColors();
	}

	public float getLampIntensity() {
		return this.lampIntensity;
	}

	private void updateLampColors() {
		final float scale = this.lampsEnabled ? this.lampIntensity : 0.0f;
		for (int idx = 0; idx < this.lampLights.size(); idx++) {
			final Color original = this.lampOriginalColors.get(idx);
			final Color scaled = new Color(
					original.r() * scale,
					original.g() * scale,
					original.b() * scale,
					original.a());
			this.lampLights.get(idx).getLight().setColor(scaled);
		}
	}

	// --- Utility ---

	private static Color computeMoonLightColor(final float height) {
		if (height <= 0.0f) {
			return new Color(0.0f, 0.0f, 0.0f, 1.0f);
		}
		// Dim bluish light, intensity proportional to height
		final float t = Math.min(height, 1.0f);
		return new Color(0.10f * t, 0.10f * t, 0.15f * t, 1.0f);
	}

	private static Color computeSunLightColor(final float height) {
		if (height <= 0.0f) {
			return new Color(0.0f, 0.0f, 0.0f, 1.0f);
		}
		if (height < 0.1f) {
			final float t = height / 0.1f;
			return new Color(0.8f * t, 0.2f * t, 0.05f * t, 1.0f);
		}
		if (height < 0.4f) {
			final float t = (height - 0.1f) / 0.3f;
			return new Color(0.8f + 0.2f * t, 0.2f + 0.8f * t, 0.05f + 0.85f * t, 1.0f);
		}
		return new Color(1.0f, 1.0f, 0.9f, 1.0f);
	}
}
