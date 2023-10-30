package edu.rice.cs.hpclocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.rice.cs.hpcbase.BaseConstants;
import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcbase.ITraceDataCollector.TraceOption;
import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.trace.TraceAttribute;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpcdata.util.MergeDataFiles;
import edu.rice.cs.hpcdata.util.MergeDataFiles.MergeDataAttribute;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;


/**
 * The local disk version of the Data controller
 * 
 * @author Philip Taffet
 * 
 * @author Refined, updated, refactored by other developers
 * 
 */
public class SpaceTimeDataControllerLocal extends SpaceTimeDataController 
{	
	private static final int MIN_TRACE_SIZE = LocalTraceDataCollector.HeaderSzMin + BaseConstants.TRACE_RECORD_SIZE * 2;
	private static final int RECORD_SIZE    = Constants.SIZEOF_LONG + Constants.SIZEOF_INT;

	private IFileDB fileDB;
	
	private AtomicInteger currentLine;
	private boolean changedBounds;
	
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
	 * @throws IOException
	 */
	public SpaceTimeDataControllerLocal(
			IProgressMonitor statusMgr, 
			IExperiment experiment, 
			IFileDB fileDB)
					throws IOException {
		super(experiment);
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
		final var exp = getExperiment();
		final TraceAttribute trAttribute = (TraceAttribute) exp.getTraceAttribute();		
		final int version = exp.getMajorVersion();
		var location = Path.of(exp.getDirectory()).toFile();
		String traceFilePath = location.getAbsolutePath();
		
		if (version == 1 || version == Constants.EXPERIMENT_DENSED_VERSION)
		{	
			// original format: we may need to merge the files
			traceFilePath = getTraceFile(traceFilePath, statusMgr);			
		} 
		else if (version != Constants.EXPERIMENT_SPARSE_VERSION) 
		{
			throw new IllegalAccessError("Unknown database version: " + version);
		}
		fileDB.open(traceFilePath, trAttribute.dbHeaderSize, RECORD_SIZE);
		this.fileDB = fileDB;
		
		var dataTrace  = new FilteredBaseData(fileDB);
		super.setBaseData(dataTrace);
	}


	@Override
	public void startTrace(int numTraces, boolean changedBounds) {
		this.changedBounds = changedBounds;				
		currentLine = new AtomicInteger(numTraces);
	}


	@Override
	public IProcessTimeline getNextTrace() throws Exception {
		var line = currentLine.decrementAndGet();
		if (line < 0)
			return null;

		if (changedBounds) {			
			var profile = super.getProfileFromPixel(line);
			return new ProcessTimeline(line, this, profile);
		}
		return getProcessTimelineService().getProcessTimeline(line);
	}

	
	@Override
	public void closeDB() {
		if (fileDB != null)
			fileDB.dispose();
		
		fileDB = null;
	}


	@Override
	public String getName() {
		return getExperiment().getDirectory();
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
	 *********************/
	private static String getTraceFile(String directory, final IProgressMonitor statusMgr) 
			throws IOException
	{
		final ProgressReport traceReport = new ProgressReport(statusMgr);
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
	 * Dummy Progress bar.
	 * Should be supplied by the UI
	 *******************/
	private static class ProgressReport implements IProgressReport 
	{
		private final IProgressMonitor statusMgr;
		
		public ProgressReport(IProgressMonitor statusMgr)
		{
			this.statusMgr = statusMgr;
		}
		
		public void begin(String title, int num_tasks) {
			statusMgr.subTask(title);
		}

		public void advance() {
			statusMgr.worked(1);
		}

		public void end() {
			statusMgr.done();
		}
	}

	@Override
	public ITraceDataCollector getTraceDataCollector(int lineNum, IdTuple idtuple) {
		var idtupleType = getExperiment().getIdTupleType();
		boolean isGpuTrace = idtuple.isGPU(idtupleType);

		TraceOption traceOption = TraceOption.ORIGINAL_TRACE;

		if (TracePreferenceManager.getGPUTraceExposure() && isGpuTrace)
			traceOption = TraceOption.REVEAL_GPU_TRACE;
		
		return new LocalTraceDataCollector(getBaseData(), idtuple, getPixelHorizontal(), traceOption);
	}
}
