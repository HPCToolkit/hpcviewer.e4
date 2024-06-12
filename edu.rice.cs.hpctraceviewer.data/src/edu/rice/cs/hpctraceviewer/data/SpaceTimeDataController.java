package edu.rice.cs.hpctraceviewer.data;

import java.util.concurrent.TimeUnit;

import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpcbase.IFilteredData;
import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcbase.ITraceManager;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.trace.BaseTraceAttribute;
import edu.rice.cs.hpcdata.trace.TraceAttribute;
import edu.rice.cs.hpcdata.util.ICallPath;
import edu.rice.cs.hpctraceviewer.data.color.ColorTable;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimelineService;



/*******************************************************************************************
 * 
 * Class to store global information concerning the database and the trace.
 * The class is designed to work for both local and remote database. Any references have to 
 * 	be addressed to the methods of this class instead of the derived class to enable
 *  transparency.
 * 
 * @author Original authors: Sinchan Banarjee, Michael France, Reed Lundrum and Philip Taffet
 * 
 * Modification:
 * - 2013 Philip: refactoring into three classes : abstract (this class), local and remote
 * - 2014.2.1 Laksono: refactoring to make it as simple as possible and avoid code redundancy
 *
 *******************************************************************************************/
public abstract class SpaceTimeDataController implements ITraceManager
{
	private IExperiment  exp;

	private TraceDisplayAttribute attributes;
	
	private ColorTable colorTable = null;
	private IFilteredData dataTrace = null;
	
	private final ProcessTimelineService timelineService;

	
	/*****
	 * This constructor is used when an Experiment database is already opened, and we just
	 * want to transfer the database to this trace data.
	 * 
	 * @param context IEclipseContext
	 * @param experiment BaseExperiment
	 */
	protected SpaceTimeDataController(IExperiment experiment) 
	{
		exp = experiment;		
		timelineService = new ProcessTimelineService();
		
		// attributes initialization
		attributes = new TraceDisplayAttribute();
		
		final Display display = Display.getDefault();
		display.syncExec( ()-> 
			
			// initialize color table
			// has to be inside UI thread since we create colors
			colorTable = new ColorTable()
		);
	}

	
	/*************************************************************************
	 * Returns the current database's process time line service.
	 * This service is useful to get the next process time line
	 * @return ProcessTimelineService
	 *************************************************************************/
	protected ProcessTimelineService getProcessTimelineService() {
		return timelineService;
	}
	
	
	/***
	 * Set a new trace line into the storage
	 * 
	 * @param line
	 * 			The number of sequence order of the trace
	 * @param trace
	 * 
	 * @return {@code boolean} true if the set is correct, false otherwise.
	 * 
	 */
	public boolean setTraceline(int line, IProcessTimeline trace) {
		if (timelineService != null) {
			return timelineService.setProcessTimeline(line, trace);
		}
		return false;
	}
	
	
	/***
	 * Retrieve a specific trace line
	 * 
	 * @param line
	 * @return {@code IProcessTineline} the trace line if the line is correct, {@code null} otherwise.
	 */
	public IProcessTimeline getTraceline(int line) {
		if (timelineService != null) {
			return timelineService.getProcessTimeline(line);
		}
		return null;
	}
	
	
	/***
	 * Remove the current trace lines, and allocate a new ones
	 * 
	 * @param numTraces
	 * 			The number of the new trace lines
	 */
	public void resetTracelines(int numTraces) {
		timelineService.setProcessTimeline(new IProcessTimeline[numTraces]);
	}
	
	
	/***
	 * Get the number of stored trace lines.
	 * If no trace line has been stored via {@link setTraceline} method, then it returns zero.
	 * 
	 * @return {@code int}
	 */
	public int getNumTracelines() {
		if (timelineService != null) {
			return timelineService.getNumProcessTimeline();
		}
		return 0;
	}
	
	/*************************************************************************
	 * Check if traces have been painted out in the main trace view.
	 * Other views (like depth view) will need to check this.
	 * 
	 * @return
	 *************************************************************************/
	public boolean hasTraces() {
		return getNumTracelines() > 0;
	}
	
	
	/*************************************************************************
	 * Check if the current configuration is the home area.
	 * I don't know why we need this. Perhaps the author can describe better. 
	 * 
	 * @return true if it's home
	 *************************************************************************/
	public boolean isHomeView() {
		
		return (attributes.getProcessBegin() == 0 && attributes.getProcessEnd() == getTotalTraceCount() &&
			    attributes.getTimeBegin()    == 0 && attributes.getTimeEnd()    == getTimeWidth());
	}

	
	/*************************************************************************
	 * Get the position of the current selected process
	 * @return int
	 *************************************************************************/
	private int getCurrentlySelectedProcess()
	{
		return attributes.getPosition().process;
	}
	
