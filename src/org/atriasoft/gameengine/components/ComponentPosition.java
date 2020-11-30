package org.atriasoft.gameengine.components;

import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.internal.Log;
import org.atriasoft.gameengine.Signal;

public class ComponentPosition extends Component {
	public Signal<Transform3D> signalPosition;
	protected Transform3D transform;
	/**
	 * @brief Create a basic position component (no orientation and position (0,0,0))
	 */
	public ComponentPosition() {
		transform = Transform3D.identity();
	}
	/**
	 * @brief Create a basic position component
	 * @param transform transformation of the position
	 */
	public ComponentPosition(Transform3D transform) {
		this.transform = transform;
	}
	/**
	 * @brief set a new transformation
	 * @param transform transformation of the position
	 */
	void setTransform(Transform3D transform) {
		if (this.transform.isEqual(transform)) {
			return;
		}
		this.transform = transform;
		signalPosition.emit(this.transform);
	}
	/**
	 * @brief set a new transformation
	 * @return Transformation of the position
	 */
	public Transform3D getTransform() {
		return transform;
	}
	public String getType() {
		return "position";
	}
	public void addFriendComponent(Component component) {
		if (component.getType().contains("physics")) {
			Log.error("Can not add a 'physic' component and a 'position' component ... ==> incompatible");
		}
	}
}
