package edu.rice.cs.hpctraceviewer.data;

import java.io.IOException;

/******
 * 
 * An interface to collect data from disk or remote of the current 
 * process or thread of traces.
 *
 */
public interface ITraceDataCollector 
{
	/***
	 * Reading data from file of the current process or thread. 
	 * This method has to be called FIRST before calling other APIs.
	 * 
	 * @apiNote The current process or thread must be stored before calling this API
	 * 
	 * @param timeStart
	 * 			The current time start
	 * @param timeRange
	 * 			The current time range
	 * @param pixelLength 
	 * 			the range of time per pixel. Its unit is time, usually nanoseconds for data version 4.
	 * 
	 * @throws IOException 
	 */
	public void readInData(long timeStart, long timeRange, double pixelLength) throws IOException;

	public boolean isEmpty();
	
	public int findClosestSample(long time, boolean usingMidpoint)  throws Exception;	
	
	public long getTime(int sample);
	
	public int getCpid(int sample);
	
	public int size();
	
	public void shiftTimeBy(long lowestStartingTime);
	
	public void duplicate(ITraceDataCollector traceData);
	
	boolean isGPU();
}
