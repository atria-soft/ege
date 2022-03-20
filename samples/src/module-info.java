/** Basic module interface.
 *
 * @author Edouard DUPIN */

open module sample.atriasoft.ege {
	exports sample.atriasoft.ege.mapFactory;
	exports sample.atriasoft.ege.collisiontest;
	exports sample.atriasoft.ege.lowPoly;
	exports sample.atriasoft.ege.loxelEngine;
	exports sample.atriasoft.ege.oldTest;
	exports sample.atriasoft.ege.s1_texturedCube;
	
	requires org.atriasoft.ege;
	requires org.atriasoft.ewol;
	requires org.atriasoft.etk;
	requires org.atriasoft.gale; // for map factory
}
