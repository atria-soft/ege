package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.components.part.RenderContext;
import org.atriasoft.ege.components.part.TransformRender;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;
import org.lwjgl.opengl.GL11;

/**
 * Render the mesh as a wireframe overlay (GL_LINE polygon mode) with a small
 * negative polygon offset so the lines stay on top of any fill pass drawn just
 * before. Uses its own shader (typically {@code wireframeOverlay.vert/.frag})
 * which outputs a fixed color, so the lines are independent of the underlying
 * mesh vertex colors.
 * <p>
 * GL state (polygon mode, polygon offset, depth bias enable) is restored to
 * its pre-render value to avoid leaking into subsequent passes.
 */
public class ComponentRenderMeshWireframe extends ComponentRender {
	ComponentMesh mesh = null;
	ResourceProgram program = null;
	TransformRender renderTransform = null;

	public ComponentRenderMeshWireframe(final Uri vertexShader, final Uri fragmentShader) {
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
		}
	}

	@Override
	public void removeFriendComponent(final Component component) {
		// nothing to do.
	}

	@Override
	public void render(final RenderContext context) {
		if (this.mesh == null || this.program == null) {
			return;
		}
		// Push GL state we are about to override.
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
		GL11.glPolygonOffset(-1.0f, -1.0f);

		this.program.use();
		this.mesh.bindForRendering();
		this.renderTransform.bindForRendering(this.program);
		OpenGL.updateAllFlags();
		this.mesh.renderArrays();
		this.renderTransform.unBindForRendering();
		this.mesh.unBindForRendering();
		this.program.unUse();

		// Restore previous GL state.
		GL11.glPolygonOffset(0.0f, 0.0f);
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_LINE);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}
}
