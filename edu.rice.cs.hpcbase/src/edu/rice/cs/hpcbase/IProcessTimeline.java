package edu.rice.cs.hpcbase;

import java.io.IOException;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.util.ICallPath.ICallPathInfo;

public interface IProcessTimeline 
{

	/**
	 * Fills the ProcessTimeline with data from the file. If this is being
	 * called, it must be on local, so the cast is fine
	 * @throws IOException 
	 */
	void readInData() throws IOException;
	

	/** Gets the time that corresponds to the index sample in times. */
	long getTime(int sample);
	
	/** Gets the cpid that corresponds to the index sample in timeLine. */
	int getContextId(int sample);
	
	
	void shiftTimeBy(long lowestStartingTime);
	
	
	/***
	 * Get the info of a callpath for a given sample
	 * 
	 * @param sample
	 * 
	 * @return {@code ICallPathInfo}
	 */
	ICallPathInfo getCallPathInfo(int sample);
	
	
	/**
	 * Fills this one with the data from another
	 * 
	 * @param another
	 */
	void copyDataFrom(IProcessTimeline other);
	
	/** 
	 * Returns the number of elements in this ProcessTimeline.
	 * 
	 * @return {@code size}
	 * 			The number of samples or elements 
	 **/
	int size();
	
	/** Returns this ProcessTimeline's trace line number. 
	 *
	 *  @return {@code int}
	 *  			The trace line number
	 **/
	int line();
	
	
	/***
	 * Return the profile Id of this trace
	 * @return
	 */
	IdTuple getProfileIdTuple();
	
	/**
	 * Finds the sample to which 'time' most closely corresponds in the
	 * ProcessTimeline.
	 * 
	 * @param time : the requested time
	 * @return the index of the sample if the time is within the range, -1  otherwise
	 * */
	int findMidpointBefore(long time, boolean usingMidpoint);
	
	
	boolean isEmpty();
	
	
	boolean isGPU();
	
	
	void dispose();
}