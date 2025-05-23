package org.atriasoft.ege;

import org.atriasoft.ege.camera.Camera;

public abstract class Engine {
	protected Environement env;
	public Engine(Environement env) {
		this.env = env;
	}
	/**
	 * An Entity component has been removed ==> need remove it in local if needed
	 * @param ref Referrence on the component
	 */
	public abstract void componentRemove(Component ref);
	/**
	 * An Entity component has been added ==> need add it in local if needed
	 * @param ref Referrence on the component
	 */
	public abstract void componentAdd(Component ref);
	/**
	 * Global game engine main cycle of update internal parameters
	 * @param deltaMili time from the last update
	 */
	public abstract void update(long deltaMili);
	/**
	 * Global game engine main cycle of draw
	 * @param deltaMili time from the last render
	 * @param camera Camera property to render the engine properties ...
	 */
	public abstract void render(long deltaMili, Camera camera);
	/**
	 * Globalgame engine main cycle of draw
	 * @param deltaMili time from the last render
	 * @param camera Camera property to render the engine properties ...
	 */
	public abstract void renderDebug(long deltaMili, Camera camera);
	public abstract String getType();
}

