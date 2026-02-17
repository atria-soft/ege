package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.components.part.LightRender;
import org.atriasoft.ege.components.part.MaterialRender;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.components.part.RenderContext;
import org.atriasoft.ege.components.part.ShadowRender;
import org.atriasoft.ege.components.part.TransformRender;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;

public class ComponentRenderTexturedMaterialsStaticMesh extends ComponentRender {

	ComponentStaticMesh mesh = null;
	ResourceProgram program = null;
	ComponentTexture texture = null;

	LightRender renderLight = null;
	MaterialRender renderMaterial = null;
	TransformRender renderTransform = null;
	ShadowRender renderShadow = null;
	private boolean lightInitialized = false;
	private boolean shadowInitialized = false;

	public ComponentRenderTexturedMaterialsStaticMesh(final Uri vertexShader, final Uri fragmentShader) {
		this.renderTransform = new TransformRender();
		this.renderMaterial = new MaterialRender();
		this.program = ResourceProgram.create(vertexShader, fragmentShader);
		if (this.program != null) {
			this.renderTransform.init(this.program);
			this.renderMaterial.init(this.program);
		}
	}

	@Override
	public void addFriendComponent(final Component component) {
		if (component instanceof ComponentStaticMesh refTyped) {
			this.mesh = refTyped;
		}
		if (component instanceof ComponentTexture refTyped) {
			this.texture = refTyped;
		}
		if (component instanceof ComponentMaterial refTyped) {
			this.renderMaterial.setMaterial(refTyped);
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
		// Lazy init of ShadowRender on first render
		if (!this.shadowInitialized && context.getEngineShadow() != null && this.program != null) {
			this.renderShadow = new ShadowRender();
			this.renderShadow.init(this.program);
			this.shadowInitialized = true;
		}
		// Select the program:
		this.program.use();
		// Bind all the element for the rendering:
		this.mesh.bindForRendering();
		this.texture.bindForRendering();
		this.renderMaterial.bindForRendering(this.program);
		if (this.renderLight != null) {
			this.renderLight.bindForRendering(this.program, context.getEngineLight());
		}
		if (this.renderShadow != null) {
			this.renderShadow.bindForRendering(this.program, context.getEngineShadow());
		}
		this.renderTransform.bindForRendering(this.program);
		// update of flags is done asynchronously ==> need update before drawing...
		OpenGL.updateAllFlags();
		// Request the draw all the elements:
		this.mesh.render();
		// remove all element to render:
		this.renderTransform.unBindForRendering();
		if (this.renderShadow != null) {
			this.renderShadow.unBindForRendering();
		}
		if (this.renderLight != null) {
			this.renderLight.unBindForRendering();
		}
		this.renderMaterial.unBindForRendering();
		this.texture.unBindForRendering();
		this.mesh.unBindForRendering();
		// Disable program:
		this.program.unUse();
	}
}
