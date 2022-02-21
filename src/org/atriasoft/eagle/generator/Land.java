package org.atriasoft.eagle.generator;

import org.atriasoft.eagle.CirclePointGenerator;
import org.atriasoft.eagle.model.MetaMap;
import org.atriasoft.eagle.model.TypeElement;
import org.atriasoft.eagle.model.VoxelMap;
import org.atriasoft.etk.math.Vector3i;

public class Land implements LocalGenerator {
	
	@Override
	public void generate(MetaMap map, Vector3i[] positions, float distance) {
		CirclePointGenerator generator = new CirclePointGenerator(positions[0], (int) distance);
		
		while (generator.hasNext()) {
			Vector3i pos = generator.getValue();
			float height = map.getRand().nextFloat(0, 10);
			VoxelMap elem = map.getValueCreate(pos.x(), pos.y());
			elem.element = TypeElement.ELEMENT_STONE;
			elem.height = height;
		}
		// need to apply a filter
	}
	
}

////// https://www.redblobgames.com/maps/terrain-from-noise/
