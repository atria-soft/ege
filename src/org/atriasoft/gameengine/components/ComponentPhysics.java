package org.atriasoft.gameengine.components;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.Environement;
import org.atriasoft.gameengine.camera.Camera;
import org.atriasoft.gameengine.engines.EnginePhysics;
import org.atriasoft.gameengine.internal.Log;
import org.atriasoft.gameengine.physics.shape.Box;
import org.atriasoft.gameengine.physics.shape.Capsule;
import org.atriasoft.gameengine.physics.shape.Concave;
import org.atriasoft.gameengine.physics.shape.Cone;
import org.atriasoft.gameengine.physics.shape.ConvexHull;
import org.atriasoft.gameengine.physics.shape.Cylinder;
import org.atriasoft.gameengine.physics.shape.Shape;
import org.atriasoft.gameengine.physics.shape.Sphere;

import net.jreactphysics3d.body.BodyType;
import net.jreactphysics3d.body.RigidBody;
import net.jreactphysics3d.collision.ProxyShape;
import net.jreactphysics3d.collision.TriangleMesh;
import net.jreactphysics3d.collision.TriangleVertexArray;
import net.jreactphysics3d.collision.shapes.AABB;
import net.jreactphysics3d.collision.shapes.BoxShape;
import net.jreactphysics3d.collision.shapes.CapsuleShape;
import net.jreactphysics3d.collision.shapes.CollisionShape;
import net.jreactphysics3d.collision.shapes.ConcaveMeshShape;
import net.jreactphysics3d.collision.shapes.ConcaveShape;
import net.jreactphysics3d.collision.shapes.ConeShape;
import net.jreactphysics3d.collision.shapes.CylinderShape;
import net.jreactphysics3d.collision.shapes.SphereShape;

public class ComponentPhysics extends Component {
	
	//public Signal<Transform3D> signalPosition;
	protected Transform3D lastTransformEmit;
	protected EnginePhysics engine;
	protected RigidBody rigidBody;
	protected List<CollisionShape> listShape = new ArrayList<>();
	protected List<ProxyShape> listProxyShape = new ArrayList<>();
	
	protected Vector3f staticForceApplyCenterOfMass = new Vector3f(0, 0, 0);
	
	protected Vector3f staticTorqueApply = new Vector3f(0, 0, 0);
	
	protected List<Shape> shape = new ArrayList<>(); //!< collision shape module ... (independent of bullet lib)
	
	/**
	 * @brief Create a basic position component (no orientation and position (0,0,0))
	 */
	public ComponentPhysics(final Environement _env) {
		this.engine = (EnginePhysics) _env.getEngine(getType());
		// Initial position and orientation of the rigid body
		this.lastTransformEmit = new Transform3D(new Vector3f(0, 0, 0), Quaternion.identity());
		this.rigidBody = this.engine.getDynamicsWorld().createRigidBody(this.lastTransformEmit);
		this.rigidBody.setUserData(this);
		// set collision callback:
		//this.engine.getDynamicWorld().testCollision(this.rigidBody, this);
	}
	
	/**
	 * @brief Create a basic position component
	 * @param[in] _transform transformation of the position
	 */
	public ComponentPhysics(final Environement _env, final Transform3D _transform) {
		this.engine = (EnginePhysics) _env.getEngine(getType());
		// Create a rigid body in the world
		this.rigidBody = this.engine.getDynamicsWorld().createRigidBody(_transform);
		this.rigidBody.setUserData(this);
		this.lastTransformEmit = _transform;
		// set collision callback:
		//this.engine.getDynamicWorld().testCollision(this.rigidBody, this);
		Log.error("Bounciness=" + this.rigidBody.getMaterial().getBounciness());
		Log.error("FrictionCoefficient=" + this.rigidBody.getMaterial().getFrictionCoefficient());
		Log.error("RollingResistance=" + this.rigidBody.getMaterial().getRollingResistance());
		Log.error("LinearDamping=" + this.rigidBody.getLinearDamping());
		Log.error("AngularDamping=" + this.rigidBody.getAngularDamping());
		this.rigidBody.getMaterial().setBounciness(0.4f);
		//this.rigidBody.getMaterial().setFrictionCoefficient(0.01f);
		//this.rigidBody.getMaterial().setRollingResistance(0.01f);
		this.rigidBody.setAngularDamping(0.9f);
		this.rigidBody.setLinearDamping(0.9f);
	}
	
	public void addShape(final Shape _shape) {
		this.shape.add(_shape);
	}
	
	/**
		 * @brief Apply an external force to the body at a given point (in world-space coordinates).
		 *        If the point is not at the center of mass of the body, it will also generate some torque and therefore, change the angular velocity of the body.
		 *        If the body is sleeping, calling this method will wake it up. Note that the force will we added to the sum of the applied forces and that this sum will be reset to zero at the end of each call of the DynamicsWorld::update() method. You can only apply a force to a dynamic body otherwise, this method will do nothing.
		 * @param[in] _force The force to apply on the body
		 * @param[in] _point The point where the force is applied (in world-space coordinates)
		 */
	public void applyForce(final Vector3f _force, final Vector3f _point) {
		if (this.rigidBody == null) {
			return;
		}
		this.rigidBody.applyForce(_force, _point);
	}
	
	/**
		 * @brief Apply an external force to the body at its center of mass.
		 *        If the body is sleeping, calling this method will wake it up.
		 * @note The force will we added to the sum of the applied forces and that this sum will be reset to zero at the end of each call of the DynamicsWorld::update() method. You can only apply a force to a dynamic body otherwise, this method will do nothing.
		 * @param[in] _force The external force to apply on the center of mass of the body
		 * @param[in] _static The torque will be apply while the user des not call the same function with 0 value ...
		 */
	public void applyForceToCenterOfMass(final Vector3f _force) {
		if (this.rigidBody == null) {
			return;
		}
		this.rigidBody.applyForceToCenterOfMass(_force);
	}
	
	public void applyForceToCenterOfMassStatic(final Vector3f _force) {
		if (this.rigidBody == null) {
			return;
		}
		this.staticForceApplyCenterOfMass = _force;
	}
	
	/**
		 * @brief Apply an external force to the body at its center of mass.
		 *        If the body is sleeping, calling this method will wake it up.
		 * @note The force is apply with a relative axis of the object
		 * @note The force will we added to the sum of the applied forces and that this sum will be reset to zero at the end of each call of the DynamicsWorld::update() method. You can only apply a force to a dynamic body otherwise, this method will do nothing.
		 * @param[in] _force The external force to apply on the center of mass of the body
		 * @param[in] _static The torque will be apply while the user des not call the same function with 0 value ...
		 */
	public void applyRelativeForceToCenterOfMass(final Vector3f _force) {
		if (this.rigidBody == null) {
			return;
		}
		final Vector3f force = this.rigidBody.getTransform().getOrientation().multiply(_force);
		this.rigidBody.applyForceToCenterOfMass(force);
	}
	
