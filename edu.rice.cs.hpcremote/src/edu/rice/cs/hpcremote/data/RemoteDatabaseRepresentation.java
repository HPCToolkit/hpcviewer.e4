// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.data;

import java.io.File;
import java.io.IOException;

import org.hpctoolkit.hpcclient.v1_0.BrokerClient;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.experiment.IDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.util.Constants;


public class RemoteDatabaseRepresentation implements IDatabaseRepresentation 
{
	private final BrokerClient client;
	private String basicId;
	
	/**
	 * Caching the trace version so we can avoid sending request to the host
	 * all the time which reduce the performance significantly
	 */
	private int traceVersion = Integer.MIN_VALUE;
	
	public RemoteDatabaseRepresentation(BrokerClient hpcclient, String id) {
		this.client = hpcclient;
		this.basicId = id;
	}

	@Override
	public File getFile() {
		return new File(".");
	}

	@Override
	public void setFile(File file) {
		// should we do something here?
		// we only care remote file, not the local one.
	}

	@Override
	public void open(IExperiment experiment) throws Exception {
		RemoteDatabaseParser parser = new RemoteDatabaseParser();
		parser.parse(client, experiment);
		
		var dir = experiment == null ? "/" : experiment.getDirectory();
		basicId += dir;
	}

	@Override
	public void reopen(IExperiment experiment) throws Exception {
		open(experiment);
	}

	@Override
	public IDatabaseRepresentation duplicate() {
		return new RemoteDatabaseRepresentation(client, basicId);
	}

	@Override
	public int getTraceDataVersion() {
		if (traceVersion >= 0)
			return traceVersion;
		
		traceVersion = 0;
		try {
			if (client.isTraceSampleDataAvailable())
				traceVersion = Constants.EXPERIMENT_SPARSE_VERSION;
		} catch (IOException | InterruptedException e) {
			LoggerFactory.getLogger(getClass()).error("Fail to get trace version", e);
		}
		return traceVersion;
	}

	@Override
	public String getId() {
		return "Remote: " + basicId;
	}

	@Override
	public void close() throws IOException {
		// no op
	}

}
