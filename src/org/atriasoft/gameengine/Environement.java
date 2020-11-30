package org.atriasoft.gameengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.event.EventEntry;
import org.atriasoft.gale.event.EventInput;
import org.atriasoft.gale.event.EventTime;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeySpecial;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.gale.key.KeyType;
import org.atriasoft.gameengine.camera.Camera;
import org.atriasoft.gameengine.engines.EngineRender;
import org.atriasoft.gameengine.engines.EngineAI;
import org.atriasoft.gameengine.engines.EngineDynamicMeshs;
import org.atriasoft.gameengine.engines.EngineGravity;
import org.atriasoft.gameengine.engines.EngineLight;
import org.atriasoft.gameengine.engines.EngineParticle;
import org.atriasoft.gameengine.engines.EnginePhysics;
import org.atriasoft.gameengine.engines.EnginePlayer;
import org.atriasoft.gameengine.internal.Log;
//import org.atriasoft.gameengine.resource.Mesh;

public class Environement {
	public Signal<Float> signalPlayTimeChange = new Signal<Float>();
	private GameStatus propertyStatus = GameStatus.gameStop; // !< the display is running (not in pause)
	public float propertyRatio = 1.0f; // !< Speed ratio
	protected List<Engine> engines = new ArrayList<Engine>(); // !< EGE sub engine interface (like physique, rendering,
																// audio, ...).
	private List<Entity> listEntity = new ArrayList<Entity>(); // !< List of all entity added in the Game
	List<ControlInterface> controls = new ArrayList<ControlInterface>();
	long lastCallTime = 0;
	
	// ! list of all camera in the world
	protected Map<String, Camera> listCamera = new HashMap<String, Camera>();
	protected long gameTime = 0; // !< time of the game running
	private long startTime;
	//protected List<Mesh> listMeshToDrawFirst = new ArrayList<Mesh>();

	public Environement() {
		addEngine(new EngineGravity(this));
		addEngine(new EnginePlayer(this));
		addEngine(new EngineAI(this));
		addEngine(new EngineDynamicMeshs(this));
		addEngine(new EngineRender(this));
		addEngine(new EnginePhysics(this));
		addEngine(new EngineParticle(this));
		addEngine(new EngineLight(this));
	}

	public void addControlInterface(ControlInterface ref) {
		controls.add(ref);
	}
	public void removeControlInterface(ControlInterface ref) {
		controls.remove(ref);
	}

	public void onPointer(KeySpecial special,
			KeyType type,
			int pointerID,
			Vector2f pos,
			KeyStatus state) {
		EventInput event = new EventInput(type, state, pointerID, pos, special);
		for (ControlInterface elem : controls) {
			elem.onEventInput(event, pos);
		}
	}
	public void onKeyboard( KeySpecial special,
			KeyKeyboard type,
			Character value,
			KeyStatus state) {
		EventEntry event = new EventEntry(special, type, state, value);
		for (ControlInterface elem : controls) {
			elem.onEventEntry(event);
		}
	}

	
	public void addEngine(Engine ref) {
		if (ref == null) {
			Log.error("try to add an empty Engine");
			return;
		}
		// check if not exist
		for (Engine it : engines) {
			if (it.getType().contains(ref.getType())) {
				it = ref;
				return;
			}
		}
		// add it at the end ...
		engines.add(ref);
	}

	public void rmEngine(Engine ref) {
		engines.remove(ref);
	}

	public void rmEngine(String type) {
		for (Engine it : engines) {
			if (it.getType().contains(type)) {
				engines.remove(it);
				return;
			}
		}
	}

	public Engine getEngine(String type) {
		for (Engine it : engines) {
			if (it.getType().contains(type)) {
				return it;
			}
		}
		Log.error("try to get an unexisting engine type: '" + type + "'");
		return null;
	}

	public void engineComponentRemove(Component ref) {
		for (Engine it: engines) {
			if (it.getType().contentEquals(ref.getType())) {
				it.componentRemove(ref);
				return;
			}
		}
	}

	public void engineComponentAdd(Component ref) {
		for (Engine it: engines) {
			if (it.getType().contentEquals(ref.getType())) {
				it.componentAdd(ref);
				return;
			}
		}
	}

	public void render(long deltaMilli, String cameraName) {
		Log.error("Render: " + cameraName + "    time:" + deltaMilli);
		// get the correct camera:
		Camera camera = getCamera(cameraName);
		if (camera == null) {
			Log.error("Render: Can not get camera named: '" + cameraName + "'");
			return;
		}
		OpenGL.setCameraMatrix(camera.getConvertionMatrix());
		for (Engine it: engines) {
			//Log.verbose("    render: " + it.getType());
			it.render(deltaMilli, camera);
		}
//		for (Engine it: engine) {
//			if(it == null) {
//				continue;
//			}
//			Log.verbose("    render: " + it.getType());
//			it.renderDebug(deltaMilli, camera);
//		}

	}

