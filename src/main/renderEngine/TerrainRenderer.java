package renderEngine;

import java.util.List;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import models.RawModel;
import shaders.TerrainShader;
import terrains.Terrain;
import terrains.TerrainTexturePack;
import textures.ModelTexture;
import toolbox.Maths;

public class TerrainRenderer {

	private TerrainShader shader;

	public TerrainRenderer(TerrainShader shader, Matrix4f projectionMatrix) {
		this.shader = shader;
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.connectTextureUnits();
		shader.stop();
	}

	public void render(List<Terrain> terrains) {
		OpenGL.enable(OpenGL.Flag.flag_blend);
		OpenGL.enable(OpenGL.Flag.flag_cullFace);
		OpenGL.enable(OpenGL.Flag.flag_back);
		for (Terrain terrain : terrains) {
			prepareTerrain(terrain);
			loadModelMatrix(terrain);
			OpenGL.updateAllFlags();
			GL11.glDrawElements(GL11.GL_TRIANGLES, terrain.getModel().getVertexCount(),
					GL11.GL_UNSIGNED_INT, 0);
			unbindTexturedModel();
		}
		OpenGL.disable(OpenGL.Flag.flag_blend);
		OpenGL.disable(OpenGL.Flag.flag_cullFace);
		OpenGL.disable(OpenGL.Flag.flag_back);
	}

	private void prepareTerrain(Terrain terrain) {
		RawModel rawModel = terrain.getModel();
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		bindTextures(terrain);
		shader.loadShineVariable(1, 0);
	}

	private void bindTextures(Terrain terrain) {
		TerrainTexturePack texturePack = terrain.getTexturePack();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getBackgroundTexture().getTextureId());
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getrTexture().getTextureId());
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getgTexture().getTextureId());
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getbTexture().getTextureId());
		GL13.glActiveTexture(GL13.GL_TEXTURE4);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getBlendMap().getTextureId());
	}
	
	private void unbindTexturedModel() {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
	}

	private void loadModelMatrix(Terrain terrain) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(
				new Vector3f(terrain.getX(), 0, terrain.getZ()), new Vector3f(0, 0, 0), 1);
		shader.loadTransformationMatrix(transformationMatrix);
	}

}
