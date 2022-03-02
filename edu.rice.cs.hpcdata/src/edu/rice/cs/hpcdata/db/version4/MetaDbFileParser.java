package edu.rice.cs.hpcdata.db.version4;

import java.io.File;
import java.io.IOException;

import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.ExperimentFile;
import edu.rice.cs.hpcdata.util.IUserData;

public class MetaDbFileParser extends ExperimentFile
{

	@Override
	public File parse(File location, BaseExperiment experiment, boolean need_metrics, IUserData<String, String> userData)
			throws Exception {
		String metaDBFilename;
		if (location.isDirectory()) {
			String directory = location.getAbsolutePath(); // it's a database directory
			metaDBFilename = directory + File.separatorChar + DatabaseManager.getDatabaseFilename("xml").orElse("");
		} else if (location.canRead()){
			metaDBFilename = location.getAbsolutePath();
		} else {
			throw new IOException(location.getName() + ": not readable");
		}
		
		DataMeta data = new DataMeta();
		data.open(metaDBFilename);
		
		DataSummary profileDB = new DataSummary(null);
		
		return null;
	}

}
