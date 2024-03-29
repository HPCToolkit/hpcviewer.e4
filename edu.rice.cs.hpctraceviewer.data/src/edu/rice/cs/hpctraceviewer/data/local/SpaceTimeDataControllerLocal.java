package edu.rice.cs.hpctraceviewer.data.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcdata.experiment.extdata.IFilteredData;
import edu.rice.cs.hpcdata.trace.TraceAttribute;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpcdata.util.MergeDataFiles;
import edu.rice.cs.hpcdata.util.MergeDataFiles.MergeDataAttribute;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TraceDataByRank;
import edu.rice.cs.hpctraceviewer.data.version2.BaseData;
import edu.rice.cs.hpctraceviewer.data.version2.FilteredBaseData;


/**
 * The local disk version of the Data controller
 * 
 * @author Philip Taffet
 * 
 */
public class SpaceTimeDataControllerLocal extends SpaceTimeDataController 
{	
	private final static int MIN_TRACE_SIZE = TraceDataByRank.HeaderSzMin + TraceDataByRank.RecordSzMin * 2;
	private final static int RECORD_SIZE    = Constants.SIZEOF_LONG + Constants.SIZEOF_INT;

	private IFileDB fileDB;
	
	/***
	 * Constructor to setup local database
	 * 
	 * @param context 
	 * 			IEclipseContext
	 * @param statusMgr 
	 * 			IProgressMonitor
	 * @param experiment 
	 * 			IExperiment
	 * @param fileDB 
	 * 			IFileDB
	 * 
	 * @throws InvalExperimentException
	 * @throws Exception
	 */
	public SpaceTimeDataControllerLocal(
			IEclipseContext context, 
			IProgressMonitor statusMgr, 
			IExperiment experiment, 
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
		var location = Path.of(exp.getPath()).getParent().toFile();
		String traceFilePath = location.getAbsolutePath();
		
		if (version == 1 || version == Constants.EXPERIMENT_DENSED_VERSION)
		{	
			// original format: we may need to merge the files
			traceFilePath = getTraceFile(traceFilePath, statusMgr);			
		} 
		else if (version != Constants.EXPERIMENT_SPARSE_VERSION) 
		{
			throw new RuntimeException("Unknown database version: " + version);
		}
		fileDB.open(traceFilePath, trAttribute.dbHeaderSize, RECORD_SIZE);
		this.fileDB = fileDB;
		
		// TODO: ugly code
		dataTrace  = new BaseData(getFileDB());  
	}


	@Override
	public IFilteredData createFilteredBaseData() {
		try{
			return new FilteredBaseData(fileDB, 
										((TraceAttribute)exp.getTraceAttribute()).dbHeaderSize, 
										TraceAttribute.DEFAULT_RECORD_SIZE);
		}
		catch (Exception e){
			var log = LoggerFactory.getLogger(getClass());
			log.error(e.getMessage());
		}
		return null;
	}


	@Override
	protected IFileDB getFileDB() {
		return fileDB;
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
		return exp.getPath();
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
		
		if (att == MergeDataFiles.MergeDataAttribute.SUCCESS_ALREADY_CREATED ||
			att == MergeDataFiles.MergeDataAttribute.SUCCESS_MERGED) {
			File fileTrace = new File(outputFile);
			if (fileTrace.length() > MIN_TRACE_SIZE) {
				return fileTrace.getAbsolutePath();
			}
		}
		if (att == MergeDataAttribute.FAIL_NOT_WRITABLE) 
			throw new RuntimeException(directory + ": Directory is not writable");
		
		throw new RuntimeException(directory + ": Directory has no trace data or trace files are invalid");
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
