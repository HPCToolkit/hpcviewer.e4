package edu.rice.cs.hpcbase;

public class DebugUtil {
	private static final boolean debugOn = true;
	
	public static void debug(String message) {
		if (debugOn)
			System.out.println(message);
	}
	
	
	public static void debug(String prefix, String message) {
		if (debugOn)
			System.out.printf("[%s] %s%n", prefix, message);
	}
	
	
	public 	static void debugThread(String prefix, String message) {
		if (debugOn)
			System.out.printf("%2s [%s] %s%n", Thread.currentThread().getId(), prefix, message);
	}
}
