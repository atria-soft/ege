package sample.atriasoft.ege.mapFactory;

import org.atriasoft.etk.math.Vector3b;
import org.atriasoft.ewol.widget.Button;
import org.atriasoft.ewol.widget.Sizer;
import org.atriasoft.ewol.widget.Sizer.DisplayMode;
import org.atriasoft.ewol.widget.Windows;

import sample.atriasoft.ege.mapFactory.tools.ToolMapHeight;

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
	
	public static void eventButtonTool(final MainWindows self) {
		//Vector2b state = self.testWidget.getPropertyFill();
		//self.testWidget.setPropertyFill(state.withY(!state.y()));
		if (self.toolButton.getPropertyValue() == "Brush") {
			self.toolButton.setPropertyValue("Heigher");
			self.scene.setCurrentTool(new ToolMapHeight());
		} else {
			self.toolButton.setPropertyValue("Brush");
			self.scene.setCurrentTool(null);
		}
	}
	
	Button heightButton;
	Button toolButton;
	ApplScene scene;
	
	public MainWindows() {
		setPropertyTitle("Map Factory (create your dream world)");
		
		Sizer sizerHoryMain = new Sizer(DisplayMode.modeHori);
		sizerHoryMain.setPropertyExpand(Vector3b.TRUE);
		sizerHoryMain.setPropertyFill(Vector3b.TRUE);
		setSubWidget(sizerHoryMain);
		
		this.scene = new ApplScene();
		this.scene.setPropertyExpand(Vector3b.TRUE);
		this.scene.setPropertyFill(Vector3b.TRUE);
		sizerHoryMain.subWidgetAdd(this.scene);
		
		Sizer sizerMenu = new Sizer(DisplayMode.modeVert);
		sizerMenu.setPropertyExpand(Vector3b.FALSE_TRUE_FALSE);
		sizerMenu.setPropertyLockExpand(Vector3b.TRUE);
		sizerMenu.setPropertyFill(Vector3b.TRUE);
		sizerHoryMain.subWidgetAdd(sizerMenu);
		
		this.toolButton = new Button();
		this.toolButton.setPropertyValue("Heigher");
		this.toolButton.setPropertyExpand(Vector3b.TRUE_FALSE_FALSE);
		this.toolButton.setPropertyFill(Vector3b.TRUE);
		sizerMenu.subWidgetAdd(this.toolButton);
		this.toolButton.signalClick.connectAuto(this, MainWindows::eventButtonTool);
		
		this.heightButton = new Button();
		this.heightButton.setPropertyValue("Increase");
		this.heightButton.setPropertyExpand(Vector3b.TRUE_FALSE_FALSE);
		this.heightButton.setPropertyFill(Vector3b.TRUE);
		sizerMenu.subWidgetAdd(this.heightButton);
		this.heightButton.signalClick.connectAuto(this, MainWindows::eventButtonIncrease);
		
		// set default tools:
		this.scene.setCurrentTool(new ToolMapHeight());
		
	}
	
}