package edu.rice.cs.hpctraceviewer.data.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;

import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcdata.experiment.extdata.IFilteredData;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.trace.TraceAttribute;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpcdata.util.MergeDataFiles;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TraceDataByRank;
import edu.rice.cs.hpctraceviewer.data.version2.BaseData;
import edu.rice.cs.hpctraceviewer.data.version2.FilteredBaseData;
import edu.rice.cs.hpctraceviewer.data.version4.FileDB4;


/**
 * The local disk version of the Data controller
 * 
 * @author Philip Taffet
 * 
 */
public class SpaceTimeDataControllerLocal extends SpaceTimeDataController 
{	
	final static private int MIN_TRACE_SIZE = TraceDataByRank.HeaderSzMin + TraceDataByRank.RecordSzMin * 2;
	final static public int RECORD_SIZE    = Constants.SIZEOF_LONG + Constants.SIZEOF_INT;
	
	private String traceFilePath;
	private IFileDB fileDB;

	/************************
	 * Constructor to setup local database
	 * 
	 * @param context IEclipseContext
	 * @param statusMgr  IProgressMonitor
	 * @param databaseDirectory : database directory
	 * @param fileDB IFileDB 
	 * 
	 * @throws InvalExperimentException
	 * @throws Exception
	 */
	public SpaceTimeDataControllerLocal(
			IEclipseContext context, 
			IProgressMonitor statusMgr, 
			String databaseDirectory, 
			IFileDB fileDB) 
			throws InvalExperimentException, Exception 
	{
		super(context, new File(databaseDirectory));
		init(statusMgr, fileDB);
	}
	
	
	/***
	 * Constructor to setup local database
	 * 
	 * @param context IEclipseContext
	 * @param statusMgr IProgressMonitor
	 * @param experiment BaseExperiment
	 * @param fileDB IFileDB
	 * @throws InvalExperimentException
	 * @throws Exception
	 */
	public SpaceTimeDataControllerLocal(
			IEclipseContext context, 
			IProgressMonitor statusMgr, 
			BaseExperiment experiment, 
			IFileDB fileDB)
			throws InvalExperimentException, Exception {
		
		super(context, experiment);
		init(statusMgr, fileDB);
	}
	
	
	/****
	 * Initialize the trace view by opening the trace file according to the version of the database
	 * 
	 * @param statusMgr IProgressMonitor
	 * @param fileDB IFileDB
	 * 
	 * @throws IOException
	 */
	private void init(IProgressMonitor statusMgr, IFileDB fileDB) throws IOException {
		
		final TraceAttribute trAttribute = (TraceAttribute) exp.getTraceAttribute();		
		final int version = exp.getMajorVersion();
		
		if (version == 1 || version == Constants.EXPERIMENT_DENSED_VERSION)
		{	// original format
			traceFilePath = getTraceFile(exp.getDefaultDirectory().getAbsolutePath(), statusMgr);
			fileDB.open(traceFilePath, trAttribute.dbHeaderSize, RECORD_SIZE);
			
		} else if (version == Constants.EXPERIMENT_SPARSE_VERSION) 
		{
			// new format
			String databaseDirectory = exp.getDefaultDirectory().getAbsolutePath(); 
			traceFilePath = databaseDirectory + File.separator + exp.getDbFilename(BaseExperiment.Db_File_Type.DB_TRACE);
			
			RootScope root = (RootScope) exp.getRootScope(RootScopeType.CallingContextTree);
			DataSummary ds = root.getExperiment().getDataSummary();
			
			((FileDB4)fileDB).open(ds, databaseDirectory);
		}
		this.fileDB = fileDB;
		dataTrace 	= new BaseData(fileDB);
	}


	

	@Override
	public IFilteredData createFilteredBaseData() {
		try{
			return new FilteredBaseData(fileDB, 
										((TraceAttribute)exp.getTraceAttribute()).dbHeaderSize, 
										TraceAttribute.DEFAULT_RECORD_SIZE);
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public String getTraceFileAbsolutePath(){
		return traceFilePath;
	}

	@Override
	public void closeDB() {
		dataTrace.dispose();
	}
	
	@Override
	public void dispose() {
		closeDB();
		super.dispose();
	}


	public void fillTracesWithData(boolean changedBounds, int numThreadsToLaunch) {
		//No need to do anything. The data for local is gotten from the file
		//on demand on a per-timeline basis.
	}


	@Override
	public String getName() {
		return exp.getDefaultDirectory().getPath();
	}
	
	/*********************
	 * get the absolute path of the trace file (experiment.mt).
	 * If the file doesn't exist, it is possible it is not merged yet 
	 *  (in this case we'll merge them automatically)
	 * 
	 * @param directory
	 * @param statusMgr
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 *********************/
	static private String getTraceFile(String directory, final IProgressMonitor statusMgr) 
			throws FileNotFoundException, IOException
	{
		final ProgressReport traceReport = new ProgressReport();
		final String outputFile = directory + File.separatorChar + "experiment.mt";
		
		File dirFile = new File(directory);
		final MergeDataFiles.MergeDataAttribute att = MergeDataFiles
													 .merge(dirFile, "*.hpctrace", outputFile, traceReport);
		
		if (att != MergeDataFiles.MergeDataAttribute.FAIL_NO_DATA) {
			File fileTrace = new File(outputFile);
			if (fileTrace.length() > MIN_TRACE_SIZE) {
				return fileTrace.getAbsolutePath();
			}
			
			System.err.println("Warning! Trace file "
					+ fileTrace.getName()
					+ " is too small: "
					+ fileTrace.length() + "bytes .");
		}
		throw new RuntimeException("Trace file does not exist or file is corrupt:" + outputFile);
	}

	/*******************
	 * Progress bar
	 *
	 */
	static private class ProgressReport implements IProgressReport 
	{

		public ProgressReport()
		{
		}
		
		public void begin(String title, int num_tasks) {
			System.out.println(title + " " + num_tasks);
		}

		public void advance() {
			System.out.print(".");
		}

		public void end() {
			System.out.println();
		}
	}

}