	/**
	 * @brief Add a camera in the camera pool.
	 * @param name Name of the camera.
	 * @param camera Pointer on the camera to add.
	 */
	public void addCamera(String name, Camera camera) {
		listCamera.put(name, camera);
	}

	/**
	 * @brief Get a specific camera.
	 * @param name Name of the camera.
	 * @return A pointer on the camera requested.
	 */
	public Camera getCamera(String name) {
		return listCamera.get(name);
	}

	/**
	 * @brief Get List of all camera.
	 * @return All the camera registerred.
	 */
	public Map<String, Camera> getCameraList() {
		return listCamera;
	}

	/**
	 * @brief Remove all from the current environement
	 */
	public void clear() {
		listEntity.clear();
	}

	private static Map<String, CreatorEntity> creators = new HashMap<String, CreatorEntity>();

	/**
	 * @brief add a creator entity system
	 * @param type Type of the entity.
	 * @param creator Function pointer that reference the entity creating.
	 */
	public static void addCreator(String type, CreatorEntity creator) {
		if (creator == null) {
			Log.error("Try to add an empty CREATOR ...");
			return;
		}
		Log.debug("Add creator: " + type);
		creators.put(type, creator);
		Log.debug("Add creator: " + type + " (done)");

	}

	/**
	 * @brief Create an entity on the curent scene.
	 * @param type Type of the entity that might be created.
	 * @param description String that describe the content of the entity
	 *            properties.
	 * @param autoAddEntity this permit to add the entity if it is created == >
	 *            no more action ...
	 * @return null if an error occured OR the pointer on the entity and it is
	 *         already added on the system.
	 * @note Pointer is return in case of setting properties on it...
	 */
	public Entity createEntity(String type, Object value, boolean autoAddEntity) {
		if (creators.containsKey(type) == false) {
			Log.error("Request creating of an type that is not known '" + type + "'");
			return null;
		}
		CreatorEntity creatorPointer = creators.get(type);
		if (creatorPointer == null) {
			Log.error("null pointer creator  == > internal error... '" + type + "'");
			return null;
		}
		Entity tmpEntity = creatorPointer.create(this, value);
		if (tmpEntity == null) {
			Log.error("allocation error '" + type + "'");
			return null;
		}
		if (autoAddEntity == true) {
			addEntity(tmpEntity);
		}
		return tmpEntity;

	}

	public Entity createEntity(String type, boolean autoAddEntity) {
		return this.createEntity(type, null, autoAddEntity);
	}

	/**
	 * @breif get a reference on the curent list of entity games
	 * @return all entity list
	 */
	public List<Entity> getEntity() {
		return listEntity;
	};

	/**
	 * @brief add an entity on the list availlable.
	 * @param newEntity Entity to add.
	 */
	public void addEntity(Entity newEntity) {
		// prevent memory allocation and un allocation ...
		if (newEntity == null) {
			return;
		}
		listEntity.add(newEntity);
		newEntity.dynamicEnable();
	}

	/**
	 * @brief remove an entity on the list availlable.
	 * @param removeEntity Entity to remove.
	 */
	public void rmEntity(Entity removeEntity) {
		if (removeEntity == null) {
			return;
		}
		for (int iii=0; iii<listEntity.size() ; iii++) {
			listEntity.get(iii).entityIsRemoved(removeEntity);
		}
		if (listEntity.remove(removeEntity) == true) {
			removeEntity.onDestroy();
			removeEntity.dynamicDisable();
			removeEntity.unInit();
		}
	}

