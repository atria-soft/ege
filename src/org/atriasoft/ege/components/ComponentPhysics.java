package org.atriasoft.ege.components;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Environement;
import org.atriasoft.ege.Signal;
import org.atriasoft.ephysics.body.BodyType;
import org.atriasoft.ephysics.body.RigidBody;
import org.atriasoft.ephysics.collision.ProxyShape;
import org.atriasoft.ephysics.collision.TriangleMesh;
import org.atriasoft.ephysics.collision.TriangleVertexArray;
import org.atriasoft.ephysics.collision.shapes.AABB;
import org.atriasoft.ephysics.collision.shapes.BoxShape;
import org.atriasoft.ephysics.collision.shapes.CapsuleShape;
import org.atriasoft.ephysics.collision.shapes.CollisionShape;
import org.atriasoft.ephysics.collision.shapes.ConcaveMeshShape;
import org.atriasoft.ephysics.collision.shapes.ConcaveShape;
import org.atriasoft.ephysics.collision.shapes.ConeShape;
import org.atriasoft.ephysics.collision.shapes.CylinderShape;
import org.atriasoft.ephysics.collision.shapes.SphereShape;
import org.atriasoft.etk.Color;
import org.atriasoft.etk.math.Matrix4f;
import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Transform3D;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceColored3DObject;
import org.atriasoft.ege.camera.Camera;
import org.atriasoft.ege.engines.EnginePhysics;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.ege.physics.shape.Box;
import org.atriasoft.ege.physics.shape.Capsule;
import org.atriasoft.ege.physics.shape.Concave;
import org.atriasoft.ege.physics.shape.Cone;
import org.atriasoft.ege.physics.shape.ConvexHull;
import org.atriasoft.ege.physics.shape.Cylinder;
import org.atriasoft.ege.physics.shape.Shape;
import org.atriasoft.ege.physics.shape.Sphere;

public class ComponentPhysics extends Component {
	public Signal<Transform3D> signalPosition = new Signal<>();;
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
		this.rigidBody.getMaterial().setBounciness(0.0f);
		this.rigidBody.setAngularDamping(0.9f);
		this.rigidBody.setLinearDamping(0.9f);
		//this.rigidBody.getMaterial().setFrictionCoefficient(0.01f);
		//this.rigidBody.getMaterial().setRollingResistance(0.01f);
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
		this.rigidBody.getMaterial().setBounciness(0.0f);
		//this.rigidBody.getMaterial().setFrictionCoefficient(0.01f);
		//this.rigidBody.getMaterial().setRollingResistance(0.01f);
		this.rigidBody.setAngularDamping(0.9f);
		this.rigidBody.setLinearDamping(0.9f);
	}
	
	@Override
	public void addFriendComponent(final Component component) {
		if (component.getType().contains("position")) {
			Log.critical("Can not add a 'physic' component and a 'position' component ... ==> incompatible");
		}
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
			this.signalPosition.emit(transform);
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
	
	public void setAngularReactionEnable(final boolean value) {
		this.rigidBody.setAngularReactionEnable(value);
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
	
	public void setSleepingEnable(final boolean value) {
		this.rigidBody.setIsAllowedToSleep(value);
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
