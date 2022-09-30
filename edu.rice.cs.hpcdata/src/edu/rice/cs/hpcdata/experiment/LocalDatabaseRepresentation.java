package edu.rice.cs.hpcdata.experiment;

import java.io.File;

import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.IUserData;
import edu.rice.cs.hpcdata.util.Util;


/***********************************************
 * 
 * Database representation for local data
 *
 ***********************************************/
public class LocalDatabaseRepresentation implements IDatabaseRepresentation 
{
	private File fileExperiment;
	private final IUserData<String, String> userData; 
	private final boolean needMetric;

	/*****
	 * Create a local database representation. T
	 * 
	 * @param location 
	 * 			the location of database. It can be a file or directory
	 * @param userData
	 * 			User-defined data
	 * @param needMetric
	 * 			true if metrics are needed (for compatibility with old database only)
	 */
	public LocalDatabaseRepresentation(File location, 
			IUserData<String, String> userData, 
			boolean needMetric)
	{
		this.fileExperiment = location;
		this.userData		= userData;
		this.needMetric	    = needMetric;
	}
	

	@Override
	public void open(IExperiment experiment) throws Exception
	{	
		ExperimentFile reader = DatabaseManager.getDatabaseReader(fileExperiment);
		fileExperiment = reader.parse(fileExperiment, experiment, needMetric, userData);	
	}

	@Override
	public IDatabaseRepresentation duplicate() {
		// we need to copy the path just in case it will be modified by the caller
		final String path   = this.fileExperiment.getAbsolutePath();
		
		// create a new representation
		return new LocalDatabaseRepresentation(new File(path), userData, needMetric);
	}

	@Override
	public File getFile() {
		return fileExperiment;
	}

	@Override
	public void setFile(File file) {
		fileExperiment = file;
	}
	

	/**********************
	 * static method to check if a directory contains hpctoolkit's trace data
	 * 
	 * @param directory 
	 * 			the main database directory
	 * @return int 
	 * 			version of the database if the database is correct and valid
	 * 			   return negative number otherwise
	 * 
	 * @see Constants.EXPERIMENT_SPARSE_VERSION
	 * @see Constants.EXPERIMENT_DENSED_VERSION
	 */
	public static int directoryHasTraceData(String directory)
	{
		File file = new File(directory);
		String databaseDirectory;
		if (file.isFile()) {
			// if the argument is a file, then we'll look for its parent directory
			file = file.getParentFile();
			databaseDirectory = file.getAbsolutePath();
		} else {
			databaseDirectory = directory;
		}
		// checking for version 4.0
		String filePath = databaseDirectory + File.separatorChar + Constants.TRACE_FILE_SPARSE_VERSION;
		File tmpFile 	= new File(filePath);
		if (tmpFile.canRead()) {
			return Constants.EXPERIMENT_SPARSE_VERSION;
		}
		
		// checking for version 2.0
		filePath = databaseDirectory + File.separatorChar + "experiment.mt";
		tmpFile  = new File(filePath);
		if (tmpFile.canRead()) {
			return Constants.EXPERIMENT_DENSED_VERSION;
		}
		
		// checking for version 2.0 with old format files
		tmpFile  = new File(databaseDirectory);
		File[] fileHpctraces = tmpFile.listFiles( new Util.FileThreadsMetricFilter("*.hpctrace") );
		if (fileHpctraces != null && fileHpctraces.length>0) {
			return 1;
		}
		return -1;
	}


	@Override
	/****
	 * 
	 * @return int 
	 * 			version of the database if the database is correct and valid
	 * 			   return negative number otherwise
	 * 
	 * @see Constants.EXPERIMENT_SPARSE_VERSION
	 * @see Constants.EXPERIMENT_DENSED_VERSION
	 */
	public int getTraceDataVersion() {
		return directoryHasTraceData(fileExperiment.getAbsolutePath());
	}
}
