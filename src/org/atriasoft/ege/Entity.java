package org.atriasoft.ege;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ege.internal.Log;

public class Entity {
	private static int uIDGlobal = 0; //!< This is a reference on a basic Entity ID
	protected Environement env = null;
	protected List<Component> component = new ArrayList<Component>();
	
	private final int uID = uIDGlobal++; //!< This is a reference on a basic Entity ID
	protected float life = 100; //!< Current life of the object
	protected float lifeMax = 100; //!< Maximum possible life of the Entity
	private int group = 0; //!< Every Entity has a generic group
	
	protected float radius = 0; //!< Radius of the Entity (all Entity have a radius, if  == 0 ==> then ghost ...
	
	/**
	 * Constructor (when ructer is called just add Entity that did not change.
	 * The objest will be stored in a pool of Entity and keep a second time if needed  == > reduce memory allocation,
	 * when needed, the system will call the init and un-init function...
	 */
	public Entity(final Environement env) {
		this.env = env;
	}
	
	public void addComponent(final Component ref) {
		if (ref == null) {
			LOGGER.error("try to add an empty component");
			return;
		}
		// Remove component with the same name.
		this.removeComponent(ref.getType());
		LOGGER.print("Entity: Add New component ... [START]");
		this.component.add(ref);
		this.env.engineComponentAdd(ref);
		for (final Component it : this.component) {
			ref.addFriendComponent(it);
			it.addFriendComponent(ref);
		}
		LOGGER.print("Entity: Add New component ... [END]");
	}
	
	/**
	 * remove this Entity from the physique engine
	 */
	public void dynamicDisable() {}
	
	/**
	 * set the elment in the physique engine
	 */
	public void dynamicEnable() {}
	
	/**
	 * Debug display of the current Entity
	 * @param[in,out] draw Basic system to draw the debug shape and informations
	 * @param camera Current camera for display
	 */
	//	public void drawDebug(Colored3DObject draw, Camera camera) {
	//		/*
	//		mdebugText.clear();
	//		mdebugText.setColor(etk::Color<>(0x00, 0xFF, 0x00, 0xFF));
	//		mdebugText.setPos(Vector3f(-20,32,0));
	//		mdebugText.print(getType());
	//		mdebugText.setPos(Vector3f(-20,20,0));
	//		mdebugText.print("life=("+etk::toString(getLifeRatio()));
	//		*/
	//		//mdebugText.print(String("Axe=(")+String(mtmpAxe.x())+String(",")+etk::UString(mtmpAxe.y())+etk::UString(",")+etk::UString(m_tmpAxe.z())+etk::UString(")"));
	//		/*
	//		// TODO Keep this it can be usefull to print something in direction of the camera ...
	//		mdebugText.draw(   etk::matTranslate(getPosition())
	//		                  * etk::matRotate(Vector3f(0,0,1),camera.getAngleZ())
	//		                  * etk::matRotate(Vector3f(1,0,0),(MPI/2.0f-camera.getAngleTeta()))
	//		                  * etk::matScale(Vector3f(0.05,0.05,0.05)));
	//		*/
	//	}
	/**
	 * Event arrive when an Entity has been remove from the system  == > this permit to keep pointer of ennemy, and not search them every cycle ...
	 * @param removedEntity Pointer on the Entity removed.
	 */
	public void entityIsRemoved(final Entity removedEntity) {};
	
	public boolean exist(final String type) {
		for (int iii = 0; iii < this.component.size(); ++iii) {
			if (this.component.get(iii) == null) {
				continue;
			}
			if (this.component.get(iii).getType().contentEquals(type)) {
				return true;
			}
		}
		return false;
	}
	
	public Component getComponent(final String type) {
		// check if not exist
		for (int iii = 0; iii < this.component.size(); ++iii) {
			if (this.component.get(iii) == null) {
				continue;
			}
			if (this.component.get(iii).getType().contentEquals(type)) {
				return this.component.get(iii);
			}
		}
		return null;
	}
	
	/**
	 * get the Group of the Entity.
	 * @return The group ID
	 */
	public int getGroup() {
		return this.group;
	}
	
	/**
	 * get the curent life ratio [0..1]
	 * @return The proportionnal life
	 */
	public float getLifeRatio() {
		if (0 >= this.life) {
			return 0;
		}
		return this.life / this.lifeMax;
	};
	
	/**
	 * get the current space needed by the Entity in the workspace
	 * @return The dimention needed.
	 */
	public float getRadius() {
		return this.radius;
	}
	
	/**
	 * get the curent Entity Unique ID in the all Game.
	 * @return The requested Unique ID.
	 */
	public int getUID() {
		return this.uID;
	}
	
	/**
	 * init the Entity with the defined properties
	 * @param property Type of the next Entity
	 * @param value pointer on the value type
	 * @return true, the Entity is corectly initialized.
	 */
	public boolean init() {
		LOGGER.warn("init() not implemented: uId={}", this.uID);
		return false;
	};
	
