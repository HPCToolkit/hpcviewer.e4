package edu.rice.cs.hpctraceviewer.data.timeline;

import java.io.IOException;
import org.eclipse.core.runtime.Assert;

import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.util.ICallPath;
import edu.rice.cs.hpcdata.util.ICallPath.ICallPathInfo;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;


/** A data structure that stores one line of timestamp-cpid data. */
public class ProcessTimeline implements IProcessTimeline
{

	/** The mapping between the cpid's and the actual scopes. */
	private ICallPath scopeMap;

	/** This ProcessTimeline's line number. */
	private final int lineNum;

	private final IdTuple idTuple;
	private final IdTupleType idTupletype;

	/** The initial time in view. */
	private final long startingTime;

	/** The range of time in view. */
	private final long timeRange;

	/** The amount of time that each pixel on the screen correlates to. */
	private final double pixelLength;

	private ITraceDataCollector data;

	/*************************************************************************
	 * Reads in the call-stack trace data from the binary traceFile in the form:
	 * double time-stamp int Call-Path ID double time-stamp int Call-Path ID ...
	 ************************************************************************/

	/** Creates a new ProcessTimeline with the given parameters. 
	 * @param lineNum 
	 * 			a unique 0-based index
	 * @param IdTuple idTuple
	 * 			the index of id-tuple in the trace data
	 * @param stData
	 * 			The main data
	 */
	public ProcessTimeline(int lineNum, SpaceTimeDataController stData, IdTuple idTuple)
	{

		this.lineNum 		= lineNum;
		scopeMap 			= stData.getScopeMap();
		var attributes      = stData.getTraceDisplayAttribute();

		timeRange			= attributes.getTimeInterval();
		startingTime 		= stData.getMinBegTime() + attributes.getTimeBegin();
		this.idTuple  = idTuple;

		pixelLength = timeRange / (double) stData.getPixelHorizontal();
		
		data = stData.getTraceDataCollector(lineNum, idTuple);
		
		idTupletype = stData.getExperiment().getIdTupleType();
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
	public int getContextId(int sample) {
		return data.getCpid(sample);
	}

	public void shiftTimeBy(long lowestStartingTime) {
		data.shiftTimeBy(lowestStartingTime);
	}

	/** returns the call path corresponding to the sample and depth given */
	public Scope getCallPath(int sample, int depth) {
		Assert.isTrue(sample>=0, "sample number is negative");
		
		int cpid = getContextId(sample);
		return scopeMap.getCallPathScope(cpid);
	}
	
	
	public ICallPathInfo getCallPathInfo(int sample) {
		int cpid = getContextId(sample);
		return scopeMap.getCallPathInfo(cpid);
	}
	
	
	public ICallPath getCallPathInfo() {
		return scopeMap;
	}
	
	/**
	 * Fills this one with the data from another
	 * @param another
	 */
	public void copyDataFrom(IProcessTimeline another) {
		if (another instanceof ProcessTimeline) {
			data.duplicate(((ProcessTimeline)another).data);
		}
	}

	/** Returns the number of elements in this ProcessTimeline. */
	public int size() {
		return data.size();
	}

	/** Returns this ProcessTimeline's line number. */
	public int line() {
		return lineNum;
	}
	
	/***
	 * return the process ID number
	 * 
	 * @return the process id
	 */
	public int getProcessNum() {
		return idTuple.getProfileIndex() - 1;
	}
	
	
	public IdTuple getProfileIdTuple() {
		return idTuple;
	}

	/**
	 * Finds the sample to which 'time' most closely corresponds in the
	 * ProcessTimeline.
	 * 
	 * @param time : the requested time
	 * @return the index of the sample if the time is within the range, -1  otherwise
	 * */
	public int findMidpointBefore(long time, boolean usingMidpoint)
	{
		try {
			return data.findClosestSample(time, usingMidpoint);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid time: " + time);
		}
	}

	
	public boolean isEmpty()
	{
		return data.isEmpty();
	}
	
	public boolean isGPU() 
	{
		return idTuple.isGPU(idTupletype);
	}

	public void dispose() {
		if (scopeMap != null)
			scopeMap.dispose();
		if (data != null)
			data.dispose();
		
		scopeMap = null;
		data = null;
	}
}