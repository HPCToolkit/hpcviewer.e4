package edu.rice.cs.hpctraceviewer.ui.callstack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.e4.core.services.events.IEventBroker;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.string.StringUtil;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpctraceviewer.ui.base.AbstractBaseTableViewer;
import edu.rice.cs.hpctraceviewer.ui.base.ColorColumnLabelProvider;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;
import edu.rice.cs.hpctraceviewer.ui.internal.TraceEventData;
import edu.rice.cs.hpctraceviewer.ui.operation.AbstractTraceOperation;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.color.ColorTable;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import edu.rice.cs.hpctraceviewer.data.Position;



/**************************************************
 * A viewer for CallStackSamples.
 *************************************************/
public class CallStackViewer extends AbstractBaseTableViewer
	implements IOperationHistoryListener, 
			   DisposeListener,
			   EventHandler
{	
	private static final String EMPTY_FUNCTION = "--------------";
	
	private final IEventBroker eventBroker;
	private final ITracePart   tracePart;
	private final TableViewerColumn viewerColumn;
	private final ColorLabelProvider colorLabelProvider ;
	
	private SpaceTimeDataController stData ;
	private Listener selectionListener;
	
    /**
     * Creates a CallStackViewer with Composite parent, SpaceTimeDataController _stData, and HPCTraceView _view.
     * */
	public CallStackViewer(final ITracePart   tracePart,
						   final Composite    parent, 
						   final IEventBroker eventBroker)
	{
		super(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.FULL_SELECTION);
		
		this.tracePart   = tracePart;
		this.eventBroker = eventBroker;
		
        final Table stack = this.getTable();
        
        GridData data = new GridData(GridData.FILL_BOTH);
        stack.setLayoutData(data);
        
        //------------------------------------------------
        // add content provider
        //------------------------------------------------
        setContentProvider(ArrayContentProvider.getInstance());
        
        stack.setVisible(false);
        selectionListener = event -> {
			int depth = stack.getSelectionIndex(); 
			notifyChange(depth);
        };

		stack.addListener(SWT.Selection, selectionListener);
		
        //------------------------------------------------
		// add color column
        //------------------------------------------------
		TableViewerColumn colorViewer = new TableViewerColumn(this, SWT.NONE);
		colorLabelProvider = new ColorLabelProvider();
		colorViewer.setLabelProvider(colorLabelProvider);
				
		TableColumn col = colorViewer.getColumn();
		col.setText(" ");
		col.setWidth(IConstants.COLUMN_COLOR_WIDTH_PIXELS);
		col.setResizable(false);

        //------------------------------------------------
        // add label provider
        //------------------------------------------------

		final ColumnLabelProvider myLableProvider = new ColumnLabelProvider() {
        	
			@Override
			public Font getFont(Object element) {
				return FontManager.getFontGeneric();
			}
			
        	@Override
        	public String getText(Object element)
        	{
        		if (element instanceof String)
        			return (String) element;
        		return null;
        	}
        	
        	@Override
        	public String getToolTipText(Object element)
        	{
        		final String originalText = getText(element);
        		
        		return StringUtil.wrapScopeName(originalText, 100);
        	}
        	
        	@Override
        	public int getToolTipDisplayDelayTime(Object object)
        	{
        		return Constants.TOOLTIP_DELAY_MS;
        	}
		};
		
		viewerColumn = new TableViewerColumn(this, SWT.NONE);
		viewerColumn.setLabelProvider(myLableProvider);
		viewerColumn.getColumn().setWidth(100);
		getTable().setVisible(true);

		ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE);
		
		tracePart.getOperationHistory().addOperationHistoryListener(this);
		
		getTable().addDisposeListener(this);
	}
	
	
	/***
	 * refresh the call stack in case there's a new data
	 */
	public void setInput(SpaceTimeDataController data)
	{
		this.stData = data;
		colorLabelProvider.colorTable = data.getColorTable();
		eventBroker.subscribe(IConstants.TOPIC_DEPTH_UPDATE,  this);
	}

	
	/**********************************************************************
	 * Sets the sample displayed on the callstack viewer to be the one
	 * that most closely corresponds to (closeTime, process). Additionally,
	 * sets the depth to _depth.
	 *********************************************************************/
	private void setSample(Position position, final int depth)
	{
		//-------------------------------------------------------------------------------------------
		// dirty hack: the call stack viewer requires relative index of process, not the absolute !
		// so if the region is zoomed, then the relative index is based on the displayed processes
		//
		// however, if the selected process is less than the start of displayed process, 
		// 	then we keep the selected process
		//-------------------------------------------------------------------------------------------
		
		if (stData == null) {
			return;
		}
		var ptl = stData.getCurrentSelectedTraceline();
		
		// it's very unlikely if a timeline process cannot be found of a given process
		// If this really happens, possible scenarios:
		// - data is corrupted
		// - bug in computing the estimated process
		// - empty trace
		
		if (ptl == null)
			return;
		
		int sample = 0;
		boolean isMidpointEnabled = TracePreferenceManager.isMidpointEnabled();
		final List<String> listOfFunctions;
		
		sample = ptl.findMidpointBefore(position.time, isMidpointEnabled);				
		if (sample >= 0) {
			listOfFunctions = new ArrayList<>();
			var ctxId = ptl.getContextId(sample);
			if (ctxId >= 0) {
				var exp = stData.getExperiment();
				var cpInfo = exp.getScopeMap();
				var names  = cpInfo.getFunctionNames(ctxId);
				if (names != null)
					listOfFunctions.addAll(names);
			}
			if (listOfFunctions.size()<=depth)
			{
				//-----------------------------------
				// case of over depth
				//-----------------------------------
				final int numOverDepth = depth-listOfFunctions.size()+1;
				for(int l = 0; l<numOverDepth; l++)
					listOfFunctions.add(EMPTY_FUNCTION);
			}
		} else {
			listOfFunctions = new ArrayList<>(depth);
			Collections.fill(listOfFunctions, EMPTY_FUNCTION);
		}
		// fill the call stack and select the current depth
		final Display display = Display.getDefault();
		display.asyncExec( () -> {
			setInput(listOfFunctions);
			selectDepth(depth);
			viewerColumn.getColumn().pack();
		});
	}
	
	
	
	@Override
	public void widgetDisposed(DisposeEvent e) {
		super.widgetDisposed(e);
		
		tracePart.getOperationHistory().removeOperationHistoryListener(this);
		getTable().removeDisposeListener(this);
		getTable().removeListener(SWT.Selection, selectionListener);
	}

	
	/*****
	 * Select a specified depth in the call path
	 * @param _depth
	 */
	private void selectDepth(final int _depth)
	{
		this.getTable().select(_depth);
		this.getTable().redraw();
	}
	
	
	private void notifyChange(int depth)
	{
		TraceEventData data = new TraceEventData(stData, this, Integer.valueOf(depth));
		boolean result = eventBroker.post(IConstants.TOPIC_DEPTH_UPDATE, data);
		if (result)
			return;
		
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.debug("Warning: cannot broadcast new depth: " + depth);
	}
	
	
	@Override
	public void historyNotification(final OperationHistoryEvent event) {
		final IUndoableOperation operation = event.getOperation();

		if (!(operation instanceof AbstractTraceOperation)) {
			return;
		}
		AbstractTraceOperation op = (AbstractTraceOperation) operation;
		if (op.getData() != stData) 
			return;

		IUndoContext bufferCtx   = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_BUFFER);
		IUndoContext positionCtx = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_POSITION);
		
		if (operation.hasContext(bufferCtx) ||
				operation.hasContext(positionCtx)) {
			if (event.getEventType() == OperationHistoryEvent.DONE) {
				setSample(stData.getTraceDisplayAttribute().getPosition(), stData.getTraceDisplayAttribute().getDepth());			}
		}
	}
	
	
	/*************************************************************
	 * 
	 * Label provider for Color of the procedure
	 *
	 *************************************************************/
	private static class ColorLabelProvider extends ColorColumnLabelProvider 
	{
		ColorTable colorTable;
		
		@Override
		protected Color getColor(Event event, Object element) {
			if (element != EMPTY_FUNCTION && 
				element != null && 
				element instanceof String) {
				
				return colorTable.getColor((String) element);
			}
			return event.display.getSystemColor(SWT.COLOR_WHITE);
		}
	}


	@Override
	protected Point computeCellBounds(GC gc, Point extent) {
		return extent;
	}


	@Override
	public void handleEvent(org.osgi.service.event.Event event) {

		Object obj = event.getProperty(IEventBroker.DATA);
		if (obj == null) return;
		
		TraceEventData eventData = (TraceEventData) obj;
		if (eventData.source == this || eventData.data != this.stData)
			return;

		final int itemCount = this.getTable().getItemCount();
		final int depth = (Integer)eventData.value;
		
		if (itemCount <= depth)
		{
			//-----------------------------------
			// case of over depth
			//-----------------------------------
			final int overDepth = depth - itemCount + 1;
			for (int i=0; i<overDepth; i++) 
			{
				this.add(EMPTY_FUNCTION);
			}
		}
		selectDepth(depth);
	}
}
