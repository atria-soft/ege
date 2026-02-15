package sample.atriasoft.ege.mapFactory.tools;
 
import org.atriasoft.ewol.event.EventEntry;
import org.atriasoft.ewol.event.EventInput;
import org.atriasoft.ewol.widget.Widget;

import sample.atriasoft.ege.mapFactory.EgeScene;
import sample.atriasoft.ege.mapFactory.model.Map;

public interface MapToolInterface {
	Widget getWidget();

	void onDraw(Map map);

	boolean onEventEntry(final EventEntry event, Map map, EgeScene widget);

	boolean onEventInput(final EventInput event, Map relPos, EgeScene widget);

	default void onDeactivate(final EgeScene widget) {
	}
}