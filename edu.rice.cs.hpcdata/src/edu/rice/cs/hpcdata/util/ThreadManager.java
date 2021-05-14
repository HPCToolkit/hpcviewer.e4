package edu.rice.cs.hpcdata.util;

public class ThreadManager 
{

	/********
	 * retrieve the maximum number of threads given the maximum threads, based
	 * 	on the number of available cores. 
	 *  See {@link java.lang.Runtime.availableProcessors }
	 * @param maxThreads : maximum number of threads that can be supported. 
	 * 		   if the value is 0 then it will be ignored
	 * @return
	 */
	static public int getNumThreads(int maxThreads) {

		int available_cores  = Runtime.getRuntime().availableProcessors();
		
		if (maxThreads > 0)
			return Math.min(maxThreads, available_cores);
		else
			return available_cores;					
	}
}
