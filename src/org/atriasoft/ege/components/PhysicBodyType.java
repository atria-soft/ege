package org.atriasoft.ege.components;

public enum PhysicBodyType {
	BODY_DYNAMIC, // mobile object that move and interact with other (42)
	BODY_STATIC, // static object that does not move (map, wall)
	BODY_KINEMATIC // static object that can change position by the user (like doors, lift ...)
}
