package org.atriasoft.ege.components.part;

import org.atriasoft.ege.components.ComponentMaterials;
import org.atriasoft.gale.resource.ResourceProgram;

public class MaterialsRender extends MaterialRenderBase {

	ComponentMaterials material = null;
	
	public void bindForRendering(final ResourceProgram program, final String type) {
		bindForRendering(program, this.material.getMaterial(type));
	}
	
	@Override
	public void bindForRendering(final ResourceProgram program) {
		bindForRendering(program, this.material.getMaterial("default"));
	}
	

	public void setMaterial(final ComponentMaterials component) {
		this.material = component;
	}
}
