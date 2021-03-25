package org.atriasoft.ege;

import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.event.EventTime;

public interface ControlInterface {
	boolean onEventEntry(EventEntry event);
	
	boolean onEventInput(EventInput event, Vector2f relativePosition);
	
	/**
	 * Periodic call to update grapgic display
	 * @param event Time generic event
	 */
	void periodicCall(EventTime event);
}
