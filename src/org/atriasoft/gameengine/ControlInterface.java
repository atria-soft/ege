package org.atriasoft.gameengine;

import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.gale.event.EventEntry;
import org.atriasoft.gale.event.EventInput;
import org.atriasoft.gale.event.EventTime;

public interface ControlInterface {
	public boolean onEventEntry(EventEntry event);
	public boolean onEventInput(EventInput event, Vector2f relativePosition);
	/**
	 * @brief Periodic call to update grapgic display
	 * @param event Time generic event
	 */
	public void periodicCall(EventTime event);
}
