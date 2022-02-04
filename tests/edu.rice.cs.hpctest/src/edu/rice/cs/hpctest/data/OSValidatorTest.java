package edu.rice.cs.hpctest.data;

import edu.rice.cs.hpcdata.util.OSValidator;

public class OSValidatorTest {
	/****
	 * test unit
	 * @param args
	 */
	public static void main(String[] args)
	{
		if(OSValidator.isWindows()){
			System.out.println("This is Windows");
		}else if(OSValidator.isMac()){
			System.out.println("This is Mac");
		}else if(OSValidator.isUnix()){
			System.out.println("This is Unix or Linux");
		}else{
			System.out.println("Your OS is not supported!!");
		}
	}

}
