package org.atriasoft.gameengine.engines;

import java.util.Vector;

import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.gameengine.internal.Log;
import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.Engine;
import org.atriasoft.gameengine.Environement;
import org.atriasoft.gameengine.camera.Camera;
import org.atriasoft.gameengine.components.ComponentPhysics;
import org.atriasoft.gameengine.physics.PhysicCollisionAABB;

public class EnginePhysics extends Engine {
	public static final String ENGINE_NAME = "physics";
	private float accumulator = 0;
	private static final float TIME_STEP = 0.005f;
	private EngineGravity gravity;
	private Vector<ComponentPhysics> components = new Vector<ComponentPhysics>();
	private ResourceColored3DObject debugDrawProperty = ResourceColored3DObject.create();
	
	public EnginePhysics(Environement env) {
		super(env);
		this.gravity = (EngineGravity)env.getEngine("gravity");
		if (this.gravity == null) {
			Log.critical("Must initialyse Gravity before physics...");
		}
	}

	@Override
	public void componentRemove(Component ref) {
		components.remove(ref);
	}

	@Override
	public void componentAdd(Component ref) {
		if (ref instanceof ComponentPhysics == false) {
			return;
		}
		components.add((ComponentPhysics)ref);
	}

	@Override
	public void update(long deltaMili) {
		// Add the time difference in the accumulator
		accumulator += (float)deltaMili*0.0001f;
		// While there is enough accumulated time to take one or several physics steps
		while (accumulator >= TIME_STEP) {
			Log.info("update physic ... " + accumulator);
			//applyForces(TIME_STEP);
			updateAABB(TIME_STEP);
			updateCollisionsAABB(TIME_STEP);
			updateCollisionsNarrowPhase(TIME_STEP);
			generateResultCollisionsForces(TIME_STEP);
			// Decrease the accumulated time
			accumulator -= TIME_STEP;
		}
		
	}

	private void applyForces(float timeStep) {
		for (ComponentPhysics it: components) {
			it.applyForces(TIME_STEP, gravity);
		}
	}
	/**
	 * Collision detection STEP 1: Upadte the AABB positioning of each elements
	 * @param timeStep Delta time since the last check
	 */
	private void updateAABB(float timeStep) {
		for (ComponentPhysics it: components) {
			it.updateAABB();
		}
	}
	/**
	 * Collision Detection STEP 2: update the list of each element that collide together in the AABB Boxs (update is done between each boxes)
	 * @param timeStep Delta time since the last check
	 */
	private void updateCollisionsAABB(float timeStep) {
		// clear all object intersection
		for (ComponentPhysics it: components) {
			it.clearAABBIntersection();
		}
		// update the current object intersection...
		for (int iii=0; iii< components.size(); iii++) {
			ComponentPhysics current = components.get(iii);
			PhysicCollisionAABB currentAABB = current.getAABB();
			for (int jjj=iii+1; jjj< components.size(); jjj++) {
				ComponentPhysics remote = components.get(jjj);
				if (currentAABB.intersect(components.get(jjj).getAABB()) == true) {
					current.addIntersection(remote); 
					remote.addIntersection(current);
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
		for (ComponentPhysics it: components) {
			it.updateForNarrowCollision();
		}
		// check for every component if the narrow collision is available.
		for (int iii=0; iii< components.size(); iii++) {
			ComponentPhysics current = components.get(iii);
			boolean collide = current.checkNarrowCollision();
			
		}
		// update the force of collision available.
		for (int iii=0; iii< components.size(); iii++) {
			ComponentPhysics current = components.get(iii);
			current.narrowCollisionCreateContactAndForce();
		}
	}
	/**
	 * Collision Detection STEP 4: apply all calculated forces (with containts) 
	 * @param timeStep 
	 */
	private void generateResultCollisionsForces(float timeStep) {
		
	}

	@Override
	public void render(long deltaMili, Camera camera) {
		// TODO Auto-generated method stub
		for (ComponentPhysics it: this.components) {
			//Log.info("Render " + it);
			it.renderDebug(debugDrawProperty);
		}
		//debugDrawProperty.drawCone(2, 5, 9, 12, Matrix4f.identity(), new Color(1,1,0,1));
		//debugDrawProperty.drawSquare(new Vector3f(1,1,1), Matrix4f.identity(), new Color(1,1,0,1));
		//debugDrawProperty.drawCubeLine(new Vector3f(1,1,1), new Vector3f(5,5,5), new Color(1,0,1,1), Matrix4f.identity(), true, true);
		//debugDrawProperty.drawCubeLine(new Vector3f(0,0,0), new Vector3f(32,32,32), new Color(1,0,1,1), Matrix4f.identity(), true, true);
	}

	@Override
	public void renderDebug(long deltaMili, Camera camera) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return ENGINE_NAME;
	}

}
