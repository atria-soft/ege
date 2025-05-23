package org.atriasoft.eagle.generator;

import org.atriasoft.eagle.model.MetaMap;
import org.atriasoft.etk.math.Vector3i;

public interface LocalGenerator {
	void generate(MetaMap map, Vector3i[] positions, float distance);
}
