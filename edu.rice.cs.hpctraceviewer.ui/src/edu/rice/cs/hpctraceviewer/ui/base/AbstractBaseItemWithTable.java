package edu.rice.cs.hpctraceviewer.ui.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.export.command.ExportCommandHandler;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.theme.DarkNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.tooltip.NatTableContentTooltip;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import edu.rice.cs.hpcdata.util.string.StringUtil;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.internal.TraceEventData;


/**************************************************************************
 * 
 * Base class to display a statistic table in a view.
 * <p>This class will show a table with 3 columns:
 * <ul>
 * <li>A color column. Depending of the name of the procedure
 * <li>The procedure name
 * <li>A statistic number
 * </ul>
 * </p>
 *
 **************************************************************************/
public abstract class AbstractBaseItemWithTable extends AbstractBaseItem 
implements EventHandler, DisposeListener, IPropertyChangeListener 
{
	private final static String []TITLE = {" ", "Procedure", "%"};

	private Composite tableComposite;
	private SpaceTimeDataController input;
	private NatTable natTable;
	private EventList<StatisticItem> eventList;
	
	private IRowDataProvider<StatisticItem> rowDataProvider;
	private IColumnPropertyAccessor<StatisticItem> columnAccessor;
	private DataLayer dataLayer;
	private SortHeaderLayer<StatisticItem> sortHeaderLayer;
	private TableConfiguration tableConfiguration;
	
	private IEventBroker broker;

	public AbstractBaseItemWithTable(CTabFolder parent, int style) {
		super(parent, style);
	}

	@Override
	public void createContent(ITracePart parentPart, IEclipseContext context, IEventBroker broker,
			Composite parentComposite) {

		this.broker = broker;
		tableComposite = new Composite(parentComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);
		GridLayoutFactory.fillDefaults().applyTo(tableComposite);
	}

	@Override
	public void setInput(Object input) {
		this.input = (SpaceTimeDataController) input;
		Display.getDefault().asyncExec(()-> createTable());
	}

	
	private void createTable() {
		// only create the table once
		if (rowDataProvider != null)
			return;
		
		final List<StatisticItem> list = FastList.newList();
		eventList = GlazedLists.eventList(list);
		
		// body layer
		rowDataProvider = getRowDataProvider(eventList);
		dataLayer = new DataLayer(rowDataProvider);
		GlazedListsEventLayer<StatisticItem> listEventLayer = new GlazedListsEventLayer<StatisticItem>(dataLayer, eventList);
		DefaultBodyLayerStack bodyLayerStack = new DefaultBodyLayerStack(listEventLayer);
		bodyLayerStack.setConfigLabelAccumulator(new TableLabelAccumulator());
		
		pack();
		
		// column layer
		IColumnPropertyAccessor<StatisticItem> columnAccessor = getColumnAccessor();
		IDataProvider columnHeaderDataProvider = getColumnDataProvider(columnAccessor);
		DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());
		
        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();
		
		ISortModel sortModel = new TableSortModel(eventList);
		sortHeaderLayer = new SortHeaderLayer<StatisticItem>(columnHeaderLayer, sortModel);
		
		// composite layer
        CompositeLayer compositeLayer = new CompositeLayer(1, 2);
        compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, sortHeaderLayer, 0, 0);
        compositeLayer.setChildLayer(GridRegion.BODY, bodyLayerStack, 0, 1);
        
        // turn the auto configuration off as we want to add our header menu
        // configuration
        natTable = new NatTable(tableComposite, NatTable.DEFAULT_STYLE_OPTIONS, compositeLayer, false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
                
        new BaseTooltip(natTable);
        
        // as the auto configuration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration and the ConfigRegistry
        // manually
        natTable.setConfigRegistry(configRegistry);
        
        // special event handler to export the content of the table
        natTable.registerCommandHandler(new ExportCommandHandler(natTable));
        
		tableConfiguration = new TableConfiguration(rowDataProvider);
		natTable.addConfiguration(tableConfiguration);
		natTable.addConfiguration(new SingleClickSortConfiguration());

        natTable.configure();

        // add theme configuration. automatically detect if we are in dark mode or not
        // this configuration happens only at the start of the table. It doesn't change automatically
        // in the middle of the system switch mode
        
        ThemeConfiguration defaultConfiguration = Display.isSystemDarkTheme() ? 
        							new DarkNatTableThemeConfiguration() :  new  ModernNatTableThemeConfiguration();
        natTable.setTheme(defaultConfiguration);
        
        // add listeners
		broker.subscribe(getTopicEvent(), this);
		ViewerPreferenceManager.INSTANCE.getPreferenceStore().addPropertyChangeListener(this);
		addDisposeListener(this);
	}
	
	
	/*****
	 * Ensure the table columns are well compact 
	 */
	public void pack() {
		final String TEXT = "{XXX.XX%|";
		dataLayer.setColumnWidthByPosition(0, 20);
		
		GC gc = new GC(getDisplay());
		gc.setFont(FontManager.getMetricFont());
		Point sizeNumber = gc.textExtent(TEXT);
		dataLayer.setColumnWidthByPosition(2, sizeNumber.x);
		
		dataLayer.setColumnWidthPercentageByPosition(1, 97);
		dataLayer.setColumnPercentageSizing(1, true);
		
		// row's height
		gc.setFont(FontManager.getFontGeneric());
		Point sizeGeneric = gc.textExtent(TEXT);
		int height = 4 + Math.max(sizeGeneric.y, sizeNumber.y);
		dataLayer.setDefaultRowHeight(height);
		
		gc.dispose();
	}
	
	
	@Override
	public void widgetDisposed(DisposeEvent e) {
		ViewerPreferenceManager.INSTANCE.getPreferenceStore().removePropertyChangeListener(this);;
	}
	

	@Override
	public void handleEvent(Event event) {
		if (event.getTopic().equals(getTopicEvent())) {
			Object obj = event.getProperty(IEventBroker.DATA);
			
			if (obj == null)
				return;
			
			assert(obj instanceof TraceEventData);
			
			TraceEventData eventData = (TraceEventData) obj;			
			if (eventData.data != input)
				return;
			
			List<StatisticItem> list = getListItems(eventData.value);
			TableSortModel sortModel = (TableSortModel) sortHeaderLayer.getSortModel();
			sortModel.resetList(list);
			
			natTable.refresh(true);
		}
	}
	
	
	/**
	 * TODO: hack version to get the current number of items.
	 * Used by the parent to check if the table has been initialized or not.
	 * 
	 * @return number of items
	 */
	public int getItemCount() {
		return eventList.size();
	}

	
	/*****
	 * Get the column header data provider.
	 * I don't see the need a child class want to override this, unless they are not happy
	 * with the default class.
	 * If possible, the child class should override {@link getColumnAccessor} instead. 
	 * @param accessor
	 * @return {@link IDataProvider}
	 */
	protected IDataProvider getColumnDataProvider(final IColumnPropertyAccessor<StatisticItem> accessor) {
				
		IDataProvider dataProvider = new IDataProvider() {
			
			@Override
			public void setDataValue(int columnIndex, int rowIndex, Object newValue) {}
			
			@Override
			public int getRowCount() {
				return 1;
			}
			
			@Override
			public Object getDataValue(int columnIndex, int rowIndex) {
				return accessor.getColumnProperty(columnIndex);
			}
			
			@Override
			public int getColumnCount() {
				return accessor.getColumnCount();
			}
		};
		return dataProvider;
	}
	
	
	/*************
	 * Get the table column property.
	 * child class can override the method if necessary.
	 * 
	 * @return {@link IColumnPropertyAccessor}
	 */
	protected IColumnPropertyAccessor<StatisticItem> getColumnAccessor() {
		if (columnAccessor != null)
			return columnAccessor;
		
		IColumnPropertyAccessor<StatisticItem> columnAccessor = new IColumnPropertyAccessor<StatisticItem>() {
			@Override
			public Object getDataValue(StatisticItem rowObject, int columnIndex) {
				return TITLE[columnIndex];
			}

			@Override
			public void setDataValue(StatisticItem rowObject, int columnIndex, Object newValue) {}

			@Override
			public int getColumnCount() {
				return TITLE.length;
			}

			@Override
			public String getColumnProperty(int columnIndex) {
				return TITLE[columnIndex];
			}

			@Override
			public int getColumnIndex(String propertyName) {
				if (TITLE[2].equals(propertyName))
					return 2;
				else if (TITLE[1].equals(propertyName))
					return 1;
				return 0;
			}			
		};		
		return columnAccessor;
	}
	
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {

		final String property = event.getProperty();
		
		boolean need_to_refresh = (property.equals(PreferenceConstants.ID_FONT_GENERIC) || 
								   property.equals(PreferenceConstants.ID_FONT_METRIC)); 
		
		if (need_to_refresh) {
			tableConfiguration.configureRegistry(natTable.getConfigRegistry());
			natTable.refresh();
			pack();
		}
	}


	/****
	 * Default method to get the row data provider.
	 * The child class can override this method to supply a customized
	 * row data provider.
	 * 
	 * @param eventList
	 * @return
	 */
	protected IRowDataProvider<StatisticItem> getRowDataProvider(EventList<StatisticItem> eventList) {
		return new RowDataProvider(eventList);
	}

	/******************************************************************
	 * 
	 * Sorting model
	 *
	 ******************************************************************/
	private static class TableSortModel implements ISortModel
	{
		private List<StatisticItem> list;
		private int sortedColumn = 2;
		private SortDirectionEnum sortedDirection = SortDirectionEnum.DESC;
		
		
		public TableSortModel(List<StatisticItem> list) {
			this.list = list;
		}
		
		
		public void resetList(Collection<StatisticItem> collections) {
			this.list.clear();
			this.list.addAll(collections);
			sort(sortedColumn, sortedDirection, false);
		}
		
		@Override
		public List<Integer> getSortedColumnIndexes() {
			List<Integer> indexes = new ArrayList<Integer>(1);
			indexes.add(sortedColumn);
			return indexes;
		}

		@Override
		public boolean isColumnIndexSorted(int columnIndex) {
			return columnIndex == sortedColumn;
		}

		@Override
		public SortDirectionEnum getSortDirection(int columnIndex) {
			return sortedDirection;
		}

		@Override
		public int getSortOrder(int columnIndex) {
			if (columnIndex == sortedColumn)
				return 0;
			return 1;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public List<Comparator> getComparatorsForColumnIndex(int columnIndex) {
			List<Comparator> list = new ArrayList<Comparator>(1);
			list.add(getColumnComparator(columnIndex));
			
			return list;
		}

		@Override
		public Comparator<?> getColumnComparator(int columnIndex) {
			return new StatisticComparator(columnIndex, sortedDirection);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate) {
			this.sortedColumn    = columnIndex;
			this.sortedDirection = sortDirection;
			
			Comparator<?> comparator = getColumnComparator(columnIndex);
			list.sort((Comparator<? super StatisticItem>) comparator);			
		}

		@Override
		public void clear() {}
	}
	
	
	/******************************************************************
	 * 
	 * Comparator class for {@link StatisticItem} object for a specific
	 * sort column and sort direction.<br/>
	 * Every time the sort column or sort direction changes, the caller
	 * needs to instantiate this class.
	 *
	 ******************************************************************/
	private static class StatisticComparator implements Comparator<StatisticItem>
	{
		private final int sortColumn;
		private final SortDirectionEnum sortDirection;
		
		public StatisticComparator(int sortColumn, SortDirectionEnum sortDirection) {
			this.sortColumn = sortColumn;
			this.sortDirection = sortDirection;
		}

		@Override
		public int compare(StatisticItem o1, StatisticItem o2) {
			int factor = sortDirection == SortDirectionEnum.ASC ? -1: 1;
			switch(sortColumn) {
			case 0:
				Color c1 = o1.procedure.color;
				Color c2 = o2.procedure.color;
				return c1.getRGB().hashCode() - c2.getRGB().hashCode();
			case 1:
				String proc1 = o1.procedure.getProcedure();
				String proc2 = o2.procedure.getProcedure();
				return factor * proc1.compareTo(proc2);
				
			case 2:
				float percent1 = o1.percent;
				float percent2 = o2.percent;
				int result = 0;
				if (percent1 < percent2)
					result = 1;
				else if (percent1 > percent2)
					result = -1;
				return factor * result;
			}
			return 0;
		}

	}
	
	/******************************************************************
	 * 
	 * Table labeling
	 *
	 ******************************************************************/
	private static class TableLabelAccumulator implements IConfigLabelProvider 
	{
		public final static String LABEL_COLOR     = "color";
		public final static String LABEL_PROCEDURE = "procedure";
		public final static String LABEL_NUMBER    = "number";
		
		private final List<String> labels = Arrays.asList(LABEL_COLOR, LABEL_PROCEDURE, LABEL_NUMBER);
		
		@Override
		public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
			configLabels.add(labels.get(columnPosition));
		}

		@Override
		public Collection<String> getProvidedLabels() {
			return labels;
		}		
	}
	
	
	/******************************************************************
	 * 
	 * Basic configuration for the table
	 *
	 ******************************************************************/
	private static class TableConfiguration implements IConfiguration 
	{
		private final IDataProvider dataProvider;
		
		public TableConfiguration(IDataProvider dataProvider) {
			this.dataProvider = dataProvider;
		}

		@Override
		public void configureLayer(ILayer layer) {}

		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			// color column
			ColorCellPainter painter = new ColorCellPainter(dataProvider);					
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
												   painter, 
												   DisplayMode.NORMAL, 
												   TableLabelAccumulator.LABEL_COLOR);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
					   							   painter, 
												   DisplayMode.SELECT, 
												   TableLabelAccumulator.LABEL_COLOR);
			
			// metric column
			Style styleMetric = new Style();
			Font  fontMetric  = FontManager.getMetricFont();
			styleMetric.setAttributeValue(CellStyleAttributes.FONT, fontMetric);
			styleMetric.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
												   styleMetric, 
												   DisplayMode.NORMAL, 
												   TableLabelAccumulator.LABEL_NUMBER);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
					   							   styleMetric, 
					   							   DisplayMode.SELECT, 
					   							   TableLabelAccumulator.LABEL_NUMBER);
			
			// procedure column
			Style styleProcedure = new Style();
			Font  fontProcedure  = FontManager.getFontGeneric();
			styleProcedure.setAttributeValue(CellStyleAttributes.FONT, fontProcedure);
			styleProcedure.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
												   styleProcedure, 
												   DisplayMode.NORMAL, 
												   TableLabelAccumulator.LABEL_PROCEDURE);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
												   styleProcedure, 
					   							   DisplayMode.SELECT, 
					   							   TableLabelAccumulator.LABEL_PROCEDURE);
		}

		@Override
		public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {}		
	}
	
	
	/******************************************************************
	 * 
	 * Cell Painter for color column
	 *
	 ******************************************************************/
	private static class ColorCellPainter extends TextPainter
	{
		private final IDataProvider dataProvider;
		
		public ColorCellPainter(IDataProvider dataProvider) {
			super();
			this.dataProvider = dataProvider;
		}
		
		@Override
	    public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {

	        if (this.paintBg || this.paintFg) {
	    		int rowIndex = cell.getRowIndex();
	    		Color color = (Color) dataProvider.getDataValue(0, rowIndex);
	    		gc.setBackground(color);
	    		gc.setForeground(color);
	    		gc.fillRectangle(rectangle);
	    		return;
	        }
        	super.paintCell(cell, gc, rectangle, configRegistry);
		}
	}
	
	
	/******************************************************************
	 * 
	 * Tooltip for the table. It's the same as the default NatTable tooltip
	 * but wrapped.
	 *
	 ******************************************************************/
	private static class BaseTooltip extends NatTableContentTooltip
	{
		private final static int MAX_CHARS_WRAP = 80;
		
		public BaseTooltip(NatTable natTable) {
			super(natTable, GridRegion.BODY, GridRegion.COLUMN_HEADER);
		}
		
		@Override
	    protected String getText(org.eclipse.swt.widgets.Event event) {
			
			String text = super.getText(event);
			return StringUtil.wrapScopeName(text, MAX_CHARS_WRAP);
		}
	}
	
	

	/*****************************************************************************
	 * 
	 * Default class for {@code IRowDataProvider} implementation
	 *
	 *****************************************************************************/
	protected static class RowDataProvider implements IRowDataProvider<StatisticItem>
	{
		private static final String FORMAT_PERCENT = "%.1f%%";
		private List<StatisticItem> list;
		
		public RowDataProvider(List<StatisticItem> list) {
			this.list = list;
		}
		
		public void setList(List<StatisticItem> list) {
			this.list = list;
		}

		@Override
		public Object getDataValue(int columnIndex, int rowIndex) {
			StatisticItem item = list.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return item.procedure.color;
			case 1:
				return item.procedure.getProcedure();
			case 2:
				return String.format(FORMAT_PERCENT, item.percent);
			}
			return null;
		}

		
		@Override
		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public StatisticItem getRowObject(int rowIndex) {
			return list.get(rowIndex);
		}

		@Override
		public int indexOfRowObject(StatisticItem rowObject) {
			return list.indexOf(rowObject);
		}
		
	}

	
	/***
	 * Return the name of the event topic to be handled.
	 * Every time the event arrives, the table will be refreshed.
	 * @return name of the event topic
	 */
	abstract protected String getTopicEvent();
	
	/***
	 * Get the list of items to be displayed in the table based on input from summary view.
	 * The list has to be the type of {@code List<StatisticItem>}
	 * @param input
	 * @return
	 */
	abstract protected List<StatisticItem> getListItems(Object input);
}
