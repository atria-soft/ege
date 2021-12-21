package org.atriasoft.phyligram;

public class Collision {
	public final ColisionPoint[] colisionPointLocal;
	public final PhysicShape shapeRemote;
	public final ColisionPoint[] colisionPointRemote;
	public final boolean staticRemote;
	public Collision(ColisionPoint[] colisionPointLocal, PhysicShape shapeRemote,
			ColisionPoint[] colisionPointRemote, boolean staticRemote) {
		super();
		this.colisionPointLocal = colisionPointLocal;
		this.shapeRemote = shapeRemote;
		this.colisionPointRemote = colisionPointRemote;
		this.staticRemote = staticRemote;
	}
	
}
