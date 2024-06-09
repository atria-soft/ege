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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentPhysics extends Component {
	static final Logger LOGGER = LoggerFactory.getLogger(ComponentPhysics.class);
	public static float MINIMUM_BOUNCING_STOP = 0.0001f;
	//public static float ANGLE_MAX_BOUNCING = 0.02f;
	public static float globalMaxSpeed = Float.MAX_VALUE;
	private AABB aabb;
	private final List<ComponentPhysics> aabbIntersection = new ArrayList<>();
	private Set<ComponentPhysics> collisionPrevious = new HashSet<>();
	private final Map<ComponentPhysics, List<ColisionPoint>> collisionCurrent = new HashMap<>();
	//List<ColisionPoint> collisionPoints = new ArrayList<>();
	private final List<PhysicShape> shapes = new ArrayList<>();
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
	private final Vector3f staticForce = new Vector3f(0, 0, 0);
	// Apply dynamic force on it
	private final Vector3f dynamicForce = new Vector3f(0, 0, 0);
	// Apply dynamic force on it
	private final Vector3f dynamicForceGlobal = new Vector3f(0, 0, 0);
	private final EnginePhysics engine;
	private PhysicBodyType bodyType;
	
	public ComponentPhysics(final Environement _env) {
		this.engine = (EnginePhysics) _env.getEngine(getType());
	}
	
	@Override
	public void addFriendComponent(final Component component) {
		if (component.getType().contentEquals("position")) {
			if (component instanceof final ComponentPosition tmp) {
				this.position = tmp;
			} else {
				LOGGER.error("Not manage position model...");
			}
		}
	}
	
	public void addIntersection(final ComponentPhysics component) {
		// do not add multiple times
		if (this.aabbIntersection.contains(component)) {
			return;
		}
		this.aabbIntersection.add(component);
	}
	
	public void addShape(final PhysicShape shape) {
		this.shapes.add(shape);
	}
	
	public void applyColisionForce(final float timeStep) {
		for (final Entry<ComponentPhysics, List<ColisionPoint>> elem : this.collisionCurrent.entrySet()) {
			Float globalBouncing = null;
			if (!this.collisionPrevious.contains(elem.getKey())) {
				globalBouncing = elem.getKey().getBouncingCoefficient();
			}
			float globalFriction = elem.getKey().getFrictionCoefficient();
			Vector3f globalForce = Vector3f.ZERO;
			for (final ColisionPoint impact : elem.getValue()) {
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
	
	public void applyForces(final float timeStep, final EngineGravity gravity) {
		// get the gravity at the specific position...
		Vector3f gravityAcceleration;
		if (this.manageGravity) {
			gravityAcceleration = gravity.getGravityAtPosition(this.position.getTransform().getPosition());
		} else {
			gravityAcceleration = new Vector3f(0, 0, 0);
		}
		// apply this force on the Object
		LOGGER.info("apply gravity: {}", gravityAcceleration);
		// relative to the object
		final Vector3f staticForce = this.staticForce;
		float globalMass = 0;
		for (final PhysicShape shape : this.shapes) {
			globalMass += shape.getMass();
		}
		// note the acceleration is not real, it depend on the current delta time...
		final Vector3f staticforceOriented = this.position.getTransform().getOrientation().multiply(staticForce);
		final Vector3f dynamicforceOriented = this.position.getTransform().getOrientation().multiply(this.dynamicForce);
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
		LOGGER.info("apply acceleration: {}", this.acceleration);
		LOGGER.info("apply speed: {}", this.speed);
		this.position.setTransform(
				this.position.getTransform().withPosition(this.position.getTransform().getPosition().add(this.speed)));
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
	
	private boolean checkCollide(final PhysicShape shapeCurrent) {
		if (shapeCurrent instanceof final PhysicBox shape111) {
			for (final PhysicShape shape : this.shapes) {
				if (shape instanceof final PhysicHeightMapChunk shape222) {
					// detect collision from cube on height-map !!!
					
				} else if (shape instanceof final PhysicBox shape222) {
					// detect collision between 2 cubes
					if (ToolCollisionOBBWithOBB.testCollide(shape111, shape222)) {
						return true;
					}
				} else if (shape instanceof final PhysicSphere shape222) {
					
				} else if (shape instanceof final PhysicMapVoxel shape222) {
					
				} else {
					LOGGER.error("Not manage collision model... {}", shape);
				}
			}
		} else if (shapeCurrent instanceof final PhysicSphere shape111) {
			for (final PhysicShape shape : this.shapes) {
				if (shape instanceof final PhysicHeightMapChunk shape222) {
					// detect collision from sphere on height-map !!!
					if (ToolCollisionSphereWithHeightMapChunk.testCollide(shape111, shape222)) {
						return true;
					}
					
				} else if (shape instanceof final PhysicTriangle shape222) {
					// detect collision from sphere on height-map !!!
					if (ToolCollisionSphereWithTriangle.testCollide(shape111, shape222)) {
						return true;
					}
					
				} else if (shape instanceof final PhysicBox shape222) {
					// detect collision from sphere on cube !!!
					
				} else if (shape instanceof final PhysicSphere shape222) {
					// detect collision from sphere on sphere !!!
					if (ToolCollisionSphereWithSphere.testCollide(shape111, shape222)) {
						return true;
					}
					
				} else if (shape instanceof final PhysicMapVoxel shape222) {
					
				} else {
					LOGGER.error("Not manage collision model... {}", shape);
				}
			}
		} else if (shapeCurrent instanceof final PhysicMapVoxel shape111) {
			for (final PhysicShape shape : this.shapes) {
				if (shape instanceof final PhysicBox shape222) {
					
				} else if (shape instanceof final PhysicSphere shape222) {
					
				} else if (shape instanceof final PhysicMapVoxel shape222) {
					
				} else {
					LOGGER.error("Not manage collision model... {}", shape);
				}
			}
		} else {
			LOGGER.error("Not manage collision model... {}", shapeCurrent);
		}
		return false;
	}
	
	public boolean checkNarrowCollision() {
		if (this.bodyType != PhysicBodyType.BODY_DYNAMIC) {
			return false;
		}
		for (final ComponentPhysics elem : this.aabbIntersection) {
			boolean collide = false;
			for (final PhysicShape shapeCurrent : this.shapes) {
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
		for (final PhysicShape shape : this.shapes) {
			total = FMath.max(total, shape.getBouncingCoefficient());
		}
		return total;
	}
	
	private List<ColisionPoint> getCollidePoints(final PhysicShape shapeRemote) {
		final List<ColisionPoint> out = new ArrayList<>();
		if (shapeRemote instanceof final PhysicSphere remoteShere) {
			for (final PhysicShape shape : this.shapes) {
				if (shape instanceof final PhysicSphere localShape) {
					final ColisionPoint point = ToolCollisionSphereWithSphere.getCollisionPoint(remoteShere,
							localShape);
					if (point != null) {
						out.add(point);
					}
				} else if (shape instanceof final PhysicTriangle localShape) {
					final ColisionPoint point = ToolCollisionSphereWithTriangle.getCollisionPoint(remoteShere,
							localShape);
					if (point != null) {
						out.add(point);
					}
					
				}
			}
		} else if (shapeRemote instanceof PhysicTriangle) {
			// nothing can happens ...
		} else {
			LOGGER.error("Not manage collision model... {}", shapeRemote);
		}
		return out;
	}
	
	public float getFrictionCoefficient() {
		float total = 0.0f;
		for (final PhysicShape shape : this.shapes) {
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
		for (final Entry<ComponentPhysics, List<ColisionPoint>> elem : this.collisionCurrent.entrySet()) {
			for (final PhysicShape shapeCurrent : this.shapes) {
				//TODO Do a better method we do this many times ...
				/*
				if (!elem.checkCollide(shapeCurrent)) {
					continue;
				}
				*/
				final List<ColisionPoint> points = elem.getKey().getCollidePoints(shapeCurrent);
				elem.getValue().addAll(points);
			}
		}
	}
	
	@Override
	public void removeFriendComponent(final Component component) {
		// nothing to do.
	}
	
	public void renderDebug(final ResourceColored3DObject debugDrawProperty) {
		Color displayColor;
		displayColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
		for (final Entry<ComponentPhysics, List<ColisionPoint>> elem : this.collisionCurrent.entrySet()) {
			for (final ColisionPoint impact : elem.getValue()) {
				debugDrawProperty.drawSquare(new Vector3f(0.02f, 0.02f, 0.02f),
						Matrix4f.createMatrixTranslate(impact.position), displayColor);
			}
		}
		if (this.aabbIntersection.size() == 0) {
			displayColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
		} else if (this.collisionCurrent.size() == 0) {
			displayColor = new Color(0.0f, 1.0f, 0.0f, 1.0f);
		} else {
			displayColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
		}
		if (this.aabb != null) {
			debugDrawProperty.drawCubeLine(this.aabb.getMin(), this.aabb.getMax(), displayColor, Matrix4f.IDENTITY,
					true, true);
			//debugDrawProperty.drawCubeLine(new Vector3f(0,0,0), new Vector3f(32,32,32), new Color(1,0,1,1), Matrix4f.identity(), true, true);
		} else {
			LOGGER.error("no AABB");
		}
		
		for (final PhysicShape shape : this.shapes) {
			shape.renderDebug(this.position.getTransform(), debugDrawProperty);
		}
		
	}
	
	public void setBodyType(final PhysicBodyType bodyType) {
		this.bodyType = bodyType;
	}
	
	public void setManageGravity(final boolean manageGravity) {
		this.manageGravity = manageGravity;
	}
	
	public void setMaxSpeed(final float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}
	
	public void updateAABB() {
		
		if (this.position == null) {
			LOGGER.info("No position in Entity ");
			return;
		}
		// TODO Add a flag to check if it is needed to update the AABB...
		final AABB aabbNew = AABB.createInvertedEmpty();
		for (final PhysicShape shape : this.shapes) {
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
			LOGGER.info("No position in Entity ");
			return;
		}
		for (final PhysicShape shape : this.shapes) {
			shape.updateForNarrowCollision(this.position.getTransform());
		}
	}
}
