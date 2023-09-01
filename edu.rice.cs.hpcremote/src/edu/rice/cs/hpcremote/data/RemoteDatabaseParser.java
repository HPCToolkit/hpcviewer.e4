package edu.rice.cs.hpcremote.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.hpctoolkit.hpcclient.v1_0.HpcClient;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.version4.DataMeta;
import edu.rice.cs.hpcdata.db.version4.IDataProfile;
import edu.rice.cs.hpcdata.db.version4.MetaDbFileParser;
import edu.rice.cs.hpcdata.db.version4.MetricYamlParser;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.util.IUserData;
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
	private IDataProfile dataProfile;
	private MetricYamlParser yamlParser;
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
		dataProfile = collectProfileData(client, dataMeta);
		yamlParser  = collectMetricYAML(client);

		// fix issue #17: decoupling DataMeta and DataSummary:
		// post-processing data gathered in meta.db and profile.db is needed.
		// without this, the metrics are wrong since the root has no data summary
		// and the calling context reassignment is incorrect since the metric is incorrect.
		dataPostProcessing(dataMeta, dataProfile);

		experiment = (Experiment) dataMeta.getExperiment();
		
		// Reset the new list of metric descriptors to the experiment database
		// Note: Metrics are based on the yaml file, not meta.db
		experiment.setMetrics(yamlParser.getListMetrics());
		experiment.setMetricRaw(yamlParser.getRawMetrics());
	}
	
	
	public Experiment getExperiment() {
		return experiment;
	}
	
	
	private DataMeta collectMetaData(HpcClient client) throws IOException, InterruptedException {
		var metaDBbytes   = client.getMetaDbFileContents();
		ByteBuffer buffer = ByteBuffer.wrap(metaDBbytes);
		
		DataMeta data = new DataMeta();
		data.open(buffer);
		
		return data;
	}
	
	
	private IDataProfile collectProfileData(HpcClient client, DataMeta dataMeta) throws IOException, InterruptedException {
		Set<IdTuple> idtuples = client.getHierarchicalIdentifierTuples();
		List<IdTuple> list = new ArrayList<>(idtuples.size());
		idtuples.forEach(list::add);
		
		var data = new RemoteDataProfile(client, dataMeta.getExperiment().getIdTupleType());
		data.setIdTuple(list);
		
		return data;
	}

	
	private MetricYamlParser collectMetricYAML(HpcClient client) throws IOException, InterruptedException {
		var yamlBytes = client.getMetricsDefaultYamlContents();
		var inputStream = new ByteArrayInputStream(yamlBytes);
		
		return new MetricYamlParser(inputStream, dataMeta, dataProfile);
	}	
}
