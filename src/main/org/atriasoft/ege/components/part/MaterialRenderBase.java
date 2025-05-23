package org.atriasoft.ege.components.part;

import org.atriasoft.ege.Material;
import org.atriasoft.gale.resource.ResourceProgram;

public abstract class MaterialRenderBase implements PartRenderInterface {

	protected int GLambientFactor;
	protected int GLdiffuseFactor;
	protected int GLshininess;
	protected int GLspecularFactor;
	
	public MaterialRenderBase() {
		
	}

	@Override
	public void init(final ResourceProgram program) {
		this.GLambientFactor = program.getUniform("in_material.ambientFactor");
		this.GLdiffuseFactor = program.getUniform("in_material.diffuseFactor");
		this.GLspecularFactor = program.getUniform("in_material.specularFactor");
		this.GLshininess = program.getUniform("in_material.shininess");
	}

	public void bindForRendering(final ResourceProgram program, final Material mat) {
		program.uniformVector(this.GLambientFactor, mat.getAmbientFactor());
		program.uniformVector(this.GLdiffuseFactor, mat.getDiffuseFactor());
		program.uniformVector(this.GLspecularFactor, mat.getSpecularFactor());
		program.uniformFloat(this.GLshininess, mat.getShininess());
	}

	@Override
	public void unBindForRendering() {
	
	}
}
