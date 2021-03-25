package shaders;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.backend3d.OpenGL;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public abstract class ShaderProgram {

	private int programID;
	private int vertexShaderID;
	private int fragmentShaderID;
	

	public ShaderProgram (String vertexFile, String fragmentFile) {
		vertexShaderID = OpenGL.shaderLoad(new Uri("DATA", vertexFile), OpenGL.ShaderType.vertex);
		fragmentShaderID = OpenGL.shaderLoad(new Uri("DATA", fragmentFile), OpenGL.ShaderType.fragment);
		programID = OpenGL.programCreate();
		OpenGL.programAttach(programID, vertexShaderID);
		OpenGL.programAttach(programID, fragmentShaderID);
		bindAttributes();
		OpenGL.programCompile(programID);
		getAllUniformLocations();
	}
	protected abstract void getAllUniformLocations();

	protected int getUniformLocation(String uniformName) {
		return OpenGL.programGetUniformLocation(programID, uniformName);
	}
	
	public void start() {
		OpenGL.programUse(programID);
	}
	public void stop() {
		OpenGL.programUnUse(programID);
	}
	public void cleanUp() {
		stop();
		OpenGL.programDetach(programID, fragmentShaderID);
		OpenGL.programDetach(programID, vertexShaderID);
		OpenGL.shaderRemove(vertexShaderID);
		OpenGL.shaderRemove(fragmentShaderID);
		OpenGL.programRemove(programID);
	}
	
	protected abstract void bindAttributes();
	
	protected void bindAttribute(int attribute, String variableName) {
		OpenGL.programBindAttribute(programID, attribute, variableName);
	}
}
