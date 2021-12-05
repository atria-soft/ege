package org.atriasoft.phyligram;

import org.atriasoft.etk.math.Quaternion;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.phyligram.internal.Log;

// set the relevant elements of our oriented bounding box
class OBB {
	public Vector3f position;
	public Vector3f axisX;
	public Vector3f axisY;
	public Vector3f axisZ;
	public Vector3f halfSize;

	public OBB() {
		position = Vector3f.ZERO;
		axisX = Vector3f.ZERO;
		axisY = Vector3f.ZERO;
		axisZ = Vector3f.ZERO;
		halfSize = Vector3f.ZERO;
	}
};

public class ToolCollisionOBBWithOBB {
	private ToolCollisionOBBWithOBB() {}

	// check if there's a separating plane in between the selected axes
	private static boolean getSeparatingPlane(Vector3f rPos, Vector3f plane, OBB box1, OBB box2) {
		return (Math.abs(rPos.dot(plane)) > (Math.abs(box1.axisX.multiply(box1.halfSize.x()).dot(plane)) + Math.abs(box1.axisY.multiply(box1.halfSize.y()).dot(plane))
				+ Math.abs(box1.axisZ.multiply(box1.halfSize.z()).dot(plane)) + Math.abs(box2.axisX.multiply(box2.halfSize.x()).dot(plane))
				+ Math.abs(box2.axisY.multiply(box2.halfSize.y()).dot(plane)) + Math.abs(box2.axisZ.multiply(box2.halfSize.z()).dot(plane))));
	}

	// test for separating planes in all 15 axes
	public static boolean getCollision(OBB box1, OBB box2) {
		Vector3f rPos = box2.position.less(box1.position);
		boolean ret = getSeparatingPlane(rPos, box1.axisX, box1, box2) || getSeparatingPlane(rPos, box1.axisY, box1, box2) || getSeparatingPlane(rPos, box1.axisZ, box1, box2)
				|| getSeparatingPlane(rPos, box2.axisX, box1, box2) || getSeparatingPlane(rPos, box2.axisY, box1, box2) || getSeparatingPlane(rPos, box2.axisZ, box1, box2)
				|| getSeparatingPlane(rPos, box1.axisX.cross(box2.axisX), box1, box2) || getSeparatingPlane(rPos, box1.axisX.cross(box2.axisY), box1, box2)
				|| getSeparatingPlane(rPos, box1.axisX.cross(box2.axisZ), box1, box2) || getSeparatingPlane(rPos, box1.axisY.cross(box2.axisX), box1, box2)
				|| getSeparatingPlane(rPos, box1.axisY.cross(box2.axisY), box1, box2) || getSeparatingPlane(rPos, box1.axisY.cross(box2.axisZ), box1, box2)
				|| getSeparatingPlane(rPos, box1.axisZ.cross(box2.axisX), box1, box2) || getSeparatingPlane(rPos, box1.axisZ.cross(box2.axisY), box1, box2)
				|| getSeparatingPlane(rPos, box1.axisZ.cross(box2.axisZ), box1, box2);
		return !ret;
	}
//
//	// a quick test to see the code working
//	public static void main(String[] args) {
//		// create two obbs
//		OBB aaa = new OBB();
//		OBB bbb = new OBB();
//
//		// set the first obb's properties
//		aaa.position = new Vector3f(0.0f, 0.0f, 0.0f); // set its center position
//
//		// set the half size
//		aaa.halfSize = new Vector3f(10.0f, 1.0f, 1.0f);
//
//		// set the axes orientation
//		aaa.axisX = new Vector3f(1.0f, 0.0f, 0.0f);
//		aaa.axisY = new Vector3f(0.0f, 1.0f, 0.0f);
//		aaa.axisZ = new Vector3f(0.0f, 0.0f, 1.0f);
//
//		// set the second obb's properties
//		bbb.position = new Vector3f(20.0f, 0.0f, 0.0f); // set its center position
//
//		// set the half size
//		bbb.halfSize = new Vector3f(10.0f, 1.0f, 1.0f);
//
//		// set the axes orientation
//		bbb.axisX = new Vector3f(1.0f, 0.0f, 0.0f);
//		bbb.axisY = new Vector3f(0.0f, 1.0f, 0.0f);
//		bbb.axisZ = new Vector3f(0.0f, 0.0f, 1.0f);
//
//		// run the code and get the result as a message
//		if (getCollision(aaa, bbb)) {
//			Log.info("Collision!!!");
//		} else {
//			Log.info("NO Collision!!!");
//		}
//	}

