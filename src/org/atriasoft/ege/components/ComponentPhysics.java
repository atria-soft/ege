package org.atriasoft.ege.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.engines.EngineGravity;
import org.atriasoft.ege.engines.EnginePhysics;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.FMath;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.phyligram.ColisionPoint;
import org.atriasoft.phyligram.PhysicBox;
import org.atriasoft.phyligram.PhysicHeightMapChunk;
import org.atriasoft.phyligram.PhysicMapVoxel;
import org.atriasoft.phyligram.PhysicShape;
import org.atriasoft.phyligram.PhysicSphere;
import org.atriasoft.phyligram.PhysicTriangle;
import org.atriasoft.phyligram.math.ToolCollisionOBBWithOBB;
import org.atriasoft.phyligram.math.ToolCollisionSphereWithHeightMapChunk;
import org.atriasoft.phyligram.math.ToolCollisionSphereWithSphere;
import org.atriasoft.phyligram.math.ToolCollisionSphereWithTriangle;
import org.atriasoft.phyligram.shape.AABB;

public class ComponentPhysics extends Component {
	public static float MINIMUM_BOUNCING_STOP = 0.0001f;
	//public static float ANGLE_MAX_BOUNCING = 0.02f;
	public static float globalMaxSpeed = Float.MAX_VALUE;
	private AABB aabb;
	private List<ComponentPhysics> aabbIntersection = new ArrayList<>();
	private Set<ComponentPhysics> collisionPrevious = new HashSet<>();
	private Map<ComponentPhysics, List<ColisionPoint>> collisionCurrent = new HashMap<>();
	//List<ColisionPoint> collisionPoints = new ArrayList<>();
	private List<PhysicShape> shapes = new ArrayList<>();
	// collision already exist ==> prepare friction:
	Transform3D previousPosition;
	private ComponentPosition position;
	private boolean manageGravity = true;
	private float maxSpeed = globalMaxSpeed;
	// current speed of the object
	private Vector3f speed = new Vector3f(0, 0, 0);
	// current acceleration of the object
	private Vector3f acceleration = new Vector3f(0, 0, 0);
	// Applied static force on it
	private Vector3f staticForce = new Vector3f(0, 0, 0);
	// Apply dynamic force on it
	private Vector3f dynamicForce = new Vector3f(0, 0, 0);
	// Apply dynamic force on it
	private Vector3f dynamicForceGlobal = new Vector3f(0, 0, 0);
	private EnginePhysics engine;
	private PhysicBodyType bodyType;
	
	public ComponentPhysics(final Environement _env) {
		this.engine = (EnginePhysics) _env.getEngine(getType());
	}
	
	@Override
	public void addFriendComponent(Component component) {
		if (component.getType().contentEquals("position")) {
			if (component instanceof ComponentPosition tmp) {
				this.position = tmp;
			} else {
				Log.error("Not manage position model...");
			}
		}
	}
	
	public void addIntersection(ComponentPhysics component) {
		// do not add multiple times
		if (this.aabbIntersection.contains(component)) {
			return;
		}
		this.aabbIntersection.add(component);
	}
	
	public void addShape(PhysicShape shape) {
		this.shapes.add(shape);
	}
	
	public void applyColisionForce(float timeStep) {
		for (Entry<ComponentPhysics, List<ColisionPoint>> elem : this.collisionCurrent.entrySet()) {
			Float globalBouncing = null;
			if (!this.collisionPrevious.contains(elem.getKey())) {
				globalBouncing = elem.getKey().getBouncingCoefficient();
			}
			float globalFriction = elem.getKey().getFrictionCoefficient();
			Vector3f globalForce = Vector3f.ZERO;
			for (ColisionPoint impact : elem.getValue()) {
				globalForce = globalForce.add(impact.force);
			}
			// nothing to apply ?
			if (globalForce == Vector3f.ZERO) {
				continue;
			}
			// when tuch a kinematy force is divide between the two
			if (elem.getKey().getBodyType() == PhysicBodyType.BODY_DYNAMIC) {
				globalForce = globalForce.multiply(0.5f);
			}
			if (globalBouncing != null) {
				// detect new collision ==> apply bouncing
				if (this.speed.length2() < MINIMUM_BOUNCING_STOP) {
					this.speed = Vector3f.ZERO;
				} else {
					// this force is not depending on the mass...
					this.speed = this.speed.reflect(globalForce.normalize()).multiply(globalBouncing); // apply the bouncing...
				}
				this.position.applyForce(globalForce);
			} else {
				// second consecutive time off collision ==> friction ...
				this.position.applyForce(globalForce);
				this.speed = this.position.getTransform().getPosition().less(this.previousPosition.getPosition());//.divide(timeStep);
				globalFriction = 1.0f - FMath.avg(0.0f, globalFriction, 1.0f) * timeStep;
				this.speed = this.speed.multiply(globalFriction);
			}
		}
	}
	
