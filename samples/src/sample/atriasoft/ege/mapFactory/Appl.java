package sample.atriasoft.ege.mapFactory;

import org.atriasoft.etk.Configs;
import org.atriasoft.etk.math.Vector2f;
import org.atriasoft.ewol.context.EwolApplication;
import org.atriasoft.ewol.context.EwolContext;

public class Appl implements EwolApplication {
	
	private void localCreate(final EwolContext context) {
		// parse all the argument of the application
		for (int iii = 0; iii < context.getCmd().size(); iii++) {
			String tmpppp = context.getCmd().get(iii);
			if (tmpppp == "-h" || tmpppp == "--help") {
				Log.print("  -h/--help display this help");
				System.exit(0);
			}
		}
		context.setSize(new Vector2f(800, 600));
		Configs.getConfigFonts().set("FreeSherif", 12);
		// Create the windows
		MainWindows basicWindows = new MainWindows();
		// configure the ewol context to use the new windows
		context.setWindows(basicWindows);
	}
	
	@Override
	public void onCreate(final EwolContext context) {
		Log.info("Application onCreate: [BEGIN]");
		localCreate(context);
		Log.info("Application onCreate: [ END ]");
	}
	
	@Override
	public void onDestroy(final EwolContext context) {
		Log.info("Application onDestroy: [BEGIN]");
		
		Log.info("Application onDestroy: [ END ]");
	}
	
	@Override
	public void onPause(final EwolContext context) {
		Log.info("Application onPause: [BEGIN]");
		
		Log.info("Application onPause: [ END ]");
	}
	
	@Override
	public void onResume(final EwolContext context) {
		Log.info("Application onResume: [BEGIN]");
		
		Log.info("Application onResume: [ END ]");
	}
	
	@Override
	public void onStart(final EwolContext context) {
		Log.info("Application onStart: [BEGIN]");
		
		Log.info("Application onStart: [ END ]");
	}
	
	@Override
	public void onStop(final EwolContext context) {
		Log.info("Application onStop: [BEGIN]");
		
		Log.info("Application onStop: [ END ]");
	}
	
}