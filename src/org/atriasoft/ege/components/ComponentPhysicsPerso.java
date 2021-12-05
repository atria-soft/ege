package org.atriasoft.ege.components;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.ege.Component;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.engines.EngineGravity;
import org.atriasoft.ege.engines.EnginePhysics;
import org.atriasoft.ege.engines.EnginePhysicsPerso;
import org.atriasoft.phyligram.PhysicBox;
import org.atriasoft.phyligram.PhysicCollisionAABB;
import org.atriasoft.phyligram.PhysicMapVoxel;
import org.atriasoft.phyligram.PhysicShape;
import org.atriasoft.phyligram.PhysicSphere;
import org.atriasoft.phyligram.ToolCollisionOBBWithOBB;


public class ComponentPhysicsPerso extends Component {
	private PhysicCollisionAABB aabb;
	private List<ComponentPhysicsPerso> aabbIntersection = new ArrayList<ComponentPhysicsPerso>();
	private List<ComponentPhysicsPerso> narrowIntersection = new ArrayList<ComponentPhysicsPerso>();
	private List<PhysicShape> shapes = new ArrayList<PhysicShape>();
	private ComponentPosition position;
	private boolean staticObject = false;
	private boolean manageGravity = false;
	public static float globalMaxSpeed = Float.MAX_VALUE;
	private float maxSpeed = globalMaxSpeed;
	// current speed of the object
	private Vector3f speed = new Vector3f(0,0,0);
	// current acceleration of the object
	private Vector3f acceleration = new Vector3f(0,0,0);
	// Applied static force on it
	private Vector3f staticForce = new Vector3f(0,0,0);
	// Apply dynamic force on it
	private Vector3f dynamicForce = new Vector3f(0,0,0);
	private EnginePhysicsPerso engine;
	private PhysicBodyType bodyType; 
	
	
	@Override
	public String getType() {
		return EnginePhysicsPerso.ENGINE_NAME;
	}
	
	public ComponentPhysicsPerso(final Environement _env) {
		this.engine = (EnginePhysicsPerso) _env.getEngine(getType());
	}
	
	@Override
	public void addFriendComponent(Component component) {
		if (component.getType().contentEquals("position")) {
			if (component instanceof ComponentPosition tmp) {
				position = tmp;
			} else {
				Log.error("Not manage position model...");
			}
		}
	}
	@Override
	public void removeFriendComponent(Component component) {
		// nothing to do.
	}
	
	public void updateAABB() {
		if (position == null) {
			Log.info("No position in Entity ");
			return;
		}
		// TODO Add a flag to check if it is needed to update the AABB...
		PhysicCollisionAABB aabbNew = PhysicCollisionAABB.beforeCalculated();
		for (PhysicShape shape : shapes) {
			shape.updateAABB(position.getTransform(), aabbNew);
		}
		aabb = aabbNew;
	}
	
	public PhysicCollisionAABB getAABB() {
		return aabb;
	}
	
	public void updateForNarrowCollision() {
		narrowIntersection.clear();
		if (aabbIntersection.size() == 0) {
			return;
		}
		if (position == null) {
			Log.info("No position in Entity ");
			return;
		}
		for (PhysicShape shape : shapes) {
			shape.updateForNarrowCollision(position.getTransform());
		}
	}
	public boolean isNarrowCollide() {
		if (narrowIntersection.size() == 0) {
			return false;
		}
		return true;
	}
	public boolean checkNarrowCollision() {
		if (this.staticObject == true) {
			return false;
		}
		for (ComponentPhysicsPerso elem : aabbIntersection) {
			boolean collide = false;
			for (PhysicShape shapeCurrent : shapes) {
				if (elem.checkCollide(shapeCurrent) == true) {
					collide = true;
					break;
				}
			}
			if (collide == true) {
				narrowIntersection.add(elem);
				elem.narrowIntersection.add(this);
			}
		}
		return isNarrowCollide();
	}
	public void narrowCollisionCreateContactAndForce() {
		if (narrowIntersection.size() == 0) {
			return;
		}
		for (ComponentPhysicsPerso elem : narrowIntersection) {
			for (PhysicShape shapeCurrent : this.shapes) {
				//TODO Do a better method we do this many times ...
				if (elem.checkCollide(shapeCurrent) == false) {
					continue;
				}
				elem.getCollidePoints(shapeCurrent, this.staticObject);
			}
		}
	}