	// check if there's a separating plane in between the selected axes
	private static boolean getSeparatingPlane222(Vector3f rPos, Vector3f plane, PhysicBox box1, PhysicBox box2) {
		return (Math.abs(rPos.dot(plane)) > (Math.abs(box1.narrowPhaseAxisX.multiply(box1.narrowPhaseHalfSize.x()).dot(plane))
				+ Math.abs(box1.narrowPhaseAxisY.multiply(box1.narrowPhaseHalfSize.y()).dot(plane)) + Math.abs(box1.narrowPhaseAxisZ.multiply(box1.narrowPhaseHalfSize.z()).dot(plane))
				+ Math.abs(box2.narrowPhaseAxisX.multiply(box2.narrowPhaseHalfSize.x()).dot(plane)) + Math.abs(box2.narrowPhaseAxisY.multiply(box2.narrowPhaseHalfSize.y()).dot(plane))
				+ Math.abs(box2.narrowPhaseAxisZ.multiply(box2.narrowPhaseHalfSize.z()).dot(plane))));
	}

	public static boolean testCollide(PhysicBox box1, PhysicBox box2) {

		Vector3f rPos = box2.narrowPhaseGlobalPos.less(box1.narrowPhaseGlobalPos);
		boolean ret = getSeparatingPlane222(rPos, box1.narrowPhaseAxisX, box1, box2) || getSeparatingPlane222(rPos, box1.narrowPhaseAxisY, box1, box2)
				|| getSeparatingPlane222(rPos, box1.narrowPhaseAxisZ, box1, box2) || getSeparatingPlane222(rPos, box2.narrowPhaseAxisX, box1, box2)
				|| getSeparatingPlane222(rPos, box2.narrowPhaseAxisY, box1, box2) || getSeparatingPlane222(rPos, box2.narrowPhaseAxisZ, box1, box2)
				|| getSeparatingPlane222(rPos, box1.narrowPhaseAxisX.cross(box2.narrowPhaseAxisX), box1, box2)
				|| getSeparatingPlane222(rPos, box1.narrowPhaseAxisX.cross(box2.narrowPhaseAxisY), box1, box2)
				|| getSeparatingPlane222(rPos, box1.narrowPhaseAxisX.cross(box2.narrowPhaseAxisZ), box1, box2)
				|| getSeparatingPlane222(rPos, box1.narrowPhaseAxisY.cross(box2.narrowPhaseAxisX), box1, box2)
				|| getSeparatingPlane222(rPos, box1.narrowPhaseAxisY.cross(box2.narrowPhaseAxisY), box1, box2)
				|| getSeparatingPlane222(rPos, box1.narrowPhaseAxisY.cross(box2.narrowPhaseAxisZ), box1, box2)
				|| getSeparatingPlane222(rPos, box1.narrowPhaseAxisZ.cross(box2.narrowPhaseAxisX), box1, box2)
				|| getSeparatingPlane222(rPos, box1.narrowPhaseAxisZ.cross(box2.narrowPhaseAxisY), box1, box2)
				|| getSeparatingPlane222(rPos, box1.narrowPhaseAxisZ.cross(box2.narrowPhaseAxisZ), box1, box2);
		return !ret;
	}

