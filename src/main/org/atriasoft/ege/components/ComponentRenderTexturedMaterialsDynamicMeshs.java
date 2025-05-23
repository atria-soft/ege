package org.atriasoft.ege.components;

import java.util.Set;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.components.part.LightRender;
import org.atriasoft.ege.components.part.MaterialsRender;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.components.part.TransformRender;
import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;

public class ComponentRenderTexturedMaterialsDynamicMeshs extends ComponentRender {
	ComponentDynamicMeshs meshs = null;
	ComponentTextures textures = null;
	ComponentPosition position = null;
	ResourceProgram program = null;

	LightRender renderLight = null;
	MaterialsRender renderMaterials = null;
	TransformRender renderTransform = null;
	
	public ComponentRenderTexturedMaterialsDynamicMeshs(final Uri vertexShader, final Uri fragmentShader, final EngineLight lightEngine) {
		if (lightEngine != null) {
			this.renderLight = new LightRender(lightEngine);
		}
		this.renderTransform = new TransformRender();
		this.renderMaterials = new MaterialsRender();
		this.program = ResourceProgram.create(vertexShader, fragmentShader);
		if (this.program != null) {
			this.renderTransform.init(this.program);
			if (this.renderLight != null) {
				this.renderLight.init(this.program);
			}
			this.renderMaterials.init(this.program);
		}
		
	}
	@Override
	public void addFriendComponent(final Component component) {
		if (component instanceof ComponentDynamicMeshs refTyped) {
			this.meshs = refTyped;
		}
		if (component instanceof ComponentTextures refTyped) {
			this.textures = refTyped;
		}
		if (component instanceof ComponentMaterials refTyped) {
			this.renderMaterials.setMaterial(refTyped);
		}
		if (component instanceof PositionningInterface refTyped) {
			this.renderTransform.setPositionning(refTyped);
			if (this.renderLight != null) {
				this.renderLight.setPositionning(refTyped);
			}
		}
	}
	@Override
	public void removeFriendComponent(final Component component) {
		// nothing to do.
	}
	@Override
	public void render() {
		// Select the program:
		this.program.use();
		// Bind all the element for the rendering:
		if (this.renderLight != null) {
			this.renderLight.bindForRendering(this.program);
		}
		this.renderTransform.bindForRendering(this.program);		
		Set<String> keys = this.meshs.getKeys();
		
		for (String key : keys) {
			this.meshs.bindForRendering(key);
			this.textures.bindForRendering(key);
			this.renderMaterials.bindForRendering(this.program, key);
			// update of flags is done asynchronously ==> need update before drawing...
			OpenGL.updateAllFlags();
			// Request the draw all the elements:
			this.meshs.render(key);
			this.textures.unBindForRendering(key);
			this.meshs.unBindForRendering(key);
		}
		// remove all element to render:
		this.renderTransform.unBindForRendering();
		if (this.renderLight != null) {
			this.renderLight.unBindForRendering();
		}
		this.program.unUse();
	}
}

