// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.hpctoolkit.hpcclient.v1_0.BrokerClient;
import org.hpctoolkit.hpcclient.v1_0.UnknownCallingContextException;
import org.hpctoolkit.hpcclient.v1_0.UnknownProfileIdException;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.IDatabaseIdentification;
import edu.rice.cs.hpcbase.ProgressReport;
import edu.rice.cs.hpcdata.db.version4.DataMeta;
import edu.rice.cs.hpcdata.db.version4.IDataCCT;
import edu.rice.cs.hpcdata.db.version4.IDataProfile;
import edu.rice.cs.hpcdata.db.version4.MetaDbFileParser;
import edu.rice.cs.hpcdata.db.version4.MetricYamlParser;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.tld.v4.ThreadDataCollection4;
import edu.rice.cs.hpcdata.util.ICallPath;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpcdata.util.IUserData;
import edu.rice.cs.hpcremote.data.profile.RemoteDataProfile;
import edu.rice.cs.hpcremote.data.profile.CollectMetricsReduceScopeVisitor;


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
	public void parse(BrokerClient client, IDatabaseIdentification id) throws IOException, InterruptedException {
		dataMeta = collectMetaData(client, id);
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
	public void parse(BrokerClient client, IExperiment experiment) throws IOException, InterruptedException {
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
	private void collectOtherInformation(BrokerClient client, DataMeta dataMeta, Experiment experiment) throws IOException, InterruptedException {
		
		var dataProfile = collectProfileData(client, dataMeta);		
		var yamlParser  = collectMetricYAML(client, dataProfile);

		reassignMetrics(dataMeta.getRootCCT(), dataProfile, dataMeta.getMetrics());
		
		rearrangeCallingContext(client, experiment.getScopeMap(), dataMeta.getRootCCT());

		var dataPlot = collectCCTData(client);
		var rawMetrics = yamlParser.getRawMetrics();
		
		IThreadDataCollection threadData = new ThreadDataCollection4(dataProfile, dataPlot, rawMetrics.size());
		
		experiment.setThreadData(threadData);
		
		// Reset the new list of metric descriptors to the experiment database
		// Note: Metrics are based on the yaml file, not meta.db
		experiment.setMetrics(yamlParser.getListMetrics());
		experiment.setMetricRaw(rawMetrics);
		experiment.setMinMaxCCTID(0, dataMeta.getMaxNodeId());

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
	 * @throws InterruptedException
	 */
	protected void rearrangeCallingContext(BrokerClient client, ICallPath callpath, RootScope root) throws InterruptedException {
		
		Job task = new Job("Rearrange calling contexts") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				// first, collect the scopes to be merged where we need their metric values
				var progress = new ProgressReport(monitor);

				CollectMetricsReduceScopeVisitor reduceOp = new CollectMetricsReduceScopeVisitor(progress);
				root.dfsVisitScopeTree(reduceOp);
				
				// send query to the server to grab the metrics
				try {
					reduceOp.postProcess(client);
				} catch (UnknownCallingContextException | IOException | UnknownProfileIdException e) {
					progress.end();					
					LoggerFactory.getLogger(getClass()).error("Fail to collect metrics", e);
					return Status.CANCEL_STATUS;

				} catch (InterruptedException e) {
					progress.end();					
				    Thread.currentThread().interrupt();
					return Status.CANCEL_STATUS;
				}

				// finally call the default method to rearrange the cct.
				progress.begin(getName(), (int) (Math.max(20, dataMeta.getNumLineScope()) * 0.1));
				progress.advance();
				
				rearrangeCallingContext(root, callpath, progress);
				
				return Status.OK_STATUS;
			}
		};
		task.schedule();
		
		// make sure it's blocking
		task.join();
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
	private DataMeta collectMetaData(BrokerClient client, IDatabaseIdentification id) throws IOException, InterruptedException {
		// this is a candidate of "hack of the year" award:
		//
		// create the experiment object, and then set the database representation
		// if we don't initialize the database representation, it will "crash" later
		
		Experiment experiment = new Experiment();
		experiment.setDatabaseRepresentation(new RemoteDatabaseRepresentation(client, id.id()));
		
		return collectMetaData(client, experiment);
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
	public DataMeta collectMetaData(BrokerClient client, IExperiment experiment) throws IOException, InterruptedException {
		var metaDBbytes   = client.getMetaDbFileContents();
		ByteBuffer buffer = ByteBuffer.wrap(metaDBbytes);
		
		DataMeta data = new DataMeta(IProgressReport.dummy());
		data.open(experiment, buffer);
		
		return data;
	}
	
	
	private IDataProfile collectProfileData(BrokerClient client, DataMeta dataMeta) throws IOException, InterruptedException {
		return new RemoteDataProfile(client, dataMeta.getExperiment().getIdTupleType());
	}

	
	private MetricYamlParser collectMetricYAML(BrokerClient client, IDataProfile dataProfile) throws IOException, InterruptedException {
		var yamlBytes = client.getMetricsDefaultYamlContents();
		var inputStream = new ByteArrayInputStream(yamlBytes);
		
		return new MetricYamlParser(inputStream, dataMeta, dataProfile);
	}
	
	
	private IDataCCT collectCCTData(final BrokerClient client) {
		return new RemoteDataCCT(client);
	}
}
