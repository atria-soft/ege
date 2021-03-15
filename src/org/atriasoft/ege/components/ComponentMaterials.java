package org.atriasoft.ege.components;

import java.util.HashMap;
import java.util.Map;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Material;

public class ComponentMaterials extends Component {
	// the material is not a resource, it can change in time... with AI or selection...
	private Map<String, Material> materials = new HashMap<String, Material>();
	
	public ComponentMaterials(Material material) {
		super();
		
	}
	public ComponentMaterials() {
		super();
	}
	@Override
	public String getType() {
		return "materials";
	}
	public Material getMaterial(String name) {
		return materials.get(name);
	}
	public void setMaterial(String name, Material material) {
		this.materials.put(name, material);
	}

}
