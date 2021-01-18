/** Basic module interface.
 *
 * @author Edouard DUPIN */

open module org.atriasoft.gameengine {
	exports org.atriasoft.gameengine;
	exports org.atriasoft.gameengine.camera;
	exports org.atriasoft.gameengine.components;
	exports org.atriasoft.gameengine.engines;
	exports org.atriasoft.gameengine.geometry;
	exports org.atriasoft.gameengine.map;
	exports org.atriasoft.gameengine.physics.shape;
	exports org.atriasoft.gameengine.resource;
	
	requires transitive org.atriasoft.gale;
	requires transitive org.atriasoft.etk;
	requires transitive net.jreactphysics3d;
}
