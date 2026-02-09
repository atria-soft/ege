package org.atriasoft.ege.components;

import org.atriasoft.egami.ImageByte;
import org.atriasoft.ege.Component;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.resource.ResourceTexture2;
import org.atriasoft.iogami.IOgami;
import org.atriasoft.loader3d.resources.ResourcePaletteFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentTexturePalette extends Component {
	static final Logger LOGGER = LoggerFactory.getLogger(ComponentTexturePalette.class);
	
	private final ResourcePaletteFile palette;
	private final ResourceTexture2 texture;
	
	public ComponentTexturePalette(final Uri paletteName) {
		this.palette = ResourcePaletteFile.create(paletteName);
		this.texture = ResourceTexture2.createNamed("TEXTURE_OF_PALETTE:" + paletteName.toString());
		if (this.texture == null) {
			LOGGER.error("can not instanciate Texture ...");
		}
		// element already called
		updateFromPalette();
		// for next update (realTime reload)
		this.palette.onUpdate(() -> {
			updateFromPalette();
		});
	}
	
	public void updateFromPalette() {
		LOGGER.warn("update palet environnement");
		final ImageByte img = this.palette.getImageByte();
		IOgami.storePNG(new Uri("/home/heero/00000_palette_" + this.palette.getId() + ".png"), img);
		this.texture.set(img);
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
