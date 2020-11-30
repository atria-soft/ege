package shaders;

import java.util.List;

import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.etk.Color;

import entities.Camera;
import entities.Light;
import toolbox.Maths;

public class StaticShader extends ShaderProgram {
	private static final int MAX_LIGHTS = 4;
	private static final String VERTEX_FILE = "src/shaders/static.vert";
	private static final String FRAGMENT_FILE = "src/shaders/static.frag";

	private int location_transformationMatrix;
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_lightPosition[];
	private int location_lightColour[];
	private int location_lightAttenuation[];
	private int location_reflectivity;
	private int location_shineDamper;
	private int location_useFakeLighting;
	private int location_skyColor;
	private int location_numberOfRows;
	private int location_offset;

	public StaticShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoords");
		super.bindAttribute(2, "normal");
	}

	@Override
	protected void getAllUniformLocations() {
		location_transformationMatrix = super.getUniformLocation("transformationMatrix");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_reflectivity = super.getUniformLocation("reflectivity");
		location_shineDamper = super.getUniformLocation("shineDamper");
		location_useFakeLighting = super.getUniformLocation("useFakeLighting");
		location_skyColor = super.getUniformLocation("skyColor");
		location_numberOfRows = super.getUniformLocation("numberOfRows");
		location_offset = super.getUniformLocation("offset");
		location_lightPosition = new int[MAX_LIGHTS];
		location_lightColour = new int[MAX_LIGHTS];
		location_lightAttenuation = new int[MAX_LIGHTS];
		for (int iii=0; iii<MAX_LIGHTS; iii++) {
			location_lightPosition[iii] = super.getUniformLocation("lightPosition[" + iii+ "]");
			location_lightColour[iii] = super.getUniformLocation("lightColour[" + iii+ "]");
			location_lightAttenuation[iii] = super.getUniformLocation("lightAttenuation[" + iii+ "]");
		}
	}
	

	public void loadNumberOfRows(int numberOfRows) {
		OpenGL.programLoadUniformFloat(location_numberOfRows, numberOfRows);
	}
	
	public void loadOffset(float x, float y) {
		OpenGL.programLoadUniformVector(location_offset, new Vector2f(x,y));
	}
	
	public void loadSkyColour(Color color) {
		OpenGL.programLoadUniformColor(location_skyColor, color);
	}
	
	public void loadFakeLightingVariable(boolean useFake) {
		OpenGL.programLoadUniformBoolean(location_useFakeLighting, useFake);
	}
	
	public void loadShineVariable(float shineDamper, float reflectivity) {
		OpenGL.programLoadUniformFloat(location_reflectivity, reflectivity);
		OpenGL.programLoadUniformFloat(location_shineDamper, shineDamper);
	}
	
	public void loadTransformationMatrix(Matrix4f matrix) {
		OpenGL.programLoadUniformMatrix(location_transformationMatrix, matrix);
	}
	public void loadLights(List<Light> lights) {
		for (int iii=0; iii<MAX_LIGHTS; iii++) {
			if (iii<lights.size()) {
				OpenGL.programLoadUniformVector(location_lightPosition[iii], lights.get(iii).getPosition());
				OpenGL.programLoadUniformVector(location_lightColour[iii], lights.get(iii).getColour());
				OpenGL.programLoadUniformVector(location_lightAttenuation[iii], lights.get(iii).getAttenuation());
			} else {
				OpenGL.programLoadUniformVector(location_lightPosition[iii], new Vector3f(0,0,0));
				OpenGL.programLoadUniformVector(location_lightColour[iii], new Vector3f(0,0,0));
				OpenGL.programLoadUniformVector(location_lightAttenuation[iii], new Vector3f(1,0,0));
			}
		}
	}
	public void loadProjectionMatrix(Matrix4f projection) {
		OpenGL.programLoadUniformMatrix(location_projectionMatrix, projection);
	}
	public void loadViewMatrix(Camera camera) {
		OpenGL.programLoadUniformMatrix(location_viewMatrix, Maths.createViewMatrix(camera));
	}
}
