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
import edu.rice.cs.hpcdata.experiment.extdata.IBaseData;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;
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
											{ SWT.COLOR_MAGENTA, SWT.COLOR_DARK_GREEN   },
											{ SWT.COLOR_WHITE,   SWT.COLOR_DARK_RED     },
											{ SWT.COLOR_GRAY,    SWT.COLOR_DARK_YELLOW  }
										};
	private int columnWidth = HPCTraceView.Y_AXIS_WIDTH/4;
	
	private final Color [][]listColorObjects;
	private final ProcessTimelineService timeLine;

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
	public CanvasAxisY(ITracePart tracePart, ProcessTimelineService timeLine, Composite parent, int style) {
		super(tracePart, parent, style);
		
		this.timeLine = timeLine;
		
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
        final IBaseData traceData = stdc.getBaseData();
        
        List<IdTuple> list   = traceData.getListOfIdTuples(IdTupleOption.BRIEF);
        boolean isSequential = list==null || list.size()==0;
        
        if (isSequential) {
        	// it's a sequential code. No need to display the y-axis
        	return;
        }
        
		if (mouseState == MouseState.ST_MOUSE_INIT) {
			
			tooltip = new AxisToolTip(this);
			
			mouseState = MouseState.ST_MOUSE_NONE;
		}
        
        // for sequential code, we assume the number of parallelism is 1
        // (just to avoid the zero division)
        int partition = Math.max(traceData.getNumLevels(), 1);
		columnWidth = HPCTraceView.Y_AXIS_WIDTH / partition;

		tooltip.setData(stdc, columnWidth);
	}
	
	
	private void rebuffer() {
		if (getData() == null)
			return;
		
		if (mouseState == MouseState.ST_MOUSE_INIT)
			// not initialized yet (or it's a sequential program)
			return;
		
		final SpaceTimeDataController data   = (SpaceTimeDataController) getData();
        final IBaseData traceData 		     = data.getBaseData();

        if (traceData == null)
        	return;

		final TraceDisplayAttribute attribute = data.getTraceDisplayAttribute();
		
		List<IdTuple> listIdTuples = traceData.getListOfIdTuples(IdTupleOption.BRIEF);
		if (listIdTuples == null || listIdTuples.size() == 0)
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
		setBuffer(imageBuffer);

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
		int []oldColorIndex = new int[traceData.getNumLevels()];
		
		for (int i=0; i<getNumProcessTimeline(); i++) {
			ProcessTimeline procTimeline = getProcessTimeline(i);
			if (procTimeline == null)
				continue;
			
			final int procNumber  = procTimeline.getProcessNum();
			if (procNumber >= listIdTuples.size())
				// inconsistency between the list of processes and the current timeline
				// probably hpctraceviewer is in the middle of rebuffering
				return;
			
			final int y_curr = attribute.convertRankToPixel(procNumber);
			final int y_next = attribute.convertRankToPixel(procNumber+1);
			
			IdTuple idtuple  = listIdTuples.get(procNumber);

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

				int x_start = j * columnWidth;
				int x_end   = x_start + columnWidth - 1;

				Color color  = listColorObjects[j%5][currentColor];

				gc.setBackground(color);
				gc.fillRectangle(x_start, y_curr, x_end, y_next);
				
				oldColorIndex[j] = currentColor;
			}
			idtupleOld = idtuple;
		}
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
	
	
	protected int getNumProcessTimeline() {
		return timeLine.getNumProcessTimeline();
	}
	
	
	protected ProcessTimeline getProcessTimeline(int i) {
		return timeLine.getProcessTimeline(i);
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
	static private class AxisToolTip extends DefaultToolTip
	{
		private SpaceTimeDataController data;
		private int columnWidth;

		public AxisToolTip(Control control) {
			super(control);
		}
		
		void setData(SpaceTimeDataController data, int columnWidth) {
			this.data = data;
			this.columnWidth = columnWidth;
		}
	
		@Override
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.window.DefaultToolTip#getText(org.eclipse.swt.widgets.Event)
		 */
		protected String getText(Event event) {
			
	        final IBaseData traceData = data.getBaseData();

			final TraceDisplayAttribute attribute = data.getTraceDisplayAttribute();
			int process = attribute.convertPixelToRank(event.y);
			
			List<IdTuple> listTuples = traceData.getListOfIdTuples(IdTupleOption.BRIEF);
						
			if (process < 0 && process >= listTuples.size())
				return null;
			
			IdTuple id  = listTuples.get(process); 
			int level   = Math.min(event.x / columnWidth, id.getLength()-1);
			
			return id.toString(level, traceData.getIdTupleTypes());
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
