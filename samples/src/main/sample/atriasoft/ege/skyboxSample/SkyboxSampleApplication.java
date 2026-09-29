package sample.atriasoft.ege.skyboxSample;

import org.atriasoft.ege.ControlCameraSimple;
import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.GameStatus;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderColoredStaticMesh;
import org.atriasoft.ege.components.ComponentRenderTexturedStaticMesh;
import org.atriasoft.ege.components.ComponentStaticMesh;
import org.atriasoft.ege.components.ComponentTexture;
import org.atriasoft.ege.skybox.SkyboxConfig;
import org.atriasoft.ege.tools.MeshGenerator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample application demonstrating the skybox system.
 * <p>
 * Features:
 * <ul>
 *   <li>Cubemap skybox with cloud textures</li>
 *   <li>Ground plane with grid</li>
 *   <li>Textured cubes</li>
 *   <li>Camera controls (arrow keys + right-click drag)</li>
 *   <li>Press F1 to cycle between skybox sets</li>
 * </ul>
 */
public class SkyboxSampleApplication extends GaleApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(SkyboxSampleApplication.class);

	private static final String[] SKYBOX_NAMES = {"skybox (clouds)", "skybox2", "skybox_debug (labeled faces)"};

	private Environement env;
	private ControlCameraSimple simpleControl;
	private final SkyboxConfig[] skyboxConfigs = new SkyboxConfig[3];
	private int currentSkyboxIndex = 0;

	public SkyboxSampleApplication() {}

	@Override
	public void onCreate(final GaleContext context) {
		LOGGER.info("Skybox sample onCreate [BEGIN]");
		this.env = new Environement();
		setSize(new Vector2f(1024, 768));
		setTitle("Skybox Sample");

		// --- Configure skybox sets ---
		this.skyboxConfigs[0] = new SkyboxConfig(
				new Uri("RES", "skybox/right.png"),
				new Uri("RES", "skybox/left.png"),
				new Uri("RES", "skybox/top.png"),
				new Uri("RES", "skybox/bottom.png"),
				new Uri("RES", "skybox/front.png"),
				new Uri("RES", "skybox/back.png"));
		this.skyboxConfigs[1] = new SkyboxConfig(
				new Uri("RES", "skybox2/right.png"),
				new Uri("RES", "skybox2/left.png"),
				new Uri("RES", "skybox2/top.png"),
				new Uri("RES", "skybox2/bottom.png"),
				new Uri("RES", "skybox2/front.png"),
				new Uri("RES", "skybox2/back.png"));
		this.skyboxConfigs[2] = new SkyboxConfig(
				new Uri("RES", "skybox_debug/right.png"),
				new Uri("RES", "skybox_debug/left.png"),
				new Uri("RES", "skybox_debug/top.png"),
				new Uri("RES", "skybox_debug/bottom.png"),
				new Uri("RES", "skybox_debug/front.png"),
				new Uri("RES", "skybox_debug/back.png"));
		this.env.setSkybox(this.skyboxConfigs[0]);

		// --- Reference grid ---
		final Entity grid = new Entity(this.env);
		grid.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 0, 0))));
		grid.addComponent(new ComponentStaticMesh(MeshGenerator.createGrid(10)));
		grid.addComponent(new ComponentRenderColoredStaticMesh(
				new Uri("DATA", "wireColor.vert", "ege"),
				new Uri("DATA", "wireColor.frag", "ege")));
		this.env.addEntity(grid);

		// --- A few textured cubes ---
		createCube(new Vector3f(0, 0.5f, 0));
		createCube(new Vector3f(3, 0.5f, 2));
		createCube(new Vector3f(-2, 0.5f, 3));
		createCube(new Vector3f(-3, 1.0f, -2));
		createCube(new Vector3f(4, 0.5f, -3));

		// --- Camera ---
		final Camera mainView = new Camera();
		this.env.addCamera("default", mainView);
		mainView.setPitch((float) Math.PI * 0.15f);
		mainView.setPosition(new Vector3f(0, 5, 10));

		this.simpleControl = new ControlCameraSimple(mainView);
		this.env.addControlInterface(this.simpleControl);

		this.env.setPropertyStatus(GameStatus.gameStart);
		LOGGER.info("Skybox sample onCreate [END]");
		LOGGER.info("Controls: Arrow keys = move, Right-click drag = look around, Ctrl+Up/Down = move up/down, F1 = cycle skybox ({})", String.join(", ", SKYBOX_NAMES));
	}

	private void createCube(final Vector3f position) {
		final Entity cube = new Entity(this.env);
		cube.addComponent(new ComponentPosition(new Transform3D(position)));
		cube.addComponent(new ComponentStaticMesh(new Uri("RES", "cube.obj")));
		cube.addComponent(new ComponentTexture(new Uri("DATA", "blocks/dirt.png", "loxelEngine")));
		cube.addComponent(new ComponentRenderTexturedStaticMesh(
				new Uri("DATA", "basic.vert", "sample"),
				new Uri("DATA", "basic.frag", "sample")));
		this.env.addEntity(cube);
	}

	@Override
	public void onDraw(final GaleContext context) {
		final Vector2f size = getSize();
		OpenGL.push();
		final Matrix4f projection = Matrix4f.createMatrixPerspective(
				3.14f * 0.5f, getAspectRatio(), 0.1f, 50000);
		OpenGL.setMatrix(projection);
		OpenGL.setViewPort(new Vector2f(0, 0), size);

		final Color bgColor = new Color(0.2f, 0.3f, 0.5f, 1.0f);
		OpenGL.clearColor(bgColor);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_colorBuffer);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.enable(Flag.flag_depthTest);

		this.env.render(20, "default");

		OpenGL.pop();
	}

	@Override
	public void onKeyboard(
			final KeySpecial special,
			final KeyKeyboard type,
			final Character value,
			final KeyStatus state) {
		if (state == KeyStatus.down && type == KeyKeyboard.F1) {
			this.currentSkyboxIndex = (this.currentSkyboxIndex + 1) % this.skyboxConfigs.length;
			this.env.setSkybox(this.skyboxConfigs[this.currentSkyboxIndex]);
			LOGGER.info("Switched to skybox: {}", SKYBOX_NAMES[this.currentSkyboxIndex]);
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
		markDrawingIsNeeded();
	}
}
