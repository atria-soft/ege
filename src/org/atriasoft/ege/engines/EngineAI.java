
package org.atriasoft.ege.engines;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentAI;

public class EngineAI extends Engine {
	public static final String ENGINE_NAME = "ia";
	private static final float TIME_STEP = 5.0f;
	private float accumulator = 0;
	private final List<ComponentAI> components = new ArrayList<>();
	
	public EngineAI(Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void componentAdd(Component ref) {
		if (!(ref instanceof ComponentAI)) {
			return;
		}
		this.components.add((ComponentAI) ref);
	}
	
	@Override
	public void componentRemove(Component ref) {
		this.components.remove(ref);
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return ENGINE_NAME;
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
	public void update(long deltaMili) {
		// Add the time difference in the accumulator
		this.accumulator += deltaMili * 0.0001f;
		// While there is enough accumulated time to take one or several physics steps
		while (this.accumulator >= TIME_STEP) {
			//LOGGER.warn("AI: Generate for {} / {}  for: {}", accumulator, TIME_STEP, components.size());
			// call every object to update their constant forces applied
			for (ComponentAI it : this.components) {
				it.update(TIME_STEP);
			}
			// Decrease the accumulated time
			this.accumulator -= TIME_STEP;
		}
		
	}
	
}
