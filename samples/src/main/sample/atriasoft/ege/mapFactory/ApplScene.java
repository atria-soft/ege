package sample.atriasoft.ege.mapFactory;

import org.atriasoft.ege.ControlCameraSimple;
import org.atriasoft.ege.ControlInterface;
import org.atriasoft.ege.Entity;
import org.atriasoft.ege.components.ComponentMesh;
import org.atriasoft.ege.components.ComponentPosition;
import org.atriasoft.ege.components.ComponentRenderMeshPalette;
import org.atriasoft.ege.components.ComponentTexturePalette;
import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.etk.Uri;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sample.atriasoft.ege.mapFactory.model.Map;
import sample.atriasoft.ege.mapFactory.tools.MapToolInterface;

public class ApplScene extends EgeScene {
	final static private Logger LOGGER = LoggerFactory.getLogger(ApplScene.class);
	Map map = new Map();
	private ControlInterface simpleControl;
	private MapToolInterface currentTool = null;
	private Entity groundEntity = null;

	/**
	 * Constructor
	 */
	public ApplScene() {
		addGenericGird();
		this.groundEntity = createGroundEntity();
		this.map.updateMesh();
		this.simpleControl = new ControlCameraSimple(this.mainView);
		this.env.addControlInterface(this.simpleControl);
	}
	
	private Entity createGroundEntity() {
		final Entity entity = new Entity(this.env);
		entity.addComponent(new ComponentPosition(new Transform3D(new Vector3f(0, 0, 0))));
		entity.addComponent(new ComponentMesh(this.map.ground.createMesh()));
		entity.addComponent(new ComponentTexturePalette(new Uri("DATA", "palette_1.json")));
		entity.addComponent(new ComponentRenderMeshPalette(new Uri("DATA", "basicPalette.vert"),
				new Uri("DATA", "basicPalette.frag"), (EngineLight) this.env.getEngine(EngineLight.ENGINE_NAME)));
		this.env.addEntity(entity);
		return entity;
	}

	public Map getMap() {
		return this.map;
	}

	public void reloadGround() {
		if (this.groundEntity != null) {
			this.env.rmEntity(this.groundEntity);
		}
		this.groundEntity = createGroundEntity();
	}

	public MapToolInterface getCurrentTool() {
		return this.currentTool;
	}
	
	@Override
	protected void onDrawScene() {
		if (this.currentTool != null) {
			this.currentTool.onDraw(this.map);
		}
	}

	@Override
	public boolean onEventEntry(final EventEntry event) {
		if (this.currentTool != null) {
			if (this.currentTool.onEventEntry(event, this.map, this)) {
				return true;
			}
		}
		return super.onEventEntry(event);
	}

	@Override
	public boolean onEventInput(final EventInput event) {
		if (this.currentTool != null) {
			if (this.currentTool.onEventInput(event, this.map, this)) {
				return true;
			}
		}
		return super.onEventInput(event);
	}
	
	public void setCurrentTool(final MapToolInterface currentTool) {
		if (this.currentTool != null) {
			this.currentTool.onDeactivate(this);
		}
		this.currentTool = currentTool;
	}
}