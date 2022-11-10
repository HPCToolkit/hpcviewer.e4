package edu.rice.cs.hpcdata.db.version4;

import java.io.File;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.ExperimentFile;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.util.IUserData;

public class MetaDbFileParser extends ExperimentFile
{

	@Override
	public File parse(File location, IExperiment experiment, boolean need_metrics, IUserData<String, String> userData)
			throws Exception {
		String directory;
		if (location.isFile()) {
			directory = location.getParent();
		} else {
			directory = location.getAbsolutePath();
		}
		
		final DataMeta data = new DataMeta();
		data.open(experiment, directory);
		
		MetricYamlParser yamlParser = new MetricYamlParser(directory, data.getDataSummary(), data.getMetrics());		
		assert(yamlParser.getVersion() >= 0);

		// Reset the new list of metric descriptors to the experiment database  
		// Note: Metrics are based on the yaml file, not meta.db
		((Experiment)experiment).setMetrics(yamlParser.getListMetrics());
		((Experiment)experiment).setMetricRaw(data.getRawMerics());

		return new File(data.filename);
	}
}
