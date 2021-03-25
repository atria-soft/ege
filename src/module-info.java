/** Basic module interface.
 *
 * @author Edouard DUPIN */

open module org.atriasoft.ege {
	exports org.atriasoft.ege;
	exports org.atriasoft.ege.camera;
	exports org.atriasoft.ege.components;
	exports org.atriasoft.ege.engines;
	exports org.atriasoft.ege.geometry;
	exports org.atriasoft.ege.map;
	exports org.atriasoft.ege.physics.shape;
	exports org.atriasoft.ege.resource;
	
	requires transitive org.atriasoft.gale;
	requires transitive org.atriasoft.etk;
	requires transitive org.atriasoft.ewol;
	requires transitive org.atriasoft.ephysics;
}
