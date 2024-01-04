package edu.rice.cs.hpctraceviewer.data.timeline;

import java.io.IOException;
import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.util.ICallPath.ICallPathInfo;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;


/** 
 * A class that interfaces and manages one trace line of timestamp-cpid data. 
 * This class is designed for local trace, and cannot be used for remote database.
 * */
public class ProcessTimeline implements IProcessTimeline
{
	/** This ProcessTimeline's line number. */
	private final int lineNum;

	private final IdTuple idTuple;

	/** The initial time in view. */
	private final long startingTime;

	/** The range of time in view. */
	private final long timeRange;

	/** The amount of time that each pixel on the screen correlates to. */
	private final double pixelLength;

	private final SpaceTimeDataController dataController;
	
	private final ITraceDataCollector traceDataCollector;



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
		this.dataController = stData;

		var attributes  = stData.getTraceDisplayAttribute();
		timeRange	 = attributes.getTimeInterval();
		startingTime = stData.getMinBegTime() + attributes.getTimeBegin();
		this.idTuple = idTuple;

		pixelLength = timeRange / (double) stData.getPixelHorizontal();
		
		traceDataCollector = stData.getTraceDataCollector(lineNum, idTuple);
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
		return dataController.getScopeMap().getCallPathInfo(cpid);
	}

	
	/**
	 * Fills this one with the data from another
	 * @param another
	 */
	@Override
	public void copyDataFrom(IProcessTimeline another) {
		if (another instanceof ProcessTimeline) {
			traceDataCollector.copyDataFrom(((ProcessTimeline)another).traceDataCollector);
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
		var idTupletype = dataController.getExperiment().getIdTupleType();
		return idTuple.isGPU(idTupletype);
	}

	public void dispose() {
		if (traceDataCollector != null)
			traceDataCollector.dispose();
	}


	@Override
	public IProcessTimeline duplicate(int line, IdTuple idTuple) {
		ProcessTimeline toDonate = new ProcessTimeline(line, dataController, idTuple);
		toDonate.copyDataFrom(this);

		return toDonate;
	}
}