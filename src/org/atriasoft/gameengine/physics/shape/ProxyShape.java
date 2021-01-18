package org.atriasoft.gameengine.physics.shape;

import org.atriasoft.etk.math.Matrix3f;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;

import net.jreactphysics3d.body.RigidBody;
import net.jreactphysics3d.collision.shapes.CollisionShape;
import net.jreactphysics3d.engine.DynamicsWorld;

public class ProxyShape {

    private CollisionShape collisionShape;
    private RigidBody rigidBody;

    protected void createRigidBody(CollisionShape collisionShape, Transform3D transform, float mass, DynamicsWorld dynamicsWorld) {

        this.collisionShape = collisionShape;

        Matrix3f inertiaTensor = new Matrix3f();
        collisionShape.computeLocalInertiaTensor(inertiaTensor, mass);

        rigidBody = dynamicsWorld.createRigidBody(transform, mass, inertiaTensor, collisionShape);
    }

    public CollisionShape getCollisionShape() {
        return collisionShape;
    }

    public RigidBody getRigidBody() {
        return rigidBody;
    }

    public void updateTransform() {

        // Get the interpolated transform of the rigid body
        Transform3D transform = rigidBody.getInterpolatedTransform();

        // Compute the transform used for rendering the box
        Matrix4f glMatrix = transform.getOpenGLMatrix();

        // Apply the scaling matrix to have the correct box dimensions
        //getWorldTransform().fromOpenGLArray(glMatrix);
        //getWorldTransform().multiply(getScalingTransform());
    }

}
