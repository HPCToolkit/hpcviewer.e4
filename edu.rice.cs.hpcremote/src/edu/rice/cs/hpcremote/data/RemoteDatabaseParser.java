package edu.rice.cs.hpcremote.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.hpctoolkit.client_server_common.calling_context.CallingContextId;
import org.hpctoolkit.client_server_common.metric.MetricId;
import org.hpctoolkit.client_server_common.profile.ProfileId;
import org.hpctoolkit.hpcclient.v1_0.ContextMeasurementsMap;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.UnknownCallingContextException;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.version4.DataMeta;
import edu.rice.cs.hpcdata.db.version4.DataPlotEntry;
import edu.rice.cs.hpcdata.db.version4.IDataCCT;
import edu.rice.cs.hpcdata.db.version4.IDataProfile;
import edu.rice.cs.hpcdata.db.version4.MetaDbFileParser;
import edu.rice.cs.hpcdata.db.version4.MetricYamlParser;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.tld.v4.ThreadDataCollection4;
import edu.rice.cs.hpcdata.util.IUserData;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;


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
	
	@Override
	public File parse(File location, IExperiment experiment, boolean need_metrics, IUserData<String, String> userData)
			throws Exception {
		throw new IllegalAccessError("Not allowed access: Use parse(HpcClient client) instead.");
	}
	
	
	/****
	 * 
	 * @param client
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void parse(HpcClient client) throws IOException, InterruptedException {
		dataMeta    = collectMetaData(client);
		var dataProfile = collectProfileData(client, dataMeta);
		
		var yamlParser  = collectMetricYAML(client, dataProfile);

		// fix issue #17: decoupling DataMeta and DataSummary:
		// post-processing data gathered in meta.db and profile.db is needed.
		// without this, the metrics are wrong since the root has no data summary
		// and the calling context reassignment is incorrect since the metric is incorrect.
		dataPostProcessing(dataMeta, dataProfile);

		experiment = (Experiment) dataMeta.getExperiment();
		
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
	public DataMeta collectMetaData(HpcClient client) throws IOException, InterruptedException {
		var metaDBbytes   = client.getMetaDbFileContents();
		ByteBuffer buffer = ByteBuffer.wrap(metaDBbytes);
		
		DataMeta data = new DataMeta();
		data.open(buffer);
		
		return data;
	}
	
	
	public DataMeta collectMetaData(HpcClient client, IExperiment experiment) throws IOException, InterruptedException {
		var metaDBbytes   = client.getMetaDbFileContents();
		ByteBuffer buffer = ByteBuffer.wrap(metaDBbytes);
		
		DataMeta data = new DataMeta();
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
		return new IDataCCT() {
			
			@Override
			public DataPlotEntry[] getPlotEntry(int cct, int metric) throws IOException {
				var cctId = CallingContextId.make(cct);
				var setCCT = HashSet.of(cctId);
				
				var metricId = MetricId.make((short) metric);
				var setMetrics = HashSet.of(metricId);
				try {
					var mapValues =  client.getMetrics(setCCT, setMetrics);
					if (!mapValues.isEmpty()) {
						return generatePlotEntry(mapValues, cctId, metricId);
					}
					
				} catch (UnknownCallingContextException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
				    // Restore interrupted state...
				    Thread.currentThread().interrupt();				}
				return new DataPlotEntry[0];
			}
			
			private DataPlotEntry[] generatePlotEntry(Map<ProfileId, ContextMeasurementsMap> mapValues, CallingContextId cctId, MetricId metricId) {
				var size = mapValues.size();
				var entries = new DataPlotEntry[size];
				
				int i=0;
				var iterator = mapValues.iterator();
				while(iterator.hasNext()) {
					var item = iterator.next();
					var profileId = item._1;
					var mapMetric = item._2;
					var metrics = mapMetric.get(cctId);
					if (metrics.isEmpty())
						return new DataPlotEntry[0];
					var metric = metrics.get().getMeasurement(metricId);
					var entry = new DataPlotEntry(profileId.toInt(), metric.get().getValue());
					entries[i] = entry;
					i++;
				}
				return entries;
			}
			
			@Override
			public void close() throws IOException {
				// nothing
			}

			@Override
			public java.util.Set<Integer> getCallingContexts() {
				throw new IllegalAccessError("Not supported yet");
			}
		};
	}
}
