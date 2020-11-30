package textures;

public class ModelTexture {

	private int textureID;
	
	// Reflectivity of the light on the surface of the texture
	private float reflectivity = 0;
	// Distance of witch the camera must to be to receive the the reflection
	private float shineDamper = 1;
	private boolean hasTransparency = false;
	private boolean useFakeLighting = false;
	private int numberOfRows = 1;
	
	public ModelTexture(int id) {
		this.textureID = id;
	}

	public int getTexturedID() {
		return textureID;
	}

	public float getReflectivity() {
		return reflectivity;
	}

	public void setReflectivity(float reflectivity) {
		this.reflectivity = reflectivity;
	}

	public float getShineDamper() {
		return shineDamper;
	}

	public void setShineDamper(float shineDamper) {
		this.shineDamper = shineDamper;
	}
	
	public boolean isHasTransparency() {
		return hasTransparency;
	}

	public void setHasTransparency(boolean hasTransparency) {
		this.hasTransparency = hasTransparency;
	}

	public boolean isUseFakeLighting() {
		return useFakeLighting;
	}

	public void setUseFakeLighting(boolean useFakeLighting) {
		this.useFakeLighting = useFakeLighting;
	}

	public int getNumberOfRows() {
		return numberOfRows;
	}

	public void setNumberOfRows(int numberOfRows) {
		this.numberOfRows = numberOfRows;
	}

	
}
