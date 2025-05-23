package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Light;
import org.atriasoft.etk.math.Vector3f;

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
