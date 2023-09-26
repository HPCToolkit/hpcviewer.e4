package edu.rice.cs.hpctraceviewer.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.extdata.IBaseData;
import edu.rice.cs.hpcdata.experiment.extdata.IFilteredData;
import edu.rice.cs.hpcdata.trace.BaseTraceAttribute;
import edu.rice.cs.hpcdata.trace.TraceAttribute;
import edu.rice.cs.hpcdata.util.ICallPath;
import edu.rice.cs.hpctraceviewer.data.color.ColorTable;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;
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
public abstract class SpaceTimeDataController 
{
	protected IExperiment  exp;

	protected TraceDisplayAttribute attributes;
	
	protected ColorTable colorTable = null;
	protected IBaseData  dataTrace  = null;
	
	protected ProcessTimelineService timelineService;
	
	// nathan's data index variable
	// TODO: we need to remove this and delegate to the inherited class instead !
	//private int currentDataIdx;


	
	/*****
	 * Constructor to create a data based on input stream, which is convenient for remote database
	 * 
	 * @param context : IEclipseContext
	 * @param expStream : input stream
	 * @param Name : the name of the file on the remote server
	 *****/
	public SpaceTimeDataController(InputStream expStream) 
	{
		this(new Experiment());
	}
	
	/*****
	 * This constructor is used when an Experiment database is already opened, and we just
	 * want to transfer the database to this trace data.
	 * 
	 * @param context IEclipseContext
	 * @param experiment BaseExperiment
	 */
	public SpaceTimeDataController(IExperiment experiment) 
	{
		exp = experiment;		
		init();
	}

	
	/*************************************************************************
	 * Returns the current database's process time line service.
	 * This service is useful to get the next process time line
	 * @return ProcessTimelineService
	 *************************************************************************/
	public ProcessTimelineService getProcessTimelineService() {
		return timelineService;
	}
	
	
	/*************************************************************************
	 * Check if traces have been painted out in the main trace view.
	 * Other views (like depth view) will need to check this.
	 * 
	 * @return
	 *************************************************************************/
	public boolean hasTraces() {
		if (timelineService == null)
			return false;
		return timelineService.getNumProcessTimeline() > 0;
	}
	
	
	/*************************************************************************
	 * Initialize the object
	 * 
	 * @param _window
	 * @throws Exception 
	 *************************************************************************/
	private void init() 
	{	
		timelineService = new ProcessTimelineService();
		
		// attributes initialization
		attributes = new TraceDisplayAttribute();
		
		final Display display = Display.getDefault();
		display.syncExec( ()-> {
			
			// initialize color table
			// has to be inside UI thread since we create colors
			colorTable = new ColorTable();
		});
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
	public ProcessTimeline getCurrentDepthTrace() {
		int scaledDTProcess = computeScaledProcess();
		return  timelineService.getProcessTimeline(scaledDTProcess);
	}

	
	/*************************************************************************
	 * Get the file base data of this trace
	 * @return {@code IBaseData}
	 *************************************************************************/
	public IBaseData getBaseData(){
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
		timelineService = null;
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
	
	
	////////////////////////////////////////////////////////////////////////////////
	// Abstract methods
	////////////////////////////////////////////////////////////////////////////////
	
	/*************************************************************************
	 * Retrieve the name of the database. The name can be either the path of
	 * the directory, or the name of the profiled application, or both.
	 * <p>
	 * Ideally the name should be unique to distinguish with other databases. 
	 * 
	 * @return String: the name of the database
	 *************************************************************************/
	public abstract String getName();

	public abstract void closeDB();
	
	public abstract IFilteredData createFilteredBaseData();

	public abstract void fillTracesWithData(boolean changedBounds, int numThreadsToLaunch)
			throws IOException;

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
}