// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.depth;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpctraceviewer.ui.base.ISpaceTimeCanvas;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;
import edu.rice.cs.hpctraceviewer.ui.internal.AbstractTimeCanvas;
import edu.rice.cs.hpctraceviewer.ui.internal.BaseViewPaint;
import edu.rice.cs.hpctraceviewer.ui.operation.AbstractTraceOperation;
import edu.rice.cs.hpctraceviewer.ui.operation.PositionOperation;
import edu.rice.cs.hpctraceviewer.ui.operation.ZoomOperation;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.color.ColorTable;
import edu.rice.cs.hpcsetting.color.ColorManager;
import edu.rice.cs.hpctraceviewer.data.Frame;
import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;
import edu.rice.cs.hpctraceviewer.data.Position;



/**********************************************************
 * 
 * A canvas for displaying the depth view.
 *
 **********************************************************/
public class DepthTimeCanvas extends AbstractTimeCanvas 
	implements IOperationHistoryListener, ISpaceTimeCanvas
{	
	private static final float FRACTION_ZOOM_DEPTH = 2.0f;
	private static final int   DEPTH_MIN = 1;
	
	private final ITracePart tracePart;

	private SpaceTimeDataController stData;
	private int currentProcess = Integer.MIN_VALUE;
	private Rectangle bound;
	private int visibleDepths;

	/********************
	 * constructor to create this canvas
	 * 
	 * @param composite : the parent composite
	 */
	public DepthTimeCanvas(ITracePart tracePart, Composite composite)
    {
		super(composite);
		
		this.tracePart = tracePart;
	}
	
	/****
	 * new data update
	 * @param _stData
	 */
	public void updateView(SpaceTimeDataController stData)
	{
		super.init();
		setVisible(true);
		
		if (this.stData == null) {
			// just initialize once
			tracePart.getOperationHistory().addOperationHistoryListener(this);
		}
		this.stData = stData;
		// this is to ensure that visible depth is not zero
		visibleDepths = Math.max(1, stData.getMaxDepth());
	}
	
	@Override
	public void widgetDisposed(DisposeEvent e) {

		removeDisposeListener(this);
		tracePart.getOperationHistory().removeOperationHistoryListener(this);
		super.widgetDisposed(e);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	@Override
	public void paintControl(PaintEvent event)
	{
		bound = getClientArea();

		if (stData == null || !stData.hasTraces())
			return;
		
		super.paintControl(event);
		
		final long topLeftPixelX = Math.round(stData.getTraceDisplayAttribute().getTimeBegin()*getScalePixelsPerTime());
		final int viewHeight 	 = bound.height;

		//--------------------
		//draws cross hairs
		//--------------------
		
		event.gc.setForeground(ColorManager.COLOR_WHITE);
		
		long selectedTime = stData.getTraceDisplayAttribute().getFrame().position.time;
		
		int topPixelCrossHairX = (int)(Math.round(selectedTime*getScalePixelsPerTime())-2-topLeftPixelX);
		event.gc.fillRectangle(topPixelCrossHairX,0,4,viewHeight);
		
		final int depth    = stData.getTraceDisplayAttribute().getDepth();
		final int maxDepth = stData.getMaxDepth();
		final int minDepth = TimelineDepthThread.getMinDepth(depth, visibleDepths, maxDepth);
		final int cdepth   = depth-minDepth;
		final int width    = cdepth*viewHeight/visibleDepths + viewHeight/(2*visibleDepths);
		event.gc.fillRectangle(topPixelCrossHairX-8,width-1,20,4);
	}
	
	
    /***
     * force to refresh the content of the canvas. 
     */
    public void refresh() 
    {
    	if (stData.hasTraces())
    		rebuffer();
    }
    
    
    @Override
    public double getScalePixelsPerTime()
	{
		if (bound == null) {
			final Display display = Display.getDefault();
			display.syncExec( () -> bound = getClientArea() );
		}
		final int viewWidth = bound.width;

		return (double)viewWidth / (double)getNumTimeDisplayed();
	}

    @Override
	public double getScalePixelsPerRank() {
		return Math.max(bound.height/(double)visibleDepths, 1);
	}

	
	/****
	 * Zoom out the depth: increase the depth so users can see more 
	 */
	public void zoomOut() {
		int maxDepth  = stData.getMaxDepth();
		visibleDepths = (int) Math.min(maxDepth, visibleDepths + FRACTION_ZOOM_DEPTH);
		
		rebuffer();
	}
	
	
	/****
	 * Zoom in the depth: decrease the depth so user can see more pixels
	 */
	public void zoomIn() {
		float fraction = Math.max(FRACTION_ZOOM_DEPTH, visibleDepths * 0.1f); 
		visibleDepths  = (int) Math.max(DEPTH_MIN, visibleDepths - fraction);
		 
		rebuffer();
	}
	
	
	/****
	 * check if we can zoom out
	 * @return true if it's feasible
	 */
	public boolean canZoomOut() {
		int maxDepth = stData.getMaxDepth();
		return visibleDepths < maxDepth;
	}
	
	
	/****
	 * check if can zoom in
	 * @return true if it's possible
	 */
	public boolean canZoomIn() {
		return visibleDepths > DEPTH_MIN;
	}
	
	
	//---------------------------------------------------------------------------------------
	// PRIVATE METHODS
	//---------------------------------------------------------------------------------------

	private long getNumTimeDisplayed()
	{
		return (stData.getTraceDisplayAttribute().getTimeInterval());
	}
	

	// remove queue of jobs because it causes deadlock 
	// 
	private final ConcurrentLinkedQueue<BaseViewPaint> queue = new ConcurrentLinkedQueue<>();

	/****
	 * Remove the jobs in the waiting list
	 */
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

    /************
     * method to repaint the canvas
     * this method can be costly, please do not call this unless the data has changed
     * 
     */
	private void rebuffer()
	{
		if (stData == null )
			return;

		final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
		final Frame frame = attributes.getFrame();

		// store the current process so that we don't need to rebuffer every time
		// we change the position within the same process
		currentProcess = frame.position.process;

		final Display display = Display.getDefault();
		
		display.syncExec( new Runnable() {
			
			@Override
			public void run() {
				final Rectangle rb = getBounds();
				
				final int viewWidth  = rb.width;
				final int viewHeight = rb.height;

				if (viewWidth <= 0 && viewHeight<= 0) {
					return;
				}
				//paints the current screen

				final Image imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
				setBuffer(imageBuffer);

				final GC bufferGC = new GC(getBuffer());
				bufferGC.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
				bufferGC.fillRectangle(0,0,viewWidth,viewHeight);
				
				attributes.setDepthPixelVertical(viewHeight);
								
				BaseViewPaint depthPaint = new DepthViewPaint( 	bufferGC, 
																stData, 
																true, 
																DepthTimeCanvas.this,
																visibleDepths);
				cancelJobs();
				
				depthPaint.addJobChangeListener(new DepthJobListener());
				depthPaint.schedule();

				queue.add(depthPaint);
			}
		});
	}



	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IOperationHistoryListener#historyNotification(org.eclipse.core.commands.operations.OperationHistoryEvent)
	 */
	public void historyNotification(final OperationHistoryEvent event) {
		
		if (event.getEventType() == OperationHistoryEvent.DONE) 
		{
			final IUndoableOperation operation = event.getOperation();
			if (!(operation instanceof AbstractTraceOperation)) {
				return;
			}
			AbstractTraceOperation op = (AbstractTraceOperation) operation;
			if (op.getData() != stData) 
				return;

			IUndoContext bufferContext   = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_BUFFER);
			IUndoContext positionContext = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_POSITION);
			
			if (operation.hasContext(bufferContext)) {
				// update the visible depth in case there's a change of max depth due to filtering
				visibleDepths = Math.max(1, stData.getMaxDepth());

				// this event includes if there's a change of colors definition, so everyone needs
				// to refresh the content.
				// in case of filter, the max depth may change too, 
				try {
					super.init();
					rebuffer();
				} catch (NullPointerException e) {
					// ignore exception when there's (possibly) multiple thread access
					LoggerFactory.getLogger(getClass())
						.error(String.format("%s: %s" ,  getClass().getCanonicalName(), e.getMessage()));
				}
				
			} else if (operation.hasContext(positionContext)) {
				PositionOperation opPos = (PositionOperation) operation;
				Position position = opPos.getPosition();
				if (position.process == currentProcess)
				{
					// changing cursor position within the same process
					redraw();
				} else {
					// different process, we need to repaint every thing
					rebuffer();
				}
			}
		}
	}

	@Override
	protected void changePosition(Point point) {
    	long closeTime = stData.getTraceDisplayAttribute().getTimeBegin() + (long)(point.x / getScalePixelsPerTime());
    	
    	Position currentPosition = stData.getTraceDisplayAttribute().getPosition();
    	Position newPosition = new Position(closeTime, currentPosition.process);
    	IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_POSITION);
    		
    	try {
			tracePart.getOperationHistory().execute(
					new PositionOperation(stData, newPosition, context), 
					null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}		
	}
	

	@Override
	protected void changeRegion(Rectangle region) 
	{
		final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();

		long topLeftTime 	 = attributes.getTimeBegin() + (long)(region.x / getScalePixelsPerTime());
		long bottomRightTime = attributes.getTimeBegin() + (long)((region.width+region.x) / getScalePixelsPerTime());
		
		if (bottomRightTime-topLeftTime<=1) {
			// should send error message that the length is too small
			return;
		}
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_TRACE);
		
		final Frame oldFrame 	= attributes.getFrame();
		final Position position = oldFrame.position;
		
		Frame frame = new Frame(topLeftTime, bottomRightTime,
				attributes.getProcessBegin(), attributes.getProcessEnd(),
				attributes.getDepth(), position.time, position.process);
		try {
			tracePart.getOperationHistory().execute(
					new ZoomOperation(stData, "Time zoom out", frame, context), 
					null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private class DepthJobListener extends JobChangeAdapter
	{
		@Override
		public void done(IJobChangeEvent event) {

			Display display = Display.getDefault();
			display.asyncExec( () -> redraw() );
		}
	}

	@Override
	public void setMessage(String message) {
		// not needed.
	}

	@Override
	protected String tooltipText(int pixel, RGB rgb) {
		return null;
	}

	@Override
	protected ColorTable getColorTable() {
		return stData.getColorTable();
	}
}
