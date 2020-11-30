package org.atriasoft.gameengine.components;

import java.util.HashMap;
import java.util.Map;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.resource.ResourceTexture;
import org.atriasoft.gameengine.internal.Log;
import org.atriasoft.gameengine.Component;
import org.atriasoft.gameengine.Material;

public class ComponentTextures extends Component {
	private Map<String, ResourceTexture> textures = new HashMap<String, ResourceTexture>();
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "textures";
	}
	public ComponentTextures() {
		
	}
	public void setTexture(String name, Uri textureName) {
		ResourceTexture texture = ResourceTexture.createFromPng(textureName);
		if (texture == null) {
			Log.error("can not instanciate Texture ...");
			return;
		}
		textures.put(name, texture);
	}
	
	
	public void bindForRendering(String name) {
		this.textures.get(name).bindForRendering(0);
		
	}
	public void unBindForRendering(String name) {
		this.textures.get(name).unBindForRendering();
	}

}
