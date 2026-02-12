package sample.atriasoft.ege.mapFactory;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.Dimension2f;
import org.atriasoft.etk.Distance;
import org.atriasoft.etk.math.Vector2b;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.widget.Button;
import org.atriasoft.ewol.widget.Icon;
import org.atriasoft.ewol.widget.ScrollView;
import org.atriasoft.ewol.widget.Select;
import org.atriasoft.ewol.widget.Sizer;
import org.atriasoft.ewol.widget.Sizer.DisplayMode;
import org.atriasoft.ewol.widget.SplitPane;
import org.atriasoft.ewol.widget.Widget;
import org.atriasoft.ewol.widget.Windows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sample.atriasoft.ege.mapFactory.tools.MapToolInterface;
import sample.atriasoft.ege.mapFactory.tools.ToolMapHeight;
import sample.atriasoft.ege.mapFactory.tools.ToolObjectPlacer;
import sample.atriasoft.ege.mapFactory.tools.ToolObjectSelector;

public class MainWindows extends Windows {
	final static private Logger LOGGER = LoggerFactory.getLogger(MainWindows.class);
	
	private static final Color TOOLBAR_SELECTED_COLOR = new Color(0.3f, 0.5f, 0.8f, 1.0f);
	private static final Color TOOLBAR_NORMAL_COLOR = new Color(0.2f, 0.2f, 0.2f, 1.0f);
	
	ApplScene scene;
	Select toolSelect;
	Sizer sizerMenu;
	List<MapToolInterface> tools = new ArrayList<>();
	List<Button> toolButtons = new ArrayList<>();
	int currentToolIndex = 0;
	
	private static void onToolSelectionChanged(final MainWindows self, final Integer index) {
		if (index >= 0 && index < self.tools.size()) {
			self.selectTool(index);
		}
	}
	
	private static void onToolButton0(final MainWindows self) {
		self.selectTool(0);
	}
	
	private static void onToolButton1(final MainWindows self) {
		self.selectTool(1);
	}
	
	private static void onToolButton2(final MainWindows self) {
		self.selectTool(2);
	}
	
	private void selectTool(final int index) {
		this.currentToolIndex = index;
		
		// Clear previous tool widget
		this.sizerMenu.subWidgetRemoveAll();
		
		// Set new tool
		final MapToolInterface tool = this.tools.get(index);
		this.scene.setCurrentTool(tool);
		final Widget toolDisplay = tool.getWidget();
		this.sizerMenu.subWidgetAdd(toolDisplay);
		
		// Sync Select dropdown (does not emit signal, no feedback loop)
		this.toolSelect.setPropertySelectedIndex(index);
		
		// Update toolbar button colors
		updateToolbarSelection(index);
	}
	
	private void updateToolbarSelection(final int selectedIndex) {
		for (int i = 0; i < this.toolButtons.size(); i++) {
			this.toolButtons.get(i)
					.setPropertyColor(i == selectedIndex ? TOOLBAR_SELECTED_COLOR : TOOLBAR_NORMAL_COLOR);
		}
	}
	
	public MainWindows() {
		setPropertyTitle("Map Factory (create your dream world)");

		// Main vertical layout: toolbar on top, split pane below
		final Sizer mainLayout = new Sizer(DisplayMode.VERTICAL);
		mainLayout.setPropertyExpand(Vector2b.TRUE);
		mainLayout.setPropertyFill(Vector2b.TRUE);
		setSubWidget(mainLayout);

		// Register available tools
		this.tools.add(new ToolMapHeight());
		this.tools.add(new ToolObjectPlacer());
		this.tools.add(new ToolObjectSelector());

		// Toolbar (horizontal bar with icon buttons, limited to 50px height)
		final Sizer toolbar = new Sizer(DisplayMode.HORIZONTAL);
		toolbar.setPropertyExpand(new Vector2b(true, false));
		toolbar.setPropertyFill(new Vector2b(true, false));
		toolbar.setPropertyLockExpand(new Vector2b(true, true));
		toolbar.setPropertyMaxSize(new Dimension2f(new Vector2f(0, 50), Distance.PIXEL));
		mainLayout.subWidgetAdd(toolbar);

		final Button btnHeight = Button.create().color(TOOLBAR_NORMAL_COLOR);
		btnHeight.setSubWidget(Icon.create("pencil").fill(Color.WHITE).size(new Dimension2f(new Vector2f(24, 24))));
		btnHeight.signalClick.connectAuto(this, MainWindows::onToolButton0);
		toolbar.subWidgetAdd(btnHeight);
		this.toolButtons.add(btnHeight);

		final Button btnPlacer = Button.create().color(TOOLBAR_NORMAL_COLOR);
		btnPlacer.setSubWidget(Icon.create("add").fill(Color.WHITE).size(new Dimension2f(new Vector2f(24, 24))));
		btnPlacer.signalClick.connectAuto(this, MainWindows::onToolButton1);
		toolbar.subWidgetAdd(btnPlacer);
		this.toolButtons.add(btnPlacer);

		final Button btnSelector = Button.create().color(TOOLBAR_NORMAL_COLOR);
		btnSelector.setSubWidget(Icon.create("search").fill(Color.WHITE).size(new Dimension2f(new Vector2f(24, 24))));
		btnSelector.signalClick.connectAuto(this, MainWindows::onToolButton2);
		toolbar.subWidgetAdd(btnSelector);
		this.toolButtons.add(btnSelector);

		// SplitPane with scene on the left and menu on the right
		final SplitPane splitPane = SplitPane.horizontal().splitPosition(0.75f).separatorSize(6).minSizes(200, 150)
				.expand(true, true).fill(true, true);
		mainLayout.subWidgetAdd(splitPane);

		this.scene = new ApplScene();
		this.scene.setPropertyExpand(Vector2b.TRUE);
		this.scene.setPropertyFill(Vector2b.TRUE);
		splitPane.setFirstWidget(this.scene);

		// Right panel: Select on top, ScrollView below
		final Sizer rightPanel = new Sizer(DisplayMode.VERTICAL);
		rightPanel.setPropertyExpand(Vector2b.TRUE);
		rightPanel.setPropertyFill(Vector2b.TRUE);
		splitPane.setSecondWidget(rightPanel);

		// Tool selector dropdown (synced with toolbar)
		this.toolSelect = Select.create("Height Map", "Object Placer", "Object Selector").selectedIndex(0)
				.expand(true, false).fill(true, false);
		this.toolSelect.signalSelectionChanged.connectAuto(this, MainWindows::onToolSelectionChanged);
		rightPanel.subWidgetAdd(this.toolSelect);

		// ScrollView for the tool options (vertical only)
		final ScrollView scrollView = new ScrollView();
		scrollView.setPropertyExpand(Vector2b.TRUE);
		scrollView.setPropertyFill(Vector2b.TRUE);
		scrollView.setPropertyShowHorizontal(false);
		rightPanel.subWidgetAdd(scrollView);

		this.sizerMenu = new Sizer(DisplayMode.VERTICAL);
		this.sizerMenu.setPropertyExpand(Vector2b.TRUE);
		this.sizerMenu.setPropertyFill(Vector2b.TRUE);
		scrollView.setSubWidget(this.sizerMenu);

		// Set default tool
		selectTool(0);
	}
	
}
