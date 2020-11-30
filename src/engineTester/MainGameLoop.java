package engineTester;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.TexturedModel;
import renderEngine.DisplayManager;
import renderEngine.DisplayManagerDraw;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import terrains.TerrainTexture;
import terrains.TerrainTexturePack;
import textures.ModelTexture;

/**
 * This class contains the main method and is used to test the engine.
 * 
 * @author Karl
 *
 */
public class MainGameLoop {


	/**
	 * Loads up the position data for two triangles (which together make a quad)
	 * into a VAO. This VAO is then rendered to the screen every frame.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		DisplayManager manager = new DisplayManager();
		Loader loader = new Loader();
		manager.init();

		List<Entity> entities = new ArrayList<Entity>();
		Random random = new Random();

		
		TexturedModel staticModel = new TexturedModel(OBJLoader.loadObjModel("res/tree.obj", loader),
				new ModelTexture(loader.loadTexture("tree")));
		//ModelTexture texture = staticModel.getTexture();
		//texture.setShineDamper(10);
		//texture.setReflectivity(1);
		
		TexturedModel grassModel = new TexturedModel(OBJLoader.loadObjModel("res/grassModel.obj", loader),
				new ModelTexture(loader.loadTexture("grassTexture")));
		grassModel.getTexture().setHasTransparency(true);
		grassModel.getTexture().setUseFakeLighting(true);
		
		TexturedModel flowerModel = new TexturedModel(OBJLoader.loadObjModel("res/grassModel.obj", loader),
				new ModelTexture(loader.loadTexture("flower")));
		flowerModel.getTexture().setHasTransparency(true);
		flowerModel.getTexture().setUseFakeLighting(true);
		
		TexturedModel fernModel = new TexturedModel(OBJLoader.loadObjModel("res/fern.obj", loader),
				new ModelTexture(loader.loadTexture("fern_atlas")));
		fernModel.getTexture().setHasTransparency(true);
		fernModel.getTexture().setNumberOfRows(2);

		TexturedModel lampModel = new TexturedModel(OBJLoader.loadObjModel("res/lamp.obj", loader),
				new ModelTexture(loader.loadTexture("lamp")));
		//lampModel.getTexture().setHasTransparency(true);
		lampModel.getTexture().setUseFakeLighting(true); // this permit to the light to glow
		
		TexturedModel pineModel = new TexturedModel(OBJLoader.loadObjModel("res/pine.obj", loader),
				new ModelTexture(loader.loadTexture("pine")));
		
		List<Light> lights = new ArrayList<Light>();
		lights.add(new Light(new Vector3f(0,10000,-7000), new Vector3f(0.4f,0.4f,0.4f)));
		lights.add(new Light(new Vector3f(185,10,-293), new Vector3f(2,0,0), new Vector3f(1,0.01f,0.002f)));
		lights.add(new Light(new Vector3f(370,17,-300), new Vector3f(0,2,2), new Vector3f(1,0.01f,0.002f)));
		lights.add(new Light(new Vector3f(293,7,-305), new Vector3f(2,2,0), new Vector3f(1,0.01f,0.002f)));

		entities.add(new Entity(lampModel, 
				new Vector3f(185, -4.7f, -293),
				new Vector3f(0,0,0),
				1));
		entities.add(new Entity(lampModel, 
				new Vector3f(370, 4.2f, -300),
				new Vector3f(0,0,0),
				1));
		entities.add(new Entity(lampModel, 
				new Vector3f(293, -6.8f, -305),
				new Vector3f(0,0,0),
				1));
		
		
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grass"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);

		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		
		Terrain terrain = new Terrain(0,-1,loader, texturePack, blendMap, "heightmap");
		


		for (int iii=0; iii<250; iii++) {
			float x = random.nextFloat()*800 - 400;
			float z = random.nextFloat() * -600;
			float y = terrain.getHeightOfTerrain(x, z);
			entities.add(new Entity(staticModel,
					new Vector3f(x, y, z),
					new Vector3f(0,0,0),3));
		}
		for (int iii=0; iii<250; iii++) {
			float x = random.nextFloat()*800 - 400;
			float z = random.nextFloat() * -600;
			float y = terrain.getHeightOfTerrain(x, z);
			entities.add(new Entity(pineModel,
					new Vector3f(x, y, z),
					new Vector3f(0,0,0),0.5f));
		}
		for (int iii=0; iii<500; iii++) {
			float x = random.nextFloat()*800 - 400;
			float z = random.nextFloat() * -600;
			float y = terrain.getHeightOfTerrain(x, z);
			entities.add(new Entity(fernModel,
					random.nextInt(4),
					new Vector3f(x, y, z),
					new Vector3f(0,0,0),0.6f));
		}
		
		TexturedModel playerModel = new TexturedModel(OBJLoader.loadObjModel("res/person.obj", loader),
				new ModelTexture(loader.loadTexture("playerTexture")));
		
		Player player = new Player(playerModel, new Vector3f(180,terrain.getHeightOfTerrain(180, -250),-250), new Vector3f(0,3.14f,0), 0.4f);

		Camera camera = new Camera(player);
		
		
		List<GuiTexture> guis = new ArrayList<GuiTexture>();
		GuiTexture gui = new GuiTexture(loader.loadTexture("health"), new Vector2f(-0.75f, 0.9f), new Vector2f(0.25f, 0.25f));
		guis.add(gui);
		
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		MasterRenderer renderer = new MasterRenderer(loader);
		
		manager.setDrawer(new DisplayManagerDraw() {
			@Override
			public void draw() {
				//entity.increasePosition(0.0f, 0, -0.01f);
				//entity.increaseRotation(0, 0, 0.01f);
				//entity.increaseRotation(0.01f, 0.02f, 0.0f);
				player.move(terrain);
				camera.move();
				renderer.processTerrain(terrain);
				renderer.processEntity(player);
				for (Entity entity : entities) {
					entity.increaseRotation(0, 0.01f, 0.0f);
					renderer.processEntity(entity);
				}
				renderer.render(lights, camera);
				guiRenderer.render(guis);
			} 
		});
		manager.loop();
		guiRenderer.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		manager.unInit();
	}

}
