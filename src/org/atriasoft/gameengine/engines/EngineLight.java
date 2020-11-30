package org.atriasoft.gameengine.engines;

import java.util.Vector;

import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.Engine;
import org.atriasoft.gameengine.Environement;
import org.atriasoft.gameengine.Light;
import org.atriasoft.gameengine.internal.Log;
import org.atriasoft.gameengine.camera.Camera;
import org.atriasoft.gameengine.components.ComponentAI;
import org.atriasoft.gameengine.components.ComponentLight;
import org.atriasoft.gameengine.components.ComponentLightSun;

public class EngineLight extends Engine {
	public static final String ENGINE_NAME = "light";
	private Vector<ComponentLight> componentLights = new Vector<ComponentLight>();
	private Vector<ComponentLightSun> componentSuns = new Vector<ComponentLightSun>();
	public EngineLight(Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void componentRemove(Component ref) {
		componentLights.remove(ref);
		componentSuns.remove(ref);
	}

	@Override
	public void componentAdd(Component ref) {
		if (ref instanceof ComponentLightSun == true) {
			componentSuns.add((ComponentLightSun)ref);
			return;
		}
		if (ref instanceof ComponentLight == true) {
			componentLights.add((ComponentLight)ref);
			return;
		}
	}

	@Override
	public void update(long deltaMili) {
		// nothing to do ...
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

	public Light[] getNearest(Vector3f position) {
		Light[] out = new Light[8];
		int count = 0;
		for (ComponentLightSun elem: componentSuns) {
			out[count] = new Light(elem.getLight().getColor(), elem.getPosition(), elem.getLight().getAttenuation());
			count++;
		}
		float maxDistance = 50*50;
		for (ComponentLight elem: componentLights) {
			Vector3f pos = elem.getPosition();
			if (count>=8) {
				Log.error("need to update ligth count");
				return out;
			}
			if (pos.distance2(position) < maxDistance) {
				out[count] = new Light(elem.getLight().getColor(), pos, elem.getLight().getAttenuation());
				count++;
			}
		}
		return out;
	}

}
