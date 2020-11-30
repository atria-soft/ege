package renderEngine;

import java.util.List;
import java.util.Map;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import entities.Entity;
import models.RawModel;
import models.TexturedModel;
import shaders.StaticShader;
import textures.ModelTexture;
import toolbox.Maths;

/**
 * Handles the rendering of a model to the screen.
 * 
 * @author Karl
 *
 */
public class EntityRenderer {
	private StaticShader shader;
	public EntityRenderer(StaticShader shader, Matrix4f projectionMatrix) {
		this.shader = shader;
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}
	public void render(Map<TexturedModel, List<Entity>> entities) {
		OpenGL.enable(OpenGL.Flag.flag_blend);
		OpenGL.enable(OpenGL.Flag.flag_cullFace);
		OpenGL.enable(OpenGL.Flag.flag_back);
		for (TexturedModel model:entities.keySet()) {
			prepareTexturedModel(model);
			OpenGL.updateAllFlags();
			List<Entity> batch = entities.get(model);
			for (Entity entity:batch) {
				prepareInstance(entity);
				System.out.println("draw an entity...");
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			}
			unbindTexturedModel();
		}
		OpenGL.disable(OpenGL.Flag.flag_blend);
		OpenGL.disable(OpenGL.Flag.flag_cullFace);
		OpenGL.disable(OpenGL.Flag.flag_back);
	}
	
	private void prepareTexturedModel(TexturedModel model) {
		RawModel rawModel = model.getRawModel();
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		ModelTexture texture = model.getTexture();
		shader.loadNumberOfRows(texture.getNumberOfRows());
		if (texture.isHasTransparency() == false) {
			OpenGL.enable(OpenGL.Flag.flag_cullFace);
			OpenGL.enable(OpenGL.Flag.flag_back);
		}
		shader.loadFakeLightingVariable(texture.isUseFakeLighting());
		shader.loadShineVariable(texture.getShineDamper(), texture.getReflectivity());
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getTexturedID());
	}
	
	private void unbindTexturedModel() {
		OpenGL.disable(OpenGL.Flag.flag_cullFace);
		OpenGL.disable(OpenGL.Flag.flag_back);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		
	}
	
	private void prepareInstance(Entity entity) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
		shader.loadTransformationMatrix(transformationMatrix);
		shader.loadOffset(entity.getTextureXOffset(), entity.getTextureYOffset());
	}
	

	
}
