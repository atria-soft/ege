package sample.atriasoft.ege.mapFactory.tools;

import org.atriasoft.ege.geometry.Ray;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.widget.Label;
import org.atriasoft.ewol.widget.Sizer;
import org.atriasoft.ewol.widget.Slider;
import org.atriasoft.ewol.widget.Widget;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sample.atriasoft.ege.mapFactory.EgeScene;
import sample.atriasoft.ege.mapFactory.model.Map;
import toolbox.Maths;

public class ToolMapHeight implements MapToolInterface {
	final static private Logger LOGGER = LoggerFactory.getLogger(ToolMapHeight.class);

	protected static void onCallbackMaxChange(final ToolMapHeight self, final float value) {
		LOGGER.warn("get new value {}", value);
		self.onCallbackMax(value);
	}

	protected static void onCallbackMinChange(final ToolMapHeight self, final float value) {
		LOGGER.warn("get new value {}", value);
		self.onCallbackMin(value);
	}

	protected static void onCallbackWidthChange(final ToolMapHeight self, final float value) {
		LOGGER.warn("get new value {}", value);
		self.onCallbackValue(value);
	}

	Vector3f positionRay = null;

	float widthBrush = 3.0f;
	float maxBrush = 10.0f;
	float minBrush = -10.0f;
	ResourceColored3DObject dynamicElement;

	public ToolMapHeight() {
		this.dynamicElement = ResourceColored3DObject.create();
	}

	@Override
	public Widget getWidget() {
		final Sizer mainSizer = Sizer.vertical().expand(true, true).fill(true, true);

		// Title
		mainSizer.subWidgetAdd(Label.create("Change height map").expand(true, false).fill(true, false));

		// Width slider
		mainSizer.subWidgetAdd(Label.create("Width:").expand(true, false).fill(true, false));
		final Slider widthSlider = Slider.create().min(0.1f).max(40.0f).value(this.widthBrush)
				.expand(true, false).fill(true, false);
		widthSlider.signalValue.connectAuto(this, ToolMapHeight::onCallbackWidthChange);
		mainSizer.subWidgetAdd(widthSlider);

		// Top slider
		mainSizer.subWidgetAdd(Label.create("Top:").expand(true, false).fill(true, false));
		final Slider topSlider = Slider.create().min(-128.0f).max(128.0f).value(this.maxBrush)
				.expand(true, false).fill(true, false);
		topSlider.signalValue.connectAuto(this, ToolMapHeight::onCallbackMaxChange);
		mainSizer.subWidgetAdd(topSlider);

		// Bottom slider
		mainSizer.subWidgetAdd(Label.create("Bottom:").expand(true, false).fill(true, false));
		final Slider bottomSlider = Slider.create().min(-128.0f).max(128.0f).value(this.minBrush)
				.expand(true, false).fill(true, false);
		bottomSlider.signalValue.connectAuto(this, ToolMapHeight::onCallbackMinChange);
		mainSizer.subWidgetAdd(bottomSlider);

		return mainSizer;
	}

	protected void onCallbackMax(final float value) {
		this.maxBrush = value;
	}

	protected void onCallbackMin(final float value) {
		this.minBrush = value;
	}

	protected void onCallbackValue(final float value) {
		this.widthBrush = value;
	}

	@Override
	public void onDraw(final Map map) {
		if (this.positionRay != null) {
			map.ground.drawDynamicElement(this.dynamicElement, this.positionRay, this.widthBrush);
			final float size = this.maxBrush - this.minBrush;
			final Transform3D tmpTransform = new Transform3D(
					this.positionRay.add(new Vector3f(0.0f, 0.0f, this.minBrush + size * 0.5f)));
			this.dynamicElement.drawCylinder(this.widthBrush, size, 10, 22, tmpTransform.getOpenGLMatrix(),
					Color.AZURE.withA(0.5f), false, true);
		}
	}

	@Override
	public boolean onEventEntry(final EventEntry event, final Map map, final EgeScene widget) {
		return false;
	}

	@Override
	public boolean onEventInput(final EventInput event, final Map map, final EgeScene widget) {
		final Vector2f globalPos = event.pos();
		final Vector2f relPos = widget.relativePosition(globalPos);
		// ray-cast on the Z=0 plane (better for height editing)
		final Ray mouseRay = widget.mainView.getRayFromScreen(widget.projection, widget.getSize(), relPos);
		this.positionRay = mouseRay.intersectPlane(new Vector3f(0.0f, 0.0f, 1.0f), 0.0f);
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
				map.updateEntityPositions();
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
				map.updateEntityPositions();
			}
			return true;
		}
		// max brush
		if (event.inputId() == 4 && event.status() == KeyStatus.down && (event.specialKey() != null
				&& event.specialKey().getAltLeft() && event.specialKey().getCtrlLeft())) {
			this.maxBrush = Maths.avg(this.minBrush + 0.1f, this.maxBrush + 0.1f, 128.0f);
			LOGGER.warn(" values: {} / {}", this.minBrush, this.maxBrush);
			return true;
		}
		if (event.inputId() == 5 && event.status() == KeyStatus.down && (event.specialKey() != null
				&& event.specialKey().getAltLeft() && event.specialKey().getCtrlLeft())) {
			this.maxBrush = Maths.avg(this.minBrush + 0.1f, this.maxBrush - 0.1f, 128.0f);
			LOGGER.warn(" values: {} / {}", this.minBrush, this.maxBrush);
			return true;
		}
		// min brush
		if (event.inputId() == 4 && event.status() == KeyStatus.down
				&& (event.specialKey() != null && event.specialKey().getAltLeft())) {
			this.minBrush = Maths.avg(-128.0f, this.minBrush + 0.1f, this.maxBrush - 0.1f);
			LOGGER.warn(" values: {} / {}", this.minBrush, this.maxBrush);
			return true;
		}
		if (event.inputId() == 5 && event.status() == KeyStatus.down
				&& (event.specialKey() != null && event.specialKey().getAltLeft())) {
			this.minBrush = Maths.avg(-128.0f, this.minBrush - 0.1f, this.maxBrush - 0.1f);
			LOGGER.warn(" values: {} / {}", this.minBrush, this.maxBrush);
			return true;
		}
		// width brush
		if (event.inputId() == 4 && event.status() == KeyStatus.down
				&& (event.specialKey() != null && event.specialKey().getCtrlLeft())) {
			this.widthBrush = Maths.avg(0.1f, this.widthBrush + 0.1f, 30.0f);
			return true;
		}
		if (event.inputId() == 5 && event.status() == KeyStatus.down
				&& (event.specialKey() != null && event.specialKey().getCtrlLeft())) {
			this.widthBrush = Maths.avg(0.1f, this.widthBrush - 0.1f, 30.0f);
			return true;
		}
		return false;
	}

}
