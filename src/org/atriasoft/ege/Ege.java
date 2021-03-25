package org.atriasoft.ege;

import org.atriasoft.etk.Uri;

public class Ege {
	public static void init() {
		Uri.addLibrary("ege", Ege.class, "/resources/ege/");
	}
	
	private Ege() {}
}