	/*************************************************************************
	 * {@link getCurrentlySelectedProcess()} returns something on [begProcess,
	 * endProcess-1]. We need to map that to something on [0, numTracesShown -
	 * 1]. We use a simple linear mapping:
	 * begProcess    -> 0,
	 * endProcess-1  -> numTracesShown-1
	 *************************************************************************/
	public int computeScaledProcess() {
		int numTracesShown = Math.min(attributes.getProcessInterval(), attributes.getPixelVertical());
		int selectedProc = getCurrentlySelectedProcess();
		
		double scaledDTProcess = (((double) numTracesShown -1 )
					/ ((double) attributes.getProcessInterval() - 1) * 
					(selectedProc - attributes.getProcessBegin()));
		return (int)scaledDTProcess;
	}


	/*************************************************************************
	 * get the depth trace of the current "selected" process
	 *  
	 * @return ProcessTimeline
	 *************************************************************************/
	public IProcessTimeline getCurrentSelectedTraceline() {
		int scaledDTProcess = computeScaledProcess();
		return  timelineService.getProcessTimeline(scaledDTProcess);
	}

	
	/*************************************************************************
	 * Get the file base data of this trace
	 * @return {@code IFilteredData}
	 *************************************************************************/
	public IFilteredData getBaseData(){
		return dataTrace;
	}

	/******************************************************************************
	 * Returns number of processes (ProcessTimelines) held in this
	 * SpaceTimeData.
	 ******************************************************************************/
	public int getTotalTraceCount() {		
		return dataTrace.getNumberOfRanks();
		
	}
	
	
	/******************************************************************************
	 * 	Returns the map between cpid and callpath
	 * @return
	 ******************************************************************************/
	public ICallPath getScopeMap() {
		// TODO: we should add getScopeMap in IExperiment interface
		// however, meta.db and trace.db doesn't need it.
		return ((BaseExperiment)exp).getScopeMap();
	}

	/******************************************************************************
	 * getter/setter trace attributes
	 * @return
	 ******************************************************************************/
	
	public int getPixelHorizontal() {
		return attributes.getPixelHorizontal();
	}
	
	
	/**************************************************************************
	 * Returns the experiment database
	 * @return BaseExperiment
	 **************************************************************************/
	public IExperiment getExperiment() {
		return exp;
	}

	
	/*************************************************************************
	 * Returns the trace display attribute
	 * @return {@code ImageTraceAttributes}
	 *************************************************************************/
	public TraceDisplayAttribute getTraceDisplayAttribute() {
		return attributes;
	}


	/*************************************************************************
	 * Returns the size of the trace header file
	 * @return int
	 *************************************************************************/
	public int getHeaderSize() {
		return ((TraceAttribute)exp.getTraceAttribute()).dbHeaderSize;
	}

	/*************************************************************************
	 * Returns width of the spaceTimeData: The width (the last time in the
	 * ProcessTimeline) of the longest ProcessTimeline.
	 ************************************************************************/
	public long getTimeWidth() {
		return getMaxEndTime() - getMinBegTime();
	}

	/*************************************************************************
	 * Get the time end of the trace
	 * @return long
	 *************************************************************************/
	public long getMaxEndTime() {
		return exp.getTraceAttribute().dbTimeMax;
	}

	/*************************************************************************
	 * Get the start time of the trace
	 * @return long
	 *************************************************************************/
	public long getMinBegTime() {
		return exp.getTraceAttribute().dbTimeMin;
	}

	/*************************************************************************
	 * return the unit of the current database
	 * 
	 * @return {@link TraceDisplayAttribute.TimeUnit}
	 *************************************************************************/
	public TimeUnit getTimeUnit() {
		int version = exp.getMajorVersion();
		
		if (version == edu.rice.cs.hpcdata.util.Constants.EXPERIMENT_DENSED_VERSION) {
			if (((BaseExperiment)exp).getMinorVersion() < 2) {
				// old version of database: always microsecond
				return TimeUnit.MICROSECONDS;
			}
			// new version of database:
			// - if the measurement is from old hpcrun: microsecond
			// - if the measurement is from new hpcrun: nanosecond
			
			if (exp.getTraceAttribute().dbUnitTime == BaseTraceAttribute.PER_NANO_SECOND) {
				return TimeUnit.NANOSECONDS;
			}

		} else if (version == edu.rice.cs.hpcdata.util.Constants.EXPERIMENT_SPARSE_VERSION) {
			// newer database: sparse database always uses nanoseconds
			
			return TimeUnit.NANOSECONDS;
		}
		// we have no idea what kind of database is this.
		// this must be an error. Should we raise an exception?
		
		return TimeUnit.MICROSECONDS;
	}
	
