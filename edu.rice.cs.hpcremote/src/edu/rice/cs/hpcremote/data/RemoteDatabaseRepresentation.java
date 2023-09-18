package edu.rice.cs.hpcremote.data;

import java.io.File;
import java.io.IOException;

import org.hpctoolkit.hpcclient.v1_0.HpcClient;

import edu.rice.cs.hpcdata.experiment.IDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.util.Constants;


public class RemoteDatabaseRepresentation implements IDatabaseRepresentation 
{
	private final HpcClient client;
	private String basicId;
	
	public RemoteDatabaseRepresentation(HpcClient hpcclient, String id) {
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
		parser.collectMetaData(client, experiment);
		
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
		try {
			if (client.isTraceSampleDataAvailable())
				return Constants.EXPERIMENT_SPARSE_VERSION;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
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