	public static void getCollidePoints(PhysicBox box1, boolean isStatic1, PhysicBox box2, boolean isStatic2) {
		// Log.info("Try to calculare reverse force ........");
		Vector3f rPos1 = box1.narrowPhaseGlobalPos.less(box2.narrowPhaseGlobalPos);
		Vector3f rPos2 = box2.narrowPhaseGlobalPos.less(box1.narrowPhaseGlobalPos);
		Quaternion quat1 = box1.getQuaternionFull();
		Quaternion quat2 = box2.getQuaternionFull();
		// Step 1: set the Box 2 in the repere of the Box 1:
		Quaternion quatTransfer1 = Quaternion.diff(quat1, quat2);
		Quaternion quatTransfer2 = Quaternion.diff(quat2, quat1);
		// quatTransfer.normalize();

		// DebugDisplay.relativeTest = quatTransfer;
		// Vector3f tmp = rPos.addNew(new Vector3f(0,0,14));
		// DebugDisplay.relativeTestPos.getTransform().setPosition(tmp);
		// DebugDisplay.relativeTestPos.getTransform().setOrientation(quatTransfer);
		// DebugDisplay.boxTest.setSize(box1.getSize());
		// Log.info("" + rPos + quatTransfer1);
		// /*res = */getCollidePointsAABBCenteredWithOBB(box1.narrowPhaseHalfSize, box2.narrowPhaseHalfSize, quatTransfer, rPos);
		/* res = transfert in generic plan the new res ... */
		// test origin AABB with OBB collision
		// Step 2: set the Box 1 in the repere of the Box 2:
		// test origin AABB with OBB collision
		// tmp = rPos.addNew(new Vector3f(0,0,14));
		DebugDisplay.testRpos = quat2.inverse().getMatrix4().multiply(rPos1);
		DebugDisplay.testQTransfert = quatTransfer2;
		DebugDisplay.box1HalfSize = box2.narrowPhaseHalfSize;
		DebugDisplay.box2HalfSize = box1.narrowPhaseHalfSize;

		// DebugDisplay.relativeTestPos.getTransform().setPosition(tmp);
		// DebugDisplay.relativeTestPos.getTransform().setOrientation(quatTransfer);
		// DebugDisplay.boxTest.setSize(box1.getSize());
		// foinctionne avec la box qui n'est pas orienter...
		// getCollidePointsAABBCenteredWithOBB(box2.narrowPhaseHalfSize, box1.narrowPhaseHalfSize, quatTransfer2, rPos1);
		// fonctionne quand le block est trourner de 90% petit pb de positionnement en hauteur....
		// getCollidePointsAABBCenteredWithOBB(box2.narrowPhaseHalfSize, box1.narrowPhaseHalfSize, quatTransfer2, quat2.multiply(rPos2));
		ColisionPoints[] collide1 = getCollidePointsAABBCenteredWithOBB(box2.narrowPhaseHalfSize, box1.narrowPhaseHalfSize, quatTransfer2, quat2.inverse().getMatrix4().multiply(rPos1));
		// transfer detection point collision in global environement:
		if (collide1 != null) {
			for (int iii = 0; iii < collide1.length; iii++) {
				collide1[iii].position = quat1.multiply(collide1[iii].position).add(box1.narrowPhaseGlobalPos);
				collide1[iii].force = quat2.multiply(collide1[iii].force);//.add(box1.narrowPhaseGlobalPos);
			}
		}
		/* res = trensfert in generic plan the new res ... */

		DebugDisplay.testRpos = quat1.inverse().getMatrix4().multiply(rPos2);
		DebugDisplay.testQTransfert = quatTransfer1;
		DebugDisplay.box1HalfSize = box1.narrowPhaseHalfSize;
		DebugDisplay.box2HalfSize = box2.narrowPhaseHalfSize;
		ColisionPoints[] collide2 = getCollidePointsAABBCenteredWithOBB(box1.narrowPhaseHalfSize, box2.narrowPhaseHalfSize, quatTransfer1, quat1.inverse().getMatrix4().multiply(rPos2));
		if (collide2 != null) {
			for (int iii = 0; iii < collide2.length; iii++) {
				collide2[iii].position = quat2.multiply(collide2[iii].position).add(box2.narrowPhaseGlobalPos);
				collide2[iii].force = quat1.multiply(collide2[iii].force);//.add(box2.narrowPhaseGlobalPos);
			}
		}
		// add only if NOT static, when static no colision is performed
		if (true) { //!isStatic1) {
			Collision colision = new Collision(collide1, box2, collide2, isStatic2);
			box1.addColision(colision);
		}
		if (true) { //!isStatic1) {
			Collision colision = new Collision(collide2, box1, collide1, isStatic1);
			box2.addColision(colision);
		}

	}

