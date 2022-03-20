package sample.atriasoft.ege.mapFactory;

import org.atriasoft.etk.math.Vector2b;
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
		
		this.toolButton = new Button();
		this.toolButton.setPropertyValue("Heigher");
		this.toolButton.setPropertyExpand(Vector2b.TRUE_FALSE);
		this.toolButton.setPropertyFill(Vector2b.TRUE_TRUE);
		sizerMenu.subWidgetAdd(this.toolButton);
		this.toolButton.signalClick.connectAuto(this, MainWindows::eventButtonTool);
		
		this.heightButton = new Button();
		this.heightButton.setPropertyValue("Increase");
		this.heightButton.setPropertyExpand(Vector2b.TRUE_FALSE);
		this.heightButton.setPropertyFill(Vector2b.TRUE_TRUE);
		sizerMenu.subWidgetAdd(this.heightButton);
		this.heightButton.signalClick.connectAuto(this, MainWindows::eventButtonIncrease);
		
		// set default tools:
		this.scene.setCurrentTool(new ToolMapHeight());
		
	}
	
}