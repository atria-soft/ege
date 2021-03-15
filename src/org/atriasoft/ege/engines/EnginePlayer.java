package org.atriasoft.ege.engines;

import java.util.Vector;

import org.atriasoft.ege.internal.Log;
import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentPlayer;

public class EnginePlayer extends Engine {
	public static final String ENGINE_NAME = "player";
	private Vector<ComponentPlayer> components = new Vector<ComponentPlayer>();
	public EnginePlayer(Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void componentRemove(Component ref) {
		components.remove(ref);
	}

	@Override
	public void componentAdd(Component ref) {
		if (ref instanceof ComponentPlayer == false) {
			return;
		}
		components.add((ComponentPlayer)ref);
	}

	@Override
	public void update(long deltaMili) {
		for (ComponentPlayer it: components) {
			it.update(deltaMili);
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
