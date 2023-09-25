package edu.rice.cs.hpcremote.data;

import java.io.IOException;

import org.hpctoolkit.hpcclient.v1_0.HpcClient;

import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.extdata.IFilteredData;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;

public class SpaceTimeDataControllerR extends SpaceTimeDataController 
{
	private final HpcClient client;

	public SpaceTimeDataControllerR(HpcClient client, IExperiment experiment) {
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
	public IFilteredData createFilteredBaseData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fillTracesWithData(boolean changedBounds, int numThreadsToLaunch) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public ITraceDataCollector getTraceDataCollector(int index) {
		// TODO Auto-generated method stub
		return null;
	}

}
