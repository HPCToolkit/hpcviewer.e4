package edu.rice.cs.hpctraceviewer.data;

import java.io.IOException;

public interface ITraceDataCollector 
{
	public enum TraceOption {ORIGINAL_TRACE, REVEAL_GPU_TRACE, REVEAL_CPU_TRACE, REVEAL_ALL_TRACES};
	
	public boolean isEmpty();
	public int findClosestSample(long time, boolean usingMidpoint)  throws Exception;
	public void readInData(long timeStart, long timeRange, double pixelLength) throws IOException;
	public long getTime(int sample);
	public int getCpid(int sample);
	public int size();
	public void shiftTimeBy(long lowestStartingTime);
	public void duplicate(ITraceDataCollector traceData);
	boolean isGPU();
	public void dispose();
}
