// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.debug;

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

import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.AbstractBaseItem;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.operation.ZoomOperation;


/****************************
 * 
 * View for debugging purpose
 *
 ****************************/
public class DebugView extends AbstractBaseItem implements IOperationHistoryListener, DisposeListener, IProcessTimelineSource
{
	private TableViewer tableViewer;
	private ComboViewer listExecutionContexts;
	
	private ITracePart tracePart;
	private SpaceTimeDataController stdc;
	private ComboSelectionChangedListener comboSelectionListener;
	

	public DebugView(CTabFolder parent, int style) {
		super(parent, style);
	}

	@Override
	public void createContent(ITracePart parentPart, IEclipseContext context, IEventBroker broker,
			Composite parentComposite) {

		this.tracePart = parentPart;
		comboSelectionListener = null;
		
		Composite container = new Composite(parentComposite, SWT.NONE);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		GridLayoutFactory.fillDefaults().applyTo(container);
		
		// create top header container
		Composite topPart = new Composite(container, SWT.BORDER);
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(topPart);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(topPart);
		
		createLabel(topPart);
		
		listExecutionContexts = createComboViewer(topPart);
		
		tableViewer = createTable(container);		
		
		// we allow users to close this view if it's annoying
		setShowClose(true);
	}

	
	private Label createLabel(Composite topPart) {
		Label lblTop = new Label(topPart, SWT.LEFT);
		lblTop.setText("Execution context:");
		
		return lblTop;
	}
	

	private ComboViewer createComboViewer(Composite topPart) {
		var comboViewer = new ComboViewer(topPart, SWT.READ_ONLY);
		comboViewer.getCombo().setSize(200, 20);

		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				IdTuple item = (IdTuple) element;
				IdTupleType idType = stdc != null? stdc.getExperiment().getIdTupleType() : IdTupleType.createTypeWithOldFormat();
				
				return item.toString(idType);
			}
		});
		
		return comboViewer;
	}
	
	
	private TableViewer createTable(Composite container) {	
		var viewer = new TableViewer(container, SWT.VIRTUAL | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		var table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		GridLayoutFactory.fillDefaults().applyTo(table);
		
		createColumn(viewer);
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());		

		return viewer;
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
		
		// make sure we add the listener once
		if (comboSelectionListener == null)
			comboSelectionListener = new ComboSelectionChangedListener(tableViewer, this);
		
		listExecutionContexts.addSelectionChangedListener(comboSelectionListener);

		// just initialize the operation listener once
		tracePart.getOperationHistory().addOperationHistoryListener(this);
		addDisposeListener(this);

		refresh();
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (!listExecutionContexts.getCombo().isDisposed())
			listExecutionContexts.removeSelectionChangedListener(comboSelectionListener);
		
		tracePart.getOperationHistory().removeOperationHistoryListener(this);
		
		removeDisposeListener(this);
	}

	
	@Override
	public void historyNotification(OperationHistoryEvent event) {
		if (event.getEventType() == OperationHistoryEvent.DONE) {
			var operation = event.getOperation();
			if (operation instanceof ZoomOperation)
				refresh();
		}
	}

	
	/***
	 * Re-populate the combo and the table
	 */
	private void refresh() {
		var numTraces = stdc.getNumTracelines();
		if (numTraces == 0)
			return;
		
		final List<IdTuple> rows = new ArrayList<>();
		
		for(int i=0; i<numTraces; i++) {
			var ptl = stdc.getTraceline(i);
			if (ptl != null)
				rows.add(ptl.getProfileIdTuple());
		}
		
		if (!listExecutionContexts.getCombo().isDisposed()) {
			listExecutionContexts.setInput(rows);
			listExecutionContexts.setSelection( new StructuredSelection(rows.get(0)) );
		}
	}
	
	
	static class ComboSelectionChangedListener implements ISelectionChangedListener
	{
		private final IProcessTimelineSource timelineSource;
		private final TableViewer tableViewer;
		
		ComboSelectionChangedListener(TableViewer tableViewer, IProcessTimelineSource timelineSource) {
			this.tableViewer = tableViewer;
			this.timelineSource = timelineSource;
		}
		
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			boolean enable = (selection != null) && (!selection.isEmpty());
			if (enable) {
				var ptl = timelineSource.getCurrentProcessTimeline();
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


	@Override
	public IProcessTimeline getCurrentProcessTimeline() {
		if (stdc == null || stdc.getNumTracelines() == 0)
			return null;
		
		if (listExecutionContexts == null || listExecutionContexts.getCombo().isDisposed())
			return null;
		
		int index = listExecutionContexts.getCombo().getSelectionIndex();
		return stdc.getTraceline(index);		
	}
}
