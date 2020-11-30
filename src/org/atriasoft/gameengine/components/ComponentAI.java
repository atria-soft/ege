package org.atriasoft.gameengine.components;

import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.engines.EngineAI;

public abstract class ComponentAI extends Component {

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return EngineAI.ENGINE_NAME;
	}

	public abstract void update(float timeStep);
}