	public void applyForces(float timeStep, EngineGravity gravity) {
		// get the gravity at the specific position...
		Vector3f gravityAcceleration;
		if (this.manageGravity) {
			gravityAcceleration = gravity.getGravityAtPosition(this.position.getTransform().getPosition());
		} else {
			gravityAcceleration = new Vector3f(0, 0, 0);
		}
		// apply this force on the Object
		Log.info("apply gravity: {}", gravityAcceleration);
		// relative to the object
		Vector3f staticForce = this.staticForce;
		float globalMass = 0;
		for (PhysicShape shape : this.shapes) {
			globalMass += shape.getMass();
		}
		// note the acceleration is not real, it depend on the current delta time...
		Vector3f staticforceOriented = this.position.getTransform().getOrientation().multiply(staticForce);
		Vector3f dynamicforceOriented = this.position.getTransform().getOrientation().multiply(this.dynamicForce);
		Vector3f globalForce = staticforceOriented.add(dynamicforceOriented);
		if (globalMass > 0.0) {
			globalForce = globalForce.divide(globalMass);
		} else {
			gravityAcceleration = Vector3f.ZERO;
		}
		if (this.bodyType != PhysicBodyType.BODY_DYNAMIC) {
			gravityAcceleration = Vector3f.ZERO;
		}
		this.acceleration = gravityAcceleration.add(globalForce);
		this.speed = this.speed.add(this.acceleration.multiply(timeStep));
		limitWithMaxSpeed();
		Log.info("apply acceleration: {}", this.acceleration);
		Log.info("apply speed: {}", this.speed);
		this.position.setTransform(this.position.getTransform().withPosition(this.position.getTransform().getPosition().add(this.speed)));
	}
	
	/*
	
	public void applyColisionForce(float timeStep) {
		Vector3f globalForce = Vector3f.ZERO;
		float globalBouncing = 1.0f;
		this.haveBouncing = false;
		for (Entry<ComponentPhysics, List<ColisionPoint>> elem : this.collisionCurrent.entrySet()) {
			if (!this.collisionPrevious.contains(elem.getKey())) {
				this.haveBouncing = true;
				globalBouncing = FMath.min(globalBouncing, elem.getKey().getBouncingCoefficient());
			}
			for (ColisionPoint impact : elem.getValue()) {
				globalForce = globalForce.add(impact.force);
			}
		}
		if (globalForce == Vector3f.ZERO) {
			return;
		}
		if (this.haveBouncing) {
			// detect new collision ==> apply bouncing
			if (this.speed.length2() < MINIMUM_BOUNCING_STOP) {
				this.speed = Vector3f.ZERO;
			} else {
				// this force is not depending on the mass...
				this.speed = this.speed.reflect(globalForce.normalize()).multiply(globalBouncing); // apply the bouncing...
			}
			this.position.applyForce(globalForce);
		} else {
			this.position.applyForce(globalForce);
			this.speed = this.position.getTransform().getPosition().less(this.previousPosition.getPosition());//.divide(timeStep);
		}
	}
	
	public void applyFriction(float timeStep) {
		if (this.collisionCurrent.isEmpty()) {
			return;
		}
		float globalFriction = 0.0f;
		for (Entry<ComponentPhysics, List<ColisionPoint>> elem : this.collisionCurrent.entrySet()) {
			globalFriction += elem.getKey().getFrictionCoefficient();
		}
		globalFriction = 1.0f - FMath.avg(0.0f, globalFriction, 1.0f) * timeStep;
		if (!this.haveBouncing) {
			this.speed = this.speed.multiply(globalFriction); // c'est ini qu'il faut mettre en place l'absortion complète
		}
	}
	 */
	
