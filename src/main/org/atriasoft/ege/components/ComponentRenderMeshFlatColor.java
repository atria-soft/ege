package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.components.part.LightRender;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.components.part.RenderContext;
import org.atriasoft.ege.components.part.ShadowRender;
import org.atriasoft.ege.components.part.TransformRender;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;

/**
 * Flat-shaded terrain renderer: per-vertex RGBA color attribute, no texture,
 * no palette. Used for low-poly heightmap chunks built via
 * {@code ResourceMeshHeightMap.udateDataColor}.
 */
public class ComponentRenderMeshFlatColor extends ComponentRender {
	ComponentMesh mesh = null;
	ResourceProgram program = null;

	LightRender renderLight = null;
	TransformRender renderTransform = null;
	ShadowRender renderShadow = null;
	private boolean lightInitialized = false;
	private boolean shadowInitialized = false;

	public ComponentRenderMeshFlatColor(final Uri vertexShader, final Uri fragmentShader) {
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
		if (!this.lightInitialized && context.getEngineLight() != null && this.program != null) {
			this.renderLight = new LightRender();
			this.renderLight.init(this.program);
			if (this.renderTransform.getPositionning() != null) {
				this.renderLight.setPositionning(this.renderTransform.getPositionning());
			}
			this.lightInitialized = true;
		}
		if (!this.shadowInitialized && context.getEngineShadow() != null && this.program != null) {
			this.renderShadow = new ShadowRender();
			this.renderShadow.init(this.program);
			this.shadowInitialized = true;
		}
		this.program.use();
		this.mesh.bindForRendering();
		if (this.renderLight != null) {
			this.renderLight.bindForRendering(this.program, context.getEngineLight());
		}
		if (this.renderShadow != null) {
			this.renderShadow.bindForRendering(this.program, context.getEngineShadow());
		}
		this.renderTransform.bindForRendering(this.program);
		OpenGL.updateAllFlags();
		this.mesh.renderArrays();
		this.renderTransform.unBindForRendering();
		if (this.renderShadow != null) {
			this.renderShadow.unBindForRendering();
		}
		if (this.renderLight != null) {
			this.renderLight.unBindForRendering();
		}
		this.mesh.unBindForRendering();
		this.program.unUse();
	}
}
