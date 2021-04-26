package edu.rice.cs.hpcdata.experiment;

import java.io.File;

import edu.rice.cs.hpcdata.experiment.xml.ExperimentFileXML;
import edu.rice.cs.hpcdata.util.IUserData;


/***********************************************
 * 
 * Database representation for local data
 *
 ***********************************************/
public class LocalDatabaseRepresentation implements IDatabaseRepresentation 
{
	private File fileExperiment;
	final private IUserData<String, String> userData; 
	final private boolean need_metric;

	/*****
	 * Create a local database representation. T
	 * 
	 * @param location : the location of database. It can be a file or directory
	 * @param userData
	 * @param need_metric
	 */
	public LocalDatabaseRepresentation(File location, 
			IUserData<String, String> userData, 
			boolean need_metric)
	{
		this.fileExperiment = location;
		this.userData		= userData;
		this.need_metric	= need_metric;
	}
	

	@Override
	public void open(BaseExperiment experiment) throws Exception
	{		
		ExperimentFileXML fileXML = new ExperimentFileXML();
		fileExperiment = fileXML.parse(fileExperiment, experiment, need_metric, userData);	
	}

	@Override
	public IDatabaseRepresentation duplicate() {
		// we need to copy the path just in case it will be modified by the caller
		final String path   = this.fileExperiment.getAbsolutePath();
		File fileExperiment = new File(path);
		
		// create a new representation
		LocalDatabaseRepresentation dup = new LocalDatabaseRepresentation(fileExperiment, userData, need_metric);
		
		return dup;
	}

	@Override
	public File getFile() {
		return fileExperiment;
	}

	@Override
	public void setFile(File file) {
		fileExperiment = file;
	}
}