	public void applyRelativeForceToCenterOfMassStatic(final Vector3f _force) {
		if (this.rigidBody == null) {
			return;
		}
		final Vector3f force = this.rigidBody.getTransform().getOrientation().multiply(_force);
		
		this.staticForceApplyCenterOfMass = force;
	}
	
	/**
		 * @brief Apply an external torque to the body.
		 *        If the body is sleeping, calling this method will wake it up.
		 * @note The torque is apply with a relative axis of the object
		 * @note The force will we added to the sum of the applied torques and that this sum will be reset to zero at the end of each call of the DynamicsWorld::update() method. You can only apply a force to a dynamic body otherwise, this method will do nothing.
		 * @param[in] _torque The external torque to apply on the body
		 * @param[in] _static The torque will be apply while the user des not call the same function with 0 value ...
		 */
	public void applyRelativeTorque(final Vector3f _torque) {
		if (this.rigidBody == null) {
			return;
		}
		final Vector3f torque = this.rigidBody.getTransform().getOrientation().multiply(_torque);
		this.rigidBody.applyTorque(torque);
	}
	
	public void applyRelativeTorqueStatic(final Vector3f _torque) {
		if (this.rigidBody == null) {
			return;
		}
		final Vector3f torque = this.rigidBody.getTransform().getOrientation().multiply(_torque);
		this.staticTorqueApply = torque;
	}
	
	/**
		 * @brief Apply an external torque to the body.
		 *        If the body is sleeping, calling this method will wake it up.
		 * @note The force will we added to the sum of the applied torques and that this sum will be reset to zero at the end of each call of the DynamicsWorld::update() method. You can only apply a force to a dynamic body otherwise, this method will do nothing.
		 * @param[in] _torque The external torque to apply on the body
		 * @param[in] _static The torque will be apply while the user des not call the same function with 0 value ...
		 */
	public void applyTorque(final Vector3f _torque) {
		if (this.rigidBody == null) {
			return;
		}
		this.rigidBody.applyTorque(_torque);
	}
	
	public void applyTorqueStatic(final Vector3f _torque) {
		if (this.rigidBody == null) {
			return;
		}
		this.staticTorqueApply = _torque;
	}
	
	/**
	 * @brief Called when a new contact point is found between two bodies that were separated before.
	 * @param[in] _other The other component that have the impact
	 * @param[in] _normal Normal of the impact
	 * @param[in] _pos Position of the impact at the current object
	 * @param[in] _posOther Position of the impact at the other object
	 * @param[in] _penetrationDepth Depth penetration in the object
	 */
	public void beginContact(final Component _other, final Vector3f _normal, final Vector3f _pos, final Vector3f _posOther, final float _penetrationDepth) {
		Log.warning("    collision [BEGIN] " + _pos + " depth=" + _penetrationDepth);
	}
	
	public void drawShape(final ResourceColored3DObject _draw, final Camera _camera) {
		final Transform3D transform = getTransform();
		//final float[] mmm = new float[16];
		// Get the OpenGL matrix array of the transform 
		final Matrix4f mmm = transform.getOpenGLMatrix();
		
		final Matrix4f transformationMatrix = mmm.clone();
		transformationMatrix.transpose();
		final Color tmpColor = new Color(1.0f, 0.0f, 0.0f, 0.3f);
		for (final Shape it : this.shape) {
			if (it.isBox()) {
				Log.debug("    Box");
				final Box tmpElement = (Box) it;
				final Transform3D transformLocal = new Transform3D(it.getOrigin(), it.getOrientation());
				
				Matrix4f transformationMatrixLocal = transformLocal.getOpenGLMatrix();
				transformationMatrixLocal.transpose();
				transformationMatrixLocal = transformationMatrix.multiplyNew(transformationMatrixLocal);
				_draw.drawSquare(tmpElement.getSize(), transformationMatrixLocal, tmpColor);
			} else if (it.isCylinder()) {
				Log.debug("    Cylinder");
				final Cylinder tmpElement = (Cylinder) it;
				final Transform3D transformLocal = new Transform3D(it.getOrigin(), it.getOrientation());
				
				Matrix4f transformationMatrixLocal = transformLocal.getOpenGLMatrix();
				transformationMatrixLocal.transpose();
				transformationMatrixLocal = transformationMatrix.multiplyNew(transformationMatrixLocal);
				_draw.drawCylinder(tmpElement.getRadius(), tmpElement.getSize(), 10, 10, transformationMatrixLocal, tmpColor);
			} else if (it.isCapsule()) {
				Log.debug("    Capsule");
				final Capsule tmpElement = (Capsule) it;
				final Transform3D transformLocal = new Transform3D(it.getOrigin(), it.getOrientation());
				
				Matrix4f transformationMatrixLocal = transformLocal.getOpenGLMatrix();
				transformationMatrixLocal.transpose();
				transformationMatrixLocal = transformationMatrix.multiplyNew(transformationMatrixLocal);
				_draw.drawCapsule(tmpElement.getRadius(), tmpElement.getSize(), 10, 10, transformationMatrixLocal, tmpColor);
			} else if (it.isCone()) {
				Log.debug("    Cone");
				final Cone tmpElement = (Cone) it;
				final Transform3D transformLocal = new Transform3D(it.getOrigin(), it.getOrientation());
				
				Matrix4f transformationMatrixLocal = transformLocal.getOpenGLMatrix();
				transformationMatrixLocal.transpose();
				transformationMatrixLocal = transformationMatrix.multiplyNew(transformationMatrixLocal);
				_draw.drawCone(tmpElement.getRadius(), tmpElement.getSize(), 10, 10, transformationMatrixLocal, tmpColor);
			} else if (it.isSphere()) {
				
				Log.debug("    Sphere");
				final Sphere tmpElement = (Sphere) it;
				final Transform3D transformLocal = new Transform3D(it.getOrigin(), it.getOrientation());
				
				Matrix4f transformationMatrixLocal = transformLocal.getOpenGLMatrix();
				transformationMatrixLocal.transpose();
				transformationMatrixLocal = transformationMatrix.multiplyNew(transformationMatrixLocal);
				_draw.drawSphere(tmpElement.getRadius(), 10, 10, transformationMatrixLocal, tmpColor);
			} else if (it.isConcave()) {
				
				Log.debug("    concave");
				final Concave tmpElement = (Concave) it;
				final Transform3D transformLocal = new Transform3D(it.getOrigin(), it.getOrientation());
				
				final Matrix4f transformationMatrixLocal = transformLocal.getOpenGLMatrix();
				transformationMatrixLocal.transpose();
				transformationMatrixLocal.multiply(transformationMatrixLocal);
				
				_draw.drawTriangles(tmpElement.getVertex(), tmpElement.getIndices(), transformationMatrixLocal, tmpColor);
			} else if (it.isConvexHull()) {
				Log.debug("    convexHull");
				final ConvexHull tmpElement = (ConvexHull) it;
				break;
			}
		}
	}
	
	// call done after all cycle update of the physical engine
	public void emitAll() {
		// emit onbly of new ...
		final Transform3D transform = getTransform();
		if (this.lastTransformEmit != transform) {
			this.lastTransformEmit = transform;
			//TODO Later ... signalPosition.emit(transform);
		}
	}
	
