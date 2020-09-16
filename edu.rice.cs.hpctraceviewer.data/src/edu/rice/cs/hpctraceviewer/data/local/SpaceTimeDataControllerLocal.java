package edu.rice.cs.hpctraceviewer.data.local;

import java.io.File;
import java.io.IOException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.IFileDB;
import edu.rice.cs.hpc.data.experiment.extdata.IFilteredData;
import edu.rice.cs.hpc.data.trace.TraceAttribute;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.IProgressReport;
import edu.rice.cs.hpc.data.util.MergeDataFiles;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TraceDataByRank;
import edu.rice.cs.hpctraceviewer.data.version2.BaseData;
import edu.rice.cs.hpctraceviewer.data.version2.FilteredBaseData;
import edu.rice.cs.hpctraceviewer.data.version3.FileDB3;


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
	 * @param _window : the current active window
	 * @param databaseDirectory : database directory
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
	
	
	public SpaceTimeDataControllerLocal(
			IEclipseContext context, 
			BaseExperiment experiment, 
			IProgressMonitor statusMgr, 
			IFileDB fileDB)
			throws InvalExperimentException, Exception {
		
		super(context, experiment);
		init(statusMgr, fileDB);
	}
	
	
	private void init(IProgressMonitor statusMgr, IFileDB fileDB) throws IOException {
		
		final TraceAttribute trAttribute = exp.getTraceAttribute();		
		final int version = exp.getMajorVersion();
		if (version == 1 || version == 2)
		{	// original format
			traceFilePath = getTraceFile(exp.getDefaultDirectory().getAbsolutePath(), statusMgr);
			fileDB.open(traceFilePath, trAttribute.dbHeaderSize, RECORD_SIZE);
			
		} else if (version == Constants.EXPERIMENT_SPARSE_VERSION) 
		{
			// new format
			String databaseDirectory = exp.getDefaultDirectory().getAbsolutePath(); 
			traceFilePath = databaseDirectory + File.separator + exp.getDbFilename(BaseExperiment.Db_File_Type.DB_TRACE);
			((FileDB3)fileDB).open(databaseDirectory);
		}
		this.fileDB = fileDB;
		dataTrace 	= new BaseData(fileDB);
	}

	/*********************
	 * get the absolute path of the trace file (experiment.mt).
	 * If the file doesn't exist, it is possible it is not merged yet 
	 *  (in this case we'll merge them automatically)
	 * 
	 * @param directory
	 * @param statusMgr
	 * @return
	 *********************/
	static private String getTraceFile(String directory, final IProgressMonitor statusMgr)
	{
		final ProgressReport traceReport = new ProgressReport();
		final String outputFile = directory + File.separatorChar + "experiment.mt";
		
		try {
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
			System.err
					.println("Error: trace file(s) does not exist or fail to open "
							+ outputFile);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	

	@Override
	public IFilteredData createFilteredBaseData() {
		try{
			return new FilteredBaseData(fileDB, 
					exp.getTraceAttribute().dbHeaderSize, TraceAttribute.DEFAULT_RECORD_SIZE);
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
