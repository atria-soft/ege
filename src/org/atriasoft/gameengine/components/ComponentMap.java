package org.atriasoft.gameengine.components;

import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.engines.EngineMap;

public class ComponentMap extends Component  {

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return EngineMap.ENGINE_NAME;
	}
	
	public void update(float timeStep) {};

}
