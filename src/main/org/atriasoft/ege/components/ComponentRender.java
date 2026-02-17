package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.components.part.RenderContext;
import org.atriasoft.ege.engines.EngineRender;

public abstract class ComponentRender extends Component {
	private boolean propertyDebugNormal = false;

	@Override
	public String getType() {
		return EngineRender.ENGINE_NAME;
	}
	public void setPropertyDebugNormal(final boolean value) {
		this.propertyDebugNormal = value;
	}
	public boolean getPropertyDebugNormal() {
		return this.propertyDebugNormal;
	}
	public abstract void render(RenderContext context);
	public void update(final float timeStep) {}
}
