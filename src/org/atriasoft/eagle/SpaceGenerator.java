package org.atriasoft.eagle;

import org.atriasoft.eagle.generator.Land;
import org.atriasoft.eagle.model.MetaMap;
import org.atriasoft.etk.math.Vector3i;

public class SpaceGenerator {
	public static MetaMap generateNewMap() {
		MetaMap out = new MetaMap(System.currentTimeMillis());
		Land generator = new Land();
		generator.generate(out, new Vector3i[] { new Vector3i(0, 0, 2) }, 100);
		return out;
	}
	
	public static void insertMap(MetaMap data) {
		
	}
}
