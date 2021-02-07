package org.atriasoft.gameengine.components;

import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.Light;

public class ComponentLight extends Component {
	// the material is not a resource, it can change in time... with AI or selection...
	private final Light light;
	private ComponentPosition position;
	private ComponentPhysics playerPhysics = null;
	
	public ComponentLight() {
		super();
		this.light = new Light();
	}
	
	public ComponentLight(final Light light) {
		super();
		this.light = light;
	}
	
	@Override
	public void addFriendComponent(final Component component) {
		if (component.getType().contentEquals("position")) {
			this.position = (ComponentPosition) component;
		}
		if (component.getType().contentEquals("physics")) {
			this.playerPhysics = (ComponentPhysics) component;
		}
	}
	
	public Light getLight() {
		return this.light;
	}
	
	public Vector3f getPosition() {
		if (this.position != null) {
			return this.position.getTransform().getPosition().clone().add(this.light.getPositionDelta());
		} else if (this.playerPhysics != null) {
			return this.playerPhysics.getTransform().getPosition().clone().add(this.light.getPositionDelta());
		}
		return null;
	}
	
	@Override
	public String getType() {
		return "light";
	}
	
}
