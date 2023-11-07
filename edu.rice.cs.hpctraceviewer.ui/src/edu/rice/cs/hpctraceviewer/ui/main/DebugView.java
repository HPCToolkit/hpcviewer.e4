package edu.rice.cs.hpctraceviewer.ui.main;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimelineService;
import edu.rice.cs.hpctraceviewer.ui.base.AbstractBaseItem;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;


/****************************
 * 
 * View for debugging purpose
 *
 */
public class DebugView extends AbstractBaseItem implements IOperationHistoryListener, DisposeListener
{
	private TableViewer tableViewer;
	private ComboViewer listExecutionContexts;
	
	private ITracePart tracePart;
	private SpaceTimeDataController stdc;

	public DebugView(CTabFolder parent, int style) {
		super(parent, style);
	}

	@Override
	public void createContent(ITracePart parentPart, IEclipseContext context, IEventBroker broker,
			Composite parentComposite) {

		this.tracePart = parentPart;
		
		Composite container = new Composite(parentComposite, SWT.NONE);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		GridLayoutFactory.fillDefaults().applyTo(container);
		
		Composite topPart = new Composite(container, SWT.BORDER);
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(topPart);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(topPart);
		
		Label lblTop = new Label(topPart, SWT.LEFT);
		lblTop.setText("Execution context:");
		
		listExecutionContexts = new ComboViewer(topPart, SWT.READ_ONLY);
		listExecutionContexts.getCombo().setSize(200, 20);

		listExecutionContexts.setContentProvider(ArrayContentProvider.getInstance());
		listExecutionContexts.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				IdTuple item = (IdTuple) element;
				IdTupleType idType = stdc != null? stdc.getExperiment().getIdTupleType() : IdTupleType.createTypeWithOldFormat();
				
				return item.toString(idType);
			}
		});
		
		tableViewer = new TableViewer(container, SWT.VIRTUAL | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		var table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		GridLayoutFactory.fillDefaults().applyTo(table);
		
		createColumn(tableViewer);
		
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());			
	}
	
	
	private void createColumn(TableViewer viewer) {

		TableViewerColumn colViewerTime = new TableViewerColumn(viewer, SWT.RIGHT);
		colViewerTime.setLabelProvider(new ColumnLabelProvider() {
			@Override
		    public String getText(Object element) {
				DebugRowItem item = (DebugRowItem) element;
				return String.valueOf(item.timeStamp);
			}
			
			@Override
			public Font getFont(Object element) {
				return JFaceResources.getTextFont();
			}
		});
		var colTime = colViewerTime.getColumn();
		colTime.setWidth(200);
		colTime.setText("Time");
		
		TableViewerColumn colViewerCCT = new TableViewerColumn(viewer, SWT.RIGHT);
		colViewerCCT.setLabelProvider(new ColumnLabelProvider() {
			@Override
		    public String getText(Object element) {
				DebugRowItem item = (DebugRowItem) element;
				return String.valueOf(item.cctId);
			}
			
			@Override
			public Font getFont(Object element) {
				return JFaceResources.getTextFont();
			}
		});
		var colCCT = colViewerCCT.getColumn();
		colCCT.setWidth(100);
		colCCT.setText("Node id");		
	}
	

	@Override
	public void setInput(Object input) {
		if (!(input instanceof SpaceTimeDataController))
			return;
		
		stdc = (SpaceTimeDataController) input;

		// just initialize once
		tracePart.getOperationHistory().addOperationHistoryListener(this);
		addDisposeListener(this);
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		tracePart.getOperationHistory().removeOperationHistoryListener(this);
		removeDisposeListener(this);
	}

	
	@Override
	public void historyNotification(OperationHistoryEvent event) {

		if (event.getEventType() == OperationHistoryEvent.DONE) {
			var ptlService = stdc.getProcessTimelineService();
			
			final List<IdTuple> rows = new ArrayList<>();
			
			for(int i=0; i<ptlService.getNumProcessTimeline(); i++) {
				var ptl = ptlService.getProcessTimeline(i);
				if (ptl == null)
					continue;

				rows.add(ptl.getProfileIdTuple());
			}
			if (!listExecutionContexts.getCombo().isDisposed()) {
				listExecutionContexts.setInput(rows);
				
				listExecutionContexts.addSelectionChangedListener(new ComboSelectionChangedListener(tableViewer, listExecutionContexts, ptlService));
				listExecutionContexts.setSelection( new StructuredSelection(rows.get(0)) );
			}
		}
	}
	
	
	static class ComboSelectionChangedListener implements ISelectionChangedListener
	{
		private final ComboViewer listExecutionContexts;
		private final ProcessTimelineService ptlService;
		private final TableViewer tableViewer;
		
		ComboSelectionChangedListener(TableViewer tableViewer, ComboViewer listExecutionContexts, ProcessTimelineService ptlService) {
			this.tableViewer = tableViewer;
			this.listExecutionContexts = listExecutionContexts;
			this.ptlService = ptlService;
		}
		
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			boolean enable = (selection != null) && (!selection.isEmpty());
			if (enable) {
				var selectIndex = listExecutionContexts.getCombo().getSelectionIndex();
				var ptl = ptlService.getProcessTimeline(selectIndex);
				if (ptl != null) {
					final List<DebugRowItem> rowsTable = new ArrayList<>(ptl.size());
					for(int j=0; j<ptl.size(); j++) {
						DebugRowItem item = new DebugRowItem(ptl.getTime(j), ptl.getContextId(j));
						rowsTable.add(item);
					}
					if (!tableViewer.getTable().isDisposed()) {
						tableViewer.setInput(rowsTable);
					}
				}
			}
		}
	}
	

	/***
	 * 
	 * Record for the table's row.
	 * This class should be replaced with Java 17 Record once
	 * we move to Java 17.
	 * 
	 */
	static class DebugRowItem
	{
		public final long timeStamp;
		public final int cctId;
		
		public DebugRowItem(long timeStamp, int cctId) {
			this.timeStamp = timeStamp;
			this.cctId = cctId;
		}
		
		public String toString() {
			return String.format("%d , %d", timeStamp, cctId);
		}
	}
}
