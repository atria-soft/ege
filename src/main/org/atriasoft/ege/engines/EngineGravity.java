package org.atriasoft.ege.engines;

import java.util.Vector;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentGravity;
import org.atriasoft.etk.math.Vector3f;

public class EngineGravity extends Engine {
	public static final String ENGINE_NAME = "gravity";
	private Vector<ComponentGravity> components = new Vector<>();
	
	public EngineGravity(final Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void componentAdd(final Component ref) {
		if (ref instanceof ComponentGravity == true) {
			this.components.add((ComponentGravity) ref);
			return;
		}
	}
	
	@Override
	public void componentRemove(final Component ref) {
		this.components.remove(ref);
	}
	
	public Vector3f getGravityAtPosition(final Vector3f position) {
		Vector3f out = Vector3f.ZERO;
		for (ComponentGravity elem : this.components) {
			out = out.add(elem.getGravityAtPosition(position));
		}
		return out;
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return ENGINE_NAME;
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
	public void update(final long deltaMili) {
		// nothing to do ...
	}
	
}
