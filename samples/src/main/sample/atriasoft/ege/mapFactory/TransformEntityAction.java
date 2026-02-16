package sample.atriasoft.ege.mapFactory;

import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.etk.math.Transform3D;

public class TransformEntityAction implements MapAction {
	private final ComponentPosition position;
	private final Transform3D before;
	private final Transform3D after;

	public TransformEntityAction(final ComponentPosition position, final Transform3D before, final Transform3D after) {
		this.position = position;
		this.before = before;
		this.after = after;
	}

	@Override
	public void undo() {
		this.position.setTransform(this.before);
	}

	@Override
	public void redo() {
		this.position.setTransform(this.after);
	}
}