	public static ColisionPoints[] getCollidePointsAABBCenteredWithOBB(Vector3f box1HalfSize, Vector3f box2HalfSize, Quaternion box2Orientation, Vector3f box2Position) {

		// point in AABB
		Vector3f topBackRight = box2Orientation.multiply(new Vector3f(+box2HalfSize.x(), +box2HalfSize.y(), +box2HalfSize.z())).add(box2Position);
		Vector3f topBackLeft = box2Orientation.multiply(new Vector3f(-box2HalfSize.x(), +box2HalfSize.y(), +box2HalfSize.z())).add(box2Position);
		Vector3f topFrontRight = box2Orientation.multiply(new Vector3f(+box2HalfSize.x(), -box2HalfSize.y(), +box2HalfSize.z())).add(box2Position);
		Vector3f topFrontLeft = box2Orientation.multiply(new Vector3f(-box2HalfSize.x(), -box2HalfSize.y(), +box2HalfSize.z())).add(box2Position);
		Vector3f bottomBackRight = box2Orientation.multiply(new Vector3f(+box2HalfSize.x(), +box2HalfSize.y(), -box2HalfSize.z())).add(box2Position);
		Vector3f bottomBackLeft = box2Orientation.multiply(new Vector3f(-box2HalfSize.x(), +box2HalfSize.y(), -box2HalfSize.z())).add(box2Position);
		Vector3f bottomFrontRight = box2Orientation.multiply(new Vector3f(+box2HalfSize.x(), -box2HalfSize.y(), -box2HalfSize.z())).add(box2Position);
		Vector3f bottomFrontLeft = box2Orientation.multiply(new Vector3f(-box2HalfSize.x(), -box2HalfSize.y(), -box2HalfSize.z())).add(box2Position);
		DebugDisplay.testPoints.clear();
		DebugDisplay.testPoints.add(topBackRight);
		DebugDisplay.testPoints.add(topBackLeft);
		DebugDisplay.testPoints.add(topFrontRight);
		DebugDisplay.testPoints.add(topFrontLeft);
		DebugDisplay.testPoints.add(bottomBackRight);
		DebugDisplay.testPoints.add(bottomBackLeft);
		DebugDisplay.testPoints.add(bottomFrontRight);
		DebugDisplay.testPoints.add(bottomFrontLeft);
		DebugDisplay.testPointsBox.clear();
		DebugDisplay.testPointsBox.add(new Vector3f(+box1HalfSize.x(), +box1HalfSize.y(), +box1HalfSize.z()));
		DebugDisplay.testPointsBox.add(new Vector3f(-box1HalfSize.x(), +box1HalfSize.y(), +box1HalfSize.z()));
		DebugDisplay.testPointsBox.add(new Vector3f(+box1HalfSize.x(), -box1HalfSize.y(), +box1HalfSize.z()));
		DebugDisplay.testPointsBox.add(new Vector3f(-box1HalfSize.x(), -box1HalfSize.y(), +box1HalfSize.z()));
		DebugDisplay.testPointsBox.add(new Vector3f(+box1HalfSize.x(), +box1HalfSize.y(), -box1HalfSize.z()));
		DebugDisplay.testPointsBox.add(new Vector3f(-box1HalfSize.x(), +box1HalfSize.y(), -box1HalfSize.z()));
		DebugDisplay.testPointsBox.add(new Vector3f(+box1HalfSize.x(), -box1HalfSize.y(), -box1HalfSize.z()));
		DebugDisplay.testPointsBox.add(new Vector3f(-box1HalfSize.x(), -box1HalfSize.y(), -box1HalfSize.z()));
		Vector3f insideTopBackRight = pointDistanceInAABB(box1HalfSize, topBackRight);
		Vector3f insideTopBackLeft = pointDistanceInAABB(box1HalfSize, topBackLeft);
		Vector3f insideTopFrontRight = pointDistanceInAABB(box1HalfSize, topFrontRight);
		Vector3f insideTopFrontLeft = pointDistanceInAABB(box1HalfSize, topFrontLeft);
		Vector3f insideBottomBackRight = pointDistanceInAABB(box1HalfSize, bottomBackRight);
		Vector3f insideBottomBackLeft = pointDistanceInAABB(box1HalfSize, bottomBackLeft);
		Vector3f insideBottomFrontRight = pointDistanceInAABB(box1HalfSize, bottomFrontRight);
		Vector3f insideBottomFrontLeft = pointDistanceInAABB(box1HalfSize, bottomFrontLeft);
		DebugDisplay.testPointsCollide.clear();
		DebugDisplay.testPointsCollide.add(insideTopBackRight == null ? false : true);
		DebugDisplay.testPointsCollide.add(insideTopBackLeft == null ? false : true);
		DebugDisplay.testPointsCollide.add(insideTopFrontRight == null ? false : true);
		DebugDisplay.testPointsCollide.add(insideTopFrontLeft == null ? false : true);
		DebugDisplay.testPointsCollide.add(insideBottomBackRight == null ? false : true);
		DebugDisplay.testPointsCollide.add(insideBottomBackLeft == null ? false : true);
		DebugDisplay.testPointsCollide.add(insideBottomFrontRight == null ? false : true);
		DebugDisplay.testPointsCollide.add(insideBottomFrontLeft == null ? false : true);
		int count = 0;
		if (insideTopBackRight != null) {
			count++;
		}
		if (insideTopBackLeft != null) {
			count++;
		}
		if (insideTopFrontRight != null) {
			count++;
		}
		if (insideTopFrontLeft != null) {
			count++;
		}
		if (insideBottomBackRight != null) {
			count++;
		}
		if (insideBottomBackLeft != null) {
			count++;
		}
		if (insideBottomFrontRight != null) {
			count++;
		}
		if (insideBottomFrontLeft != null) {
			count++;
		}
		ColisionPoints[] out = new ColisionPoints[count];
		count = 0;
		if (insideTopBackRight != null) {
			out[count] = new ColisionPoints(new Vector3f(+box2HalfSize.x(), +box2HalfSize.y(), +box2HalfSize.z()), insideTopBackRight);
			count++;
		}
		if (insideTopBackLeft != null) {
			out[count] = new ColisionPoints(new Vector3f(-box2HalfSize.x(), +box2HalfSize.y(), +box2HalfSize.z()), insideTopBackLeft);
			count++;
		}
		if (insideTopFrontRight != null) {
			out[count] = new ColisionPoints(new Vector3f(+box2HalfSize.x(), -box2HalfSize.y(), +box2HalfSize.z()), insideTopFrontRight);
			count++;
		}
		if (insideTopFrontLeft != null) {
			out[count] = new ColisionPoints(new Vector3f(-box2HalfSize.x(), -box2HalfSize.y(), +box2HalfSize.z()), insideTopFrontLeft);
			count++;
		}
		if (insideBottomBackRight != null) {
			out[count] = new ColisionPoints(new Vector3f(+box2HalfSize.x(), +box2HalfSize.y(), -box2HalfSize.z()), insideBottomBackRight);
			count++;
		}
		if (insideBottomBackLeft != null) {
			out[count] = new ColisionPoints(new Vector3f(-box2HalfSize.x(), +box2HalfSize.y(), -box2HalfSize.z()), insideBottomBackLeft);
			count++;
		}
		if (insideBottomFrontRight != null) {
			out[count] = new ColisionPoints(new Vector3f(+box2HalfSize.x(), -box2HalfSize.y(), -box2HalfSize.z()), insideBottomFrontRight);
			count++;
		}
		if (insideBottomFrontLeft != null) {
			out[count] = new ColisionPoints(new Vector3f(-box2HalfSize.x(), -box2HalfSize.y(), -box2HalfSize.z()), insideBottomFrontLeft);
			count++;
		}
		if (count != 0) {
			// Find a point inside the BOX ...
			/*
			Log.info("Detect point inside ... " + insideTopBackRight + "  " + insideTopBackLeft + "  " + insideTopFrontRight + "  " + insideTopFrontLeft + "  " + insideBottomBackRight + "  "
					+ insideBottomBackLeft + "  " + insideBottomFrontRight + "  " + insideBottomFrontLeft);
			*/
			return out;
		}
		// line in AABB
		// TODO: 
		// Log.info("Need to detect line inside ..."); // pas tot a fait... si ca colisione déja avec un point de l'autre ...
		return null;
	}

