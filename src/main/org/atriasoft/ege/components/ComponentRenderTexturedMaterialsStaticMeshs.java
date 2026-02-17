package org.atriasoft.ege.components;

import java.util.Set;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.components.part.LightRender;
import org.atriasoft.ege.components.part.MaterialsRender;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.components.part.RenderContext;
import org.atriasoft.ege.components.part.TransformRender;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;

public class ComponentRenderTexturedMaterialsStaticMeshs extends ComponentRender {
	ComponentStaticMeshs meshs = null;
	ComponentTextures textures = null;
	ComponentMaterials materials = null;
	ResourceProgram program = null;
	LightRender renderLight = null;
	MaterialsRender renderMaterials = null;
	TransformRender renderTransform = null;
	private boolean lightInitialized = false;

	public ComponentRenderTexturedMaterialsStaticMeshs(final Uri vertexShader, final Uri fragmentShader) {
		this.renderTransform = new TransformRender();
		this.renderMaterials = new MaterialsRender();
		this.program = ResourceProgram.create(vertexShader, fragmentShader);
		if (this.program != null) {
			this.renderTransform.init(this.program);
			this.renderMaterials.init(this.program);
		}
	}

	@Override
	public void addFriendComponent(final Component component) {
		if (component instanceof ComponentStaticMeshs refTyped) {
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
	public void render(final RenderContext context) {
		// Lazy init of LightRender on first render
		if (!this.lightInitialized && context.getEngineLight() != null && this.program != null) {
			this.renderLight = new LightRender();
			this.renderLight.init(this.program);
			if (this.renderTransform.getPositionning() != null) {
				this.renderLight.setPositionning(this.renderTransform.getPositionning());
			}
			this.lightInitialized = true;
		}
		// Select the program:
		this.program.use();
		// Bind all the element for the rendering:
		if (this.renderLight != null) {
			this.renderLight.bindForRendering(this.program, context.getEngineLight());
		}
		this.renderTransform.bindForRendering(this.program);
		final Set<String> keys = this.meshs.getKeys();

		for (final String key : keys) {
			this.meshs.bindForRendering(key);
			this.textures.bindForRendering(key);
			this.renderMaterials.bindForRendering(this.program, key);
			// update of flags is done asynchronously ==> need update before drawing...
			OpenGL.updateAllFlags();
			// Request the draw all the elements:
			this.meshs.render(key);
			// remove all element to render:
			this.renderMaterials.unBindForRendering();
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
