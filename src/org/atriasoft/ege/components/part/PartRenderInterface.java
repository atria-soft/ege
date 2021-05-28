package org.atriasoft.ege.components.part;

import org.atriasoft.gale.resource.ResourceProgram;

/**
 * Element that permit to add prat element on the Shader rendering (permit to reduce code and normalize IO naming of the shader
 */
public interface PartRenderInterface {
	/**
	 * Initialize the render part with the program elements
	 * @param program Open GL program
	 */
	void init(final ResourceProgram program);
	/**
	 * Bing this part in the shader 
	 * @param program Program that manage the rendering
	 */
	void bindForRendering(final ResourceProgram program);
	/**
	 * Remove element from the shader.
	 */
	void unBindForRendering();

}
