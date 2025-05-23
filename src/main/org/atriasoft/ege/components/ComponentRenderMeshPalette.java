package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.components.part.LightRender;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.components.part.TransformRender;
import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;

public class ComponentRenderMeshPalette extends ComponentRender {
	ComponentMesh mesh = null;
	ResourceProgram program = null;
	ComponentTexturePalette texture = null;
	
	LightRender renderLight = null;
	TransformRender renderTransform = null;
	
	public ComponentRenderMeshPalette(final Uri vertexShader, final Uri fragmentShader, final EngineLight lightEngine) {
		if (lightEngine != null) {
			this.renderLight = new LightRender(lightEngine);
		}
		this.renderTransform = new TransformRender();
		this.program = ResourceProgram.create(vertexShader, fragmentShader);
		if (this.program != null) {
			this.renderTransform.init(this.program);
			if (this.renderLight != null) {
				this.renderLight.init(this.program);
			}
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
	public void render() {
		// Select the program:
		this.program.use();
		// Bind all the element for the rendering:
		this.mesh.bindForRendering();
		this.texture.bindForRendering();
		if (this.renderLight != null) {
			this.renderLight.bindForRendering(this.program);
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
