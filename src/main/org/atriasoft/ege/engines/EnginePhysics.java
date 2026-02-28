package org.atriasoft.ege.engines;

import java.util.Vector;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentPhysics;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnginePhysics extends Engine {
	static final Logger LOGGER = LoggerFactory.getLogger(EnginePhysics.class);
	public static final String ENGINE_NAME = "physics";
	private static final float TIME_STEP = 0.005f;
	private float accumulator = 0;
	private final EngineGravity gravity;
	private final Vector<ComponentPhysics> components = new Vector<>();
	private final ResourceColored3DObject debugDrawProperty = ResourceColored3DObject.create();

	public EnginePhysics(final Environement env) {
		super(env);
		this.gravity = (EngineGravity) env.getEngine("gravity");
		if (this.gravity == null) {
			LOGGER.error("[CRITICAL] Must initialise Gravity before physics...");
			System.exit(-1);
		}
	}

	private void applyForces(final float timeStep) {
		for (final ComponentPhysics it : this.components) {
			it.applyForces(timeStep, this.gravity);
		}
	}

	@Override
	public void componentAdd(final Component ref) {
		if (!(ref instanceof ComponentPhysics)) {
			return;
		}
		this.components.add((ComponentPhysics) ref);
	}

	@Override
	public void componentRemove(final Component ref) {
		this.components.remove(ref);
	}

	@Override
	public String getType() {
		return ENGINE_NAME;
	}

	@Override
	public void render(final long deltaMili, final Camera camera) {
		for (final ComponentPhysics it : this.components) {
			it.renderDebug(this.debugDrawProperty);
		}
	}

	@Override
	public void renderDebug(final long deltaMili, final Camera camera) {
		// TODO: render debug display when ephysics is integrated
	}

	@Override
	public void update(final long deltaMili) {
		this.accumulator += deltaMili * 0.0001f;
		while (this.accumulator >= TIME_STEP) {
			LOGGER.trace("update physic ... {}", this.accumulator);
			applyForces(TIME_STEP);
			// TODO: integrate ephysics collision detection here
			this.accumulator -= TIME_STEP;
		}
	}
}
