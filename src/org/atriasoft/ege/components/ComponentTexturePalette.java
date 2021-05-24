package org.atriasoft.ege.components;

import org.atriasoft.egami.ImageByte;
import org.atriasoft.ege.Component;
import org.atriasoft.ege.internal.Log;
import org.atriasoft.etk.Uri;
import org.atriasoft.gale.resource.ResourceTexture2;
import org.atriasoft.loader3d.resources.ResourcePaletteFile;

public class ComponentTexturePalette extends Component {
	
	private final ResourcePaletteFile palette;
	private final ResourceTexture2 texture;
	
	public ComponentTexturePalette(Uri paletteName) {
		this.palette = ResourcePaletteFile.create(paletteName);
		this.texture = ResourceTexture2.createNamed("TEXTURE_OF_PALETTE:" + paletteName.toString());
		if (this.texture == null) {
			Log.error("can not instanciate Texture ...");
		}
		this.palette.onUpdate(() -> {
			Log.warning("update palet environnement");
			final ImageByte img = this.palette.getImageByte();
			this.texture.set(img);
		});
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
