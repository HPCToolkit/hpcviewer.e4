package edu.rice.cs.hpctraceviewer.ui.main;

import java.util.List;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimelineService;
import edu.rice.cs.hpctraceviewer.ui.base.ITraceCanvas;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.base.ITraceCanvas.MouseState;
import edu.rice.cs.hpctraceviewer.ui.operation.AbstractTraceOperation;


/*********************
 * 
 * Canvas to draw vertical axis of the main view
 *
 *********************/
public class CanvasAxisY extends AbstractAxisCanvas 
{
	private final int [][]listColorSWT = {
											{ SWT.COLOR_CYAN,    SWT.COLOR_DARK_BLUE    },
											{ SWT.COLOR_YELLOW,  SWT.COLOR_DARK_MAGENTA },
											{ SWT.COLOR_GREEN,   SWT.COLOR_DARK_YELLOW  },
											{ SWT.COLOR_BLUE,    SWT.COLOR_DARK_GREEN   },
											{ SWT.COLOR_WHITE,   SWT.COLOR_BLACK  }
										};
	
	private final Color [][]listColorObjects;

	/** Relates to the condition that the mouse is in.*/
	private ITraceCanvas.MouseState mouseState;
	
	private AxisToolTip tooltip = null;


	
	/****
	 * Constructor CanvasAxisY
	 * 
	 * @param tracePart ITracePart the parent of the view
	 * @param timeLine ProcessTimelineService
	 * @param parent Composite
	 * @param style int (see {@code SWT} constants for canvas)
	 */
	public CanvasAxisY(ITracePart tracePart, Composite parent, int style) {
		super(tracePart, parent, style);
		
		listColorObjects = new Color[5][2];
		for(int i=0; i<5; i++) {
			for (int j=0; j<2; j++) {
				listColorObjects[i][j] = getDisplay().getSystemColor(listColorSWT[i][j]);				
			}
		}	
		mouseState = MouseState.ST_MOUSE_INIT;
	}
	
	
	@Override
	public void setData(Object data) {
		super.setData(data);
		
		SpaceTimeDataController stdc = (SpaceTimeDataController) data;
        final var traceData = stdc.getBaseData();
        
        List<IdTuple> list   = traceData.getListOfIdTuples(IdTupleOption.BRIEF);
        boolean isSequential = list==null || list.isEmpty();
        
        if (isSequential) {
        	// it's a sequential code. No need to display the y-axis
        	return;
        }
        
		if (mouseState == MouseState.ST_MOUSE_INIT) {
			
			tooltip = new AxisToolTip(this);
			
			mouseState = MouseState.ST_MOUSE_NONE;
		}
		tooltip.setData(stdc);
	}
	
	
	private void rebuffer() {
		if (getData() == null)
			return;
		
		if (mouseState == MouseState.ST_MOUSE_INIT)
			// not initialized yet (or it's a sequential program)
			return;
		
		final SpaceTimeDataController data   = (SpaceTimeDataController) getData();
        final var traceData = data.getBaseData();

        if (traceData == null)
        	return;

		final TraceDisplayAttribute attribute = data.getTraceDisplayAttribute();
		
		List<IdTuple> listIdTuples = traceData.getListOfIdTuples(IdTupleOption.BRIEF);
		if (listIdTuples == null || listIdTuples.isEmpty())
			return;
		
		// ------------------------------------------------------------------------------------------
		// setup the GC buffer
		// ------------------------------------------------------------------------------------------
		initBuffer();

		final int viewWidth = getBounds().width;
		final int viewHeight = getBounds().height;

		if (viewWidth == 0 || viewHeight == 0)
			return;

		final Image imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
		final GC gc = new GC(imageBuffer);

		// --------------------------------------------------------------------------
        // Manually fill the client area with the default background color
        // Some platforms don't paint the background properly 
		// --------------------------------------------------------------------------
        
		gc.setBackground(getParent().getBackground());
		gc.fillRectangle(getClientArea());

		// -----------------------------------------------------
		// collect the position and the length of each process
		// -----------------------------------------------------
		IdTuple idtupleOld  = null;
		int []oldColorIndex = new int[5];
		final ProcessTimelineService timeLine = data.getProcessTimelineService();
		
		final int numTraces = timeLine.getNumProcessTimeline();
		float pixelsPerRank = (float) attribute.getPixelVertical() / numTraces;
		
		for (int i=0; i<numTraces; i++) {
			var procTimeline = timeLine.getProcessTimeline(i);
			if (procTimeline == null)
				continue;
			
			final int y_curr = (int) (procTimeline.line() * pixelsPerRank);
			
			var nextline = i + 1 < numTraces ? timeLine.getProcessTimeline(i+1).line() : numTraces;  
			final int y_next = (int) (nextline * pixelsPerRank);
			
			final int height = y_next - y_curr + 1;

			IdTuple idtuple  = procTimeline.getProfileIdTuple();
			if (idtuple == null)
				continue;
	        
	        // for sequential code, we assume the number of parallelism is 1
	        // (just to avoid the zero division)
	        int partition   = Math.max(idtuple.getLength(), 1);
	        int columnWidth = HPCTraceView.Y_AXIS_WIDTH / partition;

			for(int j=0; j<idtuple.getLength(); j++) {

				int currentColor;
				
				if (idtupleOld != null && idtupleOld.getLength()>j && idtuple.getPhysicalIndex(j)!= idtupleOld.getPhysicalIndex(j)) {
					// make sure the current color is different than the previous one
					currentColor = 1-oldColorIndex[j];
				} else {
					currentColor = oldColorIndex[j];
				}
				
				// -----------------------------------------------------
				// draw the column for each id tuple
				// -----------------------------------------------------

				int x = j * columnWidth;
				Color color = listColorObjects[j%5][currentColor];

				gc.setBackground(color);
				gc.fillRectangle(x, y_curr, columnWidth, height);
				oldColorIndex[j] = currentColor;
			}
			idtupleOld = idtuple;
		}
		setBuffer(imageBuffer);
		gc.dispose();
		redraw();
	}
	
