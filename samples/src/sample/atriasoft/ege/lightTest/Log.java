package sample.atriasoft.ege.lightTest;

public class Log {
	private static final String LIBNAME = "LowPolySample";
	
	public static void critical(String data) {
		System.out.println("[C] " + LOGGER.LIBNAME + " | " + data);
	}
	
	public static void debug(String data) {
		System.out.println("[D] " + LOGGER.LIBNAME + " | " + data);
	}
	
	public static void error(String data) {
		System.out.println("[E] " + LOGGER.LIBNAME + " | " + data);
	}
	
	public static void info(String data) {
		System.out.println("[I] " + LOGGER.LIBNAME + " | " + data);
	}
	
	public static void print(String data) {
		System.out.println(data);
	}
	
	public static void todo(String data) {
		System.out.println("[TODO] " + LOGGER.LIBNAME + " | " + data);
	}
	
	public static void verbose(String data) {
		System.out.println("[V] " + LOGGER.LIBNAME + " | " + data);
	}
	
	public static void warning(String data) {
		System.out.println("[W] " + LOGGER.LIBNAME + " | " + data);
	}
	
	private Log() {}
}