	public void generate() {
		if (this.shape.size() == 0) {
			Log.warning("No Shape Availlable ...");
			return;
		}
		
		// TODO: support more than one shape for each elements... (ProxyShape)
		for (final Shape it : this.shape) {
			if (it == null) {
				continue;
			}
			if (it.isBox()) {
				Log.debug("    Box");
				final Box tmpElement = (Box) it;
				// Half extents of the box in the x, y and z directions
				final Vector3f halfExtents = new Vector3f(tmpElement.getSize().x, tmpElement.getSize().y, tmpElement.getSize().z);
				// Create the box shape
				final BoxShape shape = new BoxShape(halfExtents, 0.0001f);
				this.listShape.add(shape);
				// The ephysic use Y as UP ==> ege use Z as UP
				//orientation = orientation * ephysics::Quaternion(-0.707107, 0, 0, 0.707107);
				final Transform3D transform = new Transform3D(it.getOrigin(), it.getOrientation());
				final ProxyShape proxyShape = this.rigidBody.addCollisionShape(shape, transform, it.getMass());
				proxyShape.setUserData(this);
				this.listProxyShape.add(proxyShape);
			} else if (it.isCylinder()) {
				Log.debug("    Cylinder");
				final Cylinder tmpElement = (Cylinder) it;
				// Create the Cylinder shape
				// Create the Cylinder shape
				final CylinderShape shape = new CylinderShape(tmpElement.getRadius(), tmpElement.getSize());
				// The ephysic use Y as UP ==> ege use Z as UP
				final Quaternion orientation = it.getOrientation().multiplyNew(new Quaternion(-0.707107f, 0.0f, 0.0f, 0.707107f));
				final Transform3D transform = new Transform3D(it.getOrigin(), orientation);
				final ProxyShape proxyShape = this.rigidBody.addCollisionShape(shape, transform, it.getMass());
				proxyShape.setUserData(this);
				this.listProxyShape.add(proxyShape);
			} else if (it.isCapsule()) {
				Log.debug("    Capsule");
				final Capsule tmpElement = (Capsule) it;
				// Create the Capsule shape
				final CapsuleShape shape = new CapsuleShape(tmpElement.getRadius(), tmpElement.getSize());
				// The ephysic use Y as UP ==> ege use Z as UP
				final Quaternion orientation = it.getOrientation().multiplyNew(new Quaternion(-0.707107f, 0.0f, 0.0f, 0.707107f));
				final Transform3D transform = new Transform3D(it.getOrigin(), orientation);
				final ProxyShape proxyShape = this.rigidBody.addCollisionShape(shape, transform, it.getMass());
				proxyShape.setUserData(this);
				this.listProxyShape.add(proxyShape);
			} else if (it.isCone()) {
				Log.debug("    Cone");
				final Cone tmpElement = (Cone) it;
				// Create the Cone shape
				final ConeShape shape = new ConeShape(tmpElement.getRadius(), tmpElement.getSize());
				// The ephysic use Y as UP ==> ege use Z as UP
				final Quaternion orientation = it.getOrientation().multiplyNew(new Quaternion(-0.707107f, 0.0f, 0.0f, 0.707107f));
				final Transform3D transform = new Transform3D(it.getOrigin(), orientation);
				final ProxyShape proxyShape = this.rigidBody.addCollisionShape(shape, transform, it.getMass());
				proxyShape.setUserData(this);
				this.listProxyShape.add(proxyShape);
			} else if (it.isSphere()) {
				Log.debug("    Sphere");
				final Sphere tmpElement = (Sphere) it;
				// Create the box shape
				final SphereShape shape = new SphereShape(tmpElement.getRadius());
				// The ephysic use Y as UP ==> ege use Z as UP
				final Quaternion orientation = it.getOrientation().multiplyNew(new Quaternion(-0.707107f, 0.0f, 0.0f, 0.707107f));
				final Transform3D transform = new Transform3D(it.getOrigin(), orientation);
				final ProxyShape proxyShape = this.rigidBody.addCollisionShape(shape, transform, it.getMass());
				proxyShape.setUserData(this);
				this.listProxyShape.add(proxyShape);
			} else if (it.isConcave()) {
				Log.debug("    Concave");
				final Concave tmpElement = (Concave) it;
				//static  etk::Vector<Vector3f> vertices = {Vector3f(-100.0f,-100.0f,-50.0f),Vector3f(100.0f,-100.0f,-50.0f),Vector3f(100.0f,100.0f,-50.0f)};
				//static  etk::Vector<uint32_t> indices = {0,1,2};
				
				//ephysics::TriangleVertexArray* triangleArray = ETK_NEW(ephysics::TriangleVertexArray, vertices, indices);
				final TriangleVertexArray triangleArray = new TriangleVertexArray(tmpElement.getVertex(), tmpElement.getIndices());
				// Now that we have a TriangleVertexArray, we need to create a TriangleMesh and add the TriangleVertexArray into it as a subpart.
				// Once this is done, we can create the actual ConcaveMeshShape and add it to the body we want to simulate as in the following example:
				final TriangleMesh triangleMesh = new TriangleMesh();
				// Add the triangle vertex array to the triangle mesh
				triangleMesh.addSubpart(triangleArray);
				// Create the concave mesh shape
				// TODO : Manage memory leak ...
				final ConcaveShape shape = new ConcaveMeshShape(triangleMesh);
				// The ephysic use Y as UP ==> ege use Z as UP
				final Quaternion orientation = it.getOrientation().multiplyNew(new Quaternion(-0.707107f, 0.0f, 0.0f, 0.707107f));
				final Transform3D transform = new Transform3D(it.getOrigin(), it.getOrientation());
				final ProxyShape proxyShape = this.rigidBody.addCollisionShape(shape, transform, it.getMass());
				proxyShape.setUserData(this);
				this.listProxyShape.add(proxyShape);
			} else {
				Log.debug("    ???");
				// TODO: UNKNOW type ...
			}
		}
	}
	
	/**
	 * @brief Get the angular velocity (whole world).
	 * @return The angular velocity vector of the body
	 */
	public Vector3f getAngularVelocity() {
		if (this.rigidBody == null) {
			return new Vector3f(0, 0, 0);
		}
		return this.rigidBody.getAngularVelocity();
	}
	
	/**
	 * @brief Get the linear velocity (whole world).
	 * @return The linear velocity vector of the body
	 */
	public Vector3f getLinearVelocity() {
		if (this.rigidBody == null) {
			return new Vector3f(0, 0, 0);
		}
		return this.rigidBody.getLinearVelocity();
	}
	
	/**
	 * @brief Get the angular velocity (local Body).
	 * @return The angular velocity vector of the body
	 */
	public Vector3f getRelativeAngularVelocity() {
		if (this.rigidBody == null) {
			return new Vector3f(0, 0, 0);
		}
		final Vector3f value = this.rigidBody.getAngularVelocity();
		return this.rigidBody.getTransform().getOrientation().inverseNew().multiply(value);
	}
	
