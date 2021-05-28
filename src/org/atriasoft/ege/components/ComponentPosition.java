package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Signal;
import org.atriasoft.ege.components.part.PositionningInterface;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.math.Transform3D;

public class ComponentPosition extends Component implements PositionningInterface {
	public final Signal<Transform3D> signalPosition = new Signal<Transform3D>();
	protected Transform3D transform;
	
	/**
	 * Create a basic position component (no orientation and position (0,0,0))
	 */
	public ComponentPosition() {
		this.transform = Transform3D.IDENTITY;
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
		if (component.getType().contains("physics")) {
			Log.critical("Can not add a 'physic' component and a 'position' component ... ==> incompatible");
		}
	}
	
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
