package org.atriasoft.ege.map;

import org.atriasoft.etk.Uri;
import org.atriasoft.ege.components.ComponentRenderTexturedMaterialsStaticMeshs;
import org.atriasoft.ege.engines.EngineLight;

public class ComponentRenderVoxelMesh extends ComponentRenderTexturedMaterialsStaticMeshs {

	public ComponentRenderVoxelMesh(Uri vertexShader, Uri fragmentShader, EngineLight lightEngine, VoxelChunk chunk) {
		super(vertexShader, fragmentShader, lightEngine);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void update(float timeStep) {
		
	}

}
