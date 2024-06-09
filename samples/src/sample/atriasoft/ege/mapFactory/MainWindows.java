package sample.atriasoft.ege.mapFactory;

import org.atriasoft.etk.math.Vector3b;
import org.atriasoft.ewol.widget.Button;
import org.atriasoft.ewol.widget.Sizer;
import org.atriasoft.ewol.widget.Sizer.DisplayMode;
import org.atriasoft.ewol.widget.Widget;
import org.atriasoft.ewol.widget.Windows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sample.atriasoft.ege.mapFactory.tools.MapToolInterface;
import sample.atriasoft.ege.mapFactory.tools.ToolMapHeight;

public class MainWindows extends Windows {
	final static private Logger LOGGER = LoggerFactory.getLogger(MainWindows.class);
	
	public static void eventButtonTool(final MainWindows self, Boolean value) {
		//Vector2b state = self.testWidget.getPropertyFill();
		//self.testWidget.setPropertyFill(state.withY(!state.y()));
		LOGGER.warn("event elements : {}", value);
		if (value) {
			self.scene.setCurrentTool(new ToolMapHeight());
		} else {
			self.scene.setCurrentTool(null);
		}
	}
	
	Button heightButton;
	Button toolButton;
	ApplScene scene;
	
	public MainWindows() {
		setPropertyTitle("Map Factory (create your dream world)");
		
		Sizer sizerHoryMain = new Sizer(DisplayMode.HORIZONTAL);
		sizerHoryMain.setPropertyExpand(Vector3b.TRUE);
		sizerHoryMain.setPropertyFill(Vector3b.TRUE);
		setSubWidget(sizerHoryMain);
		
		this.scene = new ApplScene();
		this.scene.setPropertyExpand(Vector3b.TRUE);
		this.scene.setPropertyFill(Vector3b.TRUE);
		sizerHoryMain.subWidgetAdd(this.scene);
		
		Sizer sizerMenu = new Sizer(DisplayMode.VERTICAL);
		sizerMenu.setPropertyExpand(Vector3b.FALSE_TRUE_FALSE);
		sizerMenu.setPropertyLockExpand(Vector3b.TRUE);
		sizerMenu.setPropertyFill(Vector3b.TRUE);
		sizerHoryMain.subWidgetAdd(sizerMenu);
		
		this.toolButton = Button.createToggleLabelButton("Heigher", "Brush");
		this.toolButton.setPropertyExpand(Vector3b.TRUE_FALSE_FALSE);
		this.toolButton.setPropertyFill(Vector3b.TRUE);
		sizerMenu.subWidgetAdd(this.toolButton);
		this.toolButton.signalValue.connectAuto(this, MainWindows::eventButtonTool);
		
		// set default tools:
		MapToolInterface tool = new ToolMapHeight();
		this.scene.setCurrentTool(tool);
		Widget toolDisplay = tool.getWidget();
		sizerMenu.subWidgetAdd(toolDisplay);
	}
	
}