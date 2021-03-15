package org.atriasoft.ege.engines;

import java.util.List;
import java.util.Vector;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ephysics.body.RigidBody;
import org.atriasoft.ephysics.collision.ContactManifold;
import org.atriasoft.ephysics.collision.shapes.AABB;
import org.atriasoft.ephysics.constraint.ContactPoint;
import org.atriasoft.ephysics.constraint.ContactPointInfo;
import org.atriasoft.ephysics.engine.DynamicsWorld;
import org.atriasoft.ephysics.engine.EventListener;
import org.atriasoft.ephysics.engine.Island;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentPhysics;
import org.atriasoft.ege.internal.Log;

public class EnginePhysics extends Engine implements EventListener {
	public static final String ENGINE_NAME = "physics";
	// Constant physics time step
	private static final float TIME_STEP = 0.016f;//1.0f / 60.0f;
	boolean propertyDebugAABB = false;
	boolean propertyDebugShape = false;
	// Start engine with no gravity
	private final Vector3f gravity = new Vector3f(0.0f, 0.0f, 0.0f);
	private float accumulator = 0;
	//private final EngineGravity gravity;
	private final DynamicsWorld dynamicsWorld;
	
	private final Vector<ComponentPhysics> components = new Vector<ComponentPhysics>();
	
	private final ResourceColored3DObject debugDrawProperty = ResourceColored3DObject.create();
	
	public EnginePhysics(final Environement env) {
		super(env);
		/*
		this.gravity = (EngineGravity) env.getEngine("gravity");
		if (this.gravity == null) {
			Log.critical("Must initialyse Gravity before physics...");
		}
		*/
		final Vector3f gravity = new Vector3f(0.0f, 0.0f, 0.0f);
		this.dynamicsWorld = new DynamicsWorld(gravity);
		// Set the number of iterations of the constraint solver
		this.dynamicsWorld.setNbIterationsVelocitySolver(15);
		this.dynamicsWorld.setEventListener(this);
	}
	
	private void applyForces(final float timeStep) {
		for (final ComponentPhysics it : this.components) {
			//it.applyForces(TIME_STEP, gravity);
		}
	}
	
	@Override
	public void beginContact(final ContactPointInfo contact) {
		ComponentPhysics component1 = null;
		ComponentPhysics component2 = null;
		// Called when a new contact point is found between two bodies that were separated before.
		Log.warning("collision detection [BEGIN] " + contact.localPoint1 + " depth=" + contact.penetrationDepth);
		if (contact.shape1 != null && contact.shape1.getUserData() != null) {
			component1 = (ComponentPhysics) contact.shape1.getUserData();
		}
		if (contact.shape2 != null && contact.shape2.getUserData() != null) {
			component2 = (ComponentPhysics) contact.shape2.getUserData();
		}
		if (component1 != null) {
			component1.beginContact(component2, contact.normal, contact.localPoint1, contact.localPoint2, contact.penetrationDepth);
		}
		if (component2 != null) {
			component2.beginContact(component1, contact.normal.multiplyNew(-1), contact.localPoint2, contact.localPoint1, contact.penetrationDepth);
		}
	}
	
	@Override
	public void beginInternalTick() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void componentAdd(final Component ref) {
		if (ref instanceof ComponentPhysics == false) {
			return;
		}
		final ComponentPhysics elem = (ComponentPhysics) ref;
		this.components.add(elem);
		elem.generate();
	}
	
	@Override
	public void componentRemove(final Component ref) {
		this.components.remove(ref);
	}
	
	@Override
	public void endInternalTick() {
		// TODO Auto-generated method stub
		
	}
	
	public DynamicsWorld getDynamicsWorld() {
		return this.dynamicsWorld;
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return ENGINE_NAME;
	}
	
	@Override
	public void newContact(final ContactPointInfo contact) {
		
		ComponentPhysics component1 = null;
		ComponentPhysics component2 = null;
		//Called when a new contact point is found between two bodies.
		Log.warning("collision detection [ NEW ] " + contact.localPoint1 + " depth=" + contact.penetrationDepth);
		if (contact.shape1 != null && contact.shape1.getUserData() != null) {
			component1 = (ComponentPhysics) contact.shape1.getUserData();
		}
		if (contact.shape2 != null && contact.shape2.getUserData() != null) {
			component2 = (ComponentPhysics) contact.shape2.getUserData();
		}
		if (component1 != null) {
			component1.newContact(component2, contact.normal, contact.localPoint1, contact.localPoint2, contact.penetrationDepth);
		}
		if (component2 != null) {
			component2.newContact(component1, contact.normal.multiplyNew(-1), contact.localPoint2, contact.localPoint1, contact.penetrationDepth);
		}
	}
	
