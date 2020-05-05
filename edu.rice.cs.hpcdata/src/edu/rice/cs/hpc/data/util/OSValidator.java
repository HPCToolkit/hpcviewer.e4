package edu.rice.cs.hpc.data.util;

/*******************************************************
 * Check the Operating system of the current JVM
 * @author laksonoadhianto
 *
 *******************************************************/
public class OSValidator {
	
	static private final String os = OSValidator.getOS();
	static private final boolean mac = ( os.indexOf("mac") >= 0 );
	static private final boolean win = ( os.indexOf("win") >= 0 );
	static private final boolean unix = ( os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0 );
	
	/****
	 * test unit
	 * @param args
	 */
	public static void main(String[] args)
	{
		if(isWindows()){
			System.out.println("This is Windows");
		}else if(isMac()){
			System.out.println("This is Mac");
		}else if(isUnix()){
			System.out.println("This is Unix or Linux");
		}else{
			System.out.println("Your OS is not supported!!");
		}
	}
 
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
