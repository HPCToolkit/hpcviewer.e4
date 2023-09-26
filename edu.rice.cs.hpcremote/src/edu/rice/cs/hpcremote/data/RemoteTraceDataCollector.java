package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import org.hpctoolkit.client_server_common.time.Timestamp;
import org.hpctoolkit.client_server_common.trace.TraceId;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;

import edu.rice.cs.hpcbase.AbstractTraceDataCollector;
import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import io.vavr.collection.Set;
import io.vavr.collection.HashSet;

public class RemoteTraceDataCollector extends AbstractTraceDataCollector 
{

	private final HpcClient hpcClient;
	private final Set<TraceId> setOfTraceId;

	public RemoteTraceDataCollector(HpcClient hpcClient, int profileIndex, int numPixelH) {
		super(numPixelH);
		
		this.hpcClient = hpcClient;

		var traceId    = TraceId.make(profileIndex);
		setOfTraceId   = HashSet.of(traceId);		
		
		var option = isGPU() && TracePreferenceManager.getGPUTraceExposure() ?
				  	ITraceDataCollector.TraceOption.REVEAL_GPU_TRACE :
					ITraceDataCollector.TraceOption.ORIGINAL_TRACE;
		
		setTraceOption(option);
	}
	

	@Override
	public void readInData(long timeStart, long timeRange, double pixelLength) throws IOException {
		Timestamp t1 = Timestamp.ofEpochNano(timeStart);
		Timestamp t2 = Timestamp.ofEpochNano(timeRange + timeRange);
		
		hpcClient.sampleTracesAsync(setOfTraceId, t1, t2, (int) pixelLength);
	}
	

	@Override
	public void duplicate(ITraceDataCollector traceData) {
		var obj = new RemoteTraceDataCollector(hpcClient, setOfTraceId.get().toInt(), getNumPixelHorizontal());
		obj.setListOfCallpathId(getListOfCallpathId());
	}


	@Override
	public boolean isGPU() {
		// TODO Auto-generated method stub
		return false;
	}
}
