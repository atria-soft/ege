package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.lwjgl.opengl.GL11;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.TexturedModel;
import shaders.StaticShader;
import shaders.TerrainShader;
import terrains.Terrain;

public class MasterRenderer {
	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 10000;
	private static final Color SKY_COLOUR = new Color(0.5444f, 0.62f, 0.69f, 1.0f);
	
	public static void disableCulling() {
		OpenGL.disable(OpenGL.Flag.flag_cullFace);
		OpenGL.disable(OpenGL.Flag.flag_back);
	}
	
	public static void enableCulling() {
		OpenGL.enable(OpenGL.Flag.flag_cullFace);
		OpenGL.enable(OpenGL.Flag.flag_back);
	}
	
	private Matrix4f projectionMatrix;
	
	private StaticShader shader = new StaticShader();
	private EntityRenderer renderer;
	
	private TerrainRenderer terrainRenderer;
	private TerrainShader terrainShader = new TerrainShader();
	
	private Map<TexturedModel, List<Entity>> entities = new HashMap<>();
	
	private List<Terrain> terrains = new ArrayList<>();
	
	public MasterRenderer(final Loader loader) {
		//enableCulling();
		OpenGL.enable(OpenGL.Flag.flag_blend);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		createProjectionMatrix();
		this.renderer = new EntityRenderer(this.shader, this.projectionMatrix);
		this.terrainRenderer = new TerrainRenderer(this.terrainShader, this.projectionMatrix);
	}
	
	public void cleanUp() {
		this.shader.cleanUp();
		this.terrainShader.cleanUp();
	}
	
	private void createProjectionMatrix() {
		Vector2f windowsSize = DisplayManager.getSize();
		float aspectRatio = windowsSize.x() / windowsSize.y();
		this.projectionMatrix = Matrix4f.createMatrixPerspective(FOV, aspectRatio, NEAR_PLANE, FAR_PLANE);
	}
	
	public void prepare() {
		OpenGL.enable(OpenGL.Flag.flag_depthTest);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_colorBuffer);
		OpenGL.clearColor(SKY_COLOUR);
	}
	
	public void processEntity(final Entity entity) {
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = this.entities.get(entityModel);
		if (batch != null) {
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<>();
			newBatch.add(entity);
			this.entities.put(entityModel, newBatch);
		}
	}
	
	public void processTerrain(final Terrain terrain) {
		this.terrains.add(terrain);
	}
	
	public void render(final List<Light> lights, final Camera camera) {
		prepare();
		this.shader.start();
		this.shader.loadSkyColour(SKY_COLOUR);
		this.shader.loadLights(lights);
		this.shader.loadViewMatrix(camera);
		this.renderer.render(this.entities);
		this.shader.stop();
		this.entities.clear();
		this.terrainShader.start();
		this.terrainShader.loadSkyColour(SKY_COLOUR);
		this.terrainShader.loadLights(lights);
		this.terrainShader.loadViewMatrix(camera);
		this.terrainRenderer.render(this.terrains);
		this.terrainShader.stop();
		this.terrains.clear();
	}
	
}
