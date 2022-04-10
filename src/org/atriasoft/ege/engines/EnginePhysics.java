package org.atriasoft.ege.engines;

import java.util.Vector;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentPhysics;
import org.atriasoft.ege.components.PhysicBodyType;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.phyligram.DebugDisplay;
import org.atriasoft.phyligram.shape.AABB;

public class EnginePhysics extends Engine {
	public static final String ENGINE_NAME = "physics";
	private static final float TIME_STEP = 0.005f;
	private float accumulator = 0;
	private EngineGravity gravity;
	protected EnginePhysics engine;
	private Vector<ComponentPhysics> components = new Vector<>();
	private Vector<ComponentPhysics> componentsWithCollision = new Vector<>();
	private ResourceColored3DObject debugDrawProperty = ResourceColored3DObject.create();
	
	public EnginePhysics(Environement env) {
		super(env);
		this.gravity = (EngineGravity) env.getEngine("gravity");
		if (this.gravity == null) {
			Log.critical("Must initialyse Gravity before physics...");
		}
	}
	
	private void addIncomponentWithCollision(ComponentPhysics elem) {
		if (this.componentsWithCollision.contains(elem)) {
			return;
		}
		this.componentsWithCollision.add(elem);
	}
	
	private void applyForces(float timeStep) {
		for (ComponentPhysics it : this.components) {
			it.applyForces(timeStep, this.gravity);
		}
	}
	
	/**
	 *  Clear the previous data of collision.
	 */
	private void clearPreviousCycle() {
		for (ComponentPhysics it : this.components) {
			it.clearPreviousCollision();
		}
	}
	
	@Override
	public void componentAdd(Component ref) {
		if (ref instanceof ComponentPhysics == false) {
			return;
		}
		this.components.add((ComponentPhysics) ref);
	}
	
	@Override
	public void componentRemove(Component ref) {
		this.components.remove(ref);
	}
	
	/**
	 * Collision Detection STEP 4: apply all calculated forces (with containts)
	 * @param timeStep
	 */
	private void generateResultCollisionsForces(float timeStep) {
		for (ComponentPhysics it : this.componentsWithCollision) {
			it.applyColisionForce(timeStep);
		}
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return ENGINE_NAME;
	}
	
	@Override
	public void render(long deltaMili, Camera camera) {
		// TODO Auto-generated method stub
		for (ComponentPhysics it : this.components) {
			//Log.info("Render {}", it);
			it.renderDebug(this.debugDrawProperty);
		}
		//debugDrawProperty.drawCone(2, 5, 9, 12, Matrix4f.identity(), new Color(1,1,0,1));
		//debugDrawProperty.drawSquare(new Vector3f(1,1,1), Matrix4f.identity(), new Color(1,1,0,1));
		//debugDrawProperty.drawCubeLine(new Vector3f(1,1,1), new Vector3f(5,5,5), new Color(1,0,1,1), Matrix4f.identity(), true, true);
		//debugDrawProperty.drawCubeLine(new Vector3f(0,0,0), new Vector3f(32,32,32), new Color(1,0,1,1), Matrix4f.identity(), true, true);
	}
	
	@Override
	public void renderDebug(long deltaMili, Camera camera) {
		DebugDisplay.onDraw();
		DebugDisplay.clear();
	}
	
	@Override
	public void update(long deltaMili) {
		// Add the time difference in the accumulator
		this.accumulator += deltaMili * 0.0001f;
		// While there is enough accumulated time to take one or several physics steps
		while (this.accumulator >= TIME_STEP) {
			Log.verbose("update physic ... {}", this.accumulator);
			clearPreviousCycle();
			applyForces(TIME_STEP);
			// update AABB after because in rotation force, the Bounding box change...
			updateAABB(TIME_STEP);
			// update the collision tree between each object in the room
			updateCollisionsAABB(TIME_STEP);
			updateCollisionsNarrowPhase(TIME_STEP);
			generateResultCollisionsForces(TIME_STEP);
			// Decrease the accumulated time
			this.accumulator -= TIME_STEP;
		}
		
	}
	
	/**
	 * Collision detection STEP 1: Upadte the AABB positioning of each elements
	 * @param timeStep Delta time since the last check
	 */
	private void updateAABB(float timeStep) {
		for (ComponentPhysics it : this.components) {
			it.updateAABB();
		}
	}
	
	/**
	 * Collision Detection STEP 2: update the list of each element that collide together in the AABB Boxs (update is done between each boxes)
	 * @param timeStep Delta time since the last check
	 */
	// TODO : generate a B-TREE to manage collision, it is faster, but now, this is not the purpose ...
	private void updateCollisionsAABB(float timeStep) {
		this.componentsWithCollision.clear();
		// clear all object intersection
		for (ComponentPhysics it : this.components) {
			it.clearAABBIntersection();
		}
		// update the current object intersection...
		for (int iii = 0; iii < this.components.size(); iii++) {
			ComponentPhysics current = this.components.get(iii);
			AABB currentAABB = current.getAABB();
			for (int jjj = iii + 1; jjj < this.components.size(); jjj++) {
				ComponentPhysics remote = this.components.get(jjj);
				if (current.getBodyType() != PhysicBodyType.BODY_DYNAMIC && remote.getBodyType() != PhysicBodyType.BODY_DYNAMIC) {
					continue;
				}
				// prefer checking the collision, this a time-constant operation, check if collision already exist is a unpredictable time.
				if (currentAABB.intersect(this.components.get(jjj).getAABB()) == true) {
					current.addIntersection(remote);
					remote.addIntersection(current);
					addIncomponentWithCollision(remote);
					addIncomponentWithCollision(current);
				}
			}
		}
	}
	
	/**
	 * Collision Detection STEP 3: Narrow phase: process the collision between every OBB boxes (or other..)
	 * @param timeStep Delta time since the last check
	 */
	private void updateCollisionsNarrowPhase(float timeStep) {
		// clear all object intersection
		for (ComponentPhysics it : this.componentsWithCollision) {
			it.updateForNarrowCollision();
		}
		// check for every component if the narrow collision is available.
		for (int iii = 0; iii < this.componentsWithCollision.size(); iii++) {
			ComponentPhysics current = this.componentsWithCollision.get(iii);
			boolean collide = current.checkNarrowCollision();
			
		}
		// update the force of collision available.
		for (int iii = 0; iii < this.components.size(); iii++) {
			ComponentPhysics current = this.components.get(iii);
			current.narrowCollisionCreateContactAndForce();
		}
	}
	
}
