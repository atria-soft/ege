package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Light;
import org.atriasoft.ege.Material;
import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.resource.ResourceProgram;

public class ComponentRenderTexturedMaterialsStaticMesh extends ComponentRender {
	private static final int numberOfLight = 8;
	private int GLambientFactor;
	private int GLdiffuseFactor;
	private GlLightIndex[] GLlights;
	private int GLMatrixProjection;
	private int GLMatrixTransformation;
	private int GLMatrixView;
	private int GLshininess;
	private int GLspecularFactor;
	EngineLight lightEngine;
	ComponentMaterial material = null;
	ComponentStaticMesh mesh = null;
	private ComponentPhysics playerPhysics = null;
	ComponentPosition position = null;
	ResourceProgram program = null;
	ComponentTexture texture = null;
	
	public ComponentRenderTexturedMaterialsStaticMesh(final Uri vertexShader, final Uri fragmentShader, final EngineLight lightEngine) {
		this.lightEngine = lightEngine;
		this.program = ResourceProgram.create(vertexShader, fragmentShader);
		if (this.program != null) {
			this.GLMatrixTransformation = this.program.getUniform("in_matrixTransformation");
			this.GLMatrixProjection = this.program.getUniform("in_matrixProjection");
			this.GLMatrixView = this.program.getUniform("in_matrixView");
			this.GLambientFactor = this.program.getUniform("in_material.ambientFactor");
			this.GLdiffuseFactor = this.program.getUniform("in_material.diffuseFactor");
			this.GLspecularFactor = this.program.getUniform("in_material.specularFactor");
			this.GLshininess = this.program.getUniform("in_material.shininess");
			this.GLlights = new GlLightIndex[ComponentRenderTexturedMaterialsStaticMesh.numberOfLight];
			for (int iii = 0; iii < ComponentRenderTexturedMaterialsStaticMesh.numberOfLight; iii++) {
				final int color = this.program.getUniform("in_lights[" + iii + "].color");
				final int position = this.program.getUniform("in_lights[" + iii + "].position");
				final int attenuation = this.program.getUniform("in_lights[" + iii + "].attenuation");
				this.GLlights[iii] = new GlLightIndex(color, position, attenuation);
			}
		}
		
	}
	
	@Override
	public void addFriendComponent(final Component component) {
		if (component.getType().contentEquals("static-mesh")) {
			this.mesh = (ComponentStaticMesh) component;
		}
		if (component.getType().contentEquals("texture")) {
			this.texture = (ComponentTexture) component;
		}
		if (component.getType().contentEquals("material")) {
			this.material = (ComponentMaterial) component;
		}
		if (component.getType().contentEquals("position")) {
			this.position = (ComponentPosition) component;
		}
		if (component.getType().contentEquals("physics")) {
			this.playerPhysics = (ComponentPhysics) component;
		}
	}
	
	@Override
	public void removeFriendComponent(final Component component) {
		// nothing to do.
	}
	
	@Override
	public void render() {
		this.program.use();
		Light[] lights = null;
		Matrix4f transformationMatrix = null;
		if (this.position != null) {
			//Log.warning("position " + this.position.getTransform());
			lights = this.lightEngine.getNearest(this.position.getTransform().getPosition());
			transformationMatrix = this.position.getTransform().getOpenGLMatrix();
		} else if (this.playerPhysics != null) {
			//Log.warning("playerPosition " + this.playerPhysics.getTransform());
			lights = this.lightEngine.getNearest(this.playerPhysics.getTransform().getPosition());
			transformationMatrix = this.playerPhysics.getTransform().getOpenGLMatrix();
		}
		final Matrix4f projectionMatrix = OpenGL.getMatrix();
		final Matrix4f viewMatrix = OpenGL.getCameraMatrix();
		this.mesh.bindForRendering();
		this.texture.bindForRendering();
		
		final Material mat = this.material.getMaterial();
		this.program.uniformVector(this.GLambientFactor, mat.getAmbientFactor());
		this.program.uniformVector(this.GLdiffuseFactor, mat.getDiffuseFactor());
		this.program.uniformVector(this.GLspecularFactor, mat.getSpecularFactor());
		this.program.uniformFloat(this.GLshininess, mat.getShininess());
		for (int iii = 0; iii < ComponentRenderTexturedMaterialsStaticMesh.numberOfLight; iii++) {
			if (lights[iii] != null) {
				this.program.uniformVector(this.GLlights[iii].oGLposition, lights[iii].getPositionDelta());
				this.program.uniformVector(this.GLlights[iii].oGLcolor, lights[iii].getColor());
				this.program.uniformVector(this.GLlights[iii].oGLattenuation, lights[iii].getAttenuation());
			} else {
				this.program.uniformVector(this.GLlights[iii].oGLposition, Vector3f.ZERO);
				this.program.uniformVector(this.GLlights[iii].oGLcolor, Vector3f.ZERO);
				this.program.uniformVector(this.GLlights[iii].oGLattenuation, new Vector3f(1, 0, 0));
			}
		}
		this.program.uniformMatrix(this.GLMatrixView, viewMatrix);
		this.program.uniformMatrix(this.GLMatrixProjection, projectionMatrix);
		// Change the position for each element with the same pipeline you need to render ...
		this.program.uniformMatrix(this.GLMatrixTransformation, transformationMatrix);
		// update of flags is done asynchronously ==> need update before drawing...
		OpenGL.updateAllFlags();
		// Request the draw all the elements:
		this.mesh.render();
		this.texture.unBindForRendering();
		this.mesh.unBindForRendering();
		this.program.unUse();
	}
}
