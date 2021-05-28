package org.atriasoft.ege.components.part;

import org.atriasoft.ege.Light;
import org.atriasoft.ege.components.GlLightIndex;
import org.atriasoft.ege.engines.EngineLight;
import org.atriasoft.etk.math.Vector3f;
import org.atriasoft.gale.resource.ResourceProgram;

public class LightRender implements PartRenderInterface {

	private static final int numberOfLight = 8;
	private GlLightIndex[] GLlights;
	EngineLight lightEngine;
	PositionningInterface position = null;
	
	public LightRender(final EngineLight lightEngine) {
		this.lightEngine = lightEngine;
	}

	@Override
	public void init(final ResourceProgram program) {
		this.GLlights = new GlLightIndex[LightRender.numberOfLight];
		for (int iii = 0; iii < LightRender.numberOfLight; iii++) {
			final int color = program.getUniform("in_lights[" + iii + "].color");
			final int position = program.getUniform("in_lights[" + iii + "].position");
			final int attenuation = program.getUniform("in_lights[" + iii + "].attenuation");
			this.GLlights[iii] = new GlLightIndex(color, position, attenuation);
		}
	}

	@Override
	public void bindForRendering(final ResourceProgram program) {
		// preparing stage
		Vector3f positionObject = this.position.getTransform().getPosition();
		Light[] lights = this.lightEngine.getNearest(positionObject);
		// injection stage
		for (int iii = 0; iii < LightRender.numberOfLight; iii++) {
			if (lights[iii] != null) {
				//Log.warning("Set light : [" + iii + "] " + lights[iii]);
				program.uniformVector(this.GLlights[iii].oGLposition, lights[iii].getPositionDelta());
				program.uniformColorRGB(this.GLlights[iii].oGLcolor, lights[iii].getColor());
				program.uniformVector(this.GLlights[iii].oGLattenuation, lights[iii].getAttenuation());
			} else {
				program.uniformVector(this.GLlights[iii].oGLposition, Vector3f.ZERO);
				program.uniformVector(this.GLlights[iii].oGLcolor, Vector3f.ZERO);
				//program.uniformColorRGB(this.GLlights[iii].oGLcolor, Color.NONE);
				program.uniformVector(this.GLlights[iii].oGLattenuation, new Vector3f(1, 0, 0));
			}
		}
	}
	@Override
	public void unBindForRendering() {
		
	}

	public void setPositionning(final PositionningInterface component) {
		this.position = component;
	}
}
