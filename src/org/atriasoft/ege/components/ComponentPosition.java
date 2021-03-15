package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Signal;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.ege.internal.Log;

public class ComponentPosition extends Component {
	public Signal<Transform3D> signalPosition;
	protected Transform3D transform;
	
	/**
	 * @brief Create a basic position component (no orientation and position (0,0,0))
	 */
	public ComponentPosition() {
		this.transform = Transform3D.identity();
	}
	
	/**
	 * @brief Create a basic position component
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
	 * @brief set a new transformation
	 * @return Transformation of the position
	 */
	public Transform3D getTransform() {
		return this.transform;
	}
	
	@Override
	public String getType() {
		return "position";
	}
	
	/**
	 * @brief set a new transformation
	 * @param transform transformation of the position
	 */
	void setTransform(final Transform3D transform) {
		if (this.transform.isEqual(transform)) {
			return;
		}
		this.transform = transform;
		this.signalPosition.emit(this.transform);
	}
}
