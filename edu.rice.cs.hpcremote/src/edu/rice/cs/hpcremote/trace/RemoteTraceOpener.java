package edu.rice.cs.hpcremote.trace;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;

import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcremote.IDatabaseRemote;
import edu.rice.cs.hpctraceviewer.data.AbstractDBOpener;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;


public class RemoteTraceOpener extends AbstractDBOpener
{
	private final HpcClient client;
	private final IExperiment experiment;
	
	
	public RemoteTraceOpener(IDatabaseRemote database) {
		this(database.getClient(), database.getExperimentObject());
	}
	
	public RemoteTraceOpener(HpcClient client, IExperiment experiment) {
		this.client = client;
		this.experiment = experiment;
	}

	@Override
	public SpaceTimeDataController openDBAndCreateSTDC(IProgressMonitor statusMgr)
			throws IOException, InvalExperimentException {
		
		return new RemoteSpaceTimeDataController(client, experiment);
	}

	@Override
	public void end() {
		// close experience object?
	}

}
