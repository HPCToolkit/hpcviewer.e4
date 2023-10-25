package edu.rice.cs.hpcremote.data;

import java.io.IOException;

import org.hpctoolkit.hpcclient.v1_0.TraceSampling;

import edu.rice.cs.hpcbase.AbstractTraceDataCollector;
import edu.rice.cs.hpcbase.ITraceDataCollector;
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
		list.toStream().forEach(sample -> {
			var time = sample.getTimestamp();
			var cctId = sample.getCallingContext();
			
			var data = new DataRecord(time.toEpochNano(), cctId.toInt());
			addSampleToLastIndex(data);
		});
		var traceId = traceSampling.getTraceId().toInt();
		System.out.printf( "    [readInData] %3d  num cpid: %3d%n", traceId, getListOfCallpathId().size());
	}


	@Override
	@Deprecated
	public void readInData(long timeStart, long timeRange, double pixelLength) throws IOException {
		// nothing to do
	}
	

	@Override
	public void duplicate(ITraceDataCollector traceData) {
		var obj = new RemoteTraceDataCollectorPerProfile(getNumPixelHorizontal(), getTraceOption());
		obj.setListOfCallpathId(getListOfCallpathId());
	}
}
