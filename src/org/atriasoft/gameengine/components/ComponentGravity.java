package org.atriasoft.gameengine.components;

import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.Light;

public abstract class ComponentGravity extends Component {
	public ComponentGravity() {
		super();
	}
	@Override
	public String getType() {
		return "gravity";
	}
	public abstract Vector3f getGravityAtPosition(Vector3f position);
}
