package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.engines.EngineAI;

public abstract class ComponentAI extends Component {

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return EngineAI.ENGINE_NAME;
	}

	public abstract void update(float timeStep);
}