	/**
	 * @brief Get the linear velocity (local Body).
	 * @return The linear velocity vector of the body
	 */
	public Vector3f getRelativeLinearVelocity() {
		if (this.rigidBody == null) {
			return new Vector3f(0, 0, 0);
		}
		final Vector3f value = this.rigidBody.getLinearVelocity();
		return this.rigidBody.getTransform().getOrientation().inverseNew().multiply(value);
	}
	
	public List<Shape> getShape() {
		return this.shape;
	}
	
	/**
	 * @brief set a new transformation
	 * @return Transformation of the position
	 */
	public Transform3D getTransform() {
		if (this.rigidBody == null) {
			return Transform3D.identity();
		}
		return this.rigidBody.getTransform();
	}
	
	@Override
	public String getType() {
		return "physics";
	}
	
	/**
	 * @brief Called when a new contact point is found between two bodies.
	 * @param[in] _other The other component that have the impact
	 * @param[in] _normal Normal of the impact
	 * @param[in] _pos Position of the impact at the current object
	 * @param[in] _posOther Position of the impact at the other object
	 * @param[in] _penetrationDepth Depth penetration in the object
	 */
	public void newContact(final Component _other, final Vector3f _normal, final Vector3f _pos, final Vector3f _posOther, final float _penetrationDepth) {
		Log.warning("    collision [ NEW ] " + _pos + " depth=" + _penetrationDepth);
	}
	
	public void renderDebug(final ResourceColored3DObject _draw, final Camera _camera) {
		if (this.rigidBody == null) {
			return;
		}
		final Matrix4f transformationMatrix = Matrix4f.identity();
		final Color tmpColor = new Color(0.0f, 1.0f, 0.0f, 0.8f);
		final AABB value = this.rigidBody.getAABB();
		_draw.drawCubeLine(value.getMin(), value.getMax(), tmpColor, transformationMatrix, true, true);
		
	}
	
	/**
		 * @brief Set the angular velocity (whole world).
		 * @param[in] _linearVelocity The angular velocity vector of the body
		 */
	public void setAngularVelocity(final Vector3f _angularVelocity) {
		if (this.rigidBody == null) {
			return;
		}
		this.rigidBody.setAngularVelocity(_angularVelocity);
	}
	
	public void setBodyType(final PhysicBodyType _type) {
		if (this.rigidBody == null) {
			return;
		}
		switch (_type) {
			case BODY_STATIC:
				this.rigidBody.setType(BodyType.STATIC);
				break;
			case BODY_KINEMATIC:
				this.rigidBody.setType(BodyType.KINEMATIC);
				break;
			case BODY_DYNAMIC:
				this.rigidBody.setType(BodyType.DYNAMIC);
				break;
		}
	}
	
	/**
	 * @brief Set the linear velocity (whole world).
	 * @param[in] _linearVelocity The linear velocity vector of the body
	 */
	public void setLinearVelocity(final Vector3f _linearVelocity) {
		if (this.rigidBody == null) {
			return;
		}
		this.rigidBody.setLinearVelocity(_linearVelocity);
	}
	
	/**
		 * @brief Set the angular velocity (local Body).
		 * @param[in] _linearVelocity The angular velocity vector of the body
		 */
	public void setRelativeAngularVelocity(final Vector3f _angularVelocity) {
		if (this.rigidBody == null) {
			return;
		}
		final Vector3f value = this.rigidBody.getTransform().getOrientation().multiply(_angularVelocity);
		this.rigidBody.setAngularVelocity(value);
	}
	
	/**
		 * @brief Set the linear velocity (local Body).
		 * @param[in] _linearVelocity The linear velocity vector of the body
		 */
	public void setRelativeLinearVelocity(final Vector3f _linearVelocity) {
		if (this.rigidBody == null) {
			return;
		}
		final Vector3f value = this.rigidBody.getTransform().getOrientation().multiply(_linearVelocity);
		this.rigidBody.setLinearVelocity(value);
	}
	
	public void setShape(final List<Shape> _prop) {
		this.shape = _prop;
	}
	
	/**
	 * @brief set a new transformation
	 * @param[in] _transform transformation of the position
	 */
	public void setTransform(final Transform3D _transform) {
		if (this.rigidBody == null) {
			return;
		}
		this.rigidBody.setTransform(_transform);
	}
	
	// call of this function every time the call will be done
	public void update(final float _delta) {
		if (this.rigidBody == null) {
			return;
		}
		if (!this.staticForceApplyCenterOfMass.isZero()) {
			final Vector3f tmp = this.staticForceApplyCenterOfMass.multiplyNew(_delta);
			Log.error("FORCE : " + tmp);
			this.rigidBody.applyForceToCenterOfMass(tmp);
		}
		if (!this.staticTorqueApply.isZero()) {
			final Vector3f tmp = this.staticTorqueApply.multiplyNew(_delta);
			Log.error("TORQUE : " + tmp);
			this.rigidBody.applyTorque(tmp);
		}
	}
}

