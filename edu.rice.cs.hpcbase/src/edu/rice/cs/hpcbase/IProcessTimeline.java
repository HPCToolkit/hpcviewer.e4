package edu.rice.cs.hpcbase;

import java.io.IOException;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.util.ICallPath;
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
	
	
	Scope getCallPath(int sample, int depth);
	
	
	ICallPathInfo getCallPathInfo(int sample);
	
	
	ICallPath getCallPathInfo();
	
	/**
	 * Fills this one with the data from another
	 * @param another
	 */
	void copyDataFrom(IProcessTimeline other);
	
	/** Returns the number of elements in this ProcessTimeline. */
	int size();
	
	/** Returns this ProcessTimeline's line number. */
	int line();
	
	
	/***
	 * return the process ID number
	 * 
	 * @return the process id
	 */
	int getProcessNum();
	
	
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