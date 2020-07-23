package edu.rice.cs.hpctraceviewer.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Display;
import edu.rice.cs.hpcbase.map.ProcedureAliasMap;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IFilteredData;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.trace.TraceAttribute;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimelineService;
import edu.rice.cs.hpctraceviewer.data.util.Constants;


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
	final protected BaseExperiment  exp;

	protected ImageTraceAttributes attributes;
	/**
	 * The minimum beginning and maximum ending time stamp across all traces (in
	 * microseconds)).
	 */
	protected long maxEndTime, minBegTime;

	/** The map between the nodes and the cpid's. */
	private HashMap<Integer, CallPath> scopeMap = null;
		
	/** The maximum depth of any single CallStackSample in any trace. */
	protected int maxDepth = 0;
	
	protected ColorTable colorTable = null;
	private boolean enableMidpoint  = true;
	
	protected IBaseData dataTrace = null;
	
	protected IEclipseContext context;
	
	protected ProcessTimelineService timelineService;
	
	// nathan's data index variable
	// TODO: we need to remove this and delegate to the inherited class instead !
	private int currentDataIdx;

	/***
	 * Constructor to create a data based on File. This constructor is more suitable
	 * for local database
	 * 
	 * @param _window : SWT window
	 * @param expFile : experiment file (XML format)
	 */
	public SpaceTimeDataController(IEclipseContext context, File expFile) 
			throws InvalExperimentException, Exception 
	{			
		exp = new ExperimentWithoutMetrics();

		// possible java.lang.OutOfMemoryError exception here
		exp.open(expFile, new ProcedureAliasMap(), false);

		init(context);
	}
	
	/*****
	 * Constructor to create a data based on input stream, which is convenient for remote database
	 * 
	 * @param _window : SWT window
	 * @param expStream : input stream
	 * @param Name : the name of the file on the remote server
	 * @throws InvalExperimentException 
	 *****/
	public SpaceTimeDataController(IEclipseContext context, InputStream expStream, String Name) 
			throws InvalExperimentException, Exception 
	{	
		exp = new ExperimentWithoutMetrics();

		// Without metrics, so param 3 is false
		exp.open(expStream, new ProcedureAliasMap(), Name);
		
		init(context);
	}
	
	
	public SpaceTimeDataController(IEclipseContext context, BaseExperiment experiment) 			
			throws InvalExperimentException, Exception 
	{
		this.exp = experiment;
		init(context);
	}

	
	public ProcessTimelineService getProcessTimelineService() {
		ProcessTimelineService ptlService = (ProcessTimelineService) context.get(Constants.CONTEXT_TIMELINE);
		return ptlService;
	}
	
	public void setDataIndex(int dataIndex) 
	{
		currentDataIdx = dataIndex;
	}
	
	
	public int getDataIndex()
	{
		return currentDataIdx;
	}
	
	public void resetPredefinedColor()
	{
		colorTable.resetPredefinedColor();
	}
	
	/******
	 * Initialize the object
	 * 
	 * @param _window
	 * @throws Exception 
	 ******/
	private void init(IEclipseContext context) 
			throws InvalExperimentException 
	{	
		this.context = context;
		timelineService = (ProcessTimelineService) context.get(Constants.CONTEXT_TIMELINE);
		
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {

			@Override
			public void run() {
				
				// initialize color table
				colorTable = new ColorTable();
				
				// tree traversal to get the list of cpid, procedures and max depth
				TraceDataVisitor visitor = new TraceDataVisitor(colorTable);
				RootScope root = exp.getRootScope(RootScopeType.CallingContextTree);
				root.dfsVisitScopeTree(visitor);

				maxDepth   = visitor.getMaxDepth();
				scopeMap   = visitor.getMap();
				
				// attributes initialization
				attributes 	 = new ImageTraceAttributes();

			}			
		});
		final TraceAttribute trAttribute = exp.getTraceAttribute();
		
		if (trAttribute == null) {
			throw new InvalExperimentException("Database does not contain traces: " + exp.getDefaultDirectory());
		}
		minBegTime = trAttribute.dbTimeMin;
		maxEndTime = trAttribute.dbTimeMax;

	}

	public int getMaxDepth() 
	{
		return maxDepth;
	}
	
	
	private int getCurrentlySelectedProcess()
	{
		return attributes.getPosition().process;
	}
	
	/**
	 * {@link getCurrentlySelectedProcess()} returns something on [begProcess,
	 * endProcess-1]. We need to map that to something on [0, numTracesShown -
	 * 1]. We use a simple linear mapping:
	 * begProcess    -> 0,
	 * endProcess-1  -> numTracesShown-1
	 */
	public int computeScaledProcess() {
		int numTracesShown = Math.min(attributes.getProcessInterval(), attributes.getPixelVertical());
		int selectedProc = getCurrentlySelectedProcess();
		
		double scaledDTProcess = (((double) numTracesShown -1 )
					/ ((double) attributes.getProcessInterval() - 1) * 
					(selectedProc - attributes.getProcessBegin()));
		return (int)scaledDTProcess;
	}


	/******
	 * get the depth trace of the current "selected" process
	 *  
	 * @return ProcessTimeline
	 */
	public ProcessTimeline getCurrentDepthTrace() {
		int scaledDTProcess = computeScaledProcess();
		// TODO
		return  timelineService.getProcessTimeline(scaledDTProcess);
	}
	

	
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
	
	public HashMap<Integer, CallPath> getScopeMap() {
		return scopeMap;
	}

	/******************************************************************************
	 * getter/setter trace attributes
	 * @return
	 ******************************************************************************/
	
	public int getPixelHorizontal() {
		return attributes.getPixelHorizontal();
	}
	
	public BaseExperiment getExperiment() {
		return exp;
	}

	public ImageTraceAttributes getAttributes() {
		return attributes;
	}


	public int getHeaderSize() {
		final int headerSize = exp.getTraceAttribute().dbHeaderSize;
		return headerSize;
	}

	/*************************************************************************
	 * Returns width of the spaceTimeData: The width (the last time in the
	 * ProcessTimeline) of the longest ProcessTimeline.
	 ************************************************************************/
	public long getTimeWidth() {
		return maxEndTime - minBegTime;
	}

	public long getMaxEndTime() {
		return maxEndTime;
	}

	public long getMinBegTime() {
		return minBegTime;
	}

	/*****
	 * return the unit of the current database
	 * 
	 * @return {@link ImageTraceAttributes.TimeUnit}
	 *****/
	public TimeUnit getTimeUnit() {
		
		if (getExperiment().getMajorVersion() == 2) {
			if (getExperiment().getMinorVersion() < 2) {
				// old version of database: always microsecond
				return TimeUnit.MICROSECONDS;
			}
			// new version of database:
			// - if the measurement is from old hpcrun: microsecond
			// - if the measurement is from new hpcrun: nanosecond
			
			Experiment exp = (Experiment) getExperiment();
			if (exp.getTraceAttribute().dbUnitTime == TraceAttribute.PER_NANO_SECOND) {
				return TimeUnit.NANOSECONDS;
			}
			return TimeUnit.MICROSECONDS;
		}
		// we have no idea what kind of database is this.
		// this must be an error. Should we raise an exception?
		
		return TimeUnit.MICROSECONDS;
	}
	

	
	public ColorTable getColorTable() {
		return colorTable;
	}

	public void dispose() {
		colorTable.dispose();
	}

	public void setEnableMidpoint(boolean enable) {
		this.enableMidpoint = enable;
	}

	public boolean isEnableMidpoint() {
		return enableMidpoint;
	}

	//see the note where this is called in FilterRanks
	public IFilteredData getFilteredBaseData() {
		if (dataTrace instanceof IFilteredData)
			return (IFilteredData) dataTrace;
		return null;
	}
	/**
	 * changing the trace data, caller needs to make sure to refresh the views
	 * @param filteredBaseData
	 */
	public void setBaseData(IFilteredData filteredBaseData) {
		dataTrace = filteredBaseData;

		int endProcess = attributes.getProcessEnd();
		int begProcess = attributes.getProcessBegin();
		
		//Snap it back into the acceptable limits.
		if (endProcess > dataTrace.getNumberOfRanks())
			endProcess  = dataTrace.getNumberOfRanks();
		
		if (begProcess >= endProcess)
			begProcess = 0;
		
		attributes.setProcess(begProcess, endProcess);
	}

	public boolean isTimelineFilled() {
		return timelineService.isFilled();
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
	abstract public String getName() ;

	public abstract void closeDB();

	public abstract IFilteredData createFilteredBaseData();

	public abstract void fillTracesWithData(boolean changedBounds, int numThreadsToLaunch)
			throws IOException;
}