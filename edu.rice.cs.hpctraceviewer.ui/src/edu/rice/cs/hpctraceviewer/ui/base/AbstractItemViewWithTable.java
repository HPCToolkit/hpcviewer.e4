package edu.rice.cs.hpctraceviewer.ui.base;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.rice.cs.hpc.data.util.string.StringUtil;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;
import edu.rice.cs.hpctraceviewer.data.ColorTable;
import edu.rice.cs.hpctraceviewer.ui.preferences.TracePreferenceManager;



public abstract class AbstractItemViewWithTable extends AbstractBaseItem
		implements EventHandler, Listener, IPropertyChangeListener, DisposeListener 
{
	
	private TableViewer tableViewer;
	private ColumnProcedureLabelProvider lblProcProvider;
	private TableStatComparator comparator;
	
	private IEventBroker broker;
	
	public AbstractItemViewWithTable(CTabFolder parent, int style) {
		super(parent, style);
	}


	@Override
	public void createContent(ITracePart parentPart, IEclipseContext context, IEventBroker broker,
			Composite parent) {
		
		final Composite tableComposite = new Composite(parent, SWT.NONE);
		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);
		
		tableViewer = new TableViewer(tableComposite, SWT.BORDER|SWT.VIRTUAL | SWT.SINGLE | SWT.READ_ONLY);
		
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		// column for procedure name
		final TableViewerColumn colProc  = new TableViewerColumn(tableViewer, SWT.LEFT, 0);
		lblProcProvider = new ColumnProcedureLabelProvider();
		colProc.setLabelProvider(lblProcProvider);
		
		TableColumn column = colProc.getColumn();
		column.setText("Procedure");
		layout.setColumnData(column, new ColumnWeightData(800, 80, true));
		column.addSelectionListener(getSelectionAdapter(column, 0));
		
		// column for the percentage
		final TableViewerColumn colCount = new TableViewerColumn(tableViewer, SWT.LEFT, 1);
		colCount.setLabelProvider(new ColumnStatLabelProvider());
		
		column = colCount.getColumn();
		layout.setColumnData(column, new ColumnWeightData(200, 30, true));
		
		column.setText("%");
		column.setAlignment(SWT.RIGHT);
		column.addSelectionListener(getSelectionAdapter(column, 1));

		// setup the table viewer
		tableViewer.setContentProvider(new StatisticContentProvider());		
		
		comparator = new TableStatComparator();
		tableViewer.setComparator(comparator);
		
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
		
		this.broker = broker;
		addListener(SWT.Show, this);
	}
	
	
	/*****
	 * Create and return a new selection adapter for a given column
	 * 
	 * @param column
	 * @param index
	 * @return
	 */
	private SelectionAdapter getSelectionAdapter(final TableColumn column,
										 		 final int index) {
		SelectionAdapter adapter = new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				tableViewer.getTable().setSortDirection(dir);
				tableViewer.getTable().setSortColumn(column);
				tableViewer.refresh();
			}
		};
		return adapter;
	}
	
	
	@Override
	public void setInput(Object input) {
		broker.subscribe(getTopicEvent(), this);
		ViewerPreferenceManager.INSTANCE.getPreferenceStore().addPropertyChangeListener(this);
		
		addDisposeListener(this);
	}	


	@Override
	public void widgetDisposed(DisposeEvent e) {

		broker.unsubscribe(this);
		ViewerPreferenceManager.INSTANCE.getPreferenceStore().removePropertyChangeListener(this);
		removeDisposeListener(this);
	}


	@Override
	public void handleEvent(Event event) {
		if (event.getTopic().equals(getTopicEvent())) {
			Object obj = event.getProperty(IEventBroker.DATA);
			
			if (obj == null)
				return;
			
			List<StatisticItem> list = getListItems(obj);
			tableViewer.setInput(list);
			lblProcProvider.colorTable = getColorTable();
			
			//tableViewer.getTable().getColumn(1).pack();
		}
	}


	@Override
	public void handleEvent(org.eclipse.swt.widgets.Event event) {}


	@Override
	public void propertyChange(PropertyChangeEvent event) {
		final String property = event.getProperty();
		if (property.equals(PreferenceConstants.ID_FONT_GENERIC) || 
			property.equals(PreferenceConstants.ID_FONT_METRIC)) {
			
			tableViewer.refresh();
		}
	}

	/**
	 * TODO: hack version to get the current number of items.
	 * Used by the parent to check if the table has been initialized or not.
	 * 
	 * @return number of items
	 */
	public int getItemCount() {
		if (tableViewer == null) return 0;
		return tableViewer.getTable().getItemCount();
	}


	/*************************************************************
	 * 
	 * Content provider for the table in statistic view
	 *
	 *************************************************************/
	static private class StatisticContentProvider 
	implements IStructuredContentProvider
	{
		StatisticContentProvider() {}

		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement == null)
				return null;
			
			ArrayList<StatisticItem> list = (ArrayList<StatisticItem>) inputElement;
			int size = list.size();
			StatisticItem []items = new StatisticItem[size];
			
			list.toArray(items);
			
			return items;
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}

	
	/*************************************************************
	 * 
	 * Class to manage label of procedure name
	 *
	 *************************************************************/
	static private class ColumnProcedureLabelProvider extends ColumnLabelProvider
	{		
		ColorTable colorTable;
		
		@Override
		public Image getImage(Object element) {
			if (element != null && element instanceof StatisticItem) {
				final StatisticItem item = (StatisticItem) element;
				if (item.procedureName == ColorTable.UNKNOWN_PROCNAME)
					return null;
				
				if (colorTable != null)
					return colorTable.getImage(((StatisticItem)element).procedureName);
			}
			return null;
		}

		@Override
		public Font getFont(Object element) {
			return FontManager.getFontGeneric();
		}

		
		@Override
		public String getText(Object element) {
			if (element == null || !(element instanceof StatisticItem))
				return null;
			
			StatisticItem item = (StatisticItem) element;
			return item.procedureName;
		}
		
		@Override
		public String getToolTipText(Object element) {
    		final String originalText = getText(element);
    		return StringUtil.wrapScopeName(originalText, 100);
		}
		
		@Override
		public int getToolTipDisplayDelayTime(Object object) {
    		return TracePreferenceManager.getTooltipDelay();
		}
	}

	
	/*************************************************************
	 * 
	 * Class to manage label of statistic numnber
	 *
	 *************************************************************/
	private class ColumnStatLabelProvider extends ColumnLabelProvider
	{

		@Override
		public Font getFont(Object element) {
			return FontManager.getMetricFont();
		}

		@Override
		public String getText(Object element) {
			if (element == null || !(element instanceof StatisticItem))
				return null;
			
			StatisticItem item = (StatisticItem) element;
			return String.format("%.2f %%", item.percent);
		}
	}

	/***
	 * Return the name of the event topic to be handled.
	 * Every time the event arrives, the table will be refreshed.
	 * @return name of the event topic
	 */
	abstract protected String getTopicEvent();
	
	/***
	 * Get the color table mapping
	 * @return
	 */
	abstract protected ColorTable getColorTable();
	
	/***
	 * Get the list of items to be displayed in the table based on input from summary view.
	 * The list has to be the type of {@code List<StatisticItem>}
	 * @param input
	 * @return
	 */
	abstract protected List<StatisticItem> getListItems(Object input);
}