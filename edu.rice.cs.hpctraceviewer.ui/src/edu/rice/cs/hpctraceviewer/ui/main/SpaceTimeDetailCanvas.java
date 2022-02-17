package edu.rice.cs.hpctraceviewer.ui.main;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.experiment.extdata.IBaseData;
import edu.rice.cs.hpcdata.util.OSValidator;
import edu.rice.cs.hpcsetting.color.ColorManager;
import edu.rice.cs.hpctraceviewer.ui.base.ISpaceTimeCanvas;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.base.ITraceViewAction;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;
import edu.rice.cs.hpctraceviewer.ui.internal.AbstractTimeCanvas;
import edu.rice.cs.hpctraceviewer.ui.internal.BaseTimelineThread;
import edu.rice.cs.hpctraceviewer.ui.internal.BaseViewPaint;
import edu.rice.cs.hpctraceviewer.ui.internal.BufferPaint;
import edu.rice.cs.hpctraceviewer.ui.internal.ResizeListener;
import edu.rice.cs.hpctraceviewer.ui.internal.TraceEventData;
import edu.rice.cs.hpctraceviewer.ui.operation.AbstractTraceOperation;
import edu.rice.cs.hpctraceviewer.ui.operation.BufferRefreshOperation;
import edu.rice.cs.hpctraceviewer.ui.operation.PositionOperation;
import edu.rice.cs.hpctraceviewer.ui.operation.WindowResizeOperation;
import edu.rice.cs.hpctraceviewer.ui.operation.ZoomOperation;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.color.ColorTable;
import edu.rice.cs.hpctraceviewer.data.Frame;
import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;
import edu.rice.cs.hpctraceviewer.data.Position;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimelineService;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;
import edu.rice.cs.hpctraceviewer.ui.util.MessageLabelManager;
import edu.rice.cs.hpctraceviewer.data.util.Constants;


/*************************************************************************
 * 
 *	Canvas onto which the detail view is painted. Also takes care of
 *	zooming responsibilities of the detail view.
 *
 ************************************************************************/
