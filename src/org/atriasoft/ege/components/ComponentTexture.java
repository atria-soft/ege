package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.resource.ResourceTexture;

public class ComponentTexture extends Component {
	
	private ResourceTexture texture;
	
	public ComponentTexture(Uri textureName) {
		this.texture = ResourceTexture.createFromPng(textureName);
		if (this.texture == null) {
			LOGGER.error("can not instanciate Texture ...");
		}
		
	}
	
	public void bindForRendering() {
		this.texture.bindForRendering(0);
		
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "texture";
	}
	
	public void unBindForRendering() {
		this.texture.unBindForRendering();
	}
	
}
