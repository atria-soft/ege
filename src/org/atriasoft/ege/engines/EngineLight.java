package org.atriasoft.ege.engines;

import java.util.Vector;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.Light;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentLight;
import org.atriasoft.ege.components.ComponentLightSun;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.math.Vector3f;

public class EngineLight extends Engine {
	public static final String ENGINE_NAME = "light";
	private final Vector<ComponentLight> componentLights = new Vector<ComponentLight>();
	private final Vector<ComponentLightSun> componentSuns = new Vector<ComponentLightSun>();
	public EngineLight(final Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void componentRemove(final Component ref) {
		this.componentLights.remove(ref);
		this.componentSuns.remove(ref);
	}

	@Override
	public void componentAdd(final Component ref) {
		if (ref instanceof ComponentLightSun refTyped) {
			this.componentSuns.add(refTyped);
			return;
		}
		if (ref instanceof ComponentLight refTyped) {
			this.componentLights.add(refTyped);
		}
	}

	@Override
	public void update(final long deltaMili) {
		// nothing to do ...
	}

	@Override
	public void render(final long deltaMili, final Camera camera) {
		// nothing to do ...
	}

	@Override
	public void renderDebug(final long deltaMili, final Camera camera) {
		// nothing to do ...
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return EngineLight.ENGINE_NAME;
	}

	public Light[] getNearest(final Vector3f position) {
		Light[] out = new Light[8];
		int count = 0;
		for (ComponentLightSun elem: this.componentSuns) {
			out[count] = new Light(elem.getLight().getColor(), elem.getPosition(), elem.getLight().getAttenuation());
			if (count>=8) {
				LOGGER.error("need to update ligth count");
				return out;
			}
			count++;
		}
		//LOGGER.warn("Get {}/{} lights (SUN) ...", count, out.length);
		float maxDistance = 50*50;
		for (ComponentLight elem: this.componentLights) {
			Vector3f pos = elem.getPosition();
			if (count>=8) {
				LOGGER.error("need to update ligth count");
				return out;
			}
			if (pos.distance2(position) < maxDistance) {
				out[count] = new Light(elem.getLight().getColor(), pos, elem.getLight().getAttenuation());
				count++;
			}
		}
		//LOGGER.warn("Get {} / {} lights...", count, out.length,);
		return out;
	}

}
