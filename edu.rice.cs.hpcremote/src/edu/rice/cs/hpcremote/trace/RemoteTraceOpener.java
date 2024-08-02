// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcremote.trace;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hpctoolkit.hpcclient.v1_0.BrokerClient;

import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcremote.IDatabaseRemote;
import edu.rice.cs.hpctraceviewer.data.AbstractDBOpener;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;


public class RemoteTraceOpener extends AbstractDBOpener
{
	private final BrokerClient client;
	private final IExperiment experiment;
	
	
	public RemoteTraceOpener(IDatabaseRemote database) {
		this(database.getClient(), database.getExperimentObject());
	}
	
	public RemoteTraceOpener(BrokerClient client, IMetricManager experiment) {
		this.client = client;
		this.experiment = (IExperiment) experiment;
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