	@Override
	public void dispose() {
		if (tooltip != null) {
			tooltip.deactivate();
			tooltip = null;
		}
		super.dispose();
	}
	
		
	@Override
	public void historyNotification(final OperationHistoryEvent event) {

		if (isDisposed())
			return;
		
		if (event.getEventType() == OperationHistoryEvent.DONE) {
			final IUndoableOperation operation = event.getOperation();
			if (!(operation instanceof AbstractTraceOperation)) {
				return;
			}
			final AbstractTraceOperation op = (AbstractTraceOperation) operation;
			if (op.getData() != getData())
				return;
			
			rebuffer();
		}
	}

	/********************************************************
	 * 
	 * Tooltip class to show the rank and/or the thread of the 
	 *  current position.
	 *  
	 *  Caller needs to set data every time there is a new data
	 *
	 ********************************************************/
	private static class AxisToolTip extends DefaultToolTip
	{
		private SpaceTimeDataController data;

		public AxisToolTip(Control control) {
			super(control);
		}
		
		void setData(SpaceTimeDataController data) {
			this.data = data;
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
			var listProfiles = data.getBaseData().getListOfIdTuples(IdTupleOption.BRIEF);
			var attributes = data.getTraceDisplayAttribute();
			var index = attributes.convertPixelToRank(line);

			return listProfiles.get(Math.min(listProfiles.size()-1, index));
		}

		@Override
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.window.DefaultToolTip#getText(org.eclipse.swt.widgets.Event)
		 */
		protected String getText(Event event) {
			
	        final var traceData = data.getBaseData();
			IdTuple idtuple  = getProfileFromPixel(event.y);
			
	        int partition = Math.max(idtuple.getLength(), 1);
			var columnWidth = HPCTraceView.Y_AXIS_WIDTH / partition;

			int level = Math.min(event.x / columnWidth, idtuple.getLength()-1);
			
			return idtuple.toString(level, traceData.getIdTupleTypes());
		}
		
		
		@Override
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.window.ToolTip#getLocation(org.eclipse.swt.graphics.Point, org.eclipse.swt.widgets.Event)
		 */
		public Point getLocation(Point tipSize, Event event) {
			Object obj = getToolTipArea(event);
			Control control = (Control) obj;
			
			return control.toDisplay(event.x+5, event.y-15);
		}
	}
}
