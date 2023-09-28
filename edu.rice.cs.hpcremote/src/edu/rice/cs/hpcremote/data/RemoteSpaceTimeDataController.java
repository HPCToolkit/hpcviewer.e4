package edu.rice.cs.hpcremote.data;

import java.io.IOException;

import org.hpctoolkit.client_server_common.time.Timestamp;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;

import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcbase.ITraceDataCollector.TraceOption;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.extdata.IFilteredData;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;

public class RemoteSpaceTimeDataController extends SpaceTimeDataController 
{
	private final HpcClient client;

	public RemoteSpaceTimeDataController(HpcClient client, IExperiment experiment) {
		super(experiment);
		
		this.client = client;
	}

	@Override
	public String getName() {
		return "Remote: " + super.getExperiment().getName();
	}

	@Override
	public void closeDB() {
	}

	
	@Override
	public IFilteredData getTraceData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fillTracesWithData(boolean changedBounds, int numThreadsToLaunch) throws IOException {
		var traceAttr = getTraceDisplayAttribute();
		var frame = traceAttr.getFrame();
		var time1 = Timestamp.ofEpochNano(frame.begTime);
		var time2 = Timestamp.ofEpochNano(frame.endTime);
		
		var setFuture = client.sampleTracesAsync(traceAttr.getProcessInterval(), time1, time2, getPixelHorizontal());
	}


	@Override
	public ITraceDataCollector getTraceDataCollector(int lineNum, IdTuple idTuple) {
		TraceOption traceOption = TraceOption.ORIGINAL_TRACE;
		var idtupleType = getExperiment().getIdTupleType();

		if (TracePreferenceManager.getGPUTraceExposure() && idTuple.isGPU(idtupleType))
			traceOption = TraceOption.REVEAL_GPU_TRACE;

		var dataCollector = new RemoteTraceDataCollector(client, idTuple, getPixelHorizontal(), traceOption);
		
		return dataCollector;
	}
}
