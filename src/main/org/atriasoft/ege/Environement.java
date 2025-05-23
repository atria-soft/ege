package org.atriasoft.ege;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.engines.EngineAI;
import org.atriasoft.ege.engines.EngineDynamicMeshs;
import org.atriasoft.ege.engines.EngineGravity;
import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.ege.engines.EngineParticle;
import org.atriasoft.ege.engines.EnginePhysics;
import org.atriasoft.ege.engines.EnginePlayer;
import org.atriasoft.ege.engines.EngineRender;
//import org.atriasoft.ege.resource.Mesh;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.event.EventTime;
import org.atriasoft.gale.backend3d.OpenGL;
import org.atriasoft.gale.key.KeyKeyboard;
import org.atriasoft.gale.key.KeySpecial;
import org.atriasoft.gale.key.KeyStatus;
import org.atriasoft.gale.key.KeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Environement {
	static final Logger LOGGER = LoggerFactory.getLogger(Environement.class);
	private static Map<String, CreatorEntity> creators = new HashMap<>();

	/**
	 * add a creator entity system
	 * @param type Type of the entity.
	 * @param creator Function pointer that reference the entity creating.
	 */
	public static void addCreator(final String type, final CreatorEntity creator) {
		if (creator == null) {
			LOGGER.error("Try to add an empty CREATOR ...");
			return;
		}
		LOGGER.debug("Add creator: {}", type);
		creators.put(type, creator);
		LOGGER.debug("Add creator: {} (done)", type);

	}

	public Signal<Float> signalPlayTimeChange = new Signal<>();
	private GameStatus propertyStatus = GameStatus.gameStop; // !< the display is running (not in pause)
	public float propertyRatio = 1.0f; // !< Speed ratio
	protected List<Engine> engines = new ArrayList<>(); // !< EGE sub engine interface (like physique, rendering,
	// audio, ...).
	private final List<Entity> listEntity = new ArrayList<>(); // !< List of all entity added in the Game

	List<ControlInterface> controls = new ArrayList<>();
	long lastCallTime = 0;
	// ! list of all camera in the world
	protected Map<String, Camera> listCamera = new HashMap<>();

	protected long gameTime = 0; // !< time of the game running

	long startTime = 0;
	Clock startClock = null;

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
		this.startClock = Clock.systemUTC();
	}

	/**
	 * Add a camera in the camera pool.
	 * @param name Name of the camera.
	 * @param camera Pointer on the camera to add.
	 */
	public void addCamera(final String name, final Camera camera) {
		this.listCamera.put(name, camera);
	}

	public void addControlInterface(final ControlInterface ref) {
		this.controls.add(ref);
	}

	public void addEngine(final Engine ref) {
		if (ref == null) {
			LOGGER.error("try to add an empty Engine");
			return;
		}
		// check if not exist
		for (Engine it : this.engines) {
			if (it.getType().contains(ref.getType())) {
				it = ref;
				return;
			}
		}
		// add it at the end ...
		this.engines.add(ref);
	}

	/**
	 * add an entity on the list availlable.
	 * @param newEntity Entity to add.
	 */
	public void addEntity(final Entity newEntity) {
		// prevent memory allocation and un allocation ...
		if (newEntity == null) {
			return;
		}
		this.listEntity.add(newEntity);
		newEntity.dynamicEnable();
	}

	/**
	 * Remove all from the current environement
	 */
	public void clear() {
		this.listEntity.clear();
	}

	public Entity createEntity(final String type, final boolean autoAddEntity) {
		return this.createEntity(type, null, autoAddEntity);
	}

	/**
	 * Create an entity on the curent scene.
	 * @param type Type of the entity that might be created.
	 * @param description String that describe the content of the entity
	 *            properties.
	 * @param autoAddEntity this permit to add the entity if it is created == >
	 *            no more action ...
	 * @return null if an error occured OR the pointer on the entity and it is
	 *         already added on the system.
	 * @note Pointer is return in case of setting properties on it...
	 */
	public Entity createEntity(final String type, final Object value, final boolean autoAddEntity) {
		if (!creators.containsKey(type)) {
			LOGGER.error("Request creating of an type that is not known '{}'", type);
			return null;
		}
		final CreatorEntity creatorPointer = creators.get(type);
		if (creatorPointer == null) {
			LOGGER.error("null pointer creator  == > internal error... '{}'", type);
			return null;
		}
		final Entity tmpEntity = creatorPointer.create(this, value);
		if (tmpEntity == null) {
			LOGGER.error("allocation error ''{}'", type);
			return null;
		}
		if (autoAddEntity) {
			addEntity(tmpEntity);
		}
		return tmpEntity;

	}

	public void engineComponentAdd(final Component ref) {
		for (final Engine it : this.engines) {
			if (it.getType().contentEquals(ref.getType())) {
				it.componentAdd(ref);
				return;
			}
		}
	}

	public void engineComponentRemove(final Component ref) {
		for (final Engine it : this.engines) {
			if (it.getType().contentEquals(ref.getType())) {
				it.componentRemove(ref);
				return;
			}
		}
	}

	/**
	 * generate an event on all the sub entity of the game == > usefull for
	 *        explosion, or lazer fire ...
	 * @param event event that might be apply ...
	 */
	public void generateInteraction(final EntityInteraction event) {
		// inform the entity that an entity has been removed  ==> this permit to keep pointer on entitys ...
		for (final Entity element : this.listEntity) {
			event.applyEvent(element);
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

	/**
	 * Get a specific camera.
	 * @param name Name of the camera.
	 * @return A pointer on the camera requested.
	 */
	public Camera getCamera(final String name) {
		return this.listCamera.get(name);
	}

	/**
	 * Get List of all camera.
	 * @return All the camera registerred.
	 */
	public Map<String, Camera> getCameraList() {
		return this.listCamera;
	}

	public Engine getEngine(final String type) {
		for (final Engine it : this.engines) {
			if (it.getType().contains(type)) {
				return it;
			}
		}
		LOGGER.error("try to get an unexisting engine type: ''{}'", type);
		return null;
	}

	/**
	 * @breif get a reference on the curent list of entity games
	 * @return all entity list
	 */
	public List<Entity> getEntity() {
		return this.listEntity;
	}

	public GameStatus getPropertyStatus() {
		return this.propertyStatus;
	}

	public void onKeyboard(
			final KeySpecial special,
			final KeyKeyboard type,
			final Character value,
			final KeyStatus state) {
		final EventEntry event = new EventEntry(special, type, state, value);
		for (final ControlInterface elem : this.controls) {
			elem.onEventEntry(event);
		}
	}

	public void onPointer(
			final KeySpecial special,
			final KeyType type,
			final int pointerID,
			final Vector3f pos,
			final KeyStatus state) {
		final EventInput event = new EventInput(type, state, pointerID, new Vector2f(pos.x(), pos.y()), special);
		for (final ControlInterface elem : this.controls) {
			elem.onEventInput(event, pos);
		}
	}

	public void periodicCall() {
		if (this.lastCallTime == 0) {
			this.startTime = System.nanoTime();
			this.lastCallTime = this.startTime;
		}
		long lastUpdate = this.lastCallTime;
		this.lastCallTime = System.nanoTime();
		final Clock currentClock = Clock.systemUTC();
		// in the simulation, we need to limit the delta...
		if (this.lastCallTime - lastUpdate > 1000000000) {
			lastUpdate = this.lastCallTime - 5100000;
		}
		final EventTime event = new EventTime(currentClock, this.startClock, this.lastCallTime, this.startTime,
				Duration.ofNanos(this.lastCallTime - lastUpdate), Duration.ofNanos(this.lastCallTime - lastUpdate));
		for (final ControlInterface elem : this.controls) {
			elem.periodicCall(event);
		}
		for (final Engine engine : this.engines) {
			engine.update((this.lastCallTime - lastUpdate) / 1000000);
		}
	}

	public void removeControlInterface(final ControlInterface ref) {
		this.controls.remove(ref);
	}

	public void render(final long deltaMilli, final String cameraName) {
		//LOGGER.error("Render: {}   time: {}", cameraName, deltaMilli);
		// get the correct camera:
		final Camera camera = getCamera(cameraName);
		if (camera == null) {
			LOGGER.error("Render: Can not get camera named: '{}'", cameraName);
			return;
		}
		OpenGL.setCameraMatrix(camera.getConvertionMatrix());
		for (final Engine it : this.engines) {
			//LOGGER.trace("    render: {}", it.getType());
			it.render(deltaMilli, camera);
		}
		//		for (Engine it: engine) {
		//			if(it == null) {
		//				continue;
		//			}
		//			LOGGER.trace("    render: {}", it.getType());
		//			it.renderDebug(deltaMilli, camera);
		//		}

	}

	public void rmEngine(final Engine ref) {
		this.engines.remove(ref);
	}

	//	public void addStaticMeshToDraw(Mesh mesh) {
	//		listMeshToDrawFirst.add(mesh);
	//	}
	//
	//	public List<Mesh> getStaticMeshToDraw() {
	//		return listMeshToDrawFirst;
	//	}

	public void rmEngine(final String type) {
		for (final Engine it : this.engines) {
			if (it.getType().contains(type)) {
				this.engines.remove(it);
				return;
			}
		}
	}

	/**
	 * remove an entity on the list availlable.
	 * @param removeEntity Entity to remove.
	 */
	public void rmEntity(final Entity removeEntity) {
		if (removeEntity == null) {
			return;
		}
		for (final Entity element : this.listEntity) {
			element.entityIsRemoved(removeEntity);
		}
		if (this.listEntity.remove(removeEntity)) {
			removeEntity.onDestroy();
			removeEntity.dynamicDisable();
			removeEntity.unInit();
		}
	}

	public void setPropertyStatus(final GameStatus propertyStatus) {
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
