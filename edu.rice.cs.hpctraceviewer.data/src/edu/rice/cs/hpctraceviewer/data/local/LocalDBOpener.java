package edu.rice.cs.hpctraceviewer.data.local;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;

import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.version2.FileDB2;
import edu.rice.cs.hpcdata.db.version4.DataTrace;
import edu.rice.cs.hpcdata.db.version4.MetricValueCollection3;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.Util;
import edu.rice.cs.hpctraceviewer.data.AbstractDBOpener;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.version4.FileDB4;


/****************************************************
 * 
 * Class to open local trace database
 *
 ****************************************************/
public class LocalDBOpener extends AbstractDBOpener 
{
	private int version;
	private IExperiment experiment;
	private final IEclipseContext context;
	

	
	/*****
	 * Prepare opening a database
	 * @param IEclipseContext context
	 * @param experiment2
	 * @throws Exception 
	 */
	public LocalDBOpener(IEclipseContext context, IExperiment experiment2) throws Exception {
		this.context   = context;
		this.experiment = experiment2;
		version = experiment2.getMajorVersion();
		String directory = experiment2.getPath();
		if (directoryHasTraceData(directory)<=0) {
			throw new Exception("The directory does not contain hpctoolkit database with trace data:"
					+ directory);
		}
	}
	
	
	@Override
	public int getVersion() {
		return version;
	}

	
	/****
	 * Create an instance of {@code IFileDB} depending on the database version 
	 * @return IFileDB
	 * @throws InvalExperimentException
	 * @throws IOException 
	 */
	private IFileDB getFileDB() throws InvalExperimentException, IOException {
		IFileDB fileDB = null;
		switch (version)
		{
		case 1:
		case Constants.EXPERIMENT_DENSED_VERSION:
			fileDB = new FileDB2();
			break;
		case 3:
		case Constants.EXPERIMENT_SPARSE_VERSION:
			Experiment exp = (Experiment) experiment;
			var root = exp.getRootScope(RootScopeType.CallingContextTree);
			MetricValueCollection3 mvc = (MetricValueCollection3) root.getMetricValueCollection();
			fileDB = new FileDB4(mvc.getDataSummary());
			break;
		default:
			throw new InvalExperimentException("Trace data version is not unknown: " + version);
		}
		return fileDB;
	}
	
	
	@Override
	public SpaceTimeDataController openDBAndCreateSTDC(IProgressMonitor statusMgr)
			throws IOException, InvalExperimentException, Exception {

		// ---------------------------------------------------------------------
		// Try to open the database and refresh the data
		// ---------------------------------------------------------------------
		if (statusMgr != null)
			statusMgr.setTaskName("Opening trace data...");

		IFileDB fileDB = getFileDB();
		
		// prepare the xml experiment and all extended data
		return new SpaceTimeDataControllerLocal(context, statusMgr, experiment, fileDB);
	}

	
	@Override
	public void end() {}


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
		String file_path = database_directory + File.separatorChar + DataTrace.FILE_TRACE_DB;
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
}