	public static boolean pointInAABB(Vector3f halfSize, Vector3f point) {
		if (point.x() > -halfSize.x() && point.x() < halfSize.x() && point.y() > -halfSize.y() && point.y() < halfSize.y() && point.z() > -halfSize.z() && point.z() < halfSize.z()) {
			return true;
		}
		return false;
	}

	public static Vector3f pointDistanceInAABB(Vector3f halfSize, Vector3f point) {
		float outX = 0;
		float outY = 0;
		float outZ = 0;
		if (point.x() < 0) {
			if (point.x() > -halfSize.x()) {
				
				outX = -(halfSize.x() + point.x());
				
				//out.x = -halfSize.x - point.x;
				//out.x = -halfSize.x + point.x;
				//out.x = + point.x;
			} else {
				return null;
			}
		} else {
			if (point.x() < halfSize.x()) {
				//out.x = halfSize.x + point.x;
				outX = halfSize.x() - point.x();
				//out.x = - point.x;
			} else {
				return null;
			}
		}
		if (point.y() < 0) {
			if (point.y() > -halfSize.y()) {
				outY = -halfSize.y() - point.y();
				//out.y = -halfSize.y + point.y;
				//out.y = point.y;
			} else {
				return null;
			}
		} else {
			if (point.y() < halfSize.y()) {
				//out.y = halfSize.y + point.y;
				outY = halfSize.y() - point.y();
				//out.y = - point.y;
			} else {
				return null;
			}
		}
		if (point.z() < 0) {
			if (point.z() > -halfSize.z()) {
				outZ = -halfSize.z() - point.z();
				//out.z = -halfSize.z + point.z;
				//out.z = + point.z;
			} else {
				return null;
			}
		} else {
			if (point.z() < halfSize.z()) {
				//out.z = halfSize.z + point.z;
				outZ = halfSize.z() - point.z();
				//out.z = - point.z;
			} else {
				return null;
			}
		}
		if (Math.abs(outX) < Math.abs(outY)) {
			outY = 0;
			if (Math.abs(outX) < Math.abs(outZ)) {
				outZ = 0;
				return new Vector3f(outX, outY, outZ);
			}
			outX = 0;
			return new Vector3f(outX, outY, outZ);
		}
		outX = 0;
		if (Math.abs(outY) < Math.abs(outZ)) {
			outZ = 0;
			return new Vector3f(outX, outY, outZ);
		}
		outY = 0;
		return new Vector3f(outX, outY, outZ);
	}
}
