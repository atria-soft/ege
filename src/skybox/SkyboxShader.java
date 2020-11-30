package skybox;


import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.etk.Color;

import entities.Camera;
import renderEngine.DisplayManager;
import shaders.ShaderProgram;
import toolbox.Maths;

public class SkyboxShader extends ShaderProgram {

	private static final String VERTEX_FILE = "src/skybox/skybox.vert";
	private static final String FRAGMENT_FILE = "src/skybox/skybox.frag";
	
	private static final float ROTATE_SPEED = 0.02f;
	
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_fogColour;
	private int location_cubeMap;
	private int location_cubeMap2;
	private int location_blendFactor;
	
	private float rotation = 0;
	
	public SkyboxShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	public void loadProjectionMatrix(Matrix4f matrix){
		OpenGL.programLoadUniformMatrix(location_projectionMatrix, matrix);
	}

	public void loadViewMatrix(Camera camera){
		Matrix4f matrix = Maths.createViewMatrixNoTranslate(camera);
		rotation += ROTATE_SPEED * DisplayManager.getFrameTimeSecconds();
		matrix.rotate(new Vector3f(0,1,0), rotation);
		OpenGL.programLoadUniformMatrix(location_viewMatrix, matrix);
	}
	
	public void loadFogColour(Color colour) {
		OpenGL.programLoadUniformColor(location_fogColour, colour);
	}
	
	public void connectTextureUnits() {
		OpenGL.programLoadUniformInt(location_cubeMap, 0);
		OpenGL.programLoadUniformInt(location_cubeMap2, 1);
	}
	
	public void loadBlendFactor(float factor) {
		OpenGL.programLoadUniformFloat(location_blendFactor, factor);
	}
	
	
	@Override
	protected void getAllUniformLocations() {
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_fogColour = super.getUniformLocation("fogColour");
		location_cubeMap = super.getUniformLocation("cubeMap");
		location_cubeMap2 = super.getUniformLocation("cubeMap2");
		location_blendFactor = super.getUniformLocation("blendFactor");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}

}
