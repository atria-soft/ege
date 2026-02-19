package org.atriasoft.ege.components;

public class GlLightIndex {
	public int oGLcolor;
	public int oGLposition;
	public int oGLattenuation;
	public int oGLdirection;
	public int oGLcutoffCos;
	public int oGLcutoffCosInner;
	public int oGLradius;

	public GlLightIndex(final int gLcolor, final int gLposition, final int gLattenuation,
			final int gLdirection, final int gLcutoffCos, final int gLcutoffCosInner,
			final int gLradius) {
		this.oGLcolor = gLcolor;
		this.oGLposition = gLposition;
		this.oGLattenuation = gLattenuation;
		this.oGLdirection = gLdirection;
		this.oGLcutoffCos = gLcutoffCos;
		this.oGLcutoffCosInner = gLcutoffCosInner;
		this.oGLradius = gLradius;
	}
}
