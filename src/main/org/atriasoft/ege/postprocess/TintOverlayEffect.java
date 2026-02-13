package org.atriasoft.ege.postprocess;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.ResourceProgram;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;

/**
 * Transparent color overlay on the entity's visible pixels.
 * <p>
 * Applies a uniform tint color to all pixels inside the entity silhouette
 * using standard alpha blending (SRC_ALPHA, ONE_MINUS_SRC_ALPHA).
 * The alpha channel of the color controls the tint intensity.
 * <p>
 * Use this for selection dimming, damage flash, or status coloring.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Semi-transparent red tint
 * new TintOverlayEffect(new Color(1, 0, 0, 0.3f))
 * }</pre>
 *
 * @see PostProcessEffect
 * @see PostProcessResources
 */
public class TintOverlayEffect implements PostProcessEffect {
	private Color color;

	/**
	 * Create a tint overlay effect.
	 *
	 * @param color Tint color (alpha controls intensity, e.g. 0.3 = 30% tint)
	 */
	public TintOverlayEffect(final Color color) {
		this.color = color;
	}

	/** @return The tint color */
	public Color getColor() {
		return this.color;
	}

	/**
	 * Set the tint color.
	 * @param color New tint color
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
		OpenGL.blendFuncAuto(); // SRC_ALPHA, ONE_MINUS_SRC_ALPHA

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
