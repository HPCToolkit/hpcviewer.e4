package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.hpctoolkit.hpcclient.v1_0.TraceSampling;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.util.ICallPath.ICallPathInfo;


public class RemoteProcessTimeline implements IProcessTimeline 
{
	private final RemoteSpaceTimeDataController traceData;
	private final Future<TraceSampling> traceSampling;

	private ITraceDataCollector traceDataCollector;
	
	private int line;
	private IdTuple idTuple;
	
	RemoteProcessTimeline(RemoteSpaceTimeDataController traceData, 
						  Future<TraceSampling> sampledTrace) {
		this.traceData = traceData;
		this.traceSampling = sampledTrace;
	}
	
	@Override
	public void readInData() throws IOException {
		System.out.printf("  [RemoteProcessTimeline.readInData-%d] starts...%n", Thread.currentThread().getId());
		if (traceSampling.isCancelled() || traceSampling.isDone()) {
			traceDataCollector = ITraceDataCollector.DUMMY;
			String status = traceSampling.isCancelled() ? "CANCEL" : "DONE";
			System.out.printf("  [RemoteProcessTimeline.readInData-%d] fail to invoke get(). Status: %s %n", Thread.currentThread().getId(), status);
			return;
		}
		
		try {
			TraceSampling samples = traceSampling.get();
			var traceId = samples.getTraceId();
			var profile = traceId.toInt();
			line = traceData.getTraceLineFromProfile(profile);
			idTuple = traceData.getProfileFromPixel(line);
			
			System.out.printf("    [RemoteProcessTimeline.readInData-%d] %3d %3d %s%n", Thread.currentThread().getId(), line, profile, idTuple.toString());
					
			traceDataCollector = traceData.getTraceDataCollector(line(), getProfileIdTuple());
			((RemoteTraceDataCollectorPerProfile)traceDataCollector).readInData(samples);
			
			return;
		} catch (InterruptedException e) {
		    // Restore interrupted state...
		    Thread.currentThread().interrupt();
		} catch (ExecutionException e ) {
			LoggerFactory.getLogger(getClass()).error("Error while reading remote data", e);
		}
		// TODO: need to be clean up
		// in case of exception, we set an empty trace data collector
		traceDataCollector = ITraceDataCollector.DUMMY;
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
		if (traceDataCollector == null)
			return true;
		
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
