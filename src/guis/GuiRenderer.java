package guis;

import java.util.List;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import models.RawModel;
import renderEngine.Loader;
import toolbox.Maths;

public class GuiRenderer {

	private final RawModel quad;
	private GuiShader shader;

	public GuiRenderer(Loader loader) {
		float[] positions = { -1, 1, -1, -1, 1, 1, 1, -1 };
		quad = loader.loadToVAO(positions, 2);
		shader = new GuiShader();
	}

	public void render(List<GuiTexture> guis) {
		shader.start();
		// System.out.println("Render Gui " + guis.size());
		GL30.glBindVertexArray(quad.getVaoID());
		GL20.glEnableVertexAttribArray(0);

		OpenGL.enable(OpenGL.Flag.flag_blend);
		OpenGL.enable(OpenGL.Flag.flag_cullFace);
		OpenGL.enable(OpenGL.Flag.flag_back);
		for (GuiTexture gui : guis) {
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, gui.getTexture());
			Matrix4f matrix = Maths.createTransformationMatrix(gui.getPosition(), gui.getScale());
			shader.loadTransformation(matrix);
			OpenGL.updateAllFlags();
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());

		}
		OpenGL.disable(OpenGL.Flag.flag_blend);
		OpenGL.disable(OpenGL.Flag.flag_cullFace);
		OpenGL.disable(OpenGL.Flag.flag_back);

		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		shader.stop();
	}

	public void cleanUp() {
		shader.cleanUp();
	}

}
