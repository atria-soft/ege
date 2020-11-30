package org.atriasoft.gameengine.components;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.resource.ResourceTexture;
import org.atriasoft.gameengine.internal.Log;
import org.atriasoft.gameengine.Component;

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
