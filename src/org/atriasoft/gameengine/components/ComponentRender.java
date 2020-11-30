package org.atriasoft.gameengine.components;

import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.engines.EngineRender;

public abstract class ComponentRender extends Component {
	private boolean propertyDebugNormal = false;

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return EngineRender.ENGINE_NAME;
	}
	public void setPropertyDebugNormal(boolean value) {
		this.propertyDebugNormal = value;
	}
	public boolean getPropertyDebugNormal() {
		return this.propertyDebugNormal;
	}
	public abstract void render();
	public void update(float timeStep) {};
	
}