	private boolean checkCollide(PhysicShape shapeCurrent) {
		if (shapeCurrent instanceof PhysicBox shape111) {
			for (PhysicShape shape : this.shapes) {
				if (shape instanceof PhysicHeightMapChunk shape222) {
					// detect collision from cube on height-map !!!
					
				} else if (shape instanceof PhysicBox shape222) {
					// detect collision between 2 cubes
					if (ToolCollisionOBBWithOBB.testCollide(shape111, shape222)) {
						return true;
					}
				} else if (shape instanceof PhysicSphere shape222) {
					
				} else if (shape instanceof PhysicMapVoxel shape222) {
					
				} else {
					Log.error("Not manage collision model... {}", shape);
				}
			}
		} else if (shapeCurrent instanceof PhysicSphere shape111) {
			for (PhysicShape shape : this.shapes) {
				if (shape instanceof PhysicHeightMapChunk shape222) {
					// detect collision from sphere on height-map !!!
					if (ToolCollisionSphereWithHeightMapChunk.testCollide(shape111, shape222)) {
						return true;
					}
					
				} else if (shape instanceof PhysicTriangle shape222) {
					// detect collision from sphere on height-map !!!
					if (ToolCollisionSphereWithTriangle.testCollide(shape111, shape222)) {
						return true;
					}
					
				} else if (shape instanceof PhysicBox shape222) {
					// detect collision from sphere on cube !!!
					
				} else if (shape instanceof PhysicSphere shape222) {
					// detect collision from sphere on sphere !!!
					if (ToolCollisionSphereWithSphere.testCollide(shape111, shape222)) {
						return true;
					}
					
				} else if (shape instanceof PhysicMapVoxel shape222) {
					
				} else {
					Log.error("Not manage collision model... {}", shape);
				}
			}
		} else if (shapeCurrent instanceof PhysicMapVoxel shape111) {
			for (PhysicShape shape : this.shapes) {
				if (shape instanceof PhysicBox shape222) {
					
				} else if (shape instanceof PhysicSphere shape222) {
					
				} else if (shape instanceof PhysicMapVoxel shape222) {
					
				} else {
					Log.error("Not manage collision model... {}", shape);
				}
			}
		} else {
			Log.error("Not manage collision model... {}", shapeCurrent);
		}
		return false;
	}
	
	public boolean checkNarrowCollision() {
		if (this.bodyType != PhysicBodyType.BODY_DYNAMIC) {
			return false;
		}
		for (ComponentPhysics elem : this.aabbIntersection) {
			boolean collide = false;
			for (PhysicShape shapeCurrent : this.shapes) {
				if (elem.checkCollide(shapeCurrent)) {
					collide = true;
					break;
				}
			}
			if (collide) {
				if (!this.collisionCurrent.containsKey(elem)) {
					this.collisionCurrent.put(elem, new ArrayList<>());
				}
				if (!elem.collisionCurrent.containsKey(this)) {
					elem.collisionCurrent.put(this, new ArrayList<>());
				}
			}
		}
		return isNarrowCollide();
	}
	
	public void clearAABBIntersection() {
		this.aabbIntersection.clear();
	}
	
	public void clearPreviousCollision() {
		// store the previous list of collide elements
		this.collisionPrevious = new HashSet<>(this.collisionCurrent.keySet());
		this.previousPosition = this.position.getTransform();
		this.aabbIntersection.clear();
		this.collisionCurrent.clear();
	}
	
	public void clearShape() {
		this.shapes.clear();
	}
	
	public AABB getAABB() {
		return this.aabb;
	}
	
	public List<ComponentPhysics> getAabbIntersection() {
		return this.aabbIntersection;
	}
	
	public PhysicBodyType getBodyType() {
		return this.bodyType;
	}
	
	private float getBouncingCoefficient() {
		float total = 0.0f;
		for (PhysicShape shape : this.shapes) {
			total = FMath.max(total, shape.getBouncingCoefficient());
		}
		return total;
	}
	
	private List<ColisionPoint> getCollidePoints(PhysicShape shapeRemote) {
		List<ColisionPoint> out = new ArrayList<>();
		if (shapeRemote instanceof PhysicSphere remoteShere) {
			for (PhysicShape shape : this.shapes) {
				if (shape instanceof PhysicSphere localShape) {
					ColisionPoint point = ToolCollisionSphereWithSphere.getCollisionPoint(remoteShere, localShape);
					if (point != null) {
						out.add(point);
					}
				} else if (shape instanceof PhysicTriangle localShape) {
					ColisionPoint point = ToolCollisionSphereWithTriangle.getCollisionPoint(remoteShere, localShape);
					if (point != null) {
						out.add(point);
					}
					
				}
			}
		} else if (shapeRemote instanceof PhysicTriangle) {
			// nothing can happens ...
		} else {
			Log.error("Not manage collision model... {}", shapeRemote);
		}
		return out;
	}
	
