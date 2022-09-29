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
	public void open(IExperiment experiment) throws Exception
	{	
		ExperimentFile reader = DatabaseManager.getDatabaseReader(fileExperiment);
		fileExperiment = reader.parse(fileExperiment, experiment, need_metric, userData);	
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
	

	/**********************
	 * static method to check if a directory contains hpctoolkit's trace data
	 * 
	 * @param directory 
	 * 			the main database directory
	 * @return int 
	 * 			version of the database if the database is correct and valid
	 * 			   return negative number otherwise
	 */
	static public int directoryHasTraceData(String directory)
	{
		File file = new File(directory);
		String database_directory;
		if (file.isFile()) {
			// if the argument is a file, then we'll look for its parent directory
			file = file.getParentFile();
			database_directory = file.getAbsolutePath();
		} else {
			database_directory = directory;
		}
		// checking for version 4.0
		String file_path = database_directory + File.separatorChar + Constants.TRACE_FILE_SPARSE_VERSION;
		File tmp_file 	 = new File(file_path);
		if (tmp_file.canRead()) {
			return Constants.EXPERIMENT_SPARSE_VERSION;
		}
		
		// checking for version 2.0
		file_path = database_directory + File.separatorChar + "experiment.mt";
		tmp_file  = new File(file_path);
		if (tmp_file.canRead()) {
			return Constants.EXPERIMENT_DENSED_VERSION;
		}
		
		// checking for version 2.0 with old format files
		tmp_file  = new File(database_directory);
		File[] file_hpctraces = tmp_file.listFiles( new Util.FileThreadsMetricFilter("*.hpctrace") );
		if (file_hpctraces != null && file_hpctraces.length>0) {
			return 1;
		}
		return -1;
	}


	@Override
	public int getTraceDataVersion() {
		return directoryHasTraceData(fileExperiment.getAbsolutePath());
	}
}
