package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.hpctoolkit.hpcclient.v1_0.TraceSampling;

import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.util.ICallPath.ICallPathInfo;


public class RemoteProcessTimeline implements IProcessTimeline 
{
	private final RemoteSpaceTimeDataController traceData;
	private final Future<TraceSampling> traceSampling;

	private RemoteTraceDataCollectorPerProfile traceDataCollector;
	
	private int line;
	private IdTuple idTuple;
	
	RemoteProcessTimeline(RemoteSpaceTimeDataController traceData, 
						  Future<TraceSampling> traceSampling) {
		this.traceData = traceData;
		this.traceSampling = traceSampling;
	}
	
	@Override
	public void readInData() throws IOException {
		try {
			TraceSampling trace = traceSampling.get();
			var traceId = trace.getTraceId();
			var profile = traceId.toInt();
			line = traceData.getTraceLineFromProfile(profile);
			idTuple = traceData.getIdTupleFromProfile(profile);
					
			traceDataCollector = (RemoteTraceDataCollectorPerProfile) traceData.getTraceDataCollector(line(), getProfileIdTuple());
			traceDataCollector.readInData(trace);
			
		} catch (InterruptedException  | ExecutionException e) {
		    // Restore interrupted state...
		    Thread.currentThread().interrupt();
		}
	}

	@Override
	public long getTime(int sample) {
		return traceDataCollector.getTime(sample);
	}

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
		var cpid = getContextId(sample);
		var map  = traceData.getScopeMap();
		return map.getCallPathInfo(cpid);
	}

	@Override
	public void copyDataFrom(IProcessTimeline other) {
		traceDataCollector.duplicate( ((RemoteProcessTimeline) other).traceDataCollector );
	}

	@Override
	public int size() {
		return traceDataCollector.size();
	}

	@Override
	public int line() {
		return line;
	}

	@Override
	public IdTuple getProfileIdTuple() {
		return idTuple;
	}

	@Override
	public int findMidpointBefore(long time, boolean usingMidpoint) {
		try {
			return traceDataCollector.findClosestSample(time, usingMidpoint);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid time: " + time);
		}
	}

	@Override
	public boolean isEmpty() {
		return traceDataCollector.isEmpty();
	}

	@Override
	public boolean isGPU() {			
		return getProfileIdTuple().isGPU(traceData.getExperiment().getIdTupleType());
	}

	@Override
	public void dispose() {
		if (traceDataCollector != null)
			traceDataCollector.dispose();
		
		traceDataCollector = null;
	}		
}