	public float getFrictionCoefficient() {
		float total = 0.0f;
		for (PhysicShape shape : this.shapes) {
			total = FMath.max(total, shape.getFrictionCoefficient());
		}
		return total;
	}
	
	public float getMaxSpeed() {
		return this.maxSpeed;
	}
	
	@Override
	public String getType() {
		return EnginePhysics.ENGINE_NAME;
	}
	
	public boolean isManageGravity() {
		return this.manageGravity;
	}
	
	public boolean isNarrowCollide() {
		if (this.collisionCurrent.isEmpty()) {
			return false;
		}
		return true;
	}
	
	private void limitWithMaxSpeed() {
		if (this.speed.length2() > this.maxSpeed * this.maxSpeed) {
			this.speed = this.speed.safeNormalize().multiply(this.maxSpeed);
		}
	}
	
	public void narrowCollisionCreateContactAndForce() {
		if (this.bodyType != PhysicBodyType.BODY_DYNAMIC) {
			return;
		}
		if (this.collisionCurrent.size() == 0) {
			return;
		}
		for (Entry<ComponentPhysics, List<ColisionPoint>> elem : this.collisionCurrent.entrySet()) {
			for (PhysicShape shapeCurrent : this.shapes) {
				//TODO Do a better method we do this many times ...
				/*
				if (!elem.checkCollide(shapeCurrent)) {
					continue;
				}
				*/
				List<ColisionPoint> points = elem.getKey().getCollidePoints(shapeCurrent);
				elem.getValue().addAll(points);
			}
		}
	}
	
	@Override
	public void removeFriendComponent(Component component) {
		// nothing to do.
	}
	
	public void renderDebug(ResourceColored3DObject debugDrawProperty) {
		Color displayColor;
		displayColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
		for (Entry<ComponentPhysics, List<ColisionPoint>> elem : this.collisionCurrent.entrySet()) {
			for (ColisionPoint impact : elem.getValue()) {
				debugDrawProperty.drawSquare(new Vector3f(0.02f, 0.02f, 0.02f), Matrix4f.createMatrixTranslate(impact.position), displayColor);
			}
		}
		if (this.aabbIntersection.size() == 0) {
			displayColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
		} else {
			if (this.collisionCurrent.size() == 0) {
				displayColor = new Color(0.0f, 1.0f, 0.0f, 1.0f);
			} else {
				displayColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
			}
		}
		if (this.aabb != null) {
			debugDrawProperty.drawCubeLine(this.aabb.getMin(), this.aabb.getMax(), displayColor, Matrix4f.IDENTITY, true, true);
			//debugDrawProperty.drawCubeLine(new Vector3f(0,0,0), new Vector3f(32,32,32), new Color(1,0,1,1), Matrix4f.identity(), true, true);
		} else {
			Log.error("no AABB");
		}
		
		for (PhysicShape shape : this.shapes) {
			shape.renderDebug(this.position.getTransform(), debugDrawProperty);
		}
		
	}
	
	public void setBodyType(PhysicBodyType bodyType) {
		this.bodyType = bodyType;
	}
	
	public void setManageGravity(boolean manageGravity) {
		this.manageGravity = manageGravity;
	}
	
	public void setMaxSpeed(float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}
	
	public void updateAABB() {
		
		if (this.position == null) {
			Log.info("No position in Entity ");
			return;
		}
		// TODO Add a flag to check if it is needed to update the AABB...
		AABB aabbNew = AABB.createInvertedEmpty();
		for (PhysicShape shape : this.shapes) {
			shape.updateAABB(this.position.getTransform(), aabbNew);
		}
		this.aabb = aabbNew;
	}
	
	public void updateForNarrowCollision() {
		this.collisionCurrent.clear();
		if (this.aabbIntersection.size() == 0) {
			return;
		}
		if (this.position == null) {
			Log.info("No position in Entity ");
			return;
		}
		for (PhysicShape shape : this.shapes) {
			shape.updateForNarrowCollision(this.position.getTransform());
		}
	}
}