public class SpaceTimeDetailCanvas extends AbstractTimeCanvas 
	implements IOperationHistoryListener, 
			   ISpaceTimeCanvas, 
			   ITraceViewAction, 
			   DisposeListener,
			   EventHandler, PropertyChangeListener
{		
	/**The min number of process units you can zoom in.*/
    private final static int MIN_PROC_DISP = 1;
	
	private final IEventBroker eventBroker;

	/**The SpaceTimeData corresponding to this canvas.*/
	protected SpaceTimeDataController stData;
		
	/** The top-left and bottom-right point that you selected.*/
	final private Point selectionTopLeft, selectionBottomRight;
	
	private final IEclipseContext context;
	private final ITracePart 	  tracePart;
	
	/**The Group containing the labels. labelGroup.redraw() is called from the Detail Canvas.*/
	private Composite labelGroup;
   
    /**The Label with the time boundaries.*/
	private Label timeLabel;
   
    /**The Label with the process boundaries.*/
	private Label processLabel;
    
    /**The Label with the current cross hair information.*/
	private Label crossHairLabel;
        
    /**The Label for displaying a message info/warning to users.*/
	private Label messageLabel;

	/** thread to remove message */
	private MessageLabelManager restoreMessage;
	
	private final TraceDisplayAttribute oldAttributes;
	
	private final DecimalFormat formatTime;

	private ResizeListener resizeListener;
	private KeyListener keyListener;
	
    /**Creates a SpaceTimeDetailCanvas with the given parameters*/
	public SpaceTimeDetailCanvas(ITracePart tracePart, IEclipseContext context, IEventBroker eventBroker, Composite _composite)
	{
		super(_composite, SWT.NO_BACKGROUND | SWT.BORDER_DASH, RegionType.Rectangle );
		
		this.tracePart   = tracePart;
		this.eventBroker = eventBroker;
		this.context     = context;
		oldAttributes    = new TraceDisplayAttribute();

		selectionTopLeft     = new Point(0,0);
		selectionBottomRight = new Point(0,0);
		stData  = null;
		
		initMouseSelection();
		
		formatTime = new DecimalFormat("###,###,###.##");
	}


	private void initMouseSelection()
	{
		initSelectionRectangle();
	}
	
	/*****
	 * set new database and refresh the screen
	 * @param dataTraces
	 *****/
	public void setData(SpaceTimeDataController stData) {

		super.init();

		if (this.stData == null) 
		{
			addCanvasListener();
			tracePart.getOperationHistory().addOperationHistoryListener(this);
			
			eventBroker.subscribe(IConstants.TOPIC_DEPTH_UPDATE,  this);
			eventBroker.subscribe(IConstants.TOPIC_FILTER_RANKS,  this);
			eventBroker.subscribe(IConstants.TOPIC_COLOR_MAPPING, this);
		}

		// reinitialize the selection rectangle
		initSelectionRectangle();

		this.stData = stData;

		// init configuration
		Position p = new Position(-1, -1);
		this.stData.getTraceDisplayAttribute().setPosition(p);
		this.stData.getTraceDisplayAttribute().setDepth(0);
		
		// initialize the depth
		// we don't know which depth is the best to viewed, but heuristically
		// the first third is a better one.
		var displayAttribute = stData.getTraceDisplayAttribute();
		displayAttribute.addPropertyChangeListener(this);
		
		Frame frame = new Frame(displayAttribute.getFrame());
		frame.depth = stData.getDefaultDepth();
		
		home(frame);
		if (OSValidator.isWindows()) {
			redraw();
		}
	}
	
	/***
	 * add listeners to the canvas 
	 * caution: this method can only be called at most once ! 
	 */
	private void addCanvasListener() {

		addPaintListener(this);
		
		keyListener = new KeyListener(){
			public void keyPressed(KeyEvent e) {}

			public void keyReleased(KeyEvent e) {
				switch (e.keyCode) {
				
				case SWT.ARROW_DOWN:
					goDown();
					break;
				case SWT.ARROW_UP:
					goUp();
					break;
				case SWT.ARROW_LEFT:
					goLeft();
					break;
				case SWT.ARROW_RIGHT:
					goRight();
					break;				
				}
			}			
		};
		addKeyListener( keyListener );
				
		// ------------------------------------------------------------------------------------
		// A listener for resizing the the window.
		// In order to get the last resize position, we will use timer to check if the current
		//  resize event is invoked "long" enough to the first resize event.
		// If this is the case, then run rebuffering, otherwise just no-op.
		// ------------------------------------------------------------------------------------
		resizeListener = new ResizeListener( new DetailBufferPaint() ); 
		addControlListener(resizeListener);
		
		getDisplay().addFilter(SWT.MouseDown, resizeListener);
		getDisplay().addFilter(SWT.MouseUp, resizeListener);
	}
	
	
	/*************************************************************************
	 * Sets the bounds of the data displayed on the detail canvas to be those 
	 * specified by the zoom operation and adjusts everything accordingly.
	 *************************************************************************/
	public void zoom(long _topLeftTime, int _topLeftProcess, long _bottomRightTime, int _bottomRightProcess)
	{
		final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
		attributes.setTime(_topLeftTime, _bottomRightTime);
		attributes.assertTimeBounds(stData.getTimeWidth());
		
		attributes.setProcess(_topLeftProcess, _bottomRightProcess);
		attributes.assertProcessBounds(stData.getTotalTraceCount());
		
		final long numTimeDisplayed = this.getNumTimeUnitDisplayed();
		if (numTimeDisplayed < Constants.MIN_TIME_UNITS_DISP)
		{
			long begTime = _topLeftTime + (numTimeDisplayed - Constants.MIN_TIME_UNITS_DISP) / 2;
			long endTime = _topLeftTime + Constants.MIN_TIME_UNITS_DISP;
			attributes.setTime(begTime, endTime);
		}
		
		final double numProcessDisp = this.getNumProcessesDisplayed();
		if (numProcessDisp < MIN_PROC_DISP)
		{
			int endProcess = _topLeftProcess + MIN_PROC_DISP;
			attributes.setProcess(_topLeftProcess, endProcess );
		}

		updateButtonStates();
    	
		// ----------------------------------------------------------------------------
		// hack solution: we need to gather the data first, then we ask other views 
		//	to update their contents
		// ----------------------------------------------------------------------------
		refresh(true);
	}
	
	/*******************************************************************************
	 * Initialize attributes of selection rectangle
	 *******************************************************************************/
	private void initSelectionRectangle() 
	{
		selectionTopLeft.x = 0;
		selectionTopLeft.y = 0;
		selectionBottomRight.x = 0;
		selectionBottomRight.y = 0;
	}
	
	/*******************************************************************************
	 * Actually does the repainting of the canvas when a PaintEvent is sent to it
	 * (basically when anything at all is changed anywhere on the application 
	 * OR when redraw() is called).
	 ******************************************************************************/
	public void paintControl(PaintEvent event)
	{	
		if (this.stData == null)
			return;

		super.paintControl(event);

		final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
		
		//draws cross hairs
		long selectedTime = attributes.getPosition().time - attributes.getTimeBegin();
		int selectedProcess = attributes.getPosition().process - attributes.getProcessBegin();
		
		int topPixelCrossHairX = (int)(Math.round(selectedTime*getScalePixelsPerTime())-10);
		int topPixelCrossHairY = (int)(Math.round((selectedProcess+.5)*getScalePixelsPerRank())-10);
		
		event.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		event.gc.fillRectangle(topPixelCrossHairX,topPixelCrossHairY+8,20,4);
		event.gc.fillRectangle(topPixelCrossHairX+8,topPixelCrossHairY,4,20);
	}

	
	/**************************************************************************
	 * The action that gets performed when the 'home' button is pressed - 
	 * the bounds are reset so that the viewer is zoomed all the way out on the
	 * image.
	 **************************************************************************/
	@Override
	public void home()
	{
		//if this is the first time painting,
		//some stuff needs to get initialized
		Frame frame = new Frame(stData.getTraceDisplayAttribute().getFrame());
		
		home(frame);
	}
	
	private void home(Frame frame) 
	{
		frame.begProcess = 0;
		frame.endProcess = stData.getTotalTraceCount();
		
		frame.begTime = 0;
		frame.endTime = stData.getTimeWidth();
		
		stData.getTraceDisplayAttribute().setFrame(frame);
		
		notifyChanges(ZoomOperation.ActionHome, frame);
	}
	

	/**************************************************************************
	 * The action that gets performed when the 'open' button is pressed - 
	 * sets everything to the data stored in the Frame toBeOpened.
	 **************************************************************************/
	public void open(Frame toBeOpened)
	{
		notifyChanges("Frame", toBeOpened);	
	}
	
	
	
	/**************************************************************************
	 * The action that gets performed when the 'process zoom in' button is pressed - 
	 * zooms in processwise with a scale of .4.
	 **************************************************************************/
	@Override
	public void processZoomIn()
	{
		final double SCALE = .4;
		final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
		
		double yMid = (attributes.getProcessEnd()+attributes.getProcessBegin())/2.0;
		
		final double numProcessDisp = attributes.getProcessInterval();
		
		int p2 = (int) Math.ceil( yMid+numProcessDisp*SCALE );
		int p1 = (int) Math.floor( yMid-numProcessDisp*SCALE );
		
		attributes.assertProcessBounds(stData.getTotalTraceCount());
		
		if(p2 == attributes.getProcessEnd() && p1 == attributes.getProcessBegin())
		{
			if(numProcessDisp == 2)
				p2--;
			else if(numProcessDisp > 2)
			{
				p2--;
				p1++;
			}
		}
		final Frame frame = new Frame(stData.getTraceDisplayAttribute().getFrame());
		frame.begProcess = p1;
		frame.endProcess = p2;

		notifyChanges("Zoom-in ranks", frame);
	}
	
	
	@Override
	public boolean canProcessZoomIn() {
		if (stData == null)
			return false;
		return getNumProcessesDisplayed() > MIN_PROC_DISP; 
	}
	
	
	@Override
	public boolean canProcessZoomOut() {
		if (stData == null)
			return false;
		TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
		return attributes.getProcessBegin()>0 || attributes.getProcessEnd()<stData.getTotalTraceCount(); 
	}
	

	/**************************************************************************
	 * The action that gets performed when the 'process zoom out' button is pressed - 
	 * zooms out processwise with a scale of .625.
	 **************************************************************************/
	@Override
	public void processZoomOut()
	{
		final double SCALE = .625;
		final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
		
		//zoom out works as follows: find mid point of times (yMid).
		//Add/Subtract 1/2 of the scaled numProcessDisp to yMid to get new endProcess and begProcess
		double yMid = ((double)attributes.getProcessEnd() + (double)attributes.getProcessBegin())/2.0;
		
		final double numProcessDisp = attributes.getProcessInterval();
		

		int p2 = (int) Math.min( stData.getTotalTraceCount(), Math.ceil( yMid+numProcessDisp*SCALE ) );
		int p1 = (int) Math.max( 0, Math.floor( yMid-numProcessDisp*SCALE ) );
		
		if(p2 == attributes.getProcessEnd() && p1 == attributes.getProcessBegin())
		{
			if(numProcessDisp == 2)
				p2++;
			else if(numProcessDisp > 2)
			{
				p2++;
				p1--;
			}
		}
		final Frame frame = new Frame(stData.getTraceDisplayAttribute().getFrame());
		frame.begProcess = p1;
		frame.endProcess = p2;

		notifyChanges("Zoom-out ranks", frame);
	}

	
	/**************************************************************************
	 * The action that gets performed when the 'time zoom in' button is pressed - 
	 * zooms in timewise with a scale of .4.
	 **************************************************************************/
	@Override
	public void timeZoomIn()
	{
		final double SCALE = .4;
		final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
		
		long xMid = (attributes.getTimeEnd() + attributes.getTimeBegin()) / 2;
		
		final long numTimeUnitsDisp = attributes.getTimeInterval();
		
		long t2 = xMid + (long)(numTimeUnitsDisp * SCALE);
		long t1 = xMid - (long)(numTimeUnitsDisp * SCALE);
		
		if (t2-t1 <= Constants.MIN_TIME_UNITS_DISP) {
			// error: we cannot zoom in to less than 1
			setMessage("Zoom-in is not allowed: the interval is too small.");
			return;
		}

		final Frame frame = new Frame(stData.getTraceDisplayAttribute().getFrame());
		frame.begTime = t1;
		frame.endTime = t2;
		
		notifyChanges("Zoom-in time", frame);
	}

	
	/**************************************************************************
	 * The action that gets performed when the 'time zoom out' button is pressed - 
	 * zooms out timewise with a scale of .625.
	 **************************************************************************/
	@Override
	public void timeZoomOut()
	{
		final double SCALE = 0.65;
		final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
		
		//zoom out works as follows: find mid point of times (xMid).
		//Add/Subtract 1/2 of the scaled numTimeUnitsDisp to xMid to get new endTime and begTime

		double xMid = attributes.getTimeBegin() + (attributes.getTimeInterval() * 0.5);
		
		final double td2 = (double)(this.getNumTimeUnitDisplayed() * SCALE); 
		long t2 = (long) Math.ceil( Math.min( stData.getTimeWidth(), xMid + td2) );
		final double td1 = (long)(this.getNumTimeUnitDisplayed() * SCALE);
		long t1 = (long) Math.floor( Math.max(0, xMid - td1) );

		final Frame frame = new Frame(stData.getTraceDisplayAttribute().getFrame());
		frame.begTime = t1;
		frame.endTime = t2;
		
		notifyChanges("Zoom-out time", frame);
	}
	
	
	@Override
	public boolean canTimeZoomIn() {
		if (stData == null)
			return false;
		return getNumTimeUnitDisplayed() > Constants.MIN_TIME_UNITS_DISP;
	}
	
	@Override
	public boolean canTimeZoomOut() {
		if (stData == null)
			return false;
		final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
		return attributes.getTimeBegin()>0 || attributes.getTimeEnd()<stData.getTimeWidth();
	}

	
	/**************************************************************************
	 * Gets the scale along the x-axis (pixels per time unit).
	 **************************************************************************/
	@Override
	public double getScalePixelsPerTime()
	{
		TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
		return (double) attributes.getPixelHorizontal() / (double)this.getNumTimeUnitDisplayed();
	}
	
	/**************************************************************************
	 * Gets the scale along the y-axis (pixels per process).
	 **************************************************************************/
	@Override
	public double getScalePixelsPerRank()
	{
		return stData.getTraceDisplayAttribute().getScalePixelsPerRank();
	}
	
	/**************************************************************************
	 * Sets the depth to newDepth.
	 **************************************************************************/
	public void setDepth(int newDepth)
	{
		if (isDisposed()) return;
		
		stData.getTraceDisplayAttribute().setDepth(newDepth);
		refresh(false);
    }
	
	
	/**************************************************************************
	 * Sets up the labels (the ones below the detail canvas).
	 **************************************************************************/
	public void setLabels(Composite _labelGroup)
    {
		labelGroup = _labelGroup;

		GridLayoutFactory.fillDefaults().numColumns(4).generateLayout(labelGroup);
		GridDataFactory.fillDefaults().grab(true, false).
						align(SWT.BEGINNING, SWT.CENTER).applyTo(labelGroup);
        
        timeLabel  	   = new Label(labelGroup, SWT.LEFT);
        processLabel   = new Label(labelGroup, SWT.CENTER);
        crossHairLabel = new Label(labelGroup, SWT.RIGHT);
        messageLabel   = new Label(labelGroup, SWT.LEFT);
    }
   
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.traceviewer.painter.ISpaceTimeCanvas#setMessage(java.lang.String)
	 */
	@Override
	public void setMessage(String message)
	{
		if (restoreMessage == null)
			restoreMessage = new MessageLabelManager(getDisplay(), messageLabel);
		
		restoreMessage.showWarning(message);
	}
	
	private void clearMessage() {
  		if (restoreMessage != null)
  			restoreMessage.clear();
	}
	
	/**************************************************************************
	 * Updates what the labels display to the viewer's current state.
	 **************************************************************************/
	private void adjustLabels()
    {
		final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
        
		showTimeRange(attributes);
        showProcessRange(attributes);
        showCrossHair(attributes);
        
        // resize the composite to make sure all texts are visible
        labelGroup.setSize(labelGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }
	
	/**************************************************************************
	 * Display the time range label
	 * @param attributes ImageTraceAttributes
	 **************************************************************************/
	private void showTimeRange(final TraceDisplayAttribute attributes) {
		
		final TimeUnit dbTimeUnit = stData.getTimeUnit();
		final TimeUnit displayTimeUnit = attributes.getTimeUnit();
		
		double timeInTimeUnit  = attributes.getTimeUnitMultiplier() * displayTimeUnit.convert(attributes.getTimeBegin(), dbTimeUnit);
		final String timeStart = formatTime.format(timeInTimeUnit);
		
		timeInTimeUnit = attributes.getTimeUnitMultiplier() * displayTimeUnit.convert(attributes.getTimeEnd(), dbTimeUnit);
		final String timeEnd   = formatTime.format(timeInTimeUnit);
        
		String timeUnit = attributes.getTimeUnitName(attributes.getDisplayTimeUnit());
		
		timeLabel.setText("Time Range: [" + timeStart + timeUnit + ", " + timeEnd + timeUnit + "]");
        timeLabel.setSize(timeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));

	}
	
	/**************************************************************************
	 * Show the process range if necessary. If the program is a sequential, no 
	 * display is needed.
	 * 
	 * @param attributes ImageTraceAttributes
	 **************************************************************************/
	private void showProcessRange(final TraceDisplayAttribute attributes) {

        final IBaseData traceData = stData.getBaseData();
        if (traceData == null) {
        	// we don't want to throw an exception here, so just do nothing
        	return;
        }
        stData.getTraceDisplayAttribute().assertProcessBounds(traceData.getNumberOfRanks());

        final List<IdTuple> listIdTuples = traceData.getListOfIdTuples(IdTupleOption.BRIEF);
        if (listIdTuples == null || listIdTuples.size() <= 1)
        	// sequential program. No process range display is needed
        	return;

        int proc_start = attributes.getProcessBegin();
        if (proc_start < 0 || proc_start >= listIdTuples.size())
        	proc_start = 0;
        
        // -------------------------------------------------------------------------------------------------
        // bug fix: since the end of the process is the ceiling of the selected region,
        //			and the range of process rendering is based on inclusive min and exclusive max, then
        //			we need to decrement the value of max process (in the range).
        // WARN: the display of process range should be then between inclusive min and inclusive max
        //
        // TODO: we should fix the rendering to inclusive min and inclusive max, otherwise it is too much
        //		 headache to maintain
        // -------------------------------------------------------------------------------------------------
        int proc_end   = attributes.getProcessEnd() - 1;
        if (proc_end>=listIdTuples.size())
        	proc_end = listIdTuples.size()-1;
        
        IdTupleType type = stData.getExperiment().getIdTupleType();
        processLabel.setText("Rank Range: [" + listIdTuples.get(proc_start).toString(type) 
        									 + ", " + listIdTuples.get(proc_end).toString(type) 
        									 + "]");
        processLabel.setSize(processLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));

	}
	
	/**************************************************************************
	 * Show the cross hair (time x process).
	 * For sequential program, we just need to show the time only.
	 * 
	 * @param attributes
	 **************************************************************************/
	private void showCrossHair(final TraceDisplayAttribute attributes) {
        
        if(stData == null) {        	
            crossHairLabel.setText("Select Sample For Cross Hair");
            return;
        }
		ProcessTimeline ptl     = stData.getCurrentDepthTrace();
		if (ptl == null) 
			return;
		
    	final Position position = stData.getTraceDisplayAttribute().getPosition();    			
		final TimeUnit dbTimeUnit = stData.getTimeUnit();
		final TimeUnit displayTimeUnit = attributes.getTimeUnit();
		final float selectedTime = displayTimeUnit.convert(position.time, dbTimeUnit) * attributes.getTimeUnitMultiplier();
		
		final int selectedProc  = ptl.getProcessNum();

        final IBaseData traceData = stData.getBaseData();
        final List<IdTuple> listIdTuples = traceData.getListOfIdTuples(IdTupleOption.BRIEF);

		String timeUnit = attributes.getTimeUnitName(attributes.getDisplayTimeUnit());
		String label = "Cross Hair: ";

		if ( selectedProc >= 0 && selectedProc < listIdTuples.size() && listIdTuples.size()>1 ) {
			
			IdTuple idtuple = listIdTuples.get(selectedProc);
			String procStr  = idtuple.toString(traceData.getIdTupleTypes());
			String timeStr  = formatTime.format(selectedTime) + timeUnit;
	        final String buffer = label + "(" + timeStr + ", " +  procStr + ")";

	        crossHairLabel.setText(buffer);

		} else {
			// in case of incorrect filtering where user may have empty ranks or incorrect filters, we don't display the rank
			crossHairLabel.setText(label + formatTime.format(selectedTime) + timeUnit );
		}
	}
	
    /**************************************************************************
	 * Updates what the position of the selected box is.
	 **************************************************************************/
    private void adjustSelection(Rectangle selection)
	{
    	selectionTopLeft.x = Math.max(selection.x, 0);
        selectionTopLeft.y = Math.max(selection.y, 0);
        
        final Rectangle view = getClientArea();
        
        selectionBottomRight.x = Math.min(selection.width+selection.x, view.width-1);
        selectionBottomRight.y = Math.min(selection.height+selection.y, view.height-1);
        
    }
    
	/**************************************************************************
	 * create a new region of trace view, and check if the cross hair is inside
	 * 	the new region or not. If this is not the case, we force the position
	 * 	of crosshair to be inside the region 
	 **************************************************************************/
	private void setDetail()
    {
		TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
		int topLeftProcess = attributes.getProcessBegin() + (int) (selectionTopLeft.y / getScalePixelsPerRank());
		long topLeftTime   = attributes.getTimeBegin() + (long)(selectionTopLeft.x / getScalePixelsPerTime());
		
		// ---------------------------------------------------------------------------------------
		// we should include the partial selection of a time or a process
		// for instance if the user selects processes where the max process is between
		// 	10 and 11, we should include process 11 (just like keynote selection)
		// ---------------------------------------------------------------------------------------
		int bottomRightProcess = attributes.getProcessBegin() + (int) Math.ceil( (selectionBottomRight.y / getScalePixelsPerRank()) );
		long bottomRightTime   = attributes.getTimeBegin() + (long)Math.ceil( (selectionBottomRight.x / getScalePixelsPerTime()) );


		final Frame frame = new Frame(stData.getTraceDisplayAttribute().getFrame());
		frame.begTime = topLeftTime;
		frame.endTime = bottomRightTime;
		frame.begProcess = topLeftProcess;
		frame.endProcess = bottomRightProcess;
		
		notifyChanges("Zoom", frame);
    }
    
  
    @Override
    public boolean canGoLeft() {
    	if (stData == null) return false;
    	return (stData.getTraceDisplayAttribute().getTimeBegin() > 0);
    }

    
    @Override
    public boolean canGoRight() {
    	if (stData == null) return false;
    	return (stData.getTraceDisplayAttribute().getTimeEnd()< this.stData.getTimeWidth());
    }

    
    @Override
    public boolean canGoUp() {
    	if (stData == null) return false;
    	return (stData.getTraceDisplayAttribute().getProcessBegin()>0);
    }
    
    @Override
    public boolean canGoDown() {
    	if (stData == null) return false;
    	return (stData.getTraceDisplayAttribute().getProcessEnd()<this.stData.getTotalTraceCount());
    }

    
    private void updateButtonStates() {
    	eventBroker.send(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC,
    			UIEvents.ALL_ELEMENT_ID);	
    }
    
	final static private double SCALE_MOVE = 0.20;
    
	/***
	 * go to the left one step
	 */
    @Override
    public void goLeft()
    {
    	if (!canGoLeft())
    		return;
    	
    	final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
    	
    	long topLeftTime = attributes.getTimeBegin();
		long bottomRightTime = attributes.getTimeEnd();
		
		long deltaTime = bottomRightTime - topLeftTime;
		final long moveTime = (long)java.lang.Math.ceil(deltaTime * SCALE_MOVE);
		topLeftTime = topLeftTime - moveTime;
		
		if (topLeftTime < 0) {
			topLeftTime = 0;
		}
		bottomRightTime = topLeftTime + deltaTime;
		
		setTimeRange(topLeftTime, bottomRightTime);
		
		updateButtonStates();
    }
    
    /***
     * go to the right one step
     */
    @Override
    public void goRight()
    {
    	if (!canGoRight())
    		return;
    	
    	final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
    	
    	long topLeftTime = attributes.getTimeBegin();
		long bottomRightTime = attributes.getTimeEnd();
		
		long deltaTime = bottomRightTime - topLeftTime;
		final long moveTime = (long)java.lang.Math.ceil(deltaTime * SCALE_MOVE);
		bottomRightTime = bottomRightTime + moveTime;
		
		if (bottomRightTime > stData.getTimeWidth()) {
			bottomRightTime = stData.getTimeWidth();
		}
		topLeftTime = bottomRightTime - deltaTime;
		
		setTimeRange(topLeftTime, bottomRightTime);
		
		this.updateButtonStates();
    }
    
    /***
     * set a new range of X-axis
     * @param topLeftTime
     * @param bottomRightTime
     */
    public void setTimeRange(long topLeftTime, long bottomRightTime)
    {
    	final Frame frame = new Frame(stData.getTraceDisplayAttribute().getFrame());
    	frame.begTime = topLeftTime;
    	frame.endTime = bottomRightTime;
    	
    	notifyChanges("Zoom H", frame);
    }

    /*******
     * go north one step
     */
    @Override
    public void goUp() {
    	if (!canGoUp())
    		return;
    	
    	final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
    	
    	int proc_begin = attributes.getProcessBegin();
    	int proc_end = attributes.getProcessEnd();
    	final int delta = proc_end - proc_begin;
    	final int move = (int) java.lang.Math.ceil(delta * SCALE_MOVE);

    	proc_begin = proc_begin - move;
    	
    	if (proc_begin < 0) {
    		proc_begin = 0;
    	}
    	proc_end = proc_begin + delta;
    	this.setProcessRange(proc_begin, proc_end);
		this.updateButtonStates();
    }

    /*******
     * go south one step
     */
    @Override
    public void goDown() {
    	if (!canGoDown())
    		return;
    	
    	final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
    	
    	int proc_begin = attributes.getProcessBegin();
    	int proc_end = attributes.getProcessEnd();
    	final int delta = proc_end - proc_begin;
    	final int move = (int) java.lang.Math.ceil(delta * SCALE_MOVE);

    	proc_end = proc_end + move;
    	
    	if (proc_end > stData.getTotalTraceCount()) {
    		proc_end = stData.getTotalTraceCount();
    	}
    	proc_begin = proc_end - delta;
    	this.setProcessRange(proc_begin, proc_end);
		this.updateButtonStates();
    }
    
	
    @Override
	public void saveConfiguration() {
    	if (stData == null)
    		return;
    	
		FileDialog saveDialog = new FileDialog(getShell(), SWT.SAVE);
		saveDialog.setText("Save View Configuration");
		saveDialog.setFilterPath(stData.getExperiment().getDefaultDirectory().getAbsolutePath());
		String fileName = "";
		boolean validSaveFileFound = false;
		while(!validSaveFileFound)
		{
			Frame toSave = stData.getTraceDisplayAttribute().getFrame();
			saveDialog.setFileName( (int)toSave.begTime      + "-"  + 
									(int)toSave.endTime      + ", " +
									(int)toSave.begProcess   + "-"  +
									(int)toSave.endProcess   + ".bin");
			fileName = saveDialog.open();
			
			if (fileName == null)
				return;
			else
			{
				if (!new File(fileName).exists())
					validSaveFileFound = true;
				else
				{
					//open message box confirming whether or not they want to overwrite saved file
					//if they select yes, validSaveFileFound = true;
					//if they select no, validSaveFileFound = false;

					validSaveFileFound = MessageDialog.openConfirm(
											getShell(), 
											"File exists", 
											"This file path already exists.\nDo you want to overwrite this save file?");
				}
			}
		}
		
		try
		{
			ObjectOutputStream out = null;
			try
			{
				out = new ObjectOutputStream(new FileOutputStream(fileName));
				out.writeObject(stData.getTraceDisplayAttribute().getFrame());
			}
			finally
			{
				out.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void openConfiguration() {
		if (stData == null)
			return;
		
		FileDialog openDialog = new FileDialog(getShell(), SWT.OPEN);
		openDialog.setText("Open View Configuration");
		openDialog.setFilterPath(stData.getExperiment().getDefaultDirectory().getAbsolutePath());
		String fileName = "";
		boolean validFrameFound = false;
		while(!validFrameFound)
		{
			fileName = openDialog.open();
			
			if (fileName == null) return;
			File binFile = new File(fileName);
			
			if (binFile.exists())
			{
				ObjectInputStream in = null;
				try
				{
					in = new ObjectInputStream(new FileInputStream(fileName));
					Frame current = (Frame)in.readObject();
					notifyChanges("Frame", current);
					validFrameFound = true;
				}
				catch (IOException e)
				{
					validFrameFound = false;
					MessageDialog.openError(getShell(), "Error reading the file",
							"Fail to read the file: " + fileName );
				}
				catch (ClassNotFoundException e)
				{
					validFrameFound = false;
					MessageDialog.openError(getShell(), "Error reading the file", 
							"File format is not recognized. Either the file is corrupted or it's an old format");
				}
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						MessageDialog.openWarning(getShell(), "Error closing the file", 
								"Unable to close the file: " + fileName);
					}
				}
			}
		}
	}
    
    /***
     * set a new range for Y-axis
     * @param pBegin: the top position
     * @param pEnd: the bottom position
     */
	private void setProcessRange(int pBegin, int pEnd) 
	{
    	final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
    	final Frame frame = new Frame(attributes.getFrame());
    	frame.begProcess = pBegin;
    	frame.endProcess = pEnd;
    	
		notifyChanges("Zoom V", frame);
	}

	private Position updatePosition(Point mouseDown)
	{
    	final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
    	
    	int selectedProcess = attributes.convertPixelToRank(mouseDown.y);

    	long closeTime = attributes.getTimeBegin() + (long)(mouseDown.x / getScalePixelsPerTime());
    	
    	if (closeTime > attributes.getTimeEnd()) {
    		System.err.println("ERR STDC SCSSample time: " + closeTime +" max time: " + 
    				attributes.getTimeEnd() + "\t new: " + ((attributes.getTimeBegin() + attributes.getTimeEnd()) >> 1));
    		closeTime = (attributes.getTimeBegin() + attributes.getTimeEnd()) >> 1;
    	}

    	return new Position(closeTime, selectedProcess);
	}
	
	
	private long getNumTimeUnitDisplayed()
	{
		return (stData.getTraceDisplayAttribute().getTimeInterval());
	}
	
	private double getNumProcessesDisplayed()
	{
		return (stData.getTraceDisplayAttribute().getProcessInterval());
	}
	

	// remove queue of jobs because it causes deadlock 
	// 
	final private ConcurrentLinkedQueue<BaseViewPaint> queue = new ConcurrentLinkedQueue<>();
	
	/*********************************************************************************
	 * Refresh the content of the canvas with new input data or boundary or parameters
	 *  
	 *  @param refreshData boolean whether we need to refresh and read again the data or not
	 *********************************************************************************/
	public void refresh(boolean refreshData) {
		//Debugger.printTrace("STDC rebuffer");
		//Okay, so here's how this works. In order to draw to an Image (the Eclipse kind)
		//you need to draw to its GC. So, we have this bufferImage that we draw to, so
		//we get its GC (bufferGC), and then pass that GC to paintViewport, which draws
		//everything to it. Then the image is copied to the canvas on the screen with that
		//event.gc.drawImage call down there below the 'if' block - this is called "double buffering," 
		//and it's useful because it prevent the screen from flickering (if you draw directly then
		//you would see each sample as it was getting drawn very quickly, which you would
		//interpret as flickering. This way, you finish the puzzle before you put it on the
		//table).

		// -----------------------------------------------------------------------
		// imageFinal is the final image with info of the depth and number of samples
		// the size of the final image is the same of the size of the canvas
		// -----------------------------------------------------------------------

		final Rectangle view = getClientArea();
		if (view.width <= 0 || view.height <= 0) 
			return;
		
		final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();

		attributes.setPixelHorizontal(view.width);
		attributes.setPixelVertical(view.height);
				
		final Image imageFinal = new Image(getDisplay(), view.width, view.height);
		final GC bufferGC = new GC(imageFinal);
		bufferGC.setBackground(ColorManager.COLOR_WHITE);
		bufferGC.fillRectangle(0,0,view.width,view.height);
		
		// -----------------------------------------------------------------------
		// imageOrig is the original image without "attributes" such as depth
		// this imageOrig will be used by SummaryView to count the number of colors
		// the size of the "original" image should be equivalent to the minimum of 
		//	the number of ranks or the number of pixels
		// -----------------------------------------------------------------------
		
		final int numLines = Math.min(view.height, attributes.getProcessInterval() );
		final Image imageOrig = new Image(getDisplay(), view.width, numLines);
		
		final GC origGC = new GC(imageOrig);
		origGC.setBackground(ColorManager.COLOR_WHITE);
		origGC.fillRectangle(0,0,view.width, numLines);

		// -----------------------------------------------------------------------
		// main method to paint to the canvas
		// if there's no exception or interruption, we redraw the canvas
		// -----------------------------------------------------------------------

		final boolean changedBounds = (refreshData? refreshData : !attributes.sameTrace(oldAttributes) );
		
		oldAttributes.copy(attributes);
		if (changedBounds) {
			ProcessTimeline []traces = new ProcessTimeline[ numLines ];
			ProcessTimelineService ptlService = (ProcessTimelineService) context.get(Constants.CONTEXT_TIMELINE);

			assert(ptlService != null);
			
			ptlService.setProcessTimeline(traces);
		}

		/*************************************************************************
		 *	Paints the specified time units and processes at the specified depth
		 *	on the SpaceTimeCanvas using the SpaceTimeSamplePainter given. Also paints
		 *	the sample's max depth before becoming overDepth on samples that have gone over depth.
		 *************************************************************************/
		final DetailViewPaint detailPaint = new DetailViewPaint(getDisplay(), bufferGC, origGC, stData, 
																numLines, changedBounds, this); 

		DetailPaintJobChangeListener listener = new DetailPaintJobChangeListener(
														detailPaint, view.height, 
														imageOrig,   imageFinal, 
														bufferGC,    origGC, 
														changedBounds);
		detailPaint.addJobChangeListener(listener);

		setMessage("Processing ...");
		
		// this part of the code causes deadlock on VirtualBox Ubuntu
		// if we don't clear the queue
		cancelJobs();
		
		detailPaint.schedule();
		
		queue.add(detailPaint);
	}

	private void cancelJobs() {
  		if (!queue.isEmpty()) {
  			queue.stream().forEach(job -> {
				// a job cannot be terminated.
				// this is fine, we should wait until it terminates or
				// response that it will cancel in the future
  				job.cancel();
  			});
		}
	}
	
	private void donePainting(Image imageOrig, Image imageFinal, 
							  int imageHeight, int viewHeight, 
							  boolean refreshData)
	{		
		initBuffer();
		setBuffer( imageFinal );
		
		// in case of filter, we may need to change the cursor position
		if (refreshData) {
			final List<IdTuple> list = stData.getBaseData().getListOfIdTuples(IdTupleOption.BRIEF);
			final Position p = stData.getTraceDisplayAttribute().getPosition();
			
			if (p.process > list.size()-1) {
				// out of range: need to change the cursor position
				Position new_p = new Position( p.time, list.size() >> 1 );
				notifyChangePosition(new_p);
			}
		}

		// -----------------------------------------------------------------------
		// notify to all other views that a new image has been created,
		//	and it needs to refresh the view
		// issue #112: Ideally we should send the "original" image to other views so that
		// 			   they can compute the statistics correctly. However, when the original
		//             image doesn't fit to the canvas, some lines (like white lines) are
		// 			   overlapped with other lines. this makes the summary doesn't represent
		//             the main view.
		// 
		// -----------------------------------------------------------------------
		int deviceZoom = DPIUtil.getDeviceZoom();
		ImageData imgData = imageOrig.getImageData(deviceZoom);
		final double yscale = Math.max(getScalePixelsPerRank(), 1);		
		
		// if the original image doesn't fit the screen, we just use the final image.
		// This is not a good solution, but it at least makes the summary view appears the 
		// same as the main view.
		//
		if (yscale < BaseTimelineThread.MIN_HEIGHT_FOR_SEPARATOR_LINES)
			imgData = imageFinal.getImageData(deviceZoom);
		
		notifyChangeBuffer(imgData);
		
		updateButtonStates();
		clearMessage();
	}
	
	
	@Override
	public void widgetDisposed(DisposeEvent e) {
		
		// important: remove the existing jobs or Eclipse will
		// complain of jobs still exist
		cancelJobs();
		
		eventBroker.unsubscribe(this);
		removePaintListener(this);
		
		if (keyListener != null)
			removeKeyListener(keyListener);
		
		if (resizeListener != null)
			removeControlListener(resizeListener);
				
		tracePart.getOperationHistory().removeOperationHistoryListener(this);
		
		super.widgetDisposed(e);
	}
	
	//-----------------------------------------------------------------------------------------
	// Part for notifying changes to other views
	//-----------------------------------------------------------------------------------------
	
	
	/***********************************************************************************
	 * notify changes to other views
	 * 
	 * @param _topLeftTime
	 * @param _topLeftProcess
	 * @param _bottomRightTime
	 * @param _bottomRightProcess
	 ***********************************************************************************/
	private void notifyChanges(String label, Frame frame) 
	{
		String sLabel = (label == null ? "Set region" : label);
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_TRACE);
		ZoomOperation opZoom = new ZoomOperation(stData, sLabel, frame, context);

		// forces all other views to refresh with the new region
		try {
			// notify change of ROI
			tracePart.getOperationHistory().execute(opZoom, null, null);
			
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	/***********************************************************************************
	 * notify cursor position change to other views
	 * 
	 * @param position
	 ***********************************************************************************/
	private void notifyChangePosition(Position position) 
	{
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_POSITION);
		PositionOperation op = new PositionOperation(stData, position, context);
		try {
			tracePart.getOperationHistory().execute( op, null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	/***********************************************************************************
	 * Notify other views (especially summary view) that we have changed the buffer.
	 * The other views need to refresh the display if needed.
	 * 
	 * @param imageData
	 ***********************************************************************************/
	private void notifyChangeBuffer(ImageData imageData)
	{
		// -----------------------------------------------------------------------
		// notify to SummaryView that a new image has been created,
		//	and it needs to refresh the view
		// -----------------------------------------------------------------------
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_BUFFER);
		BufferRefreshOperation brOp = new BufferRefreshOperation(stData, imageData, context);
		try {
			tracePart.getOperationHistory().execute(brOp, null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	//-----------------------------------------------------------------------------------------
	// Part for handling operation triggered from other views
	//-----------------------------------------------------------------------------------------
	private HistoryOperation historyOperation = new HistoryOperation();
	
	@Override
	public void historyNotification(final OperationHistoryEvent event) {
		final IUndoableOperation operation = event.getOperation();
		
		if (!(operation instanceof AbstractTraceOperation)) {
			return;
		}
		AbstractTraceOperation op = (AbstractTraceOperation) operation;
		if (op.getData() != stData) 
			return;

		// handling the operations
		if ( operation.hasContext(tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_TRACE))    ||
			 operation.hasContext(tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_POSITION)) ||
			 operation.hasContext(tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_RESIZE))   ) 
		{
			int type = event.getEventType();
			// warning: hack solution
			// this space time detail canvas has priority to execute first before the others
			// the reason is most objects requires a new value of process time lines
			//	however this objects are set by this class
			switch (type)
			{
			case OperationHistoryEvent.ABOUT_TO_EXECUTE:
			case OperationHistoryEvent.ABOUT_TO_REDO:
			case OperationHistoryEvent.ABOUT_TO_UNDO:
				historyOperation.setOperation(operation);
				
				// we don't want to run the operation in a separate thread or in the UI thread
				// since this operation can incur an exception such as time out or connection error.
				// instead, we should run within the current process (it isn't ideal, but it works
				//	just fine at the moment)
				
				historyOperation.run();
				break;
			case OperationHistoryEvent.DONE:
				if (operation instanceof PositionOperation) {
					adjustLabels();
				}
				break;
			}
		} else if (operation.hasContext(tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_BUFFER))) {
			if (event.getEventType() == OperationHistoryEvent.DONE) {
				// this event is triggered by non ui-thread
				// we need to ask ui thread to execute it
				Display.getDefault().asyncExec(new Runnable() {					
					@Override
					public void run() {
						adjustLabels();
					}
				});
			}
		}
	}
	

	@Override
	protected void changePosition(Point point) 
	{
    	Position position = updatePosition(point);
    	notifyChangePosition(position);
	}


	@Override
	protected void changeRegion(Rectangle region) 
	{
		//If we're zoomed in all the way don't do anything
		if(getNumTimeUnitDisplayed() == Constants.MIN_TIME_UNITS_DISP)
		{
			if(getNumTimeUnitDisplayed() > MIN_PROC_DISP)
			{
				adjustSelection(region);
				setDetail();
			}
		}
		else
		{
			adjustSelection(region);
			setDetail();
		}
	}


	@Override
	protected String tooltipText(int pixel, RGB rgb) {
		return null;
	}


	@Override
	protected ColorTable getColorTable() {
		return stData.getColorTable();
	}


	@Override
	public void handleEvent(Event event) {

		Object obj = event.getProperty(IEventBroker.DATA);
		if (obj == null) return;
		
		TraceEventData eventData = (TraceEventData) obj;
		
		// Need to handle color mapping event here regardless the database
		// the change should be persistent and across panes and windows
		if (event.getTopic().equals(IConstants.TOPIC_COLOR_MAPPING)) {
			// reset the color table mapping
			stData.getColorTable().resetPredefinedColor();
			
			refresh(false);
			return;
		}
		if (eventData.source == this || eventData.data != this.stData)
			return;
		
		if (event.getTopic().equals(IConstants.TOPIC_DEPTH_UPDATE)) {
			Integer depth = (Integer) eventData.value;
			setDepth(depth.intValue());
			
		} else if (event.getTopic().equals(IConstants.TOPIC_FILTER_RANKS)) {
			refresh(true);
		}
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(TraceDisplayAttribute.DISPLAY_TIME_UNIT) &&
				evt.getNewValue() instanceof TimeUnit) {
			adjustLabels();
		}
	}

	//-----------------------------------------------------------------------------------------
	// PRIVATE CLASSES
	//-----------------------------------------------------------------------------------------
	
	/****************************************
	 * 
	 * Class to listen the status of a background job
	 *
	 ****************************************/
	private class DetailPaintJobChangeListener extends JobChangeAdapter
	{
		private final DetailViewPaint detailPaint;
		private final Image imageOrig, imageFinal;
		private final boolean changedBounds;
		private final GC bufferGC, origGC;
		private final int viewHeight;
		
		public DetailPaintJobChangeListener( DetailViewPaint detailPaint, 
											 int viewHeight,
											 Image imageOrig, 
											 Image imageFinal,
											 GC bufferGC, 
											 GC origGC, 
											 boolean changedBounds) {

			this.detailPaint = detailPaint;
			this.viewHeight  = viewHeight;
			this.imageOrig   = imageOrig;
			this.imageFinal  = imageFinal;
			this.bufferGC    = bufferGC;
			this.origGC      = origGC;
			this.changedBounds = changedBounds;
		}
		
		@Override
		public void aboutToRun(IJobChangeEvent event) {
			Instant.now().toEpochMilli();
		}

		
		@Override
		public void done(IJobChangeEvent event) {

			Display.getDefault().syncExec(() -> {
				queue.remove(detailPaint);
				
				if (event.getResult() == Status.OK_STATUS) {
					donePainting(imageOrig, imageFinal, 
								 detailPaint.getNumberOfLines(), viewHeight, 
								 changedBounds);
					
				} else if (event.getResult() == Status.CANCEL_STATUS) {
					// we don't need this "new image" since the paint fails
					imageFinal.dispose();	
				}

				redraw();
				
				// free resources 
				bufferGC.dispose();
				origGC.dispose();
				imageOrig.dispose();
			});
		}		
	}
	

	/*************************************************************************
	 * 
	 * Resizing thread by listening to the event if a user has finished
	 * 	the resizing or not
	 *
	 *************************************************************************/
	private class DetailBufferPaint implements BufferPaint
	{

		@Override
		public void rebuffering() {
			// force the paint to refresh the data			
			final TraceDisplayAttribute attr = stData.getTraceDisplayAttribute();
			IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_RESIZE);
			
			try {
				tracePart.getOperationHistory().execute(
						new WindowResizeOperation(stData, attr.getFrame(), context), null, null);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			//notifyChanges("Resize", attr.getFrame() );
		}
	}

	/*****
	 * 
	 * Thread-centric operation to perform undoable operations asynchronously
	 *
	 *****/
	private class HistoryOperation implements Runnable
	{
		private IUndoableOperation operation;
		
		public void setOperation(IUndoableOperation operation) {
			this.operation = operation;
		}
		
		@Override
		public void run() {
			// zoom in/out or change of ROI ?
			if (operation instanceof ZoomOperation || operation instanceof WindowResizeOperation) {
				Frame frame = ((ZoomOperation)operation).getFrame();
				final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
				
				attributes.setFrame(frame);
				zoom(frame.begTime, frame.begProcess, frame.endTime, frame.endProcess);
			}
			// change of cursor position ?
			else if (operation instanceof PositionOperation) {
				Position p = ((PositionOperation)operation).getPosition();
				stData.getTraceDisplayAttribute().setPosition(p);

				// just change the position, doesn't need to fully refresh
				redraw();
			} 
		}
	}
}
