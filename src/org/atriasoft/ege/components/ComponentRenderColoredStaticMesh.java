
package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.components.part.TransformRender;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;

public class ComponentRenderColoredStaticMesh extends ComponentRender {
	ComponentStaticMesh mesh = null;
	ResourceProgram program = null;
	TransformRender renderTransform = null;
	
	public ComponentRenderColoredStaticMesh(final Uri vertexShader, final Uri fragmentShader) {
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
			if (component instanceof PositionningInterface refTyped) {
			this.renderTransform.setPositionning(refTyped);
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
		this.renderTransform.bindForRendering(this.program);
		this.mesh.bindForRendering();
		// update of flags is done asyncronously ==> need update befor drawing...
		OpenGL.updateAllFlags();
		// Request the draw od the elements:
		this.mesh.render();
		// remove all element to render:
		this.renderTransform.unBindForRendering();
		this.mesh.unBindForRendering();
		// Disable program:
		this.program.unUse();
	}
}
