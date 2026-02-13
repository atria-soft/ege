package org.atriasoft.ege.postprocess;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.ResourceProgram;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;
import org.lwjgl.opengl.GL11;

/**
 * Additive color overlay that brightens the entity's visible pixels.
 * <p>
 * Adds color to the existing framebuffer content inside the entity silhouette
 * using additive blending (SRC_ALPHA, GL_ONE). This preserves the original
 * mesh colors while making them brighter.
 * <p>
 * Use this for glow, selection highlight, or power-up effects.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Subtle white glow
 * new AdditiveOverlayEffect(new Color(1, 1, 1, 0.2f))
 * }</pre>
 *
 * @see PostProcessEffect
 * @see PostProcessResources
 */
public class AdditiveOverlayEffect implements PostProcessEffect {
	private Color color;

	/**
	 * Create an additive overlay effect.
	 *
	 * @param color Additive color (alpha controls intensity, RGB adds to existing colors)
	 */
	public AdditiveOverlayEffect(final Color color) {
		this.color = color;
	}

	/** @return The additive color */
	public Color getColor() {
		return this.color;
	}

	/**
	 * Set the additive color.
	 * @param color New additive color
	 */
	public void setColor(final Color color) {
		this.color = color;
	}

	@Override
	public void render(final PostProcessResources resources, final Vector2f viewportSize) {
		final ResourceProgram program = resources.getMaskOverlayProgram();
		final ResourceVirtualArrayObject quad = resources.getQuadVao();
		if (program == null || quad == null) {
			return;
		}

		OpenGL.enable(Flag.flag_blend);
		OpenGL.updateAllFlags();
		OpenGL.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // additive

		program.use();
		program.setTexture0(resources.getMaskOverlaySilhouetteTex(), resources.getFboTextureId());
		program.uniformColor(resources.getMaskOverlayColorUniform(), this.color);

		quad.bindForRendering();
		OpenGL.updateAllFlags();
		quad.render(RenderMode.TRIANGLE);
		quad.unBindForRendering();

		program.unUse();
		OpenGL.disable(Flag.flag_blend);
	}
}