//	
//	
//	private List<Shape> shapes = new ArrayList<>();
//	//public esignal::Signal<Transform3D> signalPosition;
//	protected Transform3D lastTransformEmit;
//	protected RigidBody rigidBody;
//	List<CollisionShape> listShape = new ArrayList<>();
//	List<ProxyShape> listProxyShape = new ArrayList<>();
//	protected boolean isStatic = false;
//	Vector3f staticForceApplyCenterOfMass = new Vector3f(0, 0, 0);
//	Vector3f staticTorqueApply = new Vector3f(0, 0, 0);
//	
//	/**
//	 * @brief Create a basic position component (no orientation and position (0,0,0))
//	 */
//	public ComponentPhysics(final boolean isStatic) {
//		this.isStatic = isStatic;
//		this.lastTransformEmit = new Transform3D(new Vector3f(0, 0, 0), Quaternion.identity());
//	}
//	
//	public ComponentPhysics(final boolean isStatic, final Transform3D _transform) {
//		this.isStatic = isStatic;
//		this.lastTransformEmit = _transform;
//	}
//	
//	@Override
//	public void addFriendComponent(final Component component) {
//		if (component.getType().contentEquals("position")) {
//			final ComponentPosition position = (ComponentPosition) component;
//		}
//	}
//	
//	/**
//	 * @brief Create a basic position component
//	 * @param[in] _transform transformation of the position
//	 */
//	/*
//	public ComponentPhysics(Environement _env, Transform3D _transform) {
//	    this.engine = ememory::dynamicPointerCast<ege::physics::Engine>(_env.getEngine(getType()));
//		// Create a rigid body in the world
//		this.rigidBody = this.engine.getDynamicWorld().createRigidBody(_transform);
//		this.rigidBody.setUserData(this);
//		this.lastTransformEmit = _transform;
//		// set collision callback:
//		//this.engine.getDynamicWorld().testCollision(this.rigidBody, this);
//		Log.error("Bounciness=" + this.rigidBody.getMaterial().getBounciness());
//		Log.error("FrictionCoefficient=" + this.rigidBody.getMaterial().getFrictionCoefficient());
//		Log.error("RollingResistance=" + this.rigidBody.getMaterial().getRollingResistance());
//		Log.error("LinearDamping=" + this.rigidBody.getLinearDamping());
//		Log.error("AngularDamping=" + this.rigidBody.getAngularDamping());
//		this.rigidBody.getMaterial().setBounciness(0.4);
//		//this.rigidBody.getMaterial().setFrictionCoefficient(0.01);
//		//this.rigidBody.getMaterial().setRollingResistance(0.01);
//		this.rigidBody.setAngularDamping(0.9);
//		this.rigidBody.setLinearDamping(0.9);
//	}
//	*/
//	
//	//on close:
//	/*
//	~Component() {
//		if (this.rigidBody == null) {
//			return;
//		}
//		// disable callback
//		this.rigidBody.setUserData(null);
//		this.engine.getDynamicWorld().testCollision(this.rigidBody, null);
//		this.engine.getDynamicWorld().destroyRigidBody(this.rigidBody);
//		this.rigidBody = null;
//	}
//	*/
//	
//	public void addShape(final Shape _shape) {
//		this.shapes.add(_shape);
//	}
//	
//	/**
//	 * @brief Apply an external force to the body at a given point (in world-space coordinates).
//	 *        If the point is not at the center of mass of the body, it will also generate some torque and therefore, change the angular velocity of the body.
//	 *        If the body is sleeping, calling this method will wake it up. Note that the force will we added to the sum of the applied forces and that this sum will be reset to zero at the end of each call of the DynamicsWorld::update() method. You can only apply a force to a dynamic body otherwise, this method will do nothing.
//	 * @param[in] _force The force to apply on the body
//	 * @param[in] _point The point where the force is applied (in world-space coordinates)
//	 */
//	public void applyForce(final Vector3f _force, final Vector3f _point) {
//		if (this.rigidBody == null) {
//			return;
//		}
//		this.rigidBody.applyForce(_force, _point);
//	}
//	
//	/**
//	 * @brief Apply an external force to the body at its center of mass.
//	 *        If the body is sleeping, calling this method will wake it up.
//	 * @note The force will we added to the sum of the applied forces and that this sum will be reset to zero at the end of each call of the DynamicsWorld::update() method. You can only apply a force to a dynamic body otherwise, this method will do nothing.
//	 * @param[in] _force The external force to apply on the center of mass of the body
//	 * @param[in] _static The torque will be apply while the user des not call the same function with 0 value ...
//	 */
//	public void applyForceToCenterOfMass(final Vector3f _force, final boolean _static) {
//		if (this.rigidBody == null) {
//			return;
//		}
//		if (_static == true) {
//			this.staticForceApplyCenterOfMass = _force;
//		} else {
//			//this.rigidBody.applyForceToCenterOfMass(_force);
//			Log.todo("applyForceToCenterOfMass");
//		}
//	}
//	
//	/**
//	 * @brief Apply an external force to the body at its center of mass.
//	 *        If the body is sleeping, calling this method will wake it up.
//	 * @note The force is apply with a relative axis of the object
//	 * @note The force will we added to the sum of the applied forces and that this sum will be reset to zero at the end of each call of the DynamicsWorld::update() method. You can only apply a force to a dynamic body otherwise, this method will do nothing.
//	 * @param[in] _force The external force to apply on the center of mass of the body
//	 * @param[in] _static The torque will be apply while the user des not call the same function with 0 value ...
//	 */
//	public void applyRelativeForceToCenterOfMass(final Vector3f _force, final boolean _static) {
//		if (this.rigidBody == null) {
//			return;
//		}
//		final Vector3f force = this.rigidBody.getTransform().getOrientation().multiply(_force);
//		if (_static == true) {
//			this.staticForceApplyCenterOfMass = force;
//		} else {
//			//this.rigidBody.applyForceToCenterOfMass(force);
//			Log.todo("applyForceToCenterOfMass");
//		}
//	}
//	
//	/**
//	 * @brief Apply an external torque to the body.
//	 *        If the body is sleeping, calling this method will wake it up.
//	 * @note The torque is apply with a relative axis of the object
//	 * @note The force will we added to the sum of the applied torques and that this sum will be reset to zero at the end of each call of the DynamicsWorld::update() method. You can only apply a force to a dynamic body otherwise, this method will do nothing.
//	 * @param[in] _torque The external torque to apply on the body
//	 * @param[in] _static The torque will be apply while the user des not call the same function with 0 value ...
//	 */
//	public void applyRelativeTorque(final Vector3f _torque, final boolean _static) {
//		if (this.rigidBody == null) {
//			return;
//		}
//		final Vector3f torque = this.rigidBody.getTransform().getOrientation().multiply(_torque);
//		if (_static == true) {
//			this.staticTorqueApply = torque;
//		} else {
//			this.rigidBody.applyTorque(torque);
//		}
//	}
//	
//	/**
//	 * @brief Apply an external torque to the body.
//	 *        If the body is sleeping, calling this method will wake it up.
//	 * @note The force will we added to the sum of the applied torques and that this sum will be reset to zero at the end of each call of the DynamicsWorld::update() method. You can only apply a force to a dynamic body otherwise, this method will do nothing.
//	 * @param[in] _torque The external torque to apply on the body
//	 * @param[in] _static The torque will be apply while the user des not call the same function with 0 value ...
//	 */
//	public void applyTorque(final Vector3f _torque, final boolean _static) {
//		if (this.rigidBody == null) {
//			return;
//		}
//		if (_static == true) {
//			this.staticTorqueApply = _torque;
//		} else {
//			this.rigidBody.applyTorque(_torque);
//		}
//	}
//	
//	/**
//	 * @brief Called when a new contact point is found between two bodies that were separated before.
//	 * @param[in] _other The other component that have the impact
//	 * @param[in] _normal Normal of the impact
//	 * @param[in] _pos Position of the impact at the current object
//	 * @param[in] _posOther Position of the impact at the other object
//	 * @param[in] _penetrationDepth Depth penetration in the object
//	 */
//	private void beginContact(final Component _other, final Vector3f _normal, final Vector3f _pos, final Vector3f _posOther, final float _penetrationDepth) {
//		Log.warning("    collision [BEGIN] " + _pos + " depth=" + _penetrationDepth);
//	}
//	
//	public void clearShape() {
//		this.shapes.clear();
//	}
//	
//	protected RigidBody createRigidBody(final CollisionShape collisionShape, final Transform3D transform, final float mass, final DynamicsWorld dynamicsWorld) {
//		
//		//this.collisionShape = collisionShape;
//		
//		final Matrix3f inertiaTensor = new Matrix3f();
//		collisionShape.computeLocalInertiaTensor(inertiaTensor, mass);
//		
//		return dynamicsWorld.createRigidBody(transform, mass, inertiaTensor, collisionShape);
//	}
//	
//	public void drawAABB(final ResourceColored3DObject debugDrawProperty) {
//		if (this.rigidBody == null) {
//			return;
//		}
//		/*
//		Matrix4f transformationMatrix;
//		Color<float> tmpColor(0.0, 1.0, 0.0, 0.8);
//		ephysics::AABB value = this.rigidBody.getAABB();
//		_draw.drawCubeLine(value.getMin(), value.getMax(), tmpColor, transformationMatrix);
//		*/
//	}
//	
//	// call done after all cycle update of the physical engine
//	public void emitAll() {
//		// emit onbly of new ...
//		final Transform3D transform = getTransform();
//		if (this.lastTransformEmit != transform) {
//			this.lastTransformEmit = transform;
//			//signalPosition.emit(transform);
//		}
//	}
//	
//	public void generate(final DynamicsWorld dynamicsWorld) {
//		if (this.shapes.size() == 0) {
//			Log.warning("No Shape Availlable ...");
//			return;
//		}
//		for (final Shape it : this.shapes) {
//			if (it == null) {
//				continue;
//			}
//			if (it.isBox()) {
//				Log.debug("    Box");
//				final Box tmpElement = (Box) it;
//				// Half extents of the box in the x, y and z directions
//				final Vector3f halfExtents = new Vector3f(tmpElement.getSize().x, tmpElement.getSize().y, tmpElement.getSize().z);
//				// Create the box shape
//				final BoxShape shape = new BoxShape(halfExtents, 0.0001f);
//				//this.listShape.add(shape);
//				// The ephysic use Y as UP ==> ege use Z as UP
//				//orientation = orientation.multiplyNew(new Quaternion(-0.707107, 0, 0, 0.707107));
//				final Transform3D transform = new Transform3D(it.getOrigin(), it.getOrientation());
//				final ProxyShape proxyShape = this.rigidBody.addCollisionShape(shape, transform, it.getMass());
//				proxyShape.setUserData(this);
//				this.listProxyShape.add(proxyShape);
//				
//				this.rigidBody = createRigidBody(shape, transform, it.getMass(), dynamicsWorld);
//				proxyShape.setUserData(this);
//				//this.rigidBody.getMaterial().setBounciness(0.4f);
//				//this.rigidBody.setAngularDamping(0.9f);
//				//this.rigidBody.setLinearDamping(0.9f);
//			} else if (it.isCylinder()) {
//				Log.debug("    Cylinder");
//				final Cylinder tmpElement = (Cylinder) it;
//				// Create the Cylinder shape
//				final CylinderShape shape = new CylinderShape(tmpElement.getRadius(), tmpElement.getSize(), 0.0001f);
//				
//				// The ephysic use Y as UP ==> ege use Z as UP
//				//orientation = orientation.multiplyNew(new Quaternion(-0.707107, 0, 0, 0.707107));
//				final Transform3D transform = new Transform3D(it.getOrigin(), it.getOrientation());
//				//ProxyShape proxyShape = this.rigidBody.addCollisionShape(shape, transform, it.getMass());
//				this.rigidBody = createRigidBody(shape, transform, it.getMass(), dynamicsWorld);
//				//this.colisionShape = shape;
//				
//				// set it enable in the system:
//				//this.rigidBody.setIsMotionEnabled(true);
//				// Change the material properties of the rigid body
//				final Material material = this.rigidBody.getMaterial();
//				material.setBounciness(0.2f);
//			} else if (it.isCapsule()) {
//				Log.debug("    Capsule");
//				/*
//				final Capsule tmpElement = (Capsule) it;
//				
//				// Create the Capsule shape
//				final CapsuleShape shape = new CapsuleShape(tmpElement.getRadius(), tmpElement.getSize(), 0.0001f);
//				
//				// The ephysic use Y as UP ==> ege use Z as UP
//				//orientation = orientation.multiplyNew(new Quaternion(-0.707107, 0, 0, 0.707107));
//				final Transform3D transform = new Transform3D(it.getOrigin(), it.getOrientation());
//				//ProxyShape proxyShape = this.rigidBody.addCollisionShape(shape, transform, it.getMass());
//				this.rigidBody = createRigidBody(shape, transform, it.getMass(), dynamicsWorld);
//				//this.colisionShape = shape;
//				
//				// set it enable in the system:
//				//this.rigidBody.isMotionEnabled(true);
//				// Change the material properties of the rigid body
//				final Material material = this.rigidBody.getMaterial();
//				material.setBounciness(0.2f);
//				*/
//			} else if (it.isCone()) {
//				/*
//				Log.debug("    Cone");
//				final Cone tmpElement = (Cone) it;
//				final ConeShape shape = new ConeShape(tmpElement.getRadius(), tmpElement.getSize(), 0.0001f);
//				// The ephysic use Y as UP ==> ege use Z as UP
//				//orientation = orientation.multiplyNew(new Quaternion(-0.707107, 0, 0, 0.707107));
//				final Transform3D transform = new Transform3D(it.getOrigin(), it.getOrientation());
//				final ProxyShape proxyShape = this.rigidBody.addCollisionShape(shape, transform, it.getMass());
//				proxyShape.setUserData(this);
//				this.listProxyShape.add(proxyShape);
//				this.rigidBody = createRigidBody(shape, transform, it.getMass(), dynamicsWorld);
//				//this.colisionShape = shape;
//				
//				// set it enable in the system:
//				//this.rigidBody.setIsMotionEnabled(true);
//				// Change the material properties of the rigid body
//				final Material material = this.rigidBody.getMaterial();
//				material.setBounciness(0.2f);
//				*/
//			} else if (it.isSphere()) {
//				/*
//				Log.debug("    Sphere");
//				final Sphere tmpElement = (Sphere) it;
//				final SphereShape shape = new SphereShape(tmpElement.getRadius(), 0.0001f);
//				// The ephysic use Y as UP ==> ege use Z as UP
//				//orientation = orientation.multiplyNew(new Quaternion(-0.707107, 0, 0, 0.707107));
//				final Transform3D transform = new Transform3D(it.getOrigin(), it.getOrientation());
//				//ProxyShape proxyShape = this.rigidBody.addCollisionShape(shape, transform, it.getMass());
//				this.rigidBody = createRigidBody(shape, transform, it.getMass(), dynamicsWorld);
//				this.colisionShape = shape;
//				
//				// set it enable in the system:
//				this.rigidBody.setIsMotionEnabled(true);
//				// Change the material properties of the rigid body
//				final Material material = this.rigidBody.getMaterial();
//				material.setBounciness(0.2f);
//				*/
//			} else if (it.isConcave()) {
//				Log.debug("    Concave");
//				final Concave tmpElement = (Concave) it;
//				/*
//						static  Vector<Vector3f> vertices = {Vector3f(-100.0f,-100.0f,-50.0f),Vector3f(100.0f,-100.0f,-50.0f),Vector3f(100.0f,100.0f,-50.0f)};
//						static  Vector<uint32_t> indices = {0,1,2};
//						
//						ephysics::TriangleVertexArray* triangleArray = ETK_NEW(ephysics::TriangleVertexArray, vertices, indices);
//						#else
//						ephysics::TriangleVertexArray* triangleArray = ETK_NEW(ephysics::TriangleVertexArray, tmpElement.getVertex(), tmpElement.getIndices());
//						#endif
//						// Now that we have a TriangleVertexArray, we need to create a TriangleMesh and add the TriangleVertexArray into it as a subpart.
//						// Once this is done, we can create the actual ConcaveMeshShape and add it to the body we want to simulate as in the following example:
//						ephysics::TriangleMesh* triangleMesh = ETK_NEW(ephysics::TriangleMesh);
//						// Add the triangle vertex array to the triangle mesh
//						triangleMesh.addSubpart(triangleArray);
//						// Create the concave mesh shape
//						ephysics::ConcaveShape* shape = ETK_NEW(ephysics::ConcaveMeshShape, triangleMesh);
//						// The ephysic use Y as UP ==> ege use Z as UP
//						Quaternion orientation = it.getOrientation() * Quaternion(-0.707107, 0, 0, 0.707107);
//						Transform3D transform(it.getOrigin(), it.getOrientation());
//						ephysics::ProxyShape* proxyShape = this.rigidBody.addCollisionShape(shape, transform, it.getMass());
//						proxyShape.setUserData(this);
//						this.listProxyShape.pushBack(proxyShape);
//						break;
//				*/
//			} else {
//				Log.debug("    ???");
//			}
//		}
//	}
//	
//	/**
//	 * @brief Get the angular velocity (whole world).
//	 * @return The angular velocity vector of the body
//	 */
//	public Vector3f getAngularVelocity() {
//		if (this.rigidBody == null) {
//			return new Vector3f(0, 0, 0);
//		}
//		return this.rigidBody.getAngularVelocity();
//	}
//	
//	/**
//	 * @brief Get the linear velocity (whole world).
//	 * @return The linear velocity vector of the body
//	 */
//	public Vector3f getLinearVelocity() {
//		if (this.rigidBody == null) {
//			return new Vector3f(0, 0, 0);
//		}
//		return this.rigidBody.getLinearVelocity();
//	}
//	
//	/**
//	 * @brief Get the angular velocity (local Body).
//	 * @return The angular velocity vector of the body
//	 */
//	public Vector3f getRelativeAngularVelocity() {
//		if (this.rigidBody == null) {
//			return new Vector3f(0, 0, 0);
//		}
//		final Vector3f value = this.rigidBody.getAngularVelocity();
//		return this.rigidBody.getTransform().getOrientation().inverseNew().multiply(value);
//	}
//	
//	/**
//	 * @brief Get the linear velocity (local Body).
//	 * @return The linear velocity vector of the body
//	 */
//	public Vector3f getRelativeLinearVelocity() {
//		if (this.rigidBody == null) {
//			return new Vector3f(0, 0, 0);
//		}
//		final Vector3f value = this.rigidBody.getLinearVelocity();
//		return this.rigidBody.getTransform().getOrientation().inverseNew().multiply(value);
//	}
//	
//	public List<Shape> getShape() {
//		return this.shapes;
//	}
//	
//	/**
//	 * @brief set a new transformation
//	 * @return Transformation of the position
//	 */
//	public Transform3D getTransform() {
//		if (this.rigidBody == null) {
//			return Transform3D.identity();
//		}
//		return this.rigidBody.getTransform();
//	}
//	
//	@Override
//	public String getType() {
//		return "physics";
//	}
//	
//	/**
//	 * @brief Called when a new contact point is found between two bodies.
//	 * @param[in] _other The other component that have the impact
//	 * @param[in] _normal Normal of the impact
//	 * @param[in] _pos Position of the impact at the current object
//	 * @param[in] _posOther Position of the impact at the other object
//	 * @param[in] _penetrationDepth Depth penetration in the object
//	 */
//	private void newContact(final Component _other, final Vector3f _normal, final Vector3f _pos, final Vector3f _posOther, final float _penetrationDepth) {
//		Log.warning("    collision [ NEW ] " + _pos + " depth=" + _penetrationDepth);
//	}
//	
//	@Override
//	public void removeFriendComponent(final Component component) {
//		// nothing to do.
//	}
//	
//	public void renderDebug(final ResourceColored3DObject debugDrawProperty) {
//		final Color displayColor;
//		/*
//		if (this.aabbIntersection.size() == 0) {
//			displayColor = new Color(1,1,1,1);
//		} else {
//			if (this.narrowIntersection.size() == 0) {
//				displayColor = new Color(1,1,0,1);
//			} else {
//				displayColor = new Color(1,0,0,1);
//			}
//		}
//		if (aabb != null) {
//			debugDrawProperty.drawCubeLine(aabb.getMin(), aabb.getMax(), displayColor, Matrix4f.identity(), true, true);
//			//debugDrawProperty.drawCubeLine(new Vector3f(0,0,0), new Vector3f(32,32,32), new Color(1,0,1,1), Matrix4f.identity(), true, true);
//		} else {
//			Log.error("no AABB");
//		}
//		*/
//		for (final Shape shape : this.shapes) {
//			//shape.renderDebug(position.getTransform(), debugDrawProperty);
//		}
//		//}
//		//public void drawShape(ewol::resource::Colored3DObject _draw, ege::Camera _camera) {
//		/*
//		Transform3D transform = getTransform();
//		float mmm[16];
//		// Get the OpenGL matrix array of the transform 
//		transform.getOpenGLMatrix(mmm);
//		Matrix4f transformationMatrix(mmm);
//		transformationMatrix.transpose();
//		Color<float> tmpColor(1.0, 0.0, 0.0, 0.3);
//		for (auto &it: this.shape) {
//			if (it == null) {
//				continue;
//			}
//			switch (it.getType()) {
//				case ege::physics::Shape::type::box: {
//					Log.debug("    Box");
//					 ege::physics::shape::Box* tmpElement = it.toBox();
//					if (tmpElement == null) {
//						Log.error("    Box ==> can not cast in BOX");
//						continue;
//					}
//					Transform3D transformLocal(it.getOrigin(), it.getOrientation());
//					transformLocal.getOpenGLMatrix(mmm);
//					Matrix4f transformationMatrixLocal(mmm);
//					transformationMatrixLocal.transpose();
//					transformationMatrixLocal = transformationMatrix * transformationMatrixLocal;
//					_draw.drawSquare(tmpElement.getSize(), transformationMatrixLocal, tmpColor);
//					break;
//				}
//				case ege::physics::Shape::type::cylinder: {
//					Log.debug("    Cylinder");
//					 ege::physics::shape::Cylinder* tmpElement = it.toCylinder();
//					if (tmpElement == null) {
//						Log.error("    Cylinder ==> can not cast in Cylinder");
//						continue;
//					}
//					Transform3D transformLocal(it.getOrigin(), it.getOrientation());
//					transformLocal.getOpenGLMatrix(mmm);
//					Matrix4f transformationMatrixLocal(mmm);
//					transformationMatrixLocal.transpose();
//					transformationMatrixLocal = transformationMatrix * transformationMatrixLocal;
//					_draw.drawCylinder(tmpElement.getRadius(), tmpElement.getSize(), 10, 10, transformationMatrixLocal, tmpColor);
//					break;
//				}
//				case ege::physics::Shape::type::capsule: {
//					Log.debug("    Capsule");
//					 ege::physics::shape::Capsule* tmpElement = it.toCapsule();
//					if (tmpElement == null) {
//						Log.error("    Capsule ==> can not cast in Capsule");
//						continue;
//					}
//					Transform3D transformLocal(it.getOrigin(), it.getOrientation());
//					transformLocal.getOpenGLMatrix(mmm);
//					Matrix4f transformationMatrixLocal(mmm);
//					transformationMatrixLocal.transpose();
//					transformationMatrixLocal = transformationMatrix * transformationMatrixLocal;
//					_draw.drawCapsule(tmpElement.getRadius(), tmpElement.getSize(), 10, 10, transformationMatrixLocal, tmpColor);
//					break;
//				}
//				case ege::physics::Shape::type::cone: {
//					Log.debug("    Cone");
//					 ege::physics::shape::Cone* tmpElement = it.toCone();
//					if (tmpElement == null) {
//						Log.error("    Cone ==> can not cast in Cone");
//						continue;
//					}
//					Transform3D transformLocal(it.getOrigin(), it.getOrientation());
//					transformLocal.getOpenGLMatrix(mmm);
//					Matrix4f transformationMatrixLocal(mmm);
//					transformationMatrixLocal.transpose();
//					transformationMatrixLocal = transformationMatrix * transformationMatrixLocal;
//					_draw.drawCone(tmpElement.getRadius(), tmpElement.getSize(), 10, 10, transformationMatrixLocal, tmpColor);
//					break;
//				}
//				case ege::physics::Shape::type::sphere: {
//					Log.debug("    Sphere");
//					 ege::physics::shape::Sphere* tmpElement = it.toSphere();
//					if (tmpElement == null) {
//						Log.error("    Sphere ==> can not cast in Sphere");
//						continue;
//					}
//					Transform3D transformLocal(it.getOrigin(), it.getOrientation());
//					transformLocal.getOpenGLMatrix(mmm);
//					Matrix4f transformationMatrixLocal(mmm);
//					transformationMatrixLocal.transpose();
//					transformationMatrixLocal = transformationMatrix * transformationMatrixLocal;
//					_draw.drawSphere(tmpElement.getRadius(), 10, 10, transformationMatrixLocal, tmpColor);
//					break;
//				}
//				case ege::physics::Shape::type::concave: {
//					Log.debug("    concave");
//					 ege::physics::shape::Concave* tmpElement = it.toConcave();
//					if (tmpElement == null) {
//						Log.error("    concave ==> can not cast in convexHull");
//						continue;
//					}
//					Transform3D transformLocal(it.getOrigin(), it.getOrientation());
//					transformLocal.getOpenGLMatrix(mmm);
//					Matrix4f transformationMatrixLocal(mmm);
//					transformationMatrixLocal.transpose();
//					transformationMatrixLocal = transformationMatrix * transformationMatrixLocal;
//					
//					_draw.drawTriangles(tmpElement.getVertex(), tmpElement.getIndices(), transformationMatrixLocal, tmpColor);
//					break;
//				}
//				case ege::physics::Shape::type::convexHull: {
//					Log.debug("    convexHull");
//					 ege::physics::shape::ConvexHull* tmpElement = it.toConvexHull();
//					if (tmpElement == null) {
//						Log.error("    convexHull ==> can not cast in convexHull");
//						continue;
//					}
//					break;
//				}
//				default :
//					Log.debug("    ???");
//					break;
//			}
//		}
//		*/
//	}
//	
//	/**
//	 * @brief Set the angular velocity (whole world).
//	 * @param[in] _linearVelocity The angular velocity vector of the body
//	 */
//	public void setAngularVelocity(final Vector3f _angularVelocity) {
//		if (this.rigidBody == null) {
//			return;
//		}
//		this.rigidBody.setAngularVelocity(_angularVelocity);
//	}
//	
//	/**
//	 * @brief Set the linear velocity (whole world).
//	 * @param[in] _linearVelocity The linear velocity vector of the body
//	 */
//	public void setLinearVelocity(final Vector3f _linearVelocity) {
//		if (this.rigidBody == null) {
//			return;
//		}
//		this.rigidBody.setLinearVelocity(_linearVelocity);
//	};
//	
//	/**
//	 * @brief Set the angular velocity (local Body).
//	 * @param[in] _linearVelocity The angular velocity vector of the body
//	 */
//	public void setRelativeAngularVelocity(final Vector3f _angularVelocity) {
//		if (this.rigidBody == null) {
//			return;
//		}
//		final Vector3f value = this.rigidBody.getTransform().getOrientation().multiply(_angularVelocity);
//		this.rigidBody.setAngularVelocity(value);
//	}
//	
//	/**
//	 * @brief Set the linear velocity (local Body).
//	 * @param[in] _linearVelocity The linear velocity vector of the body
//	 */
//	public void setRelativeLinearVelocity(final Vector3f _linearVelocity) {
//		if (this.rigidBody == null) {
//			return;
//		}
//		final Vector3f value = this.rigidBody.getTransform().getOrientation().multiply(_linearVelocity);
//		this.rigidBody.setLinearVelocity(value);
//	}
//	
//	public void setShape(final List<Shape> _prop) {
//		this.shapes = _prop;
//	}
//	
//	/*
//	public enum type {
//		bodyDynamic;
//		bodyStatic;
//		bodyKinematic;
//	}
//	
//	public void setType(type _type) {
//		if (this.rigidBody == null) {
//			return;
//		}
//		switch(_type) {
//			case bodyStatic:
//				//rigidBody.setType(STATIC);
//				break;
//			case bodyKinematic:
//				//rigidBody.setType(KINEMATIC);
//				break;
//			case bodyDynamic:
//				//rigidBody.setType(DYNAMIC);
//				break;
//		}
//	}
//	*/
//	/**
//	 * @brief set a new transformation
//	 * @param[in] _transform transformation of the position
//	 */
//	public void setTransform(final Transform3D _transform) {
//		if (this.rigidBody == null) {
//			return;
//		}
//		this.rigidBody.setTransform(_transform);
//	}
//	
//	// call of this function every time the call will be done
//	private void update(final float _delta) {
//		if (this.rigidBody == null) {
//			return;
//		}
//		/*
//		if (this.staticForceApplyCenterOfMass != Vector3f(0,0,0)) {
//			Vector3f tmp = this.staticForceApplyCenterOfMass*_delta;
//			Log.error("FORCE : " + tmp );
//			this.rigidBody.applyForceToCenterOfMass(tmp);
//		}
//		if (this.staticTorqueApply != Vector3f(0,0,0)) {
//			Vector3f tmp = this.staticTorqueApply*_delta;
//			Log.error("TORQUE : " + tmp);
//			this.rigidBody.applyTorque(tmp);
//		}
//		*/
//	}
