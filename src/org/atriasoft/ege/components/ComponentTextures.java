package org.atriasoft.ege.components;

import java.util.HashMap;
import java.util.Map;

import org.atriasoft.ege.Component;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.resource.ResourceTexture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentTextures extends Component {
	static final Logger LOGGER = LoggerFactory.getLogger(ComponentTextures.class);
	private final Map<String, ResourceTexture> textures = new HashMap<>();

	public ComponentTextures() {

	}

	public void bindForRendering(final String name) {
		this.textures.get(name).bindForRendering(0);

	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "textures";
	}

	public void setTexture(final String name, final Uri textureName) {
		final ResourceTexture texture = ResourceTexture.createFromPng(textureName);
		if (texture == null) {
			LOGGER.error("can not instanciate Texture ...");
			return;
		}
		this.textures.put(name, texture);
	}

	public void unBindForRendering(final String name) {
		this.textures.get(name).unBindForRendering();
	}

}