	@Override
	public void render(final long deltaMili, final Camera camera) {
		// TODO Auto-generated method stub
		for (final ComponentPhysics it : this.components) {
			//Log.info("Render " + it);
			it.renderDebug(this.debugDrawProperty, camera);
		}
		//debugDrawProperty.drawCone(2, 5, 9, 12, Matrix4f.identity(), new Color(1,1,0,1));
		//debugDrawProperty.drawSquare(new Vector3f(1,1,1), Matrix4f.identity(), new Color(1,1,0,1));
		//debugDrawProperty.drawCubeLine(new Vector3f(1,1,1), new Vector3f(5,5,5), new Color(1,0,1,1), Matrix4f.identity(), true, true);
		//debugDrawProperty.drawCubeLine(new Vector3f(0,0,0), new Vector3f(32,32,32), new Color(1,0,1,1), Matrix4f.identity(), true, true);
	}
	
	@Override
	public void renderDebug(final long deltaMili, final Camera camera) {
		if (this.propertyDebugShape == true) {
			for (final ComponentPhysics it : this.components) {
				it.drawShape(this.debugDrawProperty, camera);
			}
		}
		if (this.propertyDebugAABB == true) {
			for (final ComponentPhysics it : this.components) {
				it.renderDebug(this.debugDrawProperty, camera);
			}
		}
		final Matrix4f transformationMatrix = Matrix4f.identity();
		final Color tmpColor = new Color(0.0f, 0.0f, 1.0f, 0.8f);
		final List<Island> islands = this.dynamicsWorld.getIslands();
		for (final Island it : islands) {
			// TODO compute island AABB to display it ...
			final AABB islandElements = new AABB();
			for (final RigidBody elem : it.getBodies()) {
				final AABB tmp = elem.getAABB();
				islandElements.mergeWithAABB(tmp);
			}
			this.debugDrawProperty.drawCubeLine(islandElements.getMin(), islandElements.getMax(), tmpColor, transformationMatrix, true, true);
		}
		
		final List<ContactManifold> listContact = this.dynamicsWorld.getContactsList();
		//Log.info("nb contact: " + listContact.size());
		for (final ContactManifold it : listContact) {
			for (int iii = 0; iii < it.getNbContactPoints(); iii++) {
				final ContactPoint contact = it.getContactPoint(iii);
				this.debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), Matrix4f.identity().multiplyNew(Matrix4f.createMatrixTranslate(contact.getWorldPointOnBody1())),
						new Color(0, 1, 0, 1));
				this.debugDrawProperty.drawSquare(new Vector3f(0.05f, 0.05f, 0.05f), Matrix4f.identity().multiplyNew(Matrix4f.createMatrixTranslate(contact.getWorldPointOnBody2())),
						new Color(0, 1, 0, 1));
			}
		}
		
	}
	
	public void setGravity(final Vector3f _axePower) {
		if (this.dynamicsWorld != null) {
			final Vector3f gravity = _axePower.clone();
			this.dynamicsWorld.setGravity(gravity);
		}
	}
	
	@Override
	public void update(long deltaMili) {
		//Log.error("=================================================================" + deltaMili);
		if (deltaMili > 1000) {
			deltaMili = (long) (TIME_STEP * 1000.0f);
		}
		final float deltaTime = deltaMili * 0.0001f;
		// Add the time difference in the accumulator
		this.accumulator += deltaTime;
		// While there is enough accumulated time to take one or several physics steps
		while (this.accumulator >= TIME_STEP) {
			//Log.error("---------------------------------------------------" + TIME_STEP);
			if (this.dynamicsWorld != null) {
				// call every object to usdate their constant forces applied
				for (final ComponentPhysics it : this.components) {
					if (it != null) {
						it.update(TIME_STEP);
					}
				}
				// Update the Dynamics world with a constant time step
				Log.debug("Update the Physic engine ... " + TIME_STEP);
				this.dynamicsWorld.update(TIME_STEP);
			}
			// Decrease the accumulated time
			this.accumulator -= TIME_STEP;
		}
		for (final ComponentPhysics elem : this.components) {
			elem.emitAll();
		}
	}
	
}
