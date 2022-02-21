package sample.atriasoft.ege.mapFactory;

import org.atriasoft.etk.math.Vector2b;
import org.atriasoft.ewol.widget.Button;
import org.atriasoft.ewol.widget.Sizer;
import org.atriasoft.ewol.widget.Sizer.DisplayMode;
import org.atriasoft.ewol.widget.Windows;

public class MainWindows extends Windows {
	
	public static void eventButtonIncrease(final MainWindows self) {
		//Vector2b state = self.testWidget.getPropertyFill();
		//self.testWidget.setPropertyFill(state.withY(!state.y()));
		if (self.heightButton.getPropertyValue() == "Increase") {
			self.heightButton.setPropertyValue("Decrease");
		} else {
			self.heightButton.setPropertyValue("Increase");
		}
	}
	
	Button heightButton;
	ApplScene scene;
	
	public MainWindows() {
		setPropertyTitle("Map Factory (create your dream world)");
		
		Sizer sizerHoryMain = new Sizer(DisplayMode.modeHori);
		sizerHoryMain.setPropertyExpand(Vector2b.TRUE_TRUE);
		sizerHoryMain.setPropertyFill(Vector2b.TRUE_TRUE);
		setSubWidget(sizerHoryMain);
		
		this.scene = new ApplScene();
		this.scene.setPropertyExpand(Vector2b.TRUE_TRUE);
		this.scene.setPropertyFill(Vector2b.TRUE_TRUE);
		sizerHoryMain.subWidgetAdd(this.scene);
		
		Sizer sizerMenu = new Sizer(DisplayMode.modeVert);
		sizerMenu.setPropertyExpand(Vector2b.FALSE_TRUE);
		sizerMenu.setPropertyLockExpand(Vector2b.TRUE_TRUE);
		sizerMenu.setPropertyFill(Vector2b.TRUE_TRUE);
		sizerHoryMain.subWidgetAdd(sizerMenu);
		
		this.heightButton = new Button();
		this.heightButton.setPropertyValue("Increase");
		this.heightButton.setPropertyExpand(Vector2b.TRUE_FALSE);
		this.heightButton.setPropertyFill(Vector2b.TRUE_TRUE);
		sizerMenu.subWidgetAdd(this.heightButton);
		this.heightButton.signalClick.connectAuto(this, MainWindows::eventButtonIncrease);
	}
	
}