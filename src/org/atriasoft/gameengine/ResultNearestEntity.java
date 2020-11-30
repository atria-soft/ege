package org.atriasoft.gameengine;

public class ResultNearestEntity {
	public Entity entity;
	public float dist;
	public ResultNearestEntity(Entity entity, float dist) {
		this.entity = entity;
		this.dist = dist;
	}
}
