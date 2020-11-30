package renderEngine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import models.RawModel;
import textures.TextureData;


public class Loader {

	private List<Integer> vaos = new ArrayList<Integer>();
	private List<Integer> vbos = new ArrayList<Integer>();
	private List<Integer> textures = new ArrayList<Integer>();
    
	private void exitOnGLError(String errorMessage) {
        int errorValue = GL11.glGetError();
         
        if (errorValue != GL11.GL_NO_ERROR) {
        	System.err.println("ERROR - " + errorMessage + ": ???" );
//            String errorString = GLU.gluErrorString(errorValue);
//            System.err.println("ERROR - " + errorMessage + ": " + errorString);
//             
//            if (Display.isCreated()) Display.destroy();
            System.exit(-1);
        }
    }
	public RawModel loadToVAO(float[] positions, float[] textureCoordinates, float[] normals, int[] indices) {
		int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0, 3, positions);
		storeDataInAttributeList(1, 2, textureCoordinates);
		storeDataInAttributeList(2, 3, normals);
		unbindVAO();
		return new RawModel(vaoID, indices.length);
	}

	public RawModel loadToVAO(float[] positions, int dimentions) {
		int vaoID = createVAO();
		this.storeDataInAttributeList(0, dimentions, positions);
		unbindVAO();
		return new RawModel(vaoID, positions.length/dimentions);
	}
	
	public int loadTexture(String filename) {
		String fullname = "res/" + filename + ".png";
		System.out.println("Load texture : " + fullname);
		int textureId = loadPNGTexture(fullname, GL13.GL_TEXTURE0);
		this.textures.add(textureId);
		return textureId;
	}
	
	public int loadCubeMap(String[] textureFiles) {
		int texId = GL11.glGenTextures();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texId);
        for (int i=0; i<textureFiles.length; i++) {
        	TextureData data = decodeTextureFile("res/" + textureFiles[i] + ".png");
        	GL13.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 0,
        			GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0,
        			GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
        			data.getBuffer());
        }
        // Setup what to do when the texture has to be scaled
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

        //this.exitOnGLError("loadCubeMap");
        textures.add(texId);
        return texId;
        
	}
	

    private TextureData decodeTextureFile(String filename) {
    	ByteBuffer buf = null;
        int tWidth = 0;
        int tHeight = 0;
        try {
            // Open the PNG file as an InputStream
            InputStream in = new FileInputStream(filename);
            // Link the PNG decoder to this stream
            PNGDecoder decoder = new PNGDecoder(in);
            // Get the width and height of the texture
            tWidth = decoder.getWidth();
            tHeight = decoder.getHeight();
            // Decode the PNG file in a ByteBuffer
            buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
            //decoder.decodeFlipped(buf, decoder.getWidth() * 4, Format.RGBA);
            decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
            buf.flip();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("try to load texture " + filename + ", didn't work");
            System.exit(-1);
        }
        return new TextureData(buf, tWidth, tHeight);
    }
    
    private int loadPNGTexture(String filename, int textureUnit) {
        TextureData data = decodeTextureFile(filename);
        // Create a new texture object in memory and bind it
        int texId = GL11.glGenTextures();
        GL13.glActiveTexture(textureUnit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
         
        // All RGB bytes are aligned to each other and each component is 1 byte
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
         
        // Upload the texture data and generate mip maps (for scaling)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
         
        // Setup the ST coordinate system
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
         
        // Setup what to do when the texture has to be scaled
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
         
        this.exitOnGLError("loadPNGTexture");
        textures.add(texId);
        return texId;
    }
	/**
	 * Deletes all the VAOs and VBOs when the game is closed. VAOs and VBOs are
	 * located in video memory.
	 */
	public void cleanUp() {
		for (int vao : vaos) {
			GL30.glDeleteVertexArrays(vao);
		}
		for (int vbo : vbos) {
			GL15.glDeleteBuffers(vbo);
		}
		for (int texture : textures) {
			GL11.glDeleteTextures(texture);
		}
	}

	/**
	 * Creates a new VAO and returns its ID. A VAO holds geometry data that we
	 * can render and is physically stored in memory on the GPU, so that it can
	 * be accessed very quickly during rendering.
	 * 
	 * Like most objects in OpenGL, the new VAO is created using a "gen" method
	 * which returns the ID of the new VAO. In order to use the VAO it needs to
	 * be made the active VAO. Only one VAO can be active at a time. To make
	 * this VAO the active VAO (so that we can store stuff in it) we have to
	 * bind it.
	 * 
	 * @return The ID of the newly created VAO.
	 */
	private int createVAO() {
		int vaoID = GL30.glGenVertexArrays();
		vaos.add(vaoID);
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	}

	private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	/**
	 * Unbinds the VAO after we're finished using it. If we want to edit or use
	 * the VAO we would have to bind it again first.
	 */
	private void unbindVAO() {
		GL30.glBindVertexArray(0);
	}

	/**
	 * Creates an index buffer, binds the index buffer to the currently active
	 * VAO, and then fills it with our indices.
	 * 
	 * The index buffer is different from other data that we might store in the
	 * attributes of the VAO. When we stored the positions we were storing data
	 * about each vertex. The positions were "attributes" of each vertex. Data
	 * like that is stored in an attribute list of the VAO.
	 * 
	 * The index buffer however does not contain data about each vertex. Instead
	 * it tells OpenGL how the vertices should be connected. Each VAO can only
	 * have one index buffer associated with it. This is why we don't store the
	 * index buffer in a certain attribute of the VAO; each VAO has one special
	 * "slot" for an index buffer and simply binding the index buffer binds it
	 * to the currently active VAO. When the VAO is rendered it will use the
	 * index buffer that is bound to it.
	 * 
	 * This is also why we don't unbind the index buffer, as that would unbind
	 * it from the VAO.
	 * 
	 * Note that we tell OpenGL that this is an index buffer by using
	 * "GL_ELEMENT_ARRAY_BUFFER" instead of "GL_ARRAY_BUFFER". This is how
	 * OpenGL knows to bind it as the index buffer for the current VAO.
	 * 
	 * @param indices
	 */
	private void bindIndicesBuffer(int[] indices) {
		int vboId = GL15.glGenBuffers();
		vbos.add(vboId);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId);
		IntBuffer buffer = storeDataInIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}

	/**
	 * Converts the indices from an int array to an IntBuffer so that they can
	 * be stored in a VBO. Very similar to the storeDataInFloatBuffer() method
	 * below.
	 * 
	 * @param data
	 *            - The indices in an int[].
	 * @return The indices in a buffer.
	 */
	private IntBuffer storeDataInIntBuffer(int[] data) {
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

	/**
	 * Before we can store data in a VBO it needs to be in a certain format: in
	 * a buffer. In this case we will use a float buffer because the data we
	 * want to store is float data. If we were storing int data we would use an
	 * IntBuffer.
	 * 
	 * First and empty buffer of the correct size is created. You can think of a
	 * buffer as basically an array with a pointer. After putting the necessary
	 * data into the buffer the pointer will have increased so that it points at
	 * the first empty element of the array. This is so that we could add more
	 * data to the buffer if we wanted and it wouldn't overwrite the data we've
	 * already put in. However, we're done with storing data and we want to make
	 * the buffer ready for reading. To do this we need to make the pointer
	 * point to the start of the data, so that OpenGL knows where in the buffer
	 * to start reading. The "flip()" method does just that, putting the pointer
	 * back to the start of the buffer.
	 * 
	 * @param data
	 *            - The float data that is going to be stored in the buffer.
	 * @return The FloatBuffer containing the data. This float buffer is ready
	 *         to be loaded into a VBO.
	 */
	private FloatBuffer storeDataInFloatBuffer(float[] data) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

}
