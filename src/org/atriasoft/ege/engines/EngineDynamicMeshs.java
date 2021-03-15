package org.atriasoft.ege.engines;

import java.util.Vector;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentDynamicMeshs;

public class EngineDynamicMeshs extends Engine {
	public static final String ENGINE_NAME = "dynamic-meshs";
	private static float TIME_STEP = 5.0f;
	private float accumulator = TIME_STEP;
	private Vector<ComponentDynamicMeshs> components = new Vector<ComponentDynamicMeshs>();
	public EngineDynamicMeshs(Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void componentRemove(Component ref) {
		components.remove(ref);
	}

	@Override
	public void componentAdd(Component ref) {
		if (ref instanceof ComponentDynamicMeshs == true) {
			components.add((ComponentDynamicMeshs)ref);
			return;
		}
	}

	@Override
	public void update(long deltaMili) {

		//Log.warning("engine update : " + deltaMili + "   " + accumulator + " >= " + TIME_STEP);
		// Add the time difference in the accumulator
		accumulator += (float)deltaMili*0.0001f;
		// While there is enough accumulated time to take one or several physics steps
		while (accumulator >= TIME_STEP) {
			//Log.warning("Generate for " + accumulator + " / " + TIME_STEP + "  for:" + components.size());
			// call every object to usdate their constant forces applyed
			for (ComponentDynamicMeshs it: components) {
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
