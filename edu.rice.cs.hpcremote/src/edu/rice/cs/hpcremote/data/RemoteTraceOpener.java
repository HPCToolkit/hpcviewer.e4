package edu.rice.cs.hpcremote.data;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;

import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpctraceviewer.data.AbstractDBOpener;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;

public class RemoteTraceOpener extends AbstractDBOpener 
{
	private final HpcClient client;
	
	public RemoteTraceOpener(HpcClient client) {
		this.client = client;
	}

	@Override
	public SpaceTimeDataController openDBAndCreateSTDC(IProgressMonitor statusMgr)
			throws IOException, InvalExperimentException {
		return null;
	}

	@Override
	public void end() {

	}

}
