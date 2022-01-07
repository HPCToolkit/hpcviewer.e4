package edu.rice.cs.hpcdata.util;

import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import java.util.Properties;
import java.util.Set;

public class JavaValidator 
{
	// minimum Java supported
	private final static int JAVA_11 = 11;
	public  final static int JAVA_SUPPORTED = JAVA_11;
	
	/***
	 * print all the JVM properties
	 */
	static public void printProperties() {
		Properties p = System.getProperties();
		Set<Entry<Object, Object>> set = p.entrySet();
		Iterator<Entry<Object, Object>> iterator = set.iterator();
		
		while(iterator.hasNext()) {
			Entry<Object, Object> entry = iterator.next();
			System.out.println(entry.getKey() + " = " + entry.getValue());
		}
	}
	
	/****
	 * check if JVM is from GNU
	 * @return
	 */
	static public boolean isGCJ() {
		// GNU JVM has "Free Software Foundation" as the vendor
		return getJavaVendor().indexOf("Free")>=0;
	}
	
	
	/****
	 * retrieve the vendor (Sun, IBM, Free Foundation, ....)
	 * @return
	 */
	static public String getJavaVendor() {
		return System.getProperty("java.vendor");
	}
	
	/*****
	 * retrieve Java version
	 * @return
	 */
	static public String getJavaVersion() {
		return System.getProperty("java.version");
	}
	
	/****
	 * Check Java version. If the version is not supported,
	 *   display an error message and return false.
	 *   
	 * @return true if Java is supported. False otherwise.
	 */
	static public boolean isCorrectJavaVersion() {

		boolean isCorrect = checkVersion();
		if (!isCorrect) {
			String message = "Error: Java " + 
					System.getProperty("java.version") +
					" is not supported.";

			System.out.println(message);

			JOptionPane.showMessageDialog(null, message);
		}
		return isCorrect;
	}

	//////////////////////////////////////////////////////////////
	///
	/// Private methods
	///
	//////////////////////////////////////////////////////////////
	

	static private boolean checkVersion() {
		final String version = getJavaVersion();
		if (version == null)
			return false;

		String verNumber[]  = version.split("\\.", 3);
		String majorVersion = verNumber[0];

		try {
			Integer major  = Integer.valueOf(majorVersion);
			int JVMversion = major.intValue();
			if (major == 1) {
				Integer minor = Integer.valueOf(verNumber[1]);
				JVMversion    = minor.intValue();
			}
			return isSupported(JVMversion);
		} catch (Exception e) {
			System.err.println("Unknown java version: " + version);
		}
		return false;
	}
	
	static private boolean isSupported(int version) {
		return JAVA_SUPPORTED <= version;
	}

}
