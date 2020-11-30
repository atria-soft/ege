package org.atriasoft.gameengine.engines;

import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.Engine;
import org.atriasoft.gameengine.Environement;
import org.atriasoft.gameengine.camera.Camera;

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
