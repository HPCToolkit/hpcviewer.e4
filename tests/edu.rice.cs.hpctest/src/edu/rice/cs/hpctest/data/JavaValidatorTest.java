package edu.rice.cs.hpctest.data;

import edu.rice.cs.hpcdata.util.JavaValidator;

public class JavaValidatorTest 
{
	public static void main(String []args) {
		System.out.println();
		
		if (JavaValidator.isGCJ()) {
			System.out.println("Unsupported JVM: GNU GCJ");
		} else {
			if (JavaValidator.isCorrectJavaVersion())
				System.out.println("Valid JVM: " + JavaValidator.getJavaVendor() + " " + JavaValidator.getJavaVersion());
			else
				System.out.println("Invalid JVM: Needs to be higher or equal than " 
						+ JavaValidator.JAVA_SUPPORTED_17);
		}
	}
	

}
