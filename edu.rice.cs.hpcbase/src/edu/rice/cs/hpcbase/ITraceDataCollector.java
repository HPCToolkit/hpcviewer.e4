package edu.rice.cs.hpcbase;

import java.io.IOException;

public interface ITraceDataCollector 
{
	enum TraceOption {ORIGINAL_TRACE, REVEAL_GPU_TRACE, REVEAL_CPU_TRACE, REVEAL_ALL_TRACES};
	
	static ITraceDataCollector DUMMY = new ITraceDataCollector() {
		
		@Override
		public int size() {
			return 0;
		}
		
		@Override
		public void shiftTimeBy(long lowestStartingTime) { /* nothing */ }
		
		@Override
		public void readInData(long timeStart, long timeRange, double pixelLength) throws IOException { /* nothing */ }
		
		@Override
		public boolean isEmpty() {
			return true;
		}
		
		@Override
		public long getTime(int sample) {
			return 0;
		}
		
		@Override
		public int getCpid(int sample) {
			return 0;
		}
		
		@Override
		public int findClosestSample(long time, boolean usingMidpoint) throws Exception {
			return 0;
		}
		
		@Override
		public void duplicate(ITraceDataCollector traceData) { /* nothing */ }
		
		@Override
		public void dispose() { /* nothing */ }
	};
	
	boolean isEmpty();
	
	int findClosestSample(long time, boolean usingMidpoint)  throws Exception;
	
	void readInData(long timeStart, long timeRange, double pixelLength) throws IOException;
	
	long getTime(int sample);
	
	int getCpid(int sample);
	
	int size();
	
	void shiftTimeBy(long lowestStartingTime);
	
	void duplicate(ITraceDataCollector traceData);
	
	void dispose();
}
