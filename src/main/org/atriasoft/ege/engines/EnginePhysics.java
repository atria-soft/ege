package org.atriasoft.ege.engines;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Engine;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.components.ComponentPhysics;
import org.atriasoft.ephysics.engine.DynamicsWorld;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.ewol.resource.OwnedResources;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnginePhysics extends Engine {
	static final Logger LOGGER = LoggerFactory.getLogger(EnginePhysics.class);
	public static final String ENGINE_NAME = "physics";
	private static final float TIME_STEP = 1.0f / 60.0f;
	private float accumulator = 0;
	private final EngineGravity gravity;
	private final List<ComponentPhysics> components = new ArrayList<>();
	/** The OpenGL resources of the engine, released once (by gale) when the engine is collected. */
	private final OwnedResources resources = new OwnedResources(this);
	/** Debug drawing of the bodies, created at the first frame drawn. */
	private ResourceColored3DObject debugDrawProperty = null;
	private final DynamicsWorld dynamicsWorld;

	public EnginePhysics(final Environement env) {
		super(env);
		this.gravity = (EngineGravity) env.getEngine("gravity");
		if (this.gravity == null) {
			LOGGER.error("[CRITICAL] Must initialise Gravity before physics...");
			System.exit(-1);
		}
		final Vector3f initialGravity = this.gravity.getGravityAtPosition(Vector3f.ZERO);
		this.dynamicsWorld = new DynamicsWorld(initialGravity);
	}

	/**
	 * Get the ephysics dynamics world.
	 * @return The dynamics world instance
	 */
	public DynamicsWorld getDynamicsWorld() {
		return this.dynamicsWorld;
	}

	private void updateGravity() {
		final Vector3f currentGravity = this.gravity.getGravityAtPosition(Vector3f.ZERO);
		if (!currentGravity.isEqual(this.dynamicsWorld.getGravity())) {
			this.dynamicsWorld.setGravity(currentGravity);
		}
	}

	private void syncTransformsFromPhysics() {
		for (final ComponentPhysics comp : this.components) {
			comp.syncFromPhysics();
		}
	}

	private void syncTransformsToPhysics() {
		for (final ComponentPhysics comp : this.components) {
			comp.syncToPhysics();
		}
	}

	@Override
	public void componentAdd(final Component ref) {
		if (!(ref instanceof ComponentPhysics)) {
			return;
		}
		final ComponentPhysics physics = (ComponentPhysics) ref;
		this.components.add(physics);
		physics.createBody(this.dynamicsWorld);
	}

	@Override
	public void componentRemove(final Component ref) {
		if (!(ref instanceof ComponentPhysics)) {
			return;
		}
		final ComponentPhysics physics = (ComponentPhysics) ref;
		physics.destroyBody(this.dynamicsWorld);
		this.components.remove(physics);
	}

	@Override
	public String getType() {
		return ENGINE_NAME;
	}

	@Override
	public void render(final long deltaMili, final Camera camera) {
		if (this.components.isEmpty()) {
			return;
		}
		if (this.debugDrawProperty == null) {
			this.debugDrawProperty = this.resources.own(ResourceColored3DObject.create());
		}
		for (final ComponentPhysics it : this.components) {
			it.renderDebug(this.debugDrawProperty);
		}
	}

	@Override
	public void renderDebug(final long deltaMili, final Camera camera) {
		// Could render AABB wireframes, contact points, etc.
	}

	@Override
	public void update(final long deltaMili) {
		updateGravity();
		syncTransformsToPhysics();
		this.accumulator += deltaMili * 0.001f;
		while (this.accumulator >= TIME_STEP) {
			LOGGER.trace("update physic ... {}", this.accumulator);
			this.dynamicsWorld.update(TIME_STEP);
			this.accumulator -= TIME_STEP;
		}
		syncTransformsFromPhysics();
	}
}
