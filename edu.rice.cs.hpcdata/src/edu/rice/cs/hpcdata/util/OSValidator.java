package edu.rice.cs.hpcdata.util;

/*******************************************************
 * Check the Operating system of the current JVM
 *
 *******************************************************/
public class OSValidator {
	
	static private final String os = OSValidator.getOS();
	static private final boolean mac = ( os.indexOf("mac") >= 0 );
	static private final boolean win = ( os.indexOf("win") >= 0 );
	static private final boolean unix = ( os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0 );
	
 
	/******
	 * check if we are on windows
	 * @return true if win* is the current OS
	 */
	public static boolean isWindows(){
	    return win; 
	}
 
	/******
	 * check if we are on mac
	 * @return true if mac* is the current OS
	 */
	public static boolean isMac(){
	    return mac; 
	}
 
	/******
	 * check if we are on unix or linux
	 * @return true if *nix or *nux is the current OS
	 */
	public static boolean isUnix(){ 
	    return unix;
	}

	 	
	private static String getOS() {
		return System.getProperty("os.name").toLowerCase();
	}
}
