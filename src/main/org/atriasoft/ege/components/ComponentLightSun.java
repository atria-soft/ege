package org.atriasoft.ege.components;

import org.atriasoft.ege.Light;
import org.atriasoft.ege.engines.EngineLight;

public class ComponentLightSun extends ComponentLight {

	public ComponentLightSun() {
	}

	public ComponentLightSun(final Light light) {
		super(light);
	}
	
	@Override
	public String getType() {
		return EngineLight.ENGINE_NAME;
	}
	
}
