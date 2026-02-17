package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.components.part.RenderContext;
import org.atriasoft.ege.components.part.TransformRender;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;

public class ComponentRenderTexturedStaticMesh extends ComponentRender {
	ComponentStaticMesh mesh = null;
	private final ComponentPhysics playerPhysics = null;
	ComponentPosition position = null;
	ResourceProgram program = null;
	ComponentTexture texture = null;
	
	TransformRender renderTransform = null;
	
	public ComponentRenderTexturedStaticMesh(final Uri vertexShader, final Uri fragmentShader) {
		this.renderTransform = new TransformRender();
		this.program = ResourceProgram.create(vertexShader, fragmentShader);
		if (this.program != null) {
			this.renderTransform.init(this.program);
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
		if (component instanceof PositionningInterface refTyped) {
			this.renderTransform.setPositionning(refTyped);
		}
	}
	
	@Override
	public void removeFriendComponent(final Component component) {
		// nothing to do.
	}
	
	@Override
	public void render(final RenderContext context) {
		// Select the program:
		this.program.use();
		// Bind all the element for the rendering:
		this.mesh.bindForRendering();
		this.texture.bindForRendering();
		this.renderTransform.bindForRendering(this.program);	
		// update of flags is done asynchronously ==> need update before drawing...
		OpenGL.updateAllFlags();
		// Request the draw all the elements:
		this.mesh.render();
		// remove all element to render:
		this.renderTransform.unBindForRendering();
		this.texture.unBindForRendering();
		this.mesh.unBindForRendering();
		this.program.unUse();
	}
}
