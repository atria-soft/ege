package org.atriasoft.ege.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.atriasoft.ephysics.collision.shapes.BoxShape;
import org.atriasoft.ephysics.engine.DynamicsWorld;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.junit.jupiter.api.Test;

/** Position component and rigid body stay in step: physics drives dynamic bodies, outside moves are teleports. */
class TestComponentPhysicsSync {

	private static ComponentPhysics make(final DynamicsWorld world, final ComponentPosition position, final PhysicBodyType type) {
		final ComponentPhysics physics = new ComponentPhysics();
		physics.addFriendComponent(position);
		physics.setBodyType(type);
		physics.addShape(new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f), 0.0f), 1.0f);
		physics.createBody(world);
		return physics;
	}

	private static void step(final DynamicsWorld world, final ComponentPhysics physics, final int frames) {
		for (int i = 0; i < frames; i++) {
			physics.syncToPhysics();
			world.update(1.0f / 60.0f);
			physics.syncFromPhysics();
		}
	}

	@Test
	void dynamicBodyFallsAndPositionFollows() {
		final DynamicsWorld world = new DynamicsWorld(new Vector3f(0.0f, -9.81f, 0.0f));
		final ComponentPosition position = new ComponentPosition(new Transform3D(new Vector3f(0, 10, 0)));
		final ComponentPhysics physics = make(world, position, PhysicBodyType.BODY_DYNAMIC);
		step(world, physics, 30);
		assertTrue(position.getTransform().getPosition().y() < 9.0f);
	}

	@Test
	void externalMoveTeleportsADynamicBodyAndResetsItsVelocity() {
		final DynamicsWorld world = new DynamicsWorld(new Vector3f(0.0f, -9.81f, 0.0f));
		final ComponentPosition position = new ComponentPosition(new Transform3D(new Vector3f(0, 10, 0)));
		final ComponentPhysics physics = make(world, position, PhysicBodyType.BODY_DYNAMIC);
		step(world, physics, 30);
		position.setTransform(new Transform3D(new Vector3f(100, 50, 0)));
		physics.syncToPhysics();
		assertEquals(100.0f, physics.getRigidBody().getTransform().getPosition().x(), 1e-4f);
		assertEquals(50.0f, physics.getRigidBody().getTransform().getPosition().y(), 1e-4f);
		assertEquals(0.0f, physics.getRigidBody().getLinearVelocity().length(), 1e-6f);
		// the body keeps falling from the new place, it is not pushed back again
		step(world, physics, 5);
		assertTrue(position.getTransform().getPosition().x() > 99.0f);
		assertTrue(position.getTransform().getPosition().y() < 50.0f);
		assertTrue(position.getTransform().getPosition().y() > 49.0f);
	}

	@Test
	void teleportMovesBodyAndPosition() {
		final DynamicsWorld world = new DynamicsWorld(new Vector3f(0.0f, -9.81f, 0.0f));
		final ComponentPosition position = new ComponentPosition(new Transform3D(new Vector3f(0, 10, 0)));
		final ComponentPhysics physics = make(world, position, PhysicBodyType.BODY_DYNAMIC);
		step(world, physics, 30);
		physics.teleport(new Transform3D(new Vector3f(-7, 20, 3)));
		assertEquals(-7.0f, position.getTransform().getPosition().x(), 1e-4f);
		assertEquals(-7.0f, physics.getRigidBody().getTransform().getPosition().x(), 1e-4f);
		assertEquals(0.0f, physics.getRigidBody().getLinearVelocity().length(), 1e-6f);
		step(world, physics, 1);
		assertEquals(-7.0f, position.getTransform().getPosition().x(), 1e-3f);
	}

	@Test
	void kinematicBodyFollowsThePositionComponent() {
		final DynamicsWorld world = new DynamicsWorld(new Vector3f(0.0f, -9.81f, 0.0f));
		final ComponentPosition position = new ComponentPosition(new Transform3D(new Vector3f(0, 1, 0)));
		final ComponentPhysics physics = make(world, position, PhysicBodyType.BODY_KINEMATIC);
		position.setTransform(new Transform3D(new Vector3f(3, 1, 0)));
		step(world, physics, 10);
		assertEquals(3.0f, position.getTransform().getPosition().x(), 1e-4f);
		assertEquals(1.0f, position.getTransform().getPosition().y(), 1e-4f);
	}
}
