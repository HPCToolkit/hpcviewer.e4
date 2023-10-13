package edu.rice.cs.hpctraceviewer.data.timeline;

import java.io.IOException;
import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
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

	private ITraceDataCollector traceDataCollector;

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
		
		traceDataCollector = stData.getTraceDataCollector(lineNum, idTuple);
		
		idTupletype = stData.getExperiment().getIdTupleType();
	}

	
	/**
	 * Fills the ProcessTimeline with data from the file. If this is being
	 * called, it must be on local, so the cast is fine
	 * @throws IOException 
	 */
	@Override
	public void readInData() throws IOException {
		traceDataCollector.readInData(startingTime, timeRange, pixelLength);
	}

	/** Gets the time that corresponds to the index sample in times. */
	@Override
	public long getTime(int sample) {
		return traceDataCollector.getTime(sample);
	}

	/** Gets the cpid that corresponds to the index sample in timeLine. */
	@Override
	public int getContextId(int sample) {
		return traceDataCollector.getCpid(sample);
	}

	@Override
	public void shiftTimeBy(long lowestStartingTime) {
		traceDataCollector.shiftTimeBy(lowestStartingTime);
	}
	
	
	@Override
	public ICallPathInfo getCallPathInfo(int sample) {
		int cpid = getContextId(sample);
		return scopeMap.getCallPathInfo(cpid);
	}

	
	/**
	 * Fills this one with the data from another
	 * @param another
	 */
	@Override
	public void copyDataFrom(IProcessTimeline another) {
		if (another instanceof ProcessTimeline) {
			traceDataCollector.duplicate(((ProcessTimeline)another).traceDataCollector);
		}
	}

	/** Returns the number of elements in this ProcessTimeline. */
	@Override
	public int size() {
		return traceDataCollector.size();
	}

	/** Returns this ProcessTimeline's line number. */
	@Override
	public int line() {
		return lineNum;
	}
	

	@Override
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
	@Override
	public int findMidpointBefore(long time, boolean usingMidpoint)
	{
		try {
			return traceDataCollector.findClosestSample(time, usingMidpoint);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid time: " + time);
		}
	}

	
	public boolean isEmpty()
	{
		return traceDataCollector.isEmpty();
	}
	
	public boolean isGPU() 
	{
		return idTuple.isGPU(idTupletype);
	}

	public void dispose() {
		if (scopeMap != null)
			scopeMap.dispose();
		if (traceDataCollector != null)
			traceDataCollector.dispose();
		
		scopeMap = null;
		traceDataCollector = null;
	}
}