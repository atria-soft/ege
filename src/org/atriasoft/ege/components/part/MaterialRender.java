package org.atriasoft.ege.components.part;

import org.atriasoft.ege.components.ComponentMaterial;
import org.atriasoft.gale.resource.ResourceProgram;

public class MaterialRender extends MaterialRenderBase {

	ComponentMaterial material = null;

	@Override
	public void bindForRendering(final ResourceProgram program) {
		bindForRendering(program, this.material.getMaterial());
	}
	
	public void setMaterial(final ComponentMaterial component) {
		this.material = component;
	}

}
