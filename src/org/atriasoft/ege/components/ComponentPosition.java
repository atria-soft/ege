package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Signal;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;

public class ComponentPosition extends Component implements PositionningInterface {
	public final Signal<Transform3D> signalPosition = new Signal<>();
	protected Transform3D transform;
	//protected Vector3f speed;
	
	/**
	 * Create a basic position component (no orientation and position (0,0,0))
	 */
	public ComponentPosition() {
		this.transform = Transform3D.IDENTITY;
		//this.speed = Vector3f.ZERO;
	}
	
	/**
	 * Create a basic position component
	 * @param transform transformation of the position
	 */
	public ComponentPosition(final Transform3D transform) {
		this.transform = transform;
	}
	
	@Override
	public void addFriendComponent(final Component component) {
		if (component.getType().equals("physics")) {
			Log.critical("Can not add a 'physics' component and a 'position' component ... ==> incompatible");
		}
	}
	
	public void applyForce(Vector3f force) {
		this.transform = this.transform.withPosition(this.transform.getPosition().add(force));
	}
	
	//public Vector3f getSpeed() {
	//	return this.speed;
	//}
	
	/**
	 * set a new transformation
	 * @return Transformation of the position
	 */
	@Override
	public Transform3D getTransform() {
		return this.transform;
	}
	
	@Override
	public String getType() {
		return "position";
	}
	
	//public void setSpeed(Vector3f speed) {
	//	this.speed = speed;
	//}
	
	/**
	 * set a new transformation
	 * @param transform transformation of the position
	 */
	public void setTransform(final Transform3D transform) {
		if (this.transform.isEqual(transform)) {
			return;
		}
		this.transform = transform;
		this.signalPosition.emit(this.transform);
	}
}
