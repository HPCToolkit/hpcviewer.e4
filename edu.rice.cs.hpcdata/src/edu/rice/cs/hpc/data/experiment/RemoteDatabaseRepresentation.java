package edu.rice.cs.hpc.data.experiment;

import java.io.File;
import java.io.InputStream;

import edu.rice.cs.hpc.data.experiment.xml.ExperimentFileXML;
import edu.rice.cs.hpc.data.util.IUserData;

/***********************************************
 * 
 * Database representation for remote data
 *
 ***********************************************/
public class RemoteDatabaseRepresentation implements IDatabaseRepresentation 
{
	final private InputStream expStream;
	final private IUserData<String, String> userData;
	final private String name;
	private File fileExperiment;

	public RemoteDatabaseRepresentation( 
			InputStream expStream, 
			IUserData<String, String> userData,
			String name)
	{
		this.expStream 	= expStream;
		this.userData  	= userData;
		this.name		= name;
	}
	

	@Override
	public void open(BaseExperiment experiment) throws Exception {
		
		ExperimentFileXML fileXML = new ExperimentFileXML();
		fileXML.parse(expStream, name, experiment, false, userData);
	}

	@Override
	public IDatabaseRepresentation duplicate() {
		RemoteDatabaseRepresentation dup = new RemoteDatabaseRepresentation(expStream, userData, name);	
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
