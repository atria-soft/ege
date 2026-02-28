package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.engines.EngineGravity;
import org.atriasoft.ege.engines.EnginePhysics;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentPhysics extends Component {
	static final Logger LOGGER = LoggerFactory.getLogger(ComponentPhysics.class);
	public static float globalMaxSpeed = Float.MAX_VALUE;
	private ComponentPosition position;
	private boolean manageGravity = true;
	private float maxSpeed = globalMaxSpeed;
	private Vector3f speed = new Vector3f(0, 0, 0);
	private Vector3f acceleration = new Vector3f(0, 0, 0);
	private final Vector3f staticForce = new Vector3f(0, 0, 0);
	private final Vector3f dynamicForce = new Vector3f(0, 0, 0);
	private final EnginePhysics engine;
	private PhysicBodyType bodyType;

	public ComponentPhysics(final Environement env) {
		this.engine = (EnginePhysics) env.getEngine(getType());
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

	public void applyForces(final float timeStep, final EngineGravity gravity) {
		Vector3f gravityAcceleration;
		if (this.manageGravity) {
			gravityAcceleration = gravity.getGravityAtPosition(this.position.getTransform().getPosition());
		} else {
			gravityAcceleration = new Vector3f(0, 0, 0);
		}
		final Vector3f staticforceOriented = this.position.getTransform().getOrientation().multiply(this.staticForce);
		final Vector3f dynamicforceOriented = this.position.getTransform().getOrientation().multiply(this.dynamicForce);
		final Vector3f globalForce = staticforceOriented.add(dynamicforceOriented);
		if (this.bodyType != PhysicBodyType.BODY_DYNAMIC) {
			gravityAcceleration = Vector3f.ZERO;
		}
		this.acceleration = gravityAcceleration.add(globalForce);
		this.speed = this.speed.add(this.acceleration.multiply(timeStep));
		limitWithMaxSpeed();
		this.position.setTransform(
				this.position.getTransform().withPosition(this.position.getTransform().getPosition().add(this.speed)));
	}

	public PhysicBodyType getBodyType() {
		return this.bodyType;
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

	private void limitWithMaxSpeed() {
		if (this.speed.length2() > this.maxSpeed * this.maxSpeed) {
			this.speed = this.speed.safeNormalize().multiply(this.maxSpeed);
		}
	}

	public void renderDebug(final ResourceColored3DObject debugDrawProperty) {
		// TODO: render debug shapes when ephysics is integrated
	}

	@Override
	public void removeFriendComponent(final Component component) {
		// nothing to do.
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
}