	/**
	 * @brief generate an event on all the sub entity of the game == > usefull for
	 *        explosion, or lazer fire ...
	 * @param event event that might be apply ...
	 */
	public void generateInteraction(EntityInteraction event) {
		// inform the entity that an entity has been removed  ==> this permit to keep pointer on entitys ...
		for (int iii=0; iii<listEntity.size() ; iii++) {
			event.applyEvent(listEntity.get(iii));
			/*
			Vector3f destPosition = mlistEntity[iii].getPosition();
			float dist = (sourcePosition - destPosition).length;
			if (dist == 0 || dist>decreasePower) {
				continue;
			}
			float inpact = (decreasePower-dist)/decreasePower * power;
			glistEntity[iii].setFireOn(groupIdSource, type, -inpact, sourcePosition);
			*/
		}

	}
//	private void onCallbackPeriodicCall(ewol::event::Time event) {
//	float curentDelta = event.getDeltaCall();
//	EGEVERBOSE("periodic call : " + event);
//	// small hack to change speed ...
//	curentDelta *= *propertyRatio;
//	// check if the processing is availlable
//	if (propertyStatus.get() == gameStop) {
//		return;
//	}
//	// update game time:
//	int lastGameTime = mgameTime*0.000001f;
//	mgameTime += curentDelta;
//	if (lastGameTime != (int)(mgameTime*0.000001f)) {
//		EGEVERBOSE("    Emit Signal");
//		signalPlayTimeChange.emit(mgameTime*0.000001f);
//	}
//	
//	//EWOLDEBUG("Time: mlastCallTime=" + mlastCallTime + " deltaTime=" + deltaTime);
//	
//	// update camera positions:
//	for (auto it : mlistCamera) {
//		if (it.second != null) {
//			EGEVERBOSE("    update camera : '" + it.first + "'");
//			it.second.periodicCall(curentDelta);
//		}
//	}
//	EGEVERBOSE("    step simulation : " + curentDelta);
//	for (auto it: mengine) {
//		if(it == null) {
//			continue;
//		}
//		EGEVERBOSE("    update: " + it.getType());
//		it.update(echrono::Duration(double(curentDelta)));
//	}
//	
//	//EGE.debug("stepSimulation (start)");
//	///step the simulation
//	// TODO mphysicEngine.update(curentDelta);
//	// TODO //optional but useful: debug drawing
//	// TODO mphysicEngine.debugDrawWorld();
//	// TODO EGEINFO("    Update particule engine");
//	// TODO mparticuleEngine.update(curentDelta);
//	// remove all entity that requested it ...
//	/**
//	{
//		int numberEnnemyKilled=0;
//		int victoryPoint=0;
//		auto it(mlistEntity.begin());
//		while (it != mlistEntity.end()) {
//			if(*it != null) {
//				if ((*it).needToRemove() == true) {
//					if ((*it).getGroup() > 1) {
//						numberEnnemyKilled++;
//						victoryPoint++;
//					}
//					EGEINFO("[" + (*it).getUID() + "] entity Removing ... " + (*it).getType());
//					rmEntity((*it));
//					it = mlistEntity.begin();
//				} else {
//					++it;
//				}
//			} else {
//				++it;
//			}
//		}
//		if (numberEnnemyKilled != 0) {
//			//signalKillEnemy.emit(numberEnnemyKilled);
//		}
//	}
//	*/
//	}

//	public void addStaticMeshToDraw(Mesh mesh) {
//		listMeshToDrawFirst.add(mesh);
//	}
//
//	public List<Mesh> getStaticMeshToDraw() {
//		return listMeshToDrawFirst;
//	}

	public GameStatus getPropertyStatus() {
		return propertyStatus;
	}

	public void setPropertyStatus(GameStatus propertyStatus) {
		if (this.propertyStatus == propertyStatus) {
			return;
		}
		this.propertyStatus = propertyStatus;
//		if (propertyStatus == GameStatus.gameStart) {
//			mperiodicCallConnection = getObjectManager().periodicCall.connect(this, Environement::onCallbackPeriodicCall);
//		} else {
//			mperiodicCallConnection.disconnect();
//		}
	}

	public void periodicCall() {
		if (lastCallTime == 0 ) {
			startTime = System.nanoTime()/1000;
			lastCallTime = startTime;
		}
		long lastUpdate = lastCallTime;
		lastCallTime = System.nanoTime()/1000;
		EventTime event = new EventTime(lastCallTime, lastCallTime-startTime, lastCallTime-lastUpdate, lastCallTime-lastUpdate);
		for (ControlInterface elem : controls) {
			elem.periodicCall(event);
		}for (Engine engine : engines) {
			engine.update((lastCallTime-lastUpdate)/100);
		}
	}
}

/*
 * void Environement::getEntityNearest( Vector3f sourcePosition, float
 * distanceMax, List<Environement::ResultNearestEntity> resultList) {
 * resultList.clear(); Environement::ResultNearestEntity result; result.dist =
 * 99999999999.0f; result.entity = null; for (int iii=0; iii<mlistEntity.size()
 * ; iii++) { // chack null pointer result.entity = mlistEntity[iii]; if
 * (result.entity == null) { continue; } // check distance ... Vector3f
 * destPosition = result.entity.getPosition(); if (sourcePosition ==
 * destPosition) { continue; } result.dist = (sourcePosition -
 * destPosition).length(); //EGE.debug("Distance : " + distance + " >? " +
 * distance + " id=" + iii); if (distanceMax>result.dist) {
 * resultList.pushBack(result); } } }
 * 
 * void Environement::getEntityNearestFixed( Vector3f sourcePosition, float
 * distanceMax, List<Environement::ResultNearestEntity> resultList) {
 * resultList.clear(); Environement::ResultNearestEntity result; result.dist =
 * 99999999999.0f; result.entity = null; for (int iii=0; iii<mlistEntity.size()
 * ; iii++) { // chack null pointer result.entity = mlistEntity[iii]; if
 * (result.entity == null) { continue; } if (result.entity.isFixed() == false)
 * { continue; } // check distance ... Vector3f destPosition =
 * result.entity.getPositionTheoric(); result.dist = (sourcePosition -
 * destPosition).length(); //EGE.debug("Distance : " + distance + " >? " +
 * distance + " id=" + iii); if (distanceMax <= result.dist) { continue; } //
 * try to add the entity at the best positions: int jjj; for (jjj=0;
 * jjj<resultList.size(); jjj++) { if (resultList[jjj].dist>result.dist) {
 * resultList.insert(resultList.begin()+jjj, result); break; } } // add entity
 * at the end : if (jjj >= resultList.size()) { resultList.pushBack(result); } }
 * }
 */