	/*************************************************************************
	 * Maximum call depth in the CCT
	 *  
	 * @return
	 *************************************************************************/
	public int getMaxDepth() {
		return ((TraceAttribute)exp.getTraceAttribute()).maxDepth;
	}

	public int getDefaultDepth() {
		return (int)(getMaxDepth() * 0.3);
	}

	
	/*************************************************************************
	 * get the color mapping table
	 * @return
	 *************************************************************************/
	public ColorTable getColorTable() {
		return colorTable;
	}

	
	/*************************************************************************
	 * Dispose allocated resources.
	 * All callers HAS TO call this method when the resource is not needed anymore
	 *************************************************************************/
	public void dispose() {
		if (colorTable != null)
			colorTable.dispose();
		
		if (timelineService != null)
			timelineService.dispose();
		
		if (dataTrace != null)
			dataTrace.dispose();
		
		exp = null;
		colorTable = null;
		attributes = null;
		
		closeDB();
	}

	/*************************************************************************
	 * changing the trace data, caller needs to make sure to refresh the views
	 * @param filteredBaseData
	 *************************************************************************/
	public void setBaseData(IFilteredData filteredBaseData) {
		dataTrace = filteredBaseData;

		int endProcess = attributes.getProcessEnd();
		int begProcess = attributes.getProcessBegin();
		
		//Snap it back into the acceptable limits.
		if (endProcess > dataTrace.getNumberOfRanks())
			endProcess = dataTrace.getNumberOfRanks();
		
		if (begProcess >= endProcess)
			begProcess = 0;
		
		attributes.setProcess(begProcess, endProcess);
	}

	
	/** 
	 * Returns the profile id-tuple to which the line-th line corresponds. 
	 * @param line
	 * 			The trace line sequence
	 * 
	 * @return {@code IdTuple}
	 * 			The profile id-tuple
	 * */
	public IdTuple getProfileFromPixel(int line) {		
		var listProfiles = getBaseData().getListOfIdTuples(IdTupleOption.BRIEF);
		
		int numProfiles = attributes.getProcessInterval();		
		int index = 0;
		
		if (numProfiles > attributes.getPixelVertical()) {
			index = attributes.getProcessBegin() + (line * numProfiles) / attributes.getPixelVertical();
		} else {
			index = attributes.getProcessBegin() + line;
		}
		return listProfiles.get(Math.min(listProfiles.size()-1, index));
	}

	
	////////////////////////////////////////////////////////////////////////////////
	// Abstract methods
	////////////////////////////////////////////////////////////////////////////////
	
	/***
	 * Retrieve the name of the database. The name can be either the path of
	 * the directory, or the name of the profiled application, or both.
	 * <p>
	 * Ideally the name should be unique to distinguish with other databases. 
	 * 
	 * @return String: the name of the database
	 */
	public abstract String getName();

	
	/****
	 * Called when the database is closed.
	 * Dispose resources if necessary.
	 */
	public abstract void closeDB();


	/****
	 * Initialize the trace collector and ready to read traces either 
	 * remotely or locally
	 * 
	 * @param numTraces
	 * 			Number of maximum trace lines
	 * @param changedBounds
	 * 			Whether the bound has changed or not
	 */
	public abstract void startTrace(int numTraces, boolean changedBounds);

	/****
	 * Get the trace data collector associated with this data.
	 * 
	 * @param lineNum
	 * 			A sequence index of trace line to be collected and painted.
	 * 
	 * @param idTuple
	 * 			A unique index, it can be a process number or thread number, or any sequence number
	 * 
	 * @return {@code ITraceDataCollector} 
	 * 			An object to collect data from remote or local for this index
	 */
	public abstract ITraceDataCollector getTraceDataCollector(int lineNum, IdTuple idTuple);
	
	
	/***
	 * Get the next trace line
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract IProcessTimeline getNextTrace() throws Exception;
}