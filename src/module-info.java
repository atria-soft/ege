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
	exports org.atriasoft.ege.tools;
	exports org.atriasoft.phyligram;
	exports entities;
	exports guis;
	exports models;
	exports renderEngine;
	exports shaders;
	exports skybox;
	exports terrains;
	exports textures;
	exports toolbox;
	
	requires transitive java.desktop; // todo: remove this deprecated element...
	requires transitive org.atriasoft.gale;
	requires transitive org.atriasoft.etk;
	requires transitive org.atriasoft.ewol;
	requires transitive org.atriasoft.ephysics;
	requires transitive org.atriasoft.loader3d;
	requires org.atriasoft.iogami;
}
