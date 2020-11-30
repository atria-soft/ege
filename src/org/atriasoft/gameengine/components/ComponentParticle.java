package org.atriasoft.gameengine.components;

import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.engines.EngineParticle;

public class ComponentParticle extends Component {

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return EngineParticle.ENGINE_NAME;
	}
}
