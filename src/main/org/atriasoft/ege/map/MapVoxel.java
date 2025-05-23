package org.atriasoft.ege.map;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.ege.Entity;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.Light;
import org.atriasoft.ege.Material;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3i;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ege.components.ComponentLight;
import org.atriasoft.ege.components.ComponentLightSun;
import org.atriasoft.ege.components.ComponentMaterials;
import org.atriasoft.ege.components.ComponentPhysics;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderTexturedMaterialsDynamicMeshs;
import org.atriasoft.ege.components.ComponentRenderTexturedMaterialsStaticMeshs;
import org.atriasoft.ege.components.ComponentRenderTexturedStaticMesh;
import org.atriasoft.ege.components.ComponentStaticMesh;
import org.atriasoft.ege.components.ComponentStaticMeshs;
import org.atriasoft.ege.components.ComponentTexture;
import org.atriasoft.ege.components.ComponentTextures;
import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.ege.engines.EngineMap;

public class MapVoxel extends EngineMap {
	//List<VoxelChunk> listOfChunks = new ArrayList<VoxelChunk>();
	ComponentTextures textures;
	
	public MapVoxel(Environement env){
		super(env);
		// for basic test ... after generate dynamic ...
		textures = new ComponentTextures();
	}
	public void init() {
		textures.setTexture("stone", new Uri("DATA", "blocks/stone.png"));
		textures.setTexture("grass", new Uri("DATA", "blocks/dirt_podzol_top.png"));
		textures.setTexture("dirt", new Uri("DATA", "blocks/dirt.png"));
		textures.setTexture("watter", new Uri("DATA", "blocks/water_static.png"));
		textures.setTexture("unbreakable", new Uri("DATA", "blocks/stone_diorite.png"));
		
//		addNewChunk(new Vector3i(-1,-1, 0));
//		addNewChunk(new Vector3i(-1, 0, 0));
//		addNewChunk(new Vector3i(-1, 1, 0));
//		addNewChunk(new Vector3i( 0,-1, 0));
		addNewChunk(new Vector3i( 0, 0, 0));
//		addNewChunk(new Vector3i( 0, 1, 0));
//		addNewChunk(new Vector3i( 1,-1, 0));
//		addNewChunk(new Vector3i( 1, 0, 0));
//		addNewChunk(new Vector3i( 1, 1, 0));
	}
	private void addNewChunk(Vector3i position) {
		// simple sun to have a global light ...
		Entity tmpEntity = new Entity(this.env);
		tmpEntity.addComponent(new ComponentPosition(new Transform3D(new Vector3f(position.x(),position.y(),0))));
		VoxelChunk tmpVoxelChunk = new VoxelChunk(this, position);
		tmpEntity.addComponent(tmpVoxelChunk);
		ComponentDynamicMeshsVoxelMap mesh = new ComponentDynamicMeshsVoxelMap(tmpVoxelChunk);
		tmpEntity.addComponent(mesh);
		tmpEntity.addComponent(textures);
		ComponentMaterials materials = new ComponentMaterials();
		materials.setMaterial("stone", new Material());
		materials.setMaterial("grass", new Material());
		materials.setMaterial("dirt", new Material());
		materials.setMaterial("watter", new Material());
		materials.setMaterial("unbreakable", new Material());
		tmpEntity.addComponent(materials);
		tmpEntity.addComponent(new ComponentRenderTexturedMaterialsDynamicMeshs(
				new Uri("DATA", "basicMaterial.vert"),
				new Uri("DATA", "basicMaterial.frag"),
				(EngineLight)env.getEngine(EngineLight.ENGINE_NAME)));
		ComponentPhysics physics = new ComponentPhysics(this.env);
		//PhysicMapVoxel box = new PhysicMapVoxel(tmpVoxelChunk);
		//physics.addShape(box);
		//physics.setStaticObject(true);
		tmpEntity.addComponent(physics);
		this.env.addEntity(tmpEntity);
			
	}
}
