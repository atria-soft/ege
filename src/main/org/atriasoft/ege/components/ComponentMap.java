package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.engines.EngineMap;

public class ComponentMap extends Component  {

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return EngineMap.ENGINE_NAME;
	}
	
	public void update(float timeStep) {};

}
