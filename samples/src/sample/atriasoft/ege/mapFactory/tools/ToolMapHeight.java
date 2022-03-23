package sample.atriasoft.ege.mapFactory.tools;

import org.atriasoft.ege.geometry.Ray;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.gale.resource.ResourceColored3DObject;

import sample.atriasoft.ege.mapFactory.EgeScene;
import sample.atriasoft.ege.mapFactory.Log;
import sample.atriasoft.ege.mapFactory.model.Map;
import toolbox.Maths;

public class ToolMapHeight implements MapToolInterface {
	Vector3f positionRay = null;
	float widthBrush = 3.0f;
	float maxBrush = 10.0f;
	float minBrush = -10.0f;
	ResourceColored3DObject dynamicElement;
	
	public ToolMapHeight() {
		this.dynamicElement = ResourceColored3DObject.create();
	}
	
	@Override
	public void onDraw(Map map) {
		// TODO Auto-generated method stub
		if (this.positionRay != null) {
			map.ground.drawDynamicElement(this.dynamicElement, this.positionRay, this.widthBrush);
			float size = this.maxBrush - this.minBrush;
			Transform3D tmpTransform = new Transform3D(this.positionRay.add(new Vector3f(0.0f, 0.0f, this.minBrush + size * 0.5f)));
			this.dynamicElement.drawCylinder(this.widthBrush, size, 10, 22, tmpTransform.getOpenGLMatrix(), Color.AZURE.withA(0.5f), false, true);
		}
		
	}
	
	@Override
	public boolean onEventEntry(EventEntry event, Map map, EgeScene widget) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean onEventInput(EventInput event, Map map, EgeScene widget) {
		Vector3f globalPos = new Vector3f(event.pos().x(), event.pos().y(), 0);
		Vector3f relPos = widget.relativePosition(globalPos);
		// simple ray-cast on the ground
		Ray mouseRay = widget.mainView.getRayFromScreen(widget.projection, widget.getSize(), relPos);
		this.positionRay = mouseRay.intersectPlane(new Vector3f(0.0f, 0.0f, 1.0f), 0.0f);
		/*
		if (this.positionRay != null) {
			this.posRay.setTransform(this.posRay.getTransform().withPosition(this.positionRay));
		}
		*/
		if (event.inputId() == 1 && (event.status() == KeyStatus.move || event.status() == KeyStatus.down)) {
			if (this.positionRay != null) {
				map.ground.changeHeightOfElement(this.positionRay, this.widthBrush, (value, distance) -> {
					if (value > this.maxBrush) {
						return value;
					}
					if (value < this.minBrush) {
						return Maths.avg(-128.0f, value + 0.1f, this.maxBrush);
					}
					return Maths.avg(this.minBrush, value + 0.1f, this.maxBrush);
				});
			}
			return true;
		}
		if (event.inputId() == 3 && (event.status() == KeyStatus.move || event.status() == KeyStatus.down)) {
			if (this.positionRay != null) {
				map.ground.changeHeightOfElement(this.positionRay, this.widthBrush, (value, distance) -> {
					if (value < this.minBrush) {
						return value;
					}
					if (value > this.maxBrush) {
						return Maths.avg(this.minBrush, value - 0.1f, 128.0f);
					}
					return Maths.avg(this.minBrush, value - 0.1f, this.maxBrush);
				});
			}
			return true;
		}
		// max brush
		if (event.inputId() == 4 && event.status() == KeyStatus.down && (event.specialKey() != null && event.specialKey().getAltLeft() && event.specialKey().getCtrlLeft())) {
			this.maxBrush = Maths.avg(this.minBrush + 0.1f, this.maxBrush + 0.1f, 128.0f);
			Log.warning(" values: " + this.minBrush + " / " + this.maxBrush);
			return true;
		}
		if (event.inputId() == 5 && event.status() == KeyStatus.down && (event.specialKey() != null && event.specialKey().getAltLeft() && event.specialKey().getCtrlLeft())) {
			this.maxBrush = Maths.avg(this.minBrush + 0.1f, this.maxBrush - 0.1f, 128.0f);
			Log.warning(" values: " + this.minBrush + " / " + this.maxBrush);
			return true;
		}
		// min brush
		if (event.inputId() == 4 && event.status() == KeyStatus.down && (event.specialKey() != null && event.specialKey().getAltLeft())) {
			this.minBrush = Maths.avg(-128.0f, this.minBrush + 0.1f, this.maxBrush - 0.1f);
			Log.warning(" values: " + this.minBrush + " / " + this.maxBrush);
			return true;
		}
		if (event.inputId() == 5 && event.status() == KeyStatus.down && (event.specialKey() != null && event.specialKey().getAltLeft())) {
			this.minBrush = Maths.avg(-128.0f, this.minBrush - 0.1f, this.maxBrush - 0.1f);
			Log.warning(" values: " + this.minBrush + " / " + this.maxBrush);
			return true;
		}
		// width brush
		if (event.inputId() == 4 && event.status() == KeyStatus.down && (event.specialKey() != null && event.specialKey().getCtrlLeft())) {
			this.widthBrush = Maths.avg(0.1f, this.widthBrush + 0.1f, 30.0f);
			return true;
		}
		if (event.inputId() == 5 && event.status() == KeyStatus.down && (event.specialKey() != null && event.specialKey().getCtrlLeft())) {
			this.widthBrush = Maths.avg(0.1f, this.widthBrush - 0.1f, 30.0f);
			return true;
		}
		return false;
	}
	
}
