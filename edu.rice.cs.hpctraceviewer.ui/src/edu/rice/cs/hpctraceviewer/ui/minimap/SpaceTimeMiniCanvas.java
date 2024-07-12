// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.minimap;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.Frame;
import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;
import edu.rice.cs.hpctraceviewer.ui.base.ITraceCanvas;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;
import edu.rice.cs.hpctraceviewer.ui.internal.SpaceTimeCanvas;
import edu.rice.cs.hpctraceviewer.ui.operation.AbstractTraceOperation;
import edu.rice.cs.hpctraceviewer.ui.operation.ZoomOperation;


/*****************************************************************************
 * 
 * The Canvas onto which the MiniMap is painted.
 * 
 ****************************************************************************/

public class SpaceTimeMiniCanvas extends SpaceTimeCanvas 
	implements ITraceCanvas, PaintListener, IOperationHistoryListener
{
	/** Relates to the condition that the mouse is in.*/
	private MouseState mouseState;
	
	/** The point at which the mouse was clicked.*/
	private Point mouseDown;
	
	/** The point at which the mouse was on.*/
	private Point mousePrevious;
	
	/**Determines whether the first mouse click was inside the box or not.*/
	private boolean insideBox;
	
	private Rectangle view;
	
    private final Color COMPLETELY_FILTERED_OUT_COLOR;
    private final Color NOT_FILTERED_OUT_COLOR;
    private final Color COLOR_BLACK, COLOR_GRAY;
    
    /**
     * The pattern that we draw when we want to show that some ranks in the
     * region aren't shown because of filtering
     */
    private final Pattern PARTIALLY_FILTERED_PATTERN; 
    private final ControlAdapter controlAdapter;
    private final ITracePart tracePart;
    
    
    

	/**Creates a SpaceTimeMiniCanvas with the given parameters.*/
	public SpaceTimeMiniCanvas(ITracePart tracePart, Composite _composite)
	{	
		super(_composite);
		
		this.tracePart = tracePart;
		
		mouseState = MouseState.ST_MOUSE_INIT;
		insideBox = true;
		view =  new Rectangle(0, 0, 0, 0);
		
        // initialize colors
        COMPLETELY_FILTERED_OUT_COLOR = new Color(this.getDisplay(), 50,50,50);
        NOT_FILTERED_OUT_COLOR = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
        COLOR_BLACK = getDisplay().getSystemColor(SWT.COLOR_BLACK);
        COLOR_GRAY  = getDisplay().getSystemColor(SWT.COLOR_GRAY);

        // initialize pattern for filtered ranks
        PARTIALLY_FILTERED_PATTERN = createStripePattern();
        		
		controlAdapter = new ControlAdapter() {
			
			@Override
			public void controlResized(ControlEvent e) {
				if (stData != null) {
					final Frame frame = stData.getTraceDisplayAttribute().getFrame();
					setBox(frame);
				}
			}
		} ;
		addControlListener(controlAdapter );
		tracePart.getOperationHistory().addOperationHistoryListener(this);
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
    	
		tracePart.getOperationHistory().removeOperationHistoryListener(this);
    	removeControlListener(controlAdapter);
    	removeMouseListener(this);
    	removeMouseMoveListener(this);
    	removePaintListener(this);

    	COMPLETELY_FILTERED_OUT_COLOR.dispose();
    	PARTIALLY_FILTERED_PATTERN.dispose();
    	
    	super.widgetDisposed(e);
    }
    
	/*****
	 * Create a pattern image for representing a non-dense filter region
	 * 
	 * @return pattern
	 */
    private Pattern createStripePattern() {
            Image image = new Image(getDisplay(), 15, 15);
            GC gc = new GC(image);
            gc.setBackground(NOT_FILTERED_OUT_COLOR);
            gc.fillRectangle(image.getBounds());
            gc.setForeground(COMPLETELY_FILTERED_OUT_COLOR);
            // Oddly enough, drawing from points outside of the image makes the
            // lines look a lot better when the pattern is tiled.
            for (int i = 5; i < 15; i+= 5) {
                    gc.drawLine(-5, i+5, i+5, -5);
                    gc.drawLine(i-5, 20, 20, i-5);
            }
            gc.dispose();
            Pattern pattern = new Pattern(getDisplay(), image);
            image.dispose();
            
            return pattern;
    }


	/**********
	 * update the content of the view due to a new database
	 * 
	 * @param _stData : the new database
	 **********/
	public void updateView(SpaceTimeDataController _stData) 
	{
		setSpaceTimeData(_stData);

		if (this.mouseState == MouseState.ST_MOUSE_INIT) {
			this.mouseState = MouseState.ST_MOUSE_NONE;

			addMouseListener(this);
			addMouseMoveListener(this);
			addPaintListener(this);
			setVisible(true);
			tracePart.getOperationHistory().addOperationHistoryListener(this);
		}
		Rectangle r = this.getClientArea();
		view.x = 0;
		view.y = 0;
		view.height = r.height;
		view.width  = r.width;
		
		redraw();
	}
	
	/******
	 * update the view when a filtering event occurs
	 * in this case, we need to reset the content of view with the attribute
	 */
	public void updateView() 
	{
		final Frame frame = stData.getTraceDisplayAttribute().getFrame();		
		var baseData = stData.getBaseData();

		int p1 = (int) Math.round( (frame.begProcess+baseData.getFirstIncluded()) * getScalePixelsPerRank() );
		int p2 = (int) Math.round( (frame.endProcess+baseData.getFirstIncluded()) * getScalePixelsPerRank() );
		
		int t1 = (int) Math.round( frame.begTime * getScalePixelsPerTime() );
		int t2 = (int) Math.round( frame.endTime * getScalePixelsPerTime() );
		
		int dp = Math.max(p2-p1, 1);
		int dt = Math.max(t2-t1, 1);

		view.x = t1;
		view.y = p1;
		view.width  = dt; 
		view.height = dp;
		
		redraw();
	}
	
	/**The painting of the miniMap.*/
	@Override
	public void paintControl(PaintEvent event)
	{
		if (this.stData == null)
			return;
		
		final Rectangle clientArea = getClientArea();
		
		// paint the background with black color
		
		event.gc.setBackground(COLOR_BLACK);
		event.gc.fillRectangle(clientArea);
		
		// paint the current view
		
		final Frame frame = stData.getTraceDisplayAttribute().getFrame();		
		var baseData = stData.getBaseData();

		int p1 = (int) Math.round( (baseData.getFirstIncluded()) * getScalePixelsPerRank() );
		int p2 = (int) Math.round( (baseData.getLastIncluded()+1) * getScalePixelsPerRank() );
		
		int t1 = (int) Math.round( frame.begTime * getScalePixelsPerTime() );
		int t2 = (int) Math.round( frame.endTime * getScalePixelsPerTime() );
		
		int dp = Math.max(p2-p1, 1);
		int dt = Math.max(t2-t1, 1);

        if (baseData.isDenseBetweenFirstAndLast()){
            event.gc.setBackground(NOT_FILTERED_OUT_COLOR);
        } else {
            try{
            event.gc.setBackgroundPattern(PARTIALLY_FILTERED_PATTERN);
            }
            catch (SWTException e){
                    System.out.println("Advanced graphics not supported");
                    event.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
            }
        }
        // The width of the region is always the same as the width of the
        // minimap because you can't filter by time
        event.gc.fillRectangle(0, p1, getClientArea().width, dp);

		// only for the dense region. For non-dense region, it's too tricky
		if (baseData.isDenseBetweenFirstAndLast()) {
			// original current position 
	        p1 = (int) Math.round( (baseData.getFirstIncluded() +  frame.begProcess )* getScalePixelsPerRank());
	        p2 = (int) Math.round( (baseData.getFirstIncluded() +  frame.endProcess )* getScalePixelsPerRank());
	        dp = Math.max(1, p2 - p1);
	        
			event.gc.setBackground(COLOR_GRAY);		
			event.gc.fillRectangle(t1, p1, dt, dp);
		}

		if (insideBox) {
			// when we move the box
			event.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			event.gc.fillRectangle(view);
		} else {
			// when we want to create a new box
			event.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_YELLOW));
			event.gc.drawRectangle(view);
		}
	}
	
	
    /**The painting of the miniMap.
     * this is the original paintControl with Rectangles and labels
     * 
    public void paintControl(PaintEvent event)
    {
            if (this.stData == null)
                    return;
                                           
            view.width = getClientArea().width;
            view.height = getClientArea().height;
           
            IBaseData baseData = stData.getBaseData();
            ArrayList<MiniCanvasRectangle> rectangles = new ArrayList<MiniCanvasRectangle>();
            if (baseData.getFirstIncluded() != 0) {
                    Rectangle rect = new Rectangle(0, 0, getClientArea().width, (int) (baseData.getFirstIncluded()*getScaleY()));
                    rectangles.add(new MiniCanvasRectangle(SampleHiddenReason.FILTERED, true, rect));
            }
            if (baseData.getLastIncluded() + 1 != baseData.getNumberOfRanks()) {
                    int topY = (int) ((baseData.getLastIncluded()+1)*getScaleY());
                    Rectangle rect = new Rectangle(0, topY , getClientArea().width, getClientArea().height-topY);
                    rectangles.add(new MiniCanvasRectangle(SampleHiddenReason.FILTERED, true, rect));
            }
            // This is the region shown in the detail view
            EnumSet<SampleHiddenReason> mainRegionReasons = EnumSet.noneOf(SampleHiddenReason.class);
            if (attributes.getProcessInterval() < attributes.numPixelsV)
                    mainRegionReasons.add(SampleHiddenReason.VERTICAL_RESOLUTION);
            if (!baseData.isDenseBetweenFirstAndLast())
                    mainRegionReasons.add(SampleHiddenReason.FILTERED);
            if (mainRegionReasons.size() == 0)
                    mainRegionReasons.add(SampleHiddenReason.NONE);
   
            // TODO: THESE ARE APPROXIMATIONS. LOOKING UP THE ACTUAL VALUE WOULD BE BETTER.
            int topPx = (int) ((baseData.getFirstIncluded() + attributes.getProcessBegin()) * getScaleY());
            int height = (int)(attributes.getProcessInterval() * getScaleY());
            Rectangle rect = new Rectangle(
                            (int)(attributes.getTimeBegin() * getScaleX()), topPx,
                            (int)(attributes.getTimeInterval() * getScaleX()), height);
            rectangles.add(new MiniCanvasRectangle(mainRegionReasons, false, rect));
           
            //TODO: None of the code above this point belongs in the paint method.
            Control[] oldChildren = this.getChildren();
            for (int i = 0; i < oldChildren.length; i++) {
                    // oldChildren[i].dispose();
            }
            for (MiniCanvasRectangle miniCanvasRectangle : rectangles) {
                    miniCanvasRectangle.getControl(this);
            }
           
    }
    */
	
	/**Sets the white box in miniCanvas to correlate to spaceTimeDetailCanvas proportionally.*/
	private void setBox(final Frame frame)
	{
		if (this.stData == null)
			return;

		final Display display = Display.getDefault();
		display.asyncExec( new Runnable() {
			
			@Override
			public void run() {
				if (SpaceTimeMiniCanvas.this.isDisposed())
					return;
				
				var pixelsPerTime = getScalePixelsPerTime();
				var pixelsPerRank = getScalePixelsPerRank();
				
				view.x = (int)Math.round(frame.begTime * pixelsPerTime);
				view.y = (int)Math.round(frame.begProcess * pixelsPerRank);
				
				int bottomRightPixelX = (int)Math.round(frame.endTime * pixelsPerTime);
				int bottomRightPixelY = (int)Math.round(frame.endProcess * pixelsPerRank);
				
				view.width  = Math.max(1, bottomRightPixelX-view.x);
				view.height = Math.max(1, bottomRightPixelY-view.y);
				
				insideBox = true;
				redraw();
			}
		} );
	}
	
	/**Moves white box to correspond to where mouse has moved to.*/
	private void moveBox(Point mouseCurrent)
	{
		// compute the different cursor movement 
		int changeX = mouseCurrent.x-mousePrevious.x;
		int changeY = mouseCurrent.y-mousePrevious.y;
		
		// update the values of the view based on the different cursor movement
		view.x += changeX;
		view.y += changeY;
		
		// make sure that the view is not out of range

		view.x = Math.max(view.x, 0);
		view.y = Math.max(view.y, getLowestY());

    	// ---------------------------------------------------------
    	// make sure that the selected box is within the range
    	// ---------------------------------------------------------
		checkRegion(view);

		mousePrevious = mouseCurrent;
	}
	

	/**Scales coordinates and sends them to detailCanvas.*/
	private void confirmNewRegion()
	{
		Point miniTopLeft = new Point( view.x, view.y);
		Point miniBottomRight = new Point( view.x+view.width, view.y+view.height);
		
		final var data = stData.getBaseData();
		
		long detailTopLeftTime = (long)(miniTopLeft.x/getScalePixelsPerTime());
		int detailTopLeftProcess = (int) Math.round( miniTopLeft.y/getScalePixelsPerRank() - data.getFirstIncluded());
		
		long detailBottomRightTime = (long)(miniBottomRight.x / getScalePixelsPerTime());
		int detailBottomRightProcess = (int) Math.round( miniBottomRight.y/getScalePixelsPerRank()) - data.getFirstIncluded();
		
		// hack: make sure p2 > than p1 since the detail canvas assumes exclusive p2 :-(
		if (detailBottomRightProcess-detailTopLeftProcess <= 0) {
			detailBottomRightProcess = detailTopLeftProcess + 1;
		}
		
		final Frame originalFrame = stData.getTraceDisplayAttribute().getFrame();
		
		// copy the frame from the original one so that we can copy the values of depth and position
		Frame frame = new Frame( originalFrame );
		frame.set(detailTopLeftTime, detailBottomRightTime, detailTopLeftProcess, detailBottomRightProcess);
		
		int totTraces = stData.getTotalTraceCount();
		
		assert frame.begProcess >= 0 && frame.begProcess < totTraces : 
			"incorrect beg rank: " + frame.begProcess + " should be in the range [0," + 
			totTraces + "]";
		
		assert frame.endProcess <= totTraces && frame.endProcess > 0 :
			"incorrect end rank: " + frame.endProcess + " should be in the range [ 0," 
			 + totTraces + "]";
		
		// do not submit new frame is the region area is the same as the old one
		if (!frame.equals(originalFrame))
			notifyRegionChangeOperation(frame);
	}
	
	/****
	 * notify to other views that we have changed the region to view
	 * 
	 * @param frame
	 */
	private void notifyRegionChangeOperation( Frame frame )
	{
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_TRACE);
		try {
			tracePart.getOperationHistory().execute(
					new ZoomOperation(stData, "Change region", frame, context),
					null, null);
		} catch (ExecutionException e) 
		{
			e.printStackTrace();
		}
	}
	
	/****
	 * retrieve the highest Y pixels based on the number of visible ranks
	 * 
	 * @return
	 */
	private int getHighestY() {
		
		final var baseData = stData.getBaseData();
		int highestRank = baseData.getLastIncluded()+1;
		return (int) Math.round(highestRank * getScalePixelsPerRank());
	}
	
	/****
	 * retrieve the minimum Y pixel based on the number of visible ranks
	 * In case of filters, the lowest rank can be other than zero
	 * 
	 * @return
	 */
	private int getLowestY() {
		final var baseData = stData.getBaseData();
		final int lowestRank = baseData.getFirstIncluded();
		return (int) Math.round(lowestRank * getScalePixelsPerRank());
	}
	
	/**Updates the selectionBox on the MiniMap to have corners at p1 and p2.*/
	private void adjustSelection(Point p1, Point p2)
	{
    	// ---------------------------------------------------------
		// get the region of the selection
    	// ---------------------------------------------------------
		view.x = Math.max(0, Math.min(p1.x, p2.x) );
		view.y = Math.max(getLowestY(), Math.min(p1.y, p2.y) );
    	
		view.width = Math.abs( p1.x - p2.x );
		view.height = Math.abs( p1.y - p2.y );
    	
    	// ---------------------------------------------------------
    	// make sure that the selected box is within the range
    	// ---------------------------------------------------------
		checkRegion(view);
    }
	
	/****
	 * check if the new region is within the allowed area
	 * We just assume the start x and y are already good, and now
	 * 	we check the end x and y which is very tricky
	 * @param region
	 */
	private void checkRegion(Rectangle region) 
	{
		final Rectangle area = getClientArea();
		
		// check if the width is within the range
    	if ( region.x + region.width > area.width )
    		region.x = area.width - region.width;
    	
    	// check if the height is within the view
    	int y_end = region.y + region.height; 
    	if ( y_end > area.height )
    		region.y = area.height - region.height;
    	
    	int highestY = getHighestY();
		int lowestY = getLowestY();
		
		// make sure the end y is less than the max y
		if (y_end > highestY) {
    		region.y = Math.max(highestY - region.height, lowestY);
    		
    		final int maxHeight = highestY - lowestY;
    		region.height = Math.min(region.height, maxHeight);
    	}
		
		// make sure the start y is less than the max y
		region.y = Math.min(region.y, highestY - 1);
	}
	
	/**Gets the scale in the X-direction (pixels per time unit).*/
	public double getScalePixelsPerTime()
	{
		if (isDisposed())
			return 0;
		
		return (double)getClientArea().width / (double)stData.getTimeWidth();
	}

	/**Gets the scale in the Y-direction (pixels per process).*/
	public double getScalePixelsPerRank()
	{
		final var data = stData.getBaseData();
		final Rectangle area = getClientArea();
		return (double)area.height / (data.getNumberOfRanks());
	}

	
	/* *****************************************************************
	 *		
	 *		MouseListener and MouseMoveListener interface Implementation
	 *      
	 ******************************************************************/

	public void mouseDown(MouseEvent e)
	{
		if (e.button == 1 && mouseState == MouseState.ST_MOUSE_NONE)
		{
			mouseState = MouseState.ST_MOUSE_DOWN;
			mouseDown = new Point(e.x,e.y);
			mousePrevious = new Point(e.x,e.y);
			
			insideBox = ( mouseDown.x>=view.x && 
					mouseDown.x<=view.x+view.width && 
					mouseDown.y>=view.y &&  
					mouseDown.y<=view.y+view.height );
		}
	}

	public void mouseUp(MouseEvent e)
	{
		if (mouseState == MouseState.ST_MOUSE_DOWN)
		{			
			// The point at which the mouse was released.
			final var mouseUp = new Point(e.x,e.y);
			mouseState = MouseState.ST_MOUSE_NONE;
			if (insideBox)
			{
				moveBox(mouseUp);
				confirmNewRegion();
			}
			else
			{
				// If the user draws a very small region to zoom in on, we are
				// going to assume it was a mistake and not draw anything.
				if(Math.abs(mouseUp.x-mouseDown.x)>3 || Math.abs(mouseUp.y-mouseDown.y)>3) 
				{
					adjustSelection(mouseDown, mouseUp);	
					confirmNewRegion();
				}else {
					final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
					//Set the selection box back to what it was because we didn't zoom
					setBox(attributes.getFrame());
				}
			}
		}
	}
	
	public void mouseMove(MouseEvent e)
	{
		if(mouseState == MouseState.ST_MOUSE_DOWN)
		{
			Point mouseCurrent = new Point(e.x,e.y);
			if (insideBox)
				moveBox(mouseCurrent);
			else
				adjustSelection(mouseDown, mouseCurrent);
			
			redraw();
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IOperationHistoryListener#historyNotification(org.eclipse.core.commands.operations.OperationHistoryEvent)
	 */
	public void historyNotification(final OperationHistoryEvent event) {
		final IUndoableOperation operation = event.getOperation();
		
		if (!(operation instanceof AbstractTraceOperation)) {
			return;
		}
		AbstractTraceOperation op = (AbstractTraceOperation) operation;
		if (op.getData() != stData) 
			return;

		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_BUFFER);
		if (operation.hasContext(context)) {

			final Frame frame = stData.getTraceDisplayAttribute().getFrame();
			setBox(frame);
		}
	}
}