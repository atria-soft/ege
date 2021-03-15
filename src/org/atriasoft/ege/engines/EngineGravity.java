package org.atriasoft.ege.engines;

import java.util.Vector;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.Light;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentAI;
import org.atriasoft.ege.components.ComponentGravity;
import org.atriasoft.ege.components.ComponentLight;
import org.atriasoft.ege.components.ComponentLightSun;

public class EngineGravity extends Engine {
	public static final String ENGINE_NAME = "gravity";
	private Vector<ComponentGravity> components = new Vector<ComponentGravity>();
	public EngineGravity(Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void componentRemove(Component ref) {
		components.remove(ref);
	}

	@Override
	public void componentAdd(Component ref) {
		if (ref instanceof ComponentGravity == true) {
			components.add((ComponentGravity)ref);
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
	public Vector3f getGravityAtPosition(Vector3f position) {
		Vector3f out = new Vector3f();
		for (ComponentGravity elem: components) {
			out.add(elem.getGravityAtPosition(position));
		}
		return out;
	}

}
