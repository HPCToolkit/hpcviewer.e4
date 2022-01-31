package edu.rice.cs.hpcdata.db.version4;

import java.io.File;

import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.util.IUserData;

public class DataMetaLocal extends LocalDatabaseRepresentation 
{
	private DataMeta data;
	
	public DataMetaLocal(File location, IUserData<String, String> userData, boolean need_metric) {
		super(location, userData, need_metric);
	}

	
	@Override
	public void open(BaseExperiment experiment) throws Exception {
		data = new DataMeta();
	}

	public DataMeta getMetaData() {
		return data;
	}
	
}