	private boolean checkCollide(PhysicShape shapeCurrent) {
		if (shapeCurrent instanceof PhysicBox) {
			PhysicBox shape111 = (PhysicBox)shapeCurrent;
			for (PhysicShape shape : shapes) {
				if (shape instanceof PhysicBox) {
					PhysicBox shape222 = (PhysicBox)shape;
					if (ToolCollisionOBBWithOBB.testCollide(shape111, shape222) == true) {
						return true;
					}
				} else if (shape instanceof PhysicSphere) {
					
				} else if (shape instanceof PhysicMapVoxel) {
					
				} else {
					Log.error("Not manage collision model... " + shape);
				}
			}
		} else if (shapeCurrent instanceof PhysicSphere) {
			for (PhysicShape shape : shapes) {
				if (shape instanceof PhysicBox) {
					
				} else if (shape instanceof PhysicSphere) {
					
				} else if (shape instanceof PhysicMapVoxel) {
					
				} else {
					Log.error("Not manage collision model... " + shape);
				}
			}
		} else if (shapeCurrent instanceof PhysicMapVoxel) {
			for (PhysicShape shape : shapes) {
				if (shape instanceof PhysicBox) {
					
				} else if (shape instanceof PhysicSphere) {
					
				} else if (shape instanceof PhysicMapVoxel) {
					
				} else {
					Log.error("Not manage collision model... " + shape);
				}
			}
		} else {
			Log.error("Not manage collision model... " + shapeCurrent);
		}
		return false;
	}
	private void getCollidePoints(PhysicShape shapeCurrent, boolean isStatic) {
		if (shapeCurrent instanceof PhysicBox) {
			PhysicBox shape111 = (PhysicBox)shapeCurrent;
			for (PhysicShape shape : this.shapes) {
				if (shape instanceof PhysicBox) {
					PhysicBox shape222 = (PhysicBox)shape;
					ToolCollisionOBBWithOBB.getCollidePoints(shape111, isStatic, shape222, this.staticObject);
				} else if (shape instanceof PhysicSphere) {
					
				} else if (shape instanceof PhysicMapVoxel) {
					
				} else {
					Log.error("Not manage collision model... " + shape);
				}
			}
		} else if (shapeCurrent instanceof PhysicSphere) {
			for (PhysicShape shape : this.shapes) {
				if (shape instanceof PhysicBox) {
					
				} else if (shape instanceof PhysicSphere) {
					
				} else if (shape instanceof PhysicMapVoxel) {
					
				} else {
					Log.error("Not manage collision model... " + shape);
				}
			}
		} else if (shapeCurrent instanceof PhysicMapVoxel) {
			for (PhysicShape shape : this.shapes) {
				if (shape instanceof PhysicBox) {
					
				} else if (shape instanceof PhysicSphere) {
					
				} else if (shape instanceof PhysicMapVoxel) {
					
				} else {
					Log.error("Not manage collision model... " + shape);
				}
			}
		} else {
			Log.error("Not manage collision model... " + shapeCurrent);
		}
		return;
	}

	public void applyForces(float timeStep, EngineGravity gravity) {
		// get the gravity at the specific position...
		Vector3f gravityForce;
		if (manageGravity == true) {
			gravityForce = gravity.getGravityAtPosition(position.getTransform().getPosition()).multiply(timeStep);
		} else {
			gravityForce = new Vector3f(0,0,0);
		}
		// apply this force on the Object
		Log.info("apply gravity: " + gravityForce);
		// relative to the object
		Vector3f staticForce = this.staticForce.multiply(timeStep);
		float globalMass = 0;
		for (PhysicShape shape : shapes) {
			globalMass += shape.getMass();
		}
		// note the acceleration is not real, it depend on the current delta time...
		this.acceleration = gravityForce.add(this.position.getTransform().getOrientation().multiply(staticForce)).add(this.position.getTransform().getOrientation().multiply(dynamicForce)).multiply(globalMass);
		this.dynamicForce = new Vector3f(0,0,0);
		this.speed.add(this.acceleration);
		limitWithMaxSpeed();
		Log.info("apply acceleration: " + this.acceleration);
		Log.info("apply speed: " + this.speed);
		this.position.getTransform().getPosition().add(this.speed.multiply(timeStep));
	}
	
	public void renderDebug(ResourceColored3DObject debugDrawProperty) {
		Color displayColor;
		if (this.aabbIntersection.size() == 0) {
			displayColor = new Color(1,1,1,1);
		} else {
			if (this.narrowIntersection.size() == 0) {
				displayColor = new Color(1,1,0,1);
			} else {
				displayColor = new Color(1,0,0,1);
			}
		}
		if (aabb != null) {
			debugDrawProperty.drawCubeLine(aabb.getMin(), aabb.getMax(), displayColor, Matrix4f.IDENTITY, true, true);
			//debugDrawProperty.drawCubeLine(new Vector3f(0,0,0), new Vector3f(32,32,32), new Color(1,0,1,1), Matrix4f.identity(), true, true);
		} else {
			Log.error("no AABB");
		}
		for (PhysicShape shape : shapes) {
			shape.renderDebug(position.getTransform(), debugDrawProperty);
		}
	}
	public void addShape(PhysicShape shape) {
		shapes.add(shape);
	}
	public void clearShape() {
		shapes.clear();
	}
	public boolean isManageGravity() {
		return manageGravity;
	}
	public void setManageGravity(boolean manageGravity) {
		this.manageGravity = manageGravity;
	}
	private void limitWithMaxSpeed() {
		if (this.speed.length2() > this.maxSpeed*this.maxSpeed) {
			this.speed.safeNormalize().multiply(this.maxSpeed);
		}
	}
	public float getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public void clearAABBIntersection() {
		this.aabbIntersection.clear();
	}
	public void addIntersection(ComponentPhysicsPerso component) {
		// do not add multiple times
		for (ComponentPhysicsPerso elem : this.aabbIntersection) {
			if (elem == component) {
				return;
			}
		}
		this.aabbIntersection.add(component);
	}
	public List<ComponentPhysicsPerso> getAabbIntersection() {
		return aabbIntersection;
	}

	public boolean isStaticObject() {
		return staticObject;
	}

	public void setStaticObject(boolean staticObject) {
		this.staticObject = staticObject;
	}

	public PhysicBodyType getBodyType() {
		return bodyType;
	}

	public void setBodyType(PhysicBodyType bodyType) {
		this.bodyType = bodyType;
	}
}
