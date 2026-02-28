package org.atriasoft.ege.components;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.engines.EnginePhysics;
import org.atriasoft.ephysics.body.BodyType;
import org.atriasoft.ephysics.body.RigidBody;
import org.atriasoft.ephysics.collision.shapes.CollisionShape;
import org.atriasoft.ephysics.engine.DynamicsWorld;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Physics component backed by ephysics RigidBody.
 * <p>
 * Requires a {@link ComponentPosition} as a friend component. The position is synced
 * bidirectionally: dynamic bodies push their transform back to ComponentPosition after
 * simulation; kinematic/static bodies push their ComponentPosition into ephysics before simulation.
 */
public class ComponentPhysics extends Component {
	static final Logger LOGGER = LoggerFactory.getLogger(ComponentPhysics.class);
	private ComponentPosition position;
	private PhysicBodyType bodyType = PhysicBodyType.BODY_DYNAMIC;
	private RigidBody rigidBody;
	private float linearDamping = 0.0f;
	private float angularDamping = 0.0f;
	private float maxSpeed = Float.MAX_VALUE;
	private boolean gravityEnabled = true;
	private final List<ShapeEntry> pendingShapes = new ArrayList<>();

	public ComponentPhysics() {
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

	@Override
	public void removeFriendComponent(final Component component) {
		if (component == this.position) {
			this.position = null;
		}
	}

	@Override
	public String getType() {
		return EnginePhysics.ENGINE_NAME;
	}

	/**
	 * Set the body type (DYNAMIC, STATIC, KINEMATIC).
	 * If the rigid body already exists, the type is set immediately.
	 * @param bodyType The physics body type
	 */
	public void setBodyType(final PhysicBodyType bodyType) {
		this.bodyType = bodyType;
		if (this.rigidBody != null) {
			this.rigidBody.setType(toEphysicsBodyType(this.bodyType));
		}
	}

	public PhysicBodyType getBodyType() {
		return this.bodyType;
	}

	/**
	 * Add a collision shape at the body origin.
	 * If the body is already created, the shape is added immediately.
	 * Otherwise, it is queued and added when the body is created.
	 * @param shape The collision shape
	 * @param mass Mass of this shape (kg)
	 */
	public void addShape(final CollisionShape shape, final float mass) {
		addShape(shape, Transform3D.IDENTITY, mass);
	}

	/**
	 * Add a collision shape with a local transform offset.
	 * @param shape The collision shape
	 * @param localTransform Transform from shape space to body space
	 * @param mass Mass of this shape (kg)
	 */
	public void addShape(final CollisionShape shape, final Transform3D localTransform, final float mass) {
		if (this.rigidBody != null) {
			this.rigidBody.addCollisionShape(shape, localTransform, mass);
		} else {
			this.pendingShapes.add(new ShapeEntry(shape, localTransform, mass));
		}
	}

	/**
	 * Set linear damping.
	 * @param damping Linear damping factor (>= 0)
	 */
	public void setLinearDamping(final float damping) {
		this.linearDamping = damping;
		if (this.rigidBody != null) {
			this.rigidBody.setLinearDamping(damping);
		}
	}

	/**
	 * Set angular damping.
	 * @param damping Angular damping factor (>= 0)
	 */
	public void setAngularDamping(final float damping) {
		this.angularDamping = damping;
		if (this.rigidBody != null) {
			this.rigidBody.setAngularDamping(damping);
		}
	}

	/**
	 * Set max speed clamp for the body.
	 * @param maxSpeed Maximum linear speed
	 */
	public void setMaxSpeed(final float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	/**
	 * Enable or disable gravity for this body.
	 * @param enabled True to enable gravity
	 */
	public void setGravityEnabled(final boolean enabled) {
		this.gravityEnabled = enabled;
		if (this.rigidBody != null) {
			this.rigidBody.enableGravity(enabled);
		}
	}

	/**
	 * Get the underlying ephysics rigid body (may be null if not yet created).
	 * @return The rigid body, or null
	 */
	public RigidBody getRigidBody() {
		return this.rigidBody;
	}

	/**
	 * Apply a force to the body at a given world point.
	 * @param force The force vector (in Newtons)
	 * @param point The world-space point where the force is applied
	 */
	public void applyForce(final Vector3f force, final Vector3f point) {
		if (this.rigidBody != null) {
			this.rigidBody.applyForce(force, point);
		}
	}

	/**
	 * Apply a force at the center of mass.
	 * @param force The force vector (in Newtons)
	 */
	public void applyForceToCenterOfMass(final Vector3f force) {
		if (this.rigidBody != null) {
			this.rigidBody.applyForceToCenterOfMass(force);
		}
	}

	/**
	 * Apply a torque to the body.
	 * @param torque The torque vector
	 */
	public void applyTorque(final Vector3f torque) {
		if (this.rigidBody != null) {
			this.rigidBody.applyTorque(torque);
		}
	}

	/**
	 * Set the linear velocity of the body.
	 * @param velocity Linear velocity vector
	 */
	public void setLinearVelocity(final Vector3f velocity) {
		if (this.rigidBody != null) {
			this.rigidBody.setLinearVelocity(velocity);
		}
	}

	/**
	 * Set the angular velocity of the body.
	 * @param velocity Angular velocity vector
	 */
	public void setAngularVelocity(final Vector3f velocity) {
		if (this.rigidBody != null) {
			this.rigidBody.setAngularVelocity(velocity);
		}
	}

	/**
	 * Called by EnginePhysics when the component is registered.
	 * Creates the rigid body in the dynamics world.
	 * @param world The dynamics world
	 */
	public void createBody(final DynamicsWorld world) {
		if (this.rigidBody != null) {
			return;
		}
		final Transform3D initialTransform;
		if (this.position != null) {
			initialTransform = this.position.getTransform();
		} else {
			initialTransform = Transform3D.IDENTITY;
		}
		this.rigidBody = world.createRigidBody(initialTransform);
		this.rigidBody.setType(toEphysicsBodyType(this.bodyType));
		this.rigidBody.enableGravity(this.gravityEnabled);
		this.rigidBody.setLinearDamping(this.linearDamping);
		this.rigidBody.setAngularDamping(this.angularDamping);
		// Add any shapes that were queued before body creation
		for (final ShapeEntry entry : this.pendingShapes) {
			this.rigidBody.addCollisionShape(entry.shape, entry.localTransform, entry.mass);
		}
		this.pendingShapes.clear();
	}

	/**
	 * Called by EnginePhysics when the component is unregistered.
	 * Destroys the rigid body from the dynamics world.
	 * @param world The dynamics world
	 */
	public void destroyBody(final DynamicsWorld world) {
		if (this.rigidBody != null) {
			world.destroyRigidBody(this.rigidBody);
			this.rigidBody = null;
		}
	}

	/**
	 * Push the transform from ephysics back to the ComponentPosition.
	 * Called after the physics simulation step for dynamic bodies.
	 */
	public void syncFromPhysics() {
		if (this.rigidBody == null || this.position == null) {
			return;
		}
		if (this.bodyType != PhysicBodyType.BODY_DYNAMIC) {
			return;
		}
		// Speed clamping
		if (this.maxSpeed < Float.MAX_VALUE) {
			final Vector3f velocity = this.rigidBody.getLinearVelocity();
			if (velocity.length2() > this.maxSpeed * this.maxSpeed) {
				this.rigidBody.setLinearVelocity(velocity.safeNormalize().multiply(this.maxSpeed));
			}
		}
		this.position.setTransform(this.rigidBody.getTransform());
	}

	/**
	 * Push the ComponentPosition transform into ephysics.
	 * Called before the physics simulation step for kinematic/static bodies.
	 */
	public void syncToPhysics() {
		if (this.rigidBody == null || this.position == null) {
			return;
		}
		if (this.bodyType == PhysicBodyType.BODY_DYNAMIC) {
			return;
		}
		this.rigidBody.setTransform(this.position.getTransform());
	}

	public void renderDebug(final ResourceColored3DObject debugDrawProperty) {
		// Could render collision shape wireframes here
	}

	private static BodyType toEphysicsBodyType(final PhysicBodyType type) {
		return switch (type) {
			case BODY_DYNAMIC -> BodyType.DYNAMIC;
			case BODY_STATIC -> BodyType.STATIC;
			case BODY_KINEMATIC -> BodyType.KINEMATIC;
		};
	}

	private record ShapeEntry(
			CollisionShape shape,
			Transform3D localTransform,
			float mass) {
	}
}
