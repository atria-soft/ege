package org.atriasoft.ege;

import java.util.ArrayList;
import java.util.List;

import org.atriasoft.etk.math.Vector3f;

public class EntityInteraction {
	protected int type;
	protected int groupSource;
	protected List<Integer> groupDestination = new ArrayList<Integer>();
	protected Vector3f positionSource;
	public int getType() {
		return type;
	}
	public int getSourceGroup() {
		return groupSource;
	}
	public List<Integer> getDestinationGroup() {
		return groupDestination;
	}
	public void addGroupDestination(Integer id) {
		groupDestination.add(id);
	}
	public Vector3f getSourcePosition() {
		return positionSource;
	}
	public EntityInteraction(int type, int groupSource, Vector3f pos) {
		this.type = type;
		this.groupSource = groupSource;
		this.positionSource = pos;
	}
	public void applyEvent(Entity entity) {
		
	}
}
