/** Basic module interface.
 *
 * @author Edouard DUPIN */

open module sample.atriasoft.ege {
	exports sample.atriasoft.ege.mapFactory;
	exports sample.atriasoft.ege.mapFactory.model;
	exports sample.atriasoft.ege.mapFactory.tools;
	exports sample.atriasoft.ege.collisiontest;
	exports sample.atriasoft.ege.lowPoly;
	exports sample.atriasoft.ege.loxelEngine;
	exports sample.atriasoft.ege.oldTest;
	exports sample.atriasoft.ege.s1_texturedCube;
	
	requires transitive org.atriasoft.ege;
	requires transitive org.atriasoft.ewol;
	requires transitive org.atriasoft.etk;
	requires transitive org.atriasoft.gale; // for map factory
}
