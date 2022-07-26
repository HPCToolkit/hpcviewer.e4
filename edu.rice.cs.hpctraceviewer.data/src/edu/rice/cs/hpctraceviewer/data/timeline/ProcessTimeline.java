package edu.rice.cs.hpctraceviewer.data.timeline;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.runtime.Assert;

import edu.rice.cs.hpcdata.db.version4.DataRecord;
import edu.rice.cs.hpcdata.util.CallPath;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import edu.rice.cs.hpctraceviewer.data.ITraceDataCollector;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TraceDataByRank;
import edu.rice.cs.hpctraceviewer.data.version2.AbstractBaseData;

/** 
 *  A class that stores and manages one line of time-stamp and cpid data.
 * */
public class ProcessTimeline {

	/** The mapping between the cpid's and the actual scopes. */
	private HashMap<Integer, CallPath> scopeMap;

	/** This ProcessTimeline's line number. */
	private int lineNum, processNumber;

	/** The initial time in view. */
	private long startingTime;

	/** The range of time in view. */
	private long timeRange;

	/** The amount of time that each pixel on the screen correlates to. */
	private double pixelLength;

	final ITraceDataCollector data;

	/*************************************************************************
	 * Reads in the call-stack trace data from the binary traceFile in the form:
	 * double time-stamp int Call-Path ID double time-stamp int Call-Path ID ...
	 ************************************************************************/

	/** 
	 * Creates a new ProcessTimeline with the given parameters. 
	 * 
	 * @param currentLineNum
	 * 			The relative line number of this process or thread. 
	 * 			This number starts from zero which is the top line to the
	 * 			end of line to be painted.
	 * @param processNumber
	 * 			The absolute line number of this process or thread.
	 * 		    This number is based on the file index (in case of old database),
	 * 			hence it starts from the smallest process (or thread) number to 
	 * 		    the highest process (or thread) number.  
	 * @param dataController
	 * 			The main data of this trace controller
	 * 
	 * @see SpaceTimeDataController
	 */
	public ProcessTimeline(int currentLineNum, 
						   int processNumber, 
						   SpaceTimeDataController dataController)
	{
		var experiment      = dataController.getExperiment();
		lineNum 			= currentLineNum;
		scopeMap 			= (HashMap<Integer, CallPath>) experiment.getScopeMap();

		var attributes      = dataController.getTraceDisplayAttribute();
		this.timeRange		= attributes.getTimeInterval();
		this.startingTime 	= dataController.getMinBegTime() + attributes.getTimeBegin();
		this.processNumber  = processNumber;

		pixelLength = timeRange / (double) attributes.getPixelHorizontal();
		
		//TODO: Beautify
		var dataTrace = dataController.getBaseData();
		var idleContexts = dataController.getExperiment().getListIdleContextIds();
		int []idleIds = new int[idleContexts.size()];
		idleIds[0] = idleContexts.get(0);

		final boolean exposeGPU = TracePreferenceManager.getGPUTraceExposure();

		if (dataTrace instanceof AbstractBaseData)
			data = new TraceDataByRank(dataTrace, processNumber, attributes.getPixelHorizontal(), idleIds, exposeGPU);
		else
			data = new TraceDataByRank(new DataRecord[0]);
	}

	/**
	 * Remote version for ProcessTimeline constructor
	 * 
	 * @param _data
	 * @param _scopeMap
	 * @param _processNumber
	 * @param _numPixelH
	 * @param _timeRange
	 * @param _startingTime
	 */
	public ProcessTimeline(TraceDataByRank _data,
			HashMap<Integer, CallPath> _scopeMap, int _processNumber,
			int _numPixelH, long _timeRange, long _startingTime) {
		lineNum = _processNumber;
		scopeMap = _scopeMap;
		timeRange = _timeRange;
		startingTime = _startingTime;

		pixelLength = timeRange / (double) _numPixelH;
		if (_data == null)
			data = new TraceDataByRank(new DataRecord[0]);
		else
			data = _data;
		
		// laks 2016.06.23: hack for remote data: we don't have process number 
		// information from the server, so we just assign the same as the line number
		// for local data, the line number is different than the process number
		//  if the screen resolution is smaller than the number of ranks
		// at the moment, this fix works. not sure why.
		this.processNumber = _processNumber;
	}

	/**
	 * Fills the ProcessTimeline with data from the file. If this is being
	 * called, it must be on local, so the cast is fine
	 * @throws IOException 
	 */
	public void readInData() throws IOException {
		data.readInData(startingTime, timeRange, pixelLength);
	}

	/** Gets the time that corresponds to the index sample in times. */
	public long getTime(int sample) {
		return data.getTime(sample);
	}

	/** Gets the cpid that corresponds to the index sample in timeLine. */
	public int getCpid(int sample) {
		return data.getCpid(sample);
	}

	public void shiftTimeBy(long lowestStartingTime) {
		data.shiftTimeBy(lowestStartingTime);
	}

	/** returns the call path corresponding to the sample and depth given */
	public CallPath getCallPath(int sample, int depth) {
		Assert.isTrue(sample>=0, "sample number is negative");
		int cpid = getCpid(sample);

		CallPath cp = scopeMap.get(cpid);
 		return cp;
	}
	
	/**
	 * Fills this one with the data from another
	 * @param another
	 */
	public void copyDataFrom(ProcessTimeline another) {
		data.duplicate(another.data);
	}

	/** Returns the number of elements in this ProcessTimeline. */
	public int size() {
		return data.size();
	}

	/** 
	 * Returns this ProcessTimeline's line number.
	 * This returns the relative line number. 
	 * */
	public int line() {
		return lineNum;
	}
	
	/***
	 * return the process ID number
	 * 
	 * @return the process id
	 */
	public int getProcessNum() {
		return processNumber;
	}

	/**
	 * Finds the sample to which 'time' most closely corresponds in the
	 * ProcessTimeline.
	 * 
	 * @param time : the requested time
	 * @return the index of the sample if the time is within the range, -1  otherwise
	 * */
	public int findMidpointBefore(long time, boolean usingMidpoint)  throws Exception
	{
		return data.findClosestSample(time, usingMidpoint);
	}

	
	public boolean isEmpty()
	{
		return data.isEmpty();
	}
	
	public boolean isGPU() 
	{
		return data.isGPU();
	}
}