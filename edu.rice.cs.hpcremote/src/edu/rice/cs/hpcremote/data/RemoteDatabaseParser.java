package edu.rice.cs.hpcremote.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.UnknownCallingContextException;
import edu.rice.cs.hpcdata.db.version4.DataMeta;
import edu.rice.cs.hpcdata.db.version4.IDataCCT;
import edu.rice.cs.hpcdata.db.version4.IDataProfile;
import edu.rice.cs.hpcdata.db.version4.MetaDbFileParser;
import edu.rice.cs.hpcdata.db.version4.MetricYamlParser;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.tld.v4.ThreadDataCollection4;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpcdata.util.IUserData;
import edu.rice.cs.hpcremote.data.profile.ScopeToReduceCollection;


/***********
 * 
 * The remote version of database parser.
 * <br/>
 * To use this class, call {@code parse(HpcClient)}  and then retrieve 
 * the needed object attributes such as Experiment object.
 * 
 */
public class RemoteDatabaseParser extends MetaDbFileParser 
{
	private DataMeta dataMeta;
	private Experiment experiment;
	
	/*****
	 * @apiNote Use the other {@code parse(HpcClient} method for remote database
	 * @deprecated not to be used for remote database
	 * 
	 */
	@Override
	@Deprecated(since="7.0", forRemoval = true)
	public File parse(File location, IExperiment experiment, IProgressReport progress, IUserData<String, String> userData)
			throws Exception {
		throw new IllegalAccessError("Not allowed access: Use parse(HpcClient client) instead.");
	}
	
	
	/****
	 * Retrieve a remote database and set up everything needed to get metrics and traces
	 *  
	 * @param client
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void parse(HpcClient client) throws IOException, InterruptedException {
		dataMeta = collectMetaData(client);
		experiment = (Experiment) dataMeta.getExperiment();
		collectOtherInformation(client, dataMeta, experiment);
	}
	
	
	/***
	 * Retrieve a remote database and set the specified experiment object with the new remote data
	 * @param client
	 * @param experiment
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void parse(HpcClient client, IExperiment experiment) throws IOException, InterruptedException {
		dataMeta = collectMetaData(client, experiment);
		this.experiment = (Experiment) experiment;
		collectOtherInformation(client, dataMeta, this.experiment);
	}
	
	
	/****
	 * Post processing after downloading meta.db from remote.
	 * This method will gather some info in profile.db and metric.yaml file.
	 * 
	 * @param client
	 * @param dataMeta
	 * @param experiment
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void collectOtherInformation(HpcClient client, DataMeta dataMeta, Experiment experiment) throws IOException, InterruptedException {
		
		var dataProfile = collectProfileData(client, dataMeta);		
		var yamlParser  = collectMetricYAML(client, dataProfile);

		reassignMetrics(dataMeta.getRootCCT(), dataProfile, dataMeta.getMetrics());
		
		// fix issue #17: decoupling DataMeta and DataSummary:
		// post-processing data gathered in meta.db and profile.db is needed.
		// without this, the metrics are wrong since the root has no data summary
		// and the calling context reassignment is incorrect since the metric is incorrect.
		try {
			rearrangeCallingContext(client, dataMeta.getRootCCT(), yamlParser.getListMetrics());
		} catch (UnknownCallingContextException e) {
			// this shouldn't happen, unless the database is corrupted
			throw new IOException(e);
		}

		var dataPlot = collectCCTData(client);
		var rawMetrics = yamlParser.getRawMetrics();
		
		IThreadDataCollection threadData = new ThreadDataCollection4(dataProfile, dataPlot, rawMetrics.size());
		
		experiment.setThreadData(threadData);
		
		// Reset the new list of metric descriptors to the experiment database
		// Note: Metrics are based on the yaml file, not meta.db
		experiment.setMetrics(yamlParser.getListMetrics());
		experiment.setMetricRaw(rawMetrics);
		
		experiment.postprocess();
	}
	
	
	
	/***
	 * The remote version of {@code rearrangeCallingContext} by collecting scopes to be merged
	 * and then request the metrics to the server.
	 * 
	 * @param client
	 * @param root
	 * @param metrics
	 * 
	 * @throws UnknownCallingContextException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected void rearrangeCallingContext(HpcClient client, RootScope root, List<BaseMetric> metrics) 
			throws UnknownCallingContextException, IOException, InterruptedException {

		// first, collect the scopes to be merged where we need their metric values
		ScopeToReduceCollection reduceOp = new ScopeToReduceCollection();
		root.dfsVisitScopeTree(reduceOp);
		
		// send query to the server to grab the metrics
		reduceOp.postProcess(client, metrics);
		
		// finally call the default method to rearrange the cct.
		super.rearrangeCallingContext(root, IProgressReport.dummy());
	}

	
	/***
	 * Retrieve the experiment object.
	 * User needs to call {@code parse} method first before calling this one.
	 * 
	 * @return
	 */
	public Experiment getExperiment() {
		return experiment;
	}
	
	
	/****
	 * Special method to get the remote meta.db file
	 * 
	 * @param client
	 * 
	 * @return {@code DataMeta}
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private DataMeta collectMetaData(HpcClient client) throws IOException, InterruptedException {
		var metaDBbytes   = client.getMetaDbFileContents();
		ByteBuffer buffer = ByteBuffer.wrap(metaDBbytes);
		
		DataMeta data = new DataMeta(IProgressReport.dummy());
		data.open(buffer);
		
		return data;
	}
	
	
	/****
	 * Reading remote meta.db given the experiment object
	 *  
	 * @param client
	 * @param experiment
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public DataMeta collectMetaData(HpcClient client, IExperiment experiment) throws IOException, InterruptedException {
		var metaDBbytes   = client.getMetaDbFileContents();
		ByteBuffer buffer = ByteBuffer.wrap(metaDBbytes);
		
		DataMeta data = new DataMeta(IProgressReport.dummy());
		data.open(experiment, buffer);
		
		return data;
	}
	
	
	private IDataProfile collectProfileData(HpcClient client, DataMeta dataMeta) throws IOException, InterruptedException {
		return new RemoteDataProfile(client, dataMeta.getExperiment().getIdTupleType());
	}

	
	private MetricYamlParser collectMetricYAML(HpcClient client, IDataProfile dataProfile) throws IOException, InterruptedException {
		var yamlBytes = client.getMetricsDefaultYamlContents();
		var inputStream = new ByteArrayInputStream(yamlBytes);
		
		return new MetricYamlParser(inputStream, dataMeta, dataProfile);
	}
	
	
	private IDataCCT collectCCTData(final HpcClient client) {
		return new RemoteDataCCT(client);
	}
}
