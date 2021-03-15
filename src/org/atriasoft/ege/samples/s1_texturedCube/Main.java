package org.atriasoft.ege.samples.s1_texturedCube;

import org.atriasoft.etk.Uri;
import org.atriasoft.gale.Gale;

public class Main {
	public Main() {}
	public static void main(String[] args) {
		Uri.setGroup("DATA", "src/org/atriasoft/ege/samples/s1_texturedCube/");
		Uri.setGroup("DATA_EGE", "src/org/atriasoft/ege/data/");
		Uri.setGroup("RES", "res");
		Gale.run(new S1Application(), args);
	}
}
