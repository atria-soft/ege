package org.atriasoft.gameengine.engines;

import java.util.Vector;

import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.Engine;
import org.atriasoft.gameengine.Environement;
import org.atriasoft.gameengine.camera.Camera;
import org.atriasoft.gameengine.components.ComponentMap;

public class EngineMap extends Engine {
	public static final String ENGINE_NAME = "map";
	private float accumulator = 0;
	private static final float TIME_STEP = 5.0f;
	private Vector<ComponentMap> components = new Vector<ComponentMap>();
	public EngineMap(Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void componentRemove(Component ref) {
		components.remove(ref);
	}

	@Override
	public void componentAdd(Component ref) {
		if (ref instanceof ComponentMap == true) {
			components.add((ComponentMap)ref);
			return;
		}
	}

	@Override
	public void update(long deltaMili) {
		// Add the time difference in the accumulator
		accumulator += (float)deltaMili*0.0001f;
		// While there is enough accumulated time to take one or several physics steps
		while (accumulator >= TIME_STEP) {
			// Log.warning("MAP: Generate for " + accumulator + " / " + TIME_STEP + "  for:" + components.size());
			// call every object to update their constant forces applied
			for (ComponentMap it: components) {
				it.update(TIME_STEP);
			}
			// Decrease the accumulated time
			accumulator -= TIME_STEP;
		}
		
	}

	@Override
	public void render(long deltaMili, Camera camera) {
		// nothing to do ...
	}

	@Override
	public void renderDebug(long deltaMili, Camera camera) {
		// nothing to do ...
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return ENGINE_NAME;
	}

}
