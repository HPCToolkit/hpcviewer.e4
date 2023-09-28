package edu.rice.cs.hpclocal;

import java.io.IOException;
import org.eclipse.core.runtime.IProgressMonitor;
import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.version2.FileDB2;
import edu.rice.cs.hpcdata.db.version4.FileDB4;
import edu.rice.cs.hpcdata.db.version4.MetricValueCollection4;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpctraceviewer.data.AbstractDBOpener;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;


/****************************************************
 * 
 * Class to open local trace database
 *
 ****************************************************/
public class LocalDBOpener extends AbstractDBOpener 
{
	private int version;
	private IExperiment experiment;
	/*****
	 * Prepare opening a database
	 * @param IEclipseContext context
	 * @param experiment2
	 * @throws Exception 
	 */
	public LocalDBOpener(IExperiment experiment2) throws IllegalArgumentException {
		this.experiment = experiment2;
		version = experiment2.getMajorVersion();
		String directory = experiment2.getDirectory();
		
		if (directoryHasTraceData(directory)<=0) {
			throw new IllegalArgumentException("The directory does not contain hpctoolkit database with trace data:"
					+ directory);
		}
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
			MetricValueCollection4 mvc = (MetricValueCollection4) root.getMetricValueCollection();
			fileDB = new FileDB4(experiment, mvc.getDataSummary());
			break;
		default:
			throw new InvalExperimentException("Trace data version is not unknown: " + version);
		}
		return fileDB;
	}
	
	
	@Override
	public SpaceTimeDataController openDBAndCreateSTDC(IProgressMonitor statusMgr) 
			throws InvalExperimentException, IOException {

		// ---------------------------------------------------------------------
		// Try to open the database and refresh the data
		// ---------------------------------------------------------------------
		if (statusMgr != null)
			statusMgr.setTaskName("Opening trace data...");

		IFileDB fileDB = getFileDB();
		
		// prepare the xml experiment and all extended data
		return new SpaceTimeDataControllerLocal(statusMgr, experiment, fileDB);
	}

	
	@Override
	public void end() { /* unused */ }


	/**********************
	 * static method to check if a directory contains hpctoolkit's trace data
	 * 
	 * @param directory 
	 * 			the main database directory
	 * @return int 
	 * 			version of the database if the database is correct and valid
	 * 			   return negative number otherwise
	 */
	public static int directoryHasTraceData(String directory)
	{
		return LocalDatabaseRepresentation.directoryHasTraceData(directory);
	}
}