	//	 float lifeBorder = 0.1f;
	//	 float lifeHeight = 0.3f;
	//	 float lifeWidth = 2.0f;
	//	 float lifeYPos = 1.7f;
	//	void Entity::drawLife(ewol::resource::Colored3DObject> draw, Camera> camera) {
	//		if (draw == null) {
	//			return;
	//		}
	//		float ratio = getLifeRatio();
	//		if (ratio == 1.0f) {
	//			return;
	//		}
	//		#if 0
	//		mat4 transformationMatrix =   etk::matTranslate(getPosition())
	//		                            * etk::matRotate(Vector3f(0,0,1),camera.getAngleZ())
	//		                            * etk::matRotate(Vector3f(1,0,0),(MPI/2.0f-camera.getAngleTeta()));
	//		List<Vector3f> localVertices;
	//		localVertices.pushBack(Vector3f(-lifeWidth/2.0-lifeBorder,lifeYPos           -lifeBorder,0));
	//		localVertices.pushBack(Vector3f(-lifeWidth/2.0-lifeBorder,lifeYPos+lifeHeight+lifeBorder,0));
	//		localVertices.pushBack(Vector3f( lifeWidth/2.0+lifeBorder,lifeYPos+lifeHeight+lifeBorder,0));
	//		localVertices.pushBack(Vector3f(-lifeWidth/2.0-lifeBorder,lifeYPos           -lifeBorder,0));
	//		localVertices.pushBack(Vector3f( lifeWidth/2.0+lifeBorder,lifeYPos+lifeHeight+lifeBorder,0));
	//		localVertices.pushBack(Vector3f( lifeWidth/2.0+lifeBorder,lifeYPos           -lifeBorder,0));
	//		etk::Color<float> myColor(0x0000FF99);
	//		draw.draw(localVertices, myColor, transformationMatrix, false, false);
	//		localVertices.clear();
	//		/** Bounding box  == > model shape **/
	//		localVertices.pushBack(Vector3f(-lifeWidth/2.0                ,lifeYPos,0));
	//		localVertices.pushBack(Vector3f(-lifeWidth/2.0                ,lifeYPos + lifeHeight,0));
	//		localVertices.pushBack(Vector3f(-lifeWidth/2.0+lifeWidth*ratio,lifeYPos + lifeHeight,0));
	//		localVertices.pushBack(Vector3f(-lifeWidth/2.0                ,lifeYPos,0));
	//		localVertices.pushBack(Vector3f(-lifeWidth/2.0+lifeWidth*ratio,lifeYPos + lifeHeight,0));
	//		localVertices.pushBack(Vector3f(-lifeWidth/2.0+lifeWidth*ratio,lifeYPos,0));
	//		myColor =0x00FF00FF;
	//		if (ratio < 0.2f) {
	//			myColor = 0xFF0000FF;
	//		} else if (ratio < 0.4f) {
	//			myColor = 0xDA7B00FF;
	//		}
	//		draw.draw(localVertices, myColor, transformationMatrix, false, false);
	//		#endif
	//	}
	
	public boolean init(final Object description) {
		LOGGER.warn("init(Object) not implemented: uId={}", this.uID);
		return false;
	}
	
	/*
	 * Check if the Entity is dead.
	 * @return true if the Entity does not exist anymore, false otherwise.
	 */
	public boolean isDead() {
		return (0 >= this.life) ? true : false;
	};
	
	/**
	 * Request if the Entity might be removed from the system
	 * @return true  == > the object is removed
	 */
	public boolean needToRemove() {
		return isDead();
	};
	
	/**
	 * @brief, call when the Entity is removed (call only one time)
	 */
	public void onDestroy() {};
	
	/**
	 * Call when the Entity life change.
	 */
	public void onLifeChange() {}
	
	public void removeComponent(final Component ref) {
		if (ref == null) {
			LOGGER.error("try to remove an empty component");
			return;
		}
		if (this.component.remove(ref) == false) {
			LOGGER.error("try to remove an unexisting component");
			return;
		}
		this.env.engineComponentRemove(ref);
		for (final Component it : this.component) {
			it.removeFriendComponent(ref);
		}
	};
	
	public void removeComponent(final String type) {
		boolean findIt = false;
		Component componentRemoved = null;
		// check if not exist
		for (int iii = 0; iii < this.component.size(); ++iii) {
			if (this.component.get(iii).getType().contentEquals(type)) {
				componentRemoved = this.component.get(iii);
				this.component.remove(iii);
				findIt = true;
				break;
			}
		}
		if (findIt == false) {
			//LOGGER.error("try to remove an unknown component type : '{}'", type);
			return;
		}
		this.env.engineComponentRemove(componentRemoved);
		for (final Component it : this.component) {
			it.removeFriendComponent(componentRemoved);
		}
	};
	
	/**
	 * apply a fire on the Entity at a current power and a specific power.
	 * @param groupIdSource Source Id of the group, by default all event arrive at all group, buf some event can not be obviously apply at the ennemy like reparing ....
	 * @param type Type of event on the life propertied
	 * @param power Power of the event (can be >0 for adding life).
	 * @param center Some fire decrease in function of space distance...
	 */
	public void setFireOn(final int groupIdSource, final int type, final float power, final Vector3f center) {
		final float previousLife = this.life;
		this.life += power;
		this.life = Math.min(Math.max(0.0f, this.life), this.lifeMax);
		if (this.life <= 0) {
			LOGGER.debug("[{}] Entity is killed ...", getUID());
		}
		if (this.life != previousLife) {
			onLifeChange();
		}
	};
	
	/**
	 * set the group of the curent Entity
	 * @param newGroup The new Group ID of the Entity.
	 */
	public void setGroup(final int newGroup) {
		this.group = newGroup;
	}
	
	public boolean unInit() {
		return true;
	};
}
