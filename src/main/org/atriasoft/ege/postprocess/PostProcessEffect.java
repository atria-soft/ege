package org.atriasoft.ege.postprocess;

import org.atriasoft.etk.math.Vector2f;

/**
 * A single post-process effect applied on an entity's screen-space silhouette.
 * <p>
 * Effects are rendered as fullscreen quads using the entity's silhouette FBO
 * texture as a pixel mask. The engine renders the silhouette once, then each
 * effect in order draws its contribution to the final framebuffer.
 * <p>
 * Implementations must configure OpenGL blend mode before drawing and restore
 * it after. The fullscreen quad VAO and silhouette texture are provided by
 * {@link PostProcessResources}.
 * <p>
 * Built-in effects:
 * <ul>
 *   <li>{@link OutlineEffect} — screen-space outline around the silhouette</li>
 *   <li>{@link TintOverlayEffect} — transparent color overlay (alpha blend)</li>
 *   <li>{@link AdditiveOverlayEffect} — additive color overlay (brightens)</li>
 * </ul>
 *
 * @see PostProcessResources
 * @see org.atriasoft.ege.components.ComponentPostProcess
 */
public interface PostProcessEffect {
	/**
	 * Render this effect using the shared silhouette texture and fullscreen quad.
	 * <p>
	 * Called by the engine after the silhouette pass. The default framebuffer is
	 * already bound, depth test is disabled, and the silhouette texture contains
	 * the current entity's mask. The effect must:
	 * <ol>
	 *   <li>Enable the appropriate blend mode</li>
	 *   <li>Bind and configure its shader program</li>
	 *   <li>Draw the fullscreen quad from {@code resources}</li>
	 *   <li>Disable blending and unbind the program</li>
	 * </ol>
	 *
	 * @param resources    Shared post-process resources (shaders, FBO texture, quad VAO)
	 * @param viewportSize Current viewport dimensions in pixels
	 */
	void render(PostProcessResources resources, Vector2f viewportSize);
}
