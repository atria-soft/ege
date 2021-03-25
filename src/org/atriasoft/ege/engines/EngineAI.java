package org.atriasoft.ege.engines;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.ege.internal.Log;
import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentAI;

public class EngineAI extends Engine {
	public static final String ENGINE_NAME = "ia";
	private float accumulator = 0;
	private static final float TIME_STEP = 5.0f;
	private final List<ComponentAI> components = new ArrayList<ComponentAI>();
	public EngineAI(Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void componentRemove(Component ref) {
		components.remove(ref);
	}

	@Override
	public void componentAdd(Component ref) {
		if (!(ref instanceof ComponentAI)) {
			return;
		}
		components.add((ComponentAI)ref);
	}

	@Override
	public void update(long deltaMili) {
		// Add the time difference in the accumulator
		accumulator += (float)deltaMili*0.0001f;
		// While there is enough accumulated time to take one or several physics steps
		while (accumulator >= TIME_STEP) {
			//Log.warning("AI: Generate for " + accumulator + " / " + TIME_STEP + "  for:" + components.size());
			// call every object to usdate their constant forces applyed
			for (ComponentAI it: components) {
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
