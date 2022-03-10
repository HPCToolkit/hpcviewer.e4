package edu.rice.cs.hpcdata.db.version4;

import java.io.File;

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
		
		DataMeta data = new DataMeta();
		data.open(experiment, directory);
		
		return new File(data.filename);
	}
}
