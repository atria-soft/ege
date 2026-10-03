package org.atriasoft.ege.components;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.atriasoft.ege.Component;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.resource.ResourceTexture2;
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
		LOGGER.debug("update palet environnement");
		final BufferedImage img = this.palette.getImage();
		try {
			ImageIO.write(img, "png", Path.of("/home/heero/00000_palette_" + this.palette.getId() + ".png").toFile());
		} catch (final IOException ex) {
			LOGGER.error("Failed to store palette debug image", ex);
		}
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
