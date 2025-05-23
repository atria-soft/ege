package org.atriasoft.ege.engines;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;

public class EngineParticle extends Engine {
	public static final String ENGINE_NAME = "particle";

	public EngineParticle(Environement env) {
		super(env);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void componentRemove(Component ref) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentAdd(Component ref) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(long deltaMili) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(long deltaMili, Camera camera) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renderDebug(long deltaMili, Camera camera) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return ENGINE_NAME;
	}


}
