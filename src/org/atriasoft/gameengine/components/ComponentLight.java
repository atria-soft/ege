package org.atriasoft.gameengine.components;

import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.Light;

public class ComponentLight extends Component {
	// the material is not a resource, it can change in time... with AI or selection...
	private Light light;
	private ComponentPosition position;
	
	public ComponentLight(Light light) {
		super();
		this.light = light;
	}
	public void addFriendComponent(Component component) {
		if (component.getType().contentEquals("position")) {
			this.position = (ComponentPosition)component;
		}
	}
	
	public ComponentLight() {
		super();
		this.light = new Light();
	}
	@Override
	public String getType() {
		return "light";
	}
	public Light getLight() {
		return light;
	}
	public void setLight(Light light) {
		this.light = light;
	}
	
	public Vector3f getPosition() {
		return position.getTransform().getPosition().clone().add(light.getPositionDelta());
	}
	
}
