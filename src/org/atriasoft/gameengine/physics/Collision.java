package org.atriasoft.gameengine.physics;

public class Collision {
	public final ColisionPoints[] colisionPointLocal;
	public final PhysicShape shapeRemote;
	public final ColisionPoints[] colisionPointRemote;
	public final boolean staticRemote;
	public Collision(ColisionPoints[] colisionPointLocal, PhysicShape shapeRemote,
			ColisionPoints[] colisionPointRemote, boolean staticRemote) {
		super();
		this.colisionPointLocal = colisionPointLocal;
		this.shapeRemote = shapeRemote;
		this.colisionPointRemote = colisionPointRemote;
		this.staticRemote = staticRemote;
	}
	
}
