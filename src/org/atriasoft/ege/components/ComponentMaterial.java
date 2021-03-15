package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.Material;

public class ComponentMaterial extends Component {
	// the material is not a resource, it can change in time... with AI or selection...
	private Material material;
	
	public ComponentMaterial(Material material) {
		super();
		this.material = material;
	}
	
	public ComponentMaterial() {
		super();
		this.material = new Material();
	}
	@Override
	public String getType() {
		return "material";
	}
	public Material getMaterial() {
		return material;
	}
	public void setMaterial(Material material) {
		this.material = material;
	}

}
