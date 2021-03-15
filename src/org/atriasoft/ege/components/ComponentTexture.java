package org.atriasoft.ege.components;

import org.atriasoft.ege.Component;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.resource.ResourceTexture;
import org.atriasoft.ege.internal.Log;

public class ComponentTexture extends Component {

	private ResourceTexture texture;
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "texture";
	}
	public ComponentTexture(Uri textureName) {
		this.texture = ResourceTexture.createFromPng(textureName);
		if (this.texture == null) {
			Log.error("can not instanciate Texture ...");
			return;
		}
		
	}
	public void bindForRendering() {
		this.texture.bindForRendering(0);
		
	}
	public void unBindForRendering() {
		this.texture.unBindForRendering();
	}

}
