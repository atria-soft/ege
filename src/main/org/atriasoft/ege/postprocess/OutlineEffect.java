package org.atriasoft.ege.postprocess;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.backend3d.OpenGL.Flag;
import org.atriasoft.gale.backend3d.OpenGL.RenderMode;
import org.atriasoft.gale.resource.ResourceProgram;
import org.atriasoft.gale.resource.ResourceVirtualArrayObject;

/**
 * Screen-space outline effect drawn around the entity silhouette.
 * <p>
 * For each pixel outside the silhouette, samples neighbors in a circular
 * radius. If any neighbor is inside the silhouette, the pixel is colored
 * with the outline color. The outline width is constant in screen pixels
 * regardless of distance from the camera.
 * <p>
 * Example usage:
 * <pre>{@code
 * entity.addComponent(new ComponentPostProcess()
 *     .addEffect(new OutlineEffect(4.0f, new Color(0, 0, 0, 1))));
 * }</pre>
 *
 * @see PostProcessEffect
 * @see PostProcessResources
 */
public class OutlineEffect implements PostProcessEffect {
	private float widthPixels;
	private Color color;

	/**
	 * Create an outline effect.
	 *
	 * @param widthPixels Outline width in screen pixels (constant regardless of distance)
	 * @param color       Outline color (alpha channel controls opacity)
	 */
	public OutlineEffect(final float widthPixels, final Color color) {
		this.widthPixels = widthPixels;
		this.color = color;
	}

	/** @return The outline width in screen pixels */
	public float getWidthPixels() {
		return this.widthPixels;
	}

	/**
	 * Set the outline width.
	 * @param widthPixels New width in screen pixels
	 */
	public void setWidthPixels(final float widthPixels) {
		this.widthPixels = widthPixels;
	}

	/** @return The outline color */
	public Color getColor() {
		return this.color;
	}

	/**
	 * Set the outline color.
	 * @param color New outline color
	 */
	public void setColor(final Color color) {
		this.color = color;
	}

	@Override
	public void render(final PostProcessResources resources, final Vector2f viewportSize) {
		final ResourceProgram program = resources.getOutlineProgram();
		final ResourceVirtualArrayObject quad = resources.getQuadVao();
		if (program == null || quad == null) {
			return;
		}

		final int vpWidth = (int) viewportSize.x();
		final int vpHeight = (int) viewportSize.y();

		OpenGL.enable(Flag.flag_blend);
		OpenGL.updateAllFlags();
		OpenGL.blendFuncAuto();

		program.use();
		program.setTexture0(resources.getOutlineSilhouetteTex(), resources.getFboTextureId());
		program.uniformColor(resources.getOutlineColorUniform(), this.color);
		program.uniformFloat(resources.getOutlineSizeUniform(), this.widthPixels);
		program.uniformVector(resources.getOutlineTexelSizeUniform(),
				new Vector2f(1.0f / vpWidth, 1.0f / vpHeight));

		quad.bindForRendering();
		OpenGL.updateAllFlags();
		quad.render(RenderMode.TRIANGLE);
		quad.unBindForRendering();

		program.unUse();
		OpenGL.disable(Flag.flag_blend);
	}
}
