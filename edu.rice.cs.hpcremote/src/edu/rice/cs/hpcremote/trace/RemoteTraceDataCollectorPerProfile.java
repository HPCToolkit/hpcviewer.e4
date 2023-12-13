package edu.rice.cs.hpcremote.trace;

import java.io.IOException;

import org.hpctoolkit.hpcclient.v1_0.TraceSampling;

import edu.rice.cs.hpcbase.AbstractTraceDataCollector;
import edu.rice.cs.hpcbase.DebugUtil;
import edu.rice.cs.hpcdata.db.version4.DataRecord;

public class RemoteTraceDataCollectorPerProfile extends AbstractTraceDataCollector 
{
	public RemoteTraceDataCollectorPerProfile(int numPixelH, TraceOption option) {
		super(option, numPixelH);
	}
	
	
	/*****
	 * Reading the trace sampling from the remote server.
	 * 
	 * @apiNote Use this method instead of readInData(long, long, double) which
	 * is designed for local database 
	 * 
	 * @param traceSampling
	 */
	public void readInData(TraceSampling traceSampling) {
		var list = traceSampling.getSamplesChronologicallyNonDescending();
		final boolean isFinal = traceSampling.isFinalSampleOfSamplingFinalSampleOfTrace();
		list.toStream().forEach(sample -> {
			var time  = isFinal ? sample.getOriginalObservationMoment() : sample.getTraceMoment();
			var cctId = sample.getCallingContext();
			
			var data = new DataRecord(time.toEpochNano(), cctId.toInt());
			addSampleToLastIndex(data);
		});
		postProcess();
		
		DebugUtil.debugThread(getClass().getName(), String.format("\t\tSample size: %d", list.size()));
	}


	@Override
	@Deprecated
	public void readInData(long timeStart, long timeRange, double pixelLength) throws IOException {
		throw new IllegalAccessError("Use readInData(TraceSampling traceSampling) method instead");
	}
}
