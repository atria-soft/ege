package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.etk.Color;
import org.lwjgl.opengl.GL11;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.TexturedModel;
import shaders.StaticShader;
import shaders.TerrainShader;
import skybox.SkyboxRenderer;
import terrains.Terrain;

public class MasterRenderer {
	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 10000;
	private static final Color SKY_COLOUR = new Color(0.5444f, 0.62f, 0.69f, 1.0f);
	
	private Matrix4f projectionMatrix;
	
	private StaticShader shader = new StaticShader();
	private EntityRenderer renderer;
	
	private TerrainRenderer terrainRenderer;
	private TerrainShader terrainShader = new TerrainShader();
	
	private Map<TexturedModel, List<Entity>> entities = new HashMap<TexturedModel, List<Entity>>();
	private List<Terrain> terrains = new ArrayList<Terrain>();
	
	private SkyboxRenderer skyboxRenderer;
	
	public MasterRenderer(Loader loader) {
		//enableCulling();
		OpenGL.enable(OpenGL.Flag.flag_blend);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		createProjectionMatrix();
		renderer = new EntityRenderer(shader, projectionMatrix);
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
		skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);
	}

	public static void enableCulling() {
		OpenGL.enable(OpenGL.Flag.flag_cullFace);
		OpenGL.enable(OpenGL.Flag.flag_back);
	}
	public static void disableCulling() {
		OpenGL.disable(OpenGL.Flag.flag_cullFace);
		OpenGL.disable(OpenGL.Flag.flag_back);
	}
	
	public void render(List<Light> lights, Camera camera) {
		prepare();
		skyboxRenderer.render(camera, SKY_COLOUR);
		shader.start();
		shader.loadSkyColour(SKY_COLOUR);
		shader.loadLights(lights);
		shader.loadViewMatrix(camera);
		renderer.render(entities);
		shader.stop();
		entities.clear();
		terrainShader.start();
		terrainShader.loadSkyColour(SKY_COLOUR);
		terrainShader.loadLights(lights);
		terrainShader.loadViewMatrix(camera);
		terrainRenderer.render(terrains); 
		terrainShader.stop();
		terrains.clear();
	}
	
	public  void processTerrain(Terrain terrain) {
		terrains.add(terrain);
	}
	
	public void processEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = entities.get(entityModel);
		if (batch != null) {
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}

	public void prepare() {
		OpenGL.enable(OpenGL.Flag.flag_depthTest);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_depthBuffer);
		OpenGL.clear(OpenGL.ClearFlag.clearFlag_colorBuffer);
		OpenGL.clearColor(SKY_COLOUR);
	}
	
	
	public void cleanUp () {
		shader.cleanUp();
		terrainShader.cleanUp();
	}
	
	private void createProjectionMatrix() {
		Vector2f windowsSize = DisplayManager.getSize();
		float aspectRatio = windowsSize.x / windowsSize.y;
		projectionMatrix = Matrix4f.createMatrixPerspective(FOV, aspectRatio, NEAR_PLANE, FAR_PLANE);
	}
	

	
	
}
