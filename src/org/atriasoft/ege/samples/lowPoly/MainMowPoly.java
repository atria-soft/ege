package org.atriasoft.ege.samples.lowPoly;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;

public class MainMowPoly {
	public static void main(String[] args) {
		Uri.setGroup("DATA", "src/org/atriasoft/ege/samples/lowPoly/");
		Uri.setGroup("DATA_EGE", "src/org/atriasoft/ege/data/");
		Uri.setGroup("RES", "res");
		Gale.run(new LowPolyApplication(), args);
	}
}
