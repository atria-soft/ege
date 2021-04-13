package sample.atriasoft.ege.s1_texturedCube;

public class Log {
	private static final String LIBNAME = "Sample1";
	
	public static void critical(String data) {
		System.out.println("[C] " + Log.LIBNAME + " | " + data);
	}
	
	public static void debug(String data) {
		System.out.println("[D] " + Log.LIBNAME + " | " + data);
	}
	
	public static void error(String data) {
		System.out.println("[E] " + Log.LIBNAME + " | " + data);
	}
	
	public static void info(String data) {
		System.out.println("[I] " + Log.LIBNAME + " | " + data);
	}
	
	public static void print(String data) {
		System.out.println(data);
	}
	
	public static void todo(String data) {
		System.out.println("[TODO] " + Log.LIBNAME + " | " + data);
	}
	
	public static void verbose(String data) {
		System.out.println("[V] " + Log.LIBNAME + " | " + data);
	}
	
	public static void warning(String data) {
		System.out.println("[W] " + Log.LIBNAME + " | " + data);
	}
	
	private Log() {}
}
