// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpclocal;

import java.io.IOException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.hpctoolkit.db.local.db.IFileDB;
import org.hpctoolkit.db.local.db.version2.TraceDB2;
import org.hpctoolkit.db.local.db.version4.FileDB4;
import org.hpctoolkit.db.local.db.version4.MetricValueCollection4;
import org.hpctoolkit.db.local.experiment.Experiment;
import org.hpctoolkit.db.local.experiment.IExperiment;
import org.hpctoolkit.db.local.experiment.InvalExperimentException;
import org.hpctoolkit.db.local.experiment.LocalDatabaseRepresentation;
import org.hpctoolkit.db.local.experiment.scope.RootScopeType;
import org.hpctoolkit.db.local.util.Constants;
import edu.rice.cs.hpctraceviewer.data.AbstractDBOpener;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;


/****************************************************
 * 
 * Class to open local trace database
 *
 ****************************************************/
public class LocalDBOpener extends AbstractDBOpener 
{
	private IExperiment experiment;
	
	
	/*****
	 * Prepare opening a database
	 * @param IEclipseContext context
	 * @param experiment2
	 * @throws Exception 
	 */
	public LocalDBOpener(IExperiment experiment2) throws IllegalArgumentException {
		this.experiment = experiment2;
		String directory = experiment2.getDirectory();
		
		if (LocalDatabaseRepresentation.directoryHasTraceData(directory)<=0) {
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
		var version = experiment.getMajorVersion();
		Experiment exp = (Experiment) experiment;
		
		switch (version)
		{
		case 1, Constants.EXPERIMENT_DENSED_VERSION:
			fileDB = new TraceDB2(exp);
			break;
		
		case 3,	Constants.EXPERIMENT_SPARSE_VERSION:
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
		return new SpaceTimeDataControllerLocal(experiment, fileDB);
	}

	
	@Override
	public void end() { /* unused */ }
}
