package edu.rice.cs.hpc.data.util;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class JavaValidator {

	// minimum Java supported
	private final static int JavaMinVersion = 5;
	
	static public void main(String []args) {
		if (isGCJ()) {
			System.out.println("Unsupported JVM: GNU GCJ");
		} else {
			if (isCorrectJavaVersion())
				System.out.println("Valid JVM");
			else
				System.out.println("Invalid JVM: Needs to be higher or equal than " 
						+ JavaMinVersion);
		}
	}
	
	
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
	 * check if JVM has the correct version
	 * @return
	 */
	static public boolean isCorrectJavaVersion() {
		String version = getJavaVersion();
		String []items = version.split("\\.");
		int v = Integer.valueOf(items[1]);
		return (v>=JavaMinVersion);
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
}
