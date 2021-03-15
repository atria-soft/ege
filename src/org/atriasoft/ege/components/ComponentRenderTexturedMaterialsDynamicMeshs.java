package org.atriasoft.ege.components;

import java.util.Set;

import org.atriasoft.ege.internal.Log;
import org.atriasoft.ege.Component;
import org.atriasoft.ege.Light;
import org.atriasoft.ege.Material;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;
import org.atriasoft.ege.engines.EngineLight;

public class ComponentRenderTexturedMaterialsDynamicMeshs extends ComponentRender {
	private static final int numberOfLight = 8;
	ComponentDynamicMeshs meshs = null;
	ComponentTextures textures = null;
	ComponentMaterials materials = null;
	ComponentPosition position = null;
	ResourceProgram program = null;
	EngineLight lightEngine;
	private int GLMatrixTransformation;
	private int GLMatrixProjection;
	private int GLMatrixView;
	private int GLambientFactor;
	private int GLdiffuseFactor;
	private int GLspecularFactor;
	private int GLshininess;
	private GlLightIndex[] GLlights;
	
	public ComponentRenderTexturedMaterialsDynamicMeshs(Uri vertexShader, Uri fragmentShader, EngineLight lightEngine) {
		this.lightEngine = lightEngine;
		this.program = ResourceProgram.create(vertexShader, fragmentShader);
		if (this.program != null) {
			this.GLMatrixTransformation = this.program.getUniform("in_matrixTransformation");
			this.GLMatrixProjection     = this.program.getUniform("in_matrixProjection");
			this.GLMatrixView           = this.program.getUniform("in_matrixView");
			this.GLambientFactor        = this.program.getUniform("in_material.ambientFactor");
			this.GLdiffuseFactor        = this.program.getUniform("in_material.diffuseFactor");
			this.GLspecularFactor       = this.program.getUniform("in_material.specularFactor");
			this.GLshininess            = this.program.getUniform("in_material.shininess");
			this.GLlights = new GlLightIndex[numberOfLight];
			for (int iii=0; iii<numberOfLight; iii++) {
				int color       = this.program.getUniform("in_lights[" + iii + "].color");
				int position    = this.program.getUniform("in_lights[" + iii + "].position");
				int attenuation = this.program.getUniform("in_lights[" + iii + "].attenuation");
				this.GLlights[iii] = new GlLightIndex(color, position, attenuation);
			}
		}
		
	}
	@Override
	public void addFriendComponent(Component component) {
		if (component.getType().contentEquals("dynamic-meshs")) {
			meshs = (ComponentDynamicMeshs)component;
		}
		if (component.getType().contentEquals("textures")) {
			textures = (ComponentTextures)component;
		}
		if (component.getType().contentEquals("materials")) {
			materials = (ComponentMaterials)component;
		}
		if (component.getType().contentEquals("position")) {
			position = (ComponentPosition)component;
		}
	}
	@Override
	public void removeFriendComponent(Component component) {
		// nothing to do.
	}
	@Override
	public void render() {
		this.program.use();
		Light[] lights = this.lightEngine.getNearest(position.getTransform().getPosition());
		Matrix4f projectionMatrix = OpenGL.getMatrix();
		Matrix4f viewMatrix = OpenGL.getCameraMatrix();
		Matrix4f transformationMatrix = position.getTransform().getOpenGLMatrix();
		Set<String> keys = this.meshs.getKeys();
		
		for (int iii=0; iii<numberOfLight; iii++) {
			if (lights[iii] != null) {
				this.program.uniformVector(this.GLlights[iii].oGLposition, lights[iii].getPositionDelta());
				this.program.uniformVector(this.GLlights[iii].oGLcolor, lights[iii].getColor());
				this.program.uniformVector(this.GLlights[iii].oGLattenuation, lights[iii].getAttenuation());
			} else {
				this.program.uniformVector(this.GLlights[iii].oGLposition, new Vector3f(0,0,0));
				this.program.uniformVector(this.GLlights[iii].oGLcolor, new Vector3f(0,0,0));
				this.program.uniformVector(this.GLlights[iii].oGLattenuation, new Vector3f(1,0,0));
			}
		}
		this.program.uniformMatrix(this.GLMatrixView, viewMatrix);
		this.program.uniformMatrix(this.GLMatrixProjection, projectionMatrix);
		// Change the position for each element with the same pipeline you need to render ...
		this.program.uniformMatrix(this.GLMatrixTransformation, transformationMatrix);
		
		for (String key : keys) {
			this.meshs.bindForRendering(key);
			this.textures.bindForRendering(key);
			Material mat = this.materials.getMaterial(key);
			this.program.uniformVector(GLambientFactor, mat.getAmbientFactor());
			this.program.uniformVector(GLdiffuseFactor, mat.getDiffuseFactor());
			this.program.uniformVector(GLspecularFactor, mat.getSpecularFactor());
			this.program.uniformFloat(GLshininess, mat.getShininess());
			// update of flags is done asynchronously ==> need update before drawing...
			OpenGL.updateAllFlags();
			// Request the draw all the elements:
			this.meshs.render(key);
			this.textures.unBindForRendering(key);
			this.meshs.unBindForRendering(key);
		}
		this.program.unUse();
	}
}

