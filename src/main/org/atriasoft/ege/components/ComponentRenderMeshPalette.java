package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.components.part.LightRender;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.components.part.RenderContext;
import org.atriasoft.ege.components.part.TransformRender;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;

public class ComponentRenderMeshPalette extends ComponentRender {
	ComponentMesh mesh = null;
	ResourceProgram program = null;
	ComponentTexturePalette texture = null;

	LightRender renderLight = null;
	TransformRender renderTransform = null;
	private boolean lightInitialized = false;

	public ComponentRenderMeshPalette(final Uri vertexShader, final Uri fragmentShader) {
		this.renderTransform = new TransformRender();
		this.program = ResourceProgram.create(vertexShader, fragmentShader);
		if (this.program != null) {
			this.renderTransform.init(this.program);
		}
	}
	
	@Override
	public void addFriendComponent(final Component component) {
		if (component instanceof ComponentMesh refTyped) {
			this.mesh = refTyped;
		}
		if (component instanceof ComponentTexturePalette refTyped) {
			this.texture = refTyped;
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
		this.mesh.bindForRendering();
		this.texture.bindForRendering();
		if (this.renderLight != null) {
			this.renderLight.bindForRendering(this.program, context.getEngineLight());
		}
		this.renderTransform.bindForRendering(this.program);
		// update of flags is done asynchronously ==> need update before drawing...
		OpenGL.updateAllFlags();
		// Request the draw all the elements:
		this.mesh.renderArrays();
		// remove all element to render:
		this.renderTransform.unBindForRendering();
		if (this.renderLight != null) {
			this.renderLight.unBindForRendering();
		}
		this.texture.unBindForRendering();
		this.mesh.unBindForRendering();
		// Disable program:
		this.program.unUse();
	}
}
