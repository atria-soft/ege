package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Light;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.math.Vector3f;

public class ComponentLight extends Component {
	// the material is not a resource, it can change in time... with AI or selection...
	private final Light light;
	private PositionningInterface position = null;
	
	public ComponentLight() {
		this.light = new Light();
	}
	
	public ComponentLight(final Light light) {
		this.light = light;
	}
	
	@Override
	public void addFriendComponent(final Component component) {
		if (component.getType().contentEquals("position") || component.getType().contentEquals("physics")) {
			if (component instanceof PositionningInterface tmp) {
				this.position = tmp;
			} else {
				Log.error("component: {} is not an instance of {}", component.getClass().getCanonicalName(), PositionningInterface.class.getCanonicalName());
			}
		}
	}
	
	public Light getLight() {
		return this.light;
	}
	
	public Vector3f getPosition() {
		if (this.position != null) {
			return this.position.getTransform().getPosition().add(this.light.getPositionDelta());
		}
		return null;
	}
	
	@Override
	public String getType() {
		return "light";
	}
	
}
