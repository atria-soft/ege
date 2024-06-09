package org.atriasoft.ege.engines;

import java.util.Vector;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentPlayer;

public class EnginePlayer extends Engine {
	public static final String ENGINE_NAME = "player";
	private final Vector<ComponentPlayer> components = new Vector<>();
	
	public EnginePlayer(final Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void componentRemove(final Component ref) {
		this.components.remove(ref);
	}
	
	@Override
	public void componentAdd(final Component ref) {
		if (!(ref instanceof ComponentPlayer)) {
			return;
		}
		this.components.add((ComponentPlayer) ref);
	}
	
	@Override
	public void update(final long deltaMili) {
		for (final ComponentPlayer it : this.components) {
			it.update(deltaMili);
		}
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
		return ENGINE_NAME;
	}
	
}
