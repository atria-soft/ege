package org.atriasoft.ege.components;

import java.util.HashMap;
import java.util.Map;

import org.atriasoft.ege.Component;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.resource.ResourceTexture;

public class ComponentTextures extends Component {
	private final Map<String, ResourceTexture> textures = new HashMap<String, ResourceTexture>();
	
	public ComponentTextures() {
		
	}
	
	public void bindForRendering(String name) {
		this.textures.get(name).bindForRendering(0);
		
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "textures";
	}
	
	public void setTexture(String name, Uri textureName) {
		final ResourceTexture texture = ResourceTexture.createFromPng(textureName);
		if (texture == null) {
			Log.error("can not instanciate Texture ...");
			return;
		}
		this.textures.put(name, texture);
	}
	
	public void unBindForRendering(String name) {
		this.textures.get(name).unBindForRendering();
	}
	
}
