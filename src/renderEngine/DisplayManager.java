package renderEngine;

import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.gale.Fps;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;


import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * This class contains all the methods needed to set-up, maintain, and close a LWJGL display.
 * 
 * @author Karl
 *
 */
public class DisplayManager {

	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;
	private static final int FPS_CAP = 60;
	private static final String TITLE = "Open GL tutorial";
	
	private static long lastFrameTime;
	private static float delta;
	
	private Fps fps = new Fps("Main Loop", true);
	
	private DisplayManagerDraw drawer = null;
	private static double whellOffsetY;
	private static double whellOffsetX;
	private static boolean rightButtonStateDown = false;
	private static boolean leftButtonStateDown = false;
	private static double lastMousePositionX = 0;
	private static double lastMousePositionY = 0;
	private static double currentMousePositionX = 0;
	private static double currentMousePositionY = 0;
	
	public void setDrawer(DisplayManagerDraw drawer) {
		this.drawer = drawer;
	}
	// The window handle
	private static long window;

	public static Vector2f getSize() {
		return new Vector2f(WIDTH, HEIGHT);
	}

	
	public void init() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		initWindows();
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
		// Set the clear color
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
		glViewport(0, 0, WIDTH, HEIGHT);
	}
	public void unInit() {
	
		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	private static boolean valueS = false;
	private static boolean valueZ = false;
	private static boolean valueQ = false;
	private static boolean valueD = false;
	private static boolean valueA = false;
	private static boolean valueW = false;
	private static boolean valueSPACE = false;
	public static boolean isKeyDown(char value) {
		if (value == 's') {
			return valueS;
		}
		if (value == 'z') {
			return valueZ;
		}
		if (value == 'q') {
			return valueQ;
		}
		if (value == 'd') {
			return valueD;
		}
		if (value == 'w') {
			return valueW;
		}
		if (value == 'a') {
			return valueA;
		}
		if (value == ' ') {
			return valueSPACE;
		}
		return false;
	}

	private void initWindows() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() ) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}
		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		// https://www.glfw.org/docs/latest/input_guide.html
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			}
			if ( key == GLFW_KEY_Z ) {
				if (action == GLFW_PRESS ) {
					System.out.println("Key Z is pressed");
					valueZ = true;
				} else if (action == GLFW_RELEASE ) {
					System.out.println("Key Z is release");
					valueZ = false;
				}
			}
			if ( key == GLFW_KEY_S ) {
				if (action == GLFW_PRESS ) {
					System.out.println("Key S is pressed");
					valueS = true;
				} else if (action == GLFW_RELEASE ) {
					System.out.println("Key S is release");
					valueS = false;
				}
			}
			if ( key == GLFW_KEY_Q ) {
				if (action == GLFW_PRESS ) {
					System.out.println("Key Q is pressed");
					valueQ = true;
				} else if (action == GLFW_RELEASE ) {
					System.out.println("Key Q is release");
					valueQ = false;
				}
			}
			if ( key == GLFW_KEY_D ) {
				if (action == GLFW_PRESS ) {
					System.out.println("Key D is pressed");
					valueD = true;
				} else if (action == GLFW_RELEASE ) {
					System.out.println("Key D is release");
					valueD = false;
				}
			}
			if ( key == GLFW_KEY_A ) {
				if (action == GLFW_PRESS ) {
					System.out.println("Key A is pressed");
					valueA = true;
				} else if (action == GLFW_RELEASE ) {
					System.out.println("Key A is release");
					valueA = false;
				}
			}
			if ( key == GLFW_KEY_W ) {
				if (action == GLFW_PRESS ) {
					System.out.println("Key W is pressed");
					valueW = true;
				} else if (action == GLFW_RELEASE ) {
					System.out.println("Key W is release");
					valueW = false;
				}
			}
			if ( key == GLFW_KEY_SPACE ) {
				if (action == GLFW_PRESS ) {
					System.out.println("Key SPACE is pressed");
					valueSPACE = true;
				} else if (action == GLFW_RELEASE ) {
					System.out.println("Key SPACE is release");
					valueSPACE = false;
				}
			}
		});
		
		glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
			currentMousePositionX = xpos;
			currentMousePositionY = ypos;
			
		});
		
		glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
			if (button == GLFW_MOUSE_BUTTON_RIGHT) {
		    	if (action == GLFW_PRESS) {
		    		rightButtonStateDown = true;
		    	} else if (action == GLFW_RELEASE) {
		    		rightButtonStateDown = false;
		    	}
		    } else if (button == GLFW_MOUSE_BUTTON_LEFT) {
		    	if (action == GLFW_PRESS) {
		    		leftButtonStateDown = true;
		    	} else if (action == GLFW_RELEASE) {
		    		leftButtonStateDown = false;
		    	}
		    }
		});
		
		glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
			whellOffsetY += yoffset;
			whellOffsetX += xoffset;
		});
		
		
		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
		
		lastFrameTime = getCurrentTime();
		
	}

	public void loop() {

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			fps.tic();
			long currentFrameTime = getCurrentTime();
			delta = (currentFrameTime-lastFrameTime)/1000f;
			lastFrameTime = currentFrameTime;
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
			if (this.drawer != null) {
				fps.incrementCounter();
				this.drawer.draw();
			}
			lastMousePositionX = currentMousePositionX;
			lastMousePositionY = currentMousePositionY;
			whellOffsetY = 0;
			whellOffsetY = 0;
			glfwSwapBuffers(window); // swap the color buffers
			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
			fps.toc();
			fps.draw();
		}
	}
	
	public static float getFrameTimeSecconds() {
		return delta;
	}
	
	private static long getCurrentTime() {
		return System.currentTimeMillis();
	}


	public static boolean isButtonRightDown() {
		return rightButtonStateDown;
	}
	public static boolean isButtonLeftDown() {
		return leftButtonStateDown;
	}


	public static float getDX() {
		return (float) (currentMousePositionX-lastMousePositionX);
	}
	public static float getDY() {
		return (float) (currentMousePositionY-lastMousePositionY);
	}


	public static float getDWheel() {
		// TODO Auto-generated method stub
		return (float) whellOffsetY;
	}
	

}
