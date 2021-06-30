package edu.rice.cs.hpcmetric;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.VisibilityType;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcsetting.fonts.FontManager;

import java.io.Serializable;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/*********************************************************************
 * 
 * Class to display a composite to show metrics and its properties
 * This class allows to change the properties of the metric.
 * <p>
 * This class shows:
 * <ul>
 * <li>show or hide</li>
 * <li>displayed name</li>
 * <li>root value (or empty)</li>
 * <li>the description</li>
 * </ul>
 *
 *********************************************************************/
public abstract class AbstractFilterComposite extends Composite 
{	
	private static final int INDEX_VISIBILITY  = 0;
	private static final int INDEX_NAME        = 1;
	private static final int INDEX_DESCRIPTION = 2;
	private static final int INDEX_VALUE       = 3;
	
	private static final String []COLUMN_LABELS = {"Shown", "Name", "Description", "Aggregate value"};
	
	private static final String LABEL_ROW_GRAY = "row.gray";
	
	private final Composite parentContainer;

	protected Text objSearchText;

	/***
	 * 
	 * @param parent
	 * @param style
	 * @param metricManager
	 * @param root
	 */
	public AbstractFilterComposite(Composite parent, int style, IMetricManager metricManager, RootScope root) {
		super(parent, style);
		
		this.parentContainer = new Composite(parent, SWT.BORDER);

		GridLayout grid = new GridLayout();
		grid.numColumns=1;

		// prepare the buttons: check and uncheck
		GridLayout gridButtons = new GridLayout();
		gridButtons.numColumns=3;
		Composite groupButtons = new Composite(parentContainer, SWT.BORDER);
		groupButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupButtons.setLayout(gridButtons);

		// check button
		Button btnCheckAll = new Button(groupButtons, SWT.NONE);
		btnCheckAll.setText("Check all"); 
		btnCheckAll.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		// uncheck button
		Button btnUnCheckAll = new Button(groupButtons, SWT.NONE);
		btnUnCheckAll.setText("Uncheck all");
		btnUnCheckAll.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		// regular expression option
		final Button btnRegExpression = new Button(groupButtons, SWT.CHECK);
		btnRegExpression.setText("Regular expression");
		btnRegExpression.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnRegExpression.setSelection(false);

		// set the layout for group filter
		Composite groupFilter = new Composite(parentContainer, SWT.BORDER);
		groupFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(groupFilter);
		
		// string to match
		Label lblFilter = new Label (groupFilter, SWT.FLAT);
		lblFilter.setText("Filter:");
		
		objSearchText = new Text (groupFilter, SWT.BORDER);
		// expand the filter field as much as possible horizontally
		GridDataFactory.fillDefaults().grab(true, false).applyTo(objSearchText);

		NatTable nattable = setLayer(metricManager, root);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(nattable);
		
		parentContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		parentContainer.setLayout(grid);		
	}

	
	/****
	 * Set the layers of the table
	 * 
	 * @param metricManager the experiment or metric manager
	 * @param root the root scope
	 * 
	 * @return an instance of nat table
	 */
	protected NatTable setLayer(IMetricManager metricManager, RootScope root) {

		//IConfigRegistry configRegistry = new ConfigRegistry();
		List<BaseMetric> list = metricManager.getMetricList();
		EventList<BaseMetric> eventList   = GlazedLists.eventList(list);
		SortedList<BaseMetric> sortedList = new SortedList<BaseMetric>(eventList);
		FilterList<BaseMetric> filterList = new FilterList<BaseMetric>(sortedList);

		IRowDataProvider<BaseMetric> dataProvider = new FilterDataProvider(filterList, root);

		// data layer
		DataLayer dataLayer = new DataLayer(dataProvider);
		GlazedListsEventLayer<BaseMetric> listEventLayer = new GlazedListsEventLayer<BaseMetric>(dataLayer, eventList);
		DefaultBodyLayerStack defaultLayerStack = new DefaultBodyLayerStack(listEventLayer);
		
		dataLayer.setColumnWidthPercentageByPosition(INDEX_NAME, 30);
		dataLayer.setColumnWidthPercentageByPosition(INDEX_DESCRIPTION, 50);
		dataLayer.setColumnWidthByPosition(INDEX_VISIBILITY, 20);
		dataLayer.setColumnWidthByPosition(INDEX_VALUE, MetricConfiguration.getWidth().x);

		// columns header
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(COLUMN_LABELS);
		DataLayer columnDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		ColumnHeaderLayer colummnLayer = new ColumnHeaderLayer(columnDataLayer, defaultLayerStack, defaultLayerStack.getSelectionLayer());
		
		// row header
		DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(dataProvider);
		DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, defaultLayerStack, defaultLayerStack.getSelectionLayer());

		// corner layer
		DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		CornerLayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, colummnLayer);
		
		// grid layer
		GridLayer gridLayer = new GridLayer(defaultLayerStack, colummnLayer, rowHeaderLayer, cornerLayer);
		
		// this override is essential to re-label the column to be matched in the configuration
		//ColumnLabelAccumulator columnLabelAccumulator = new ColumnLabelAccumulator(dataProvider);
		//defaultLayerStack.setConfigLabelAccumulator(columnLabelAccumulator);
		defaultLayerStack.setConfigLabelAccumulator(new MetricConfigLabelAccumulator(defaultLayerStack, dataProvider));

		final SelectionLayer selectionLayer = defaultLayerStack.getSelectionLayer();
		selectionLayer.setSelectionModel(new RowSelectionModel<>(selectionLayer, dataProvider, new IRowIdAccessor<BaseMetric>() {

			@Override
			public Serializable getRowId(BaseMetric rowObject) {
				return list.indexOf(rowObject);
			}
		}));;
		
		// the table
		NatTable natTable = new NatTable(parentContainer, gridLayer, false); 

		// additional configuration
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
		natTable.addConfiguration(new CheckBoxConfiguration());
		natTable.addConfiguration(new PainterConfiguration());
		natTable.addConfiguration(new MetricConfiguration());
		
		//natTable.setConfigRegistry(configRegistry); 
		natTable.configure();
		
		return natTable;
	}
	
	
	/****************************************************************************
	 * 
	 * Configuration to paint or render a cell.
	 * It wraps texts in description column
	 *
	 *******************************************************************************/
	private static class PainterConfiguration extends AbstractRegistryConfiguration
	{

		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			TextPainter tp = new TextPainter(true, true, true);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
												   tp, 
												   DisplayMode.NORMAL, 
												   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + INDEX_DESCRIPTION);	
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, 
												   new DefaultIntegerDisplayConverter(), 
												   DisplayMode.NORMAL, 
												   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + INDEX_DESCRIPTION);	
			
			Style styleGray = new Style();
			styleGray.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_DARK_GRAY);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
												   styleGray, 
												   DisplayMode.NORMAL, 
												   LABEL_ROW_GRAY);
		}
	}
	
	
	/***************************
	 * 
	 * Label configuration
	 *
	 ***************************/
	private static class MetricConfigLabelAccumulator extends ColumnLabelAccumulator
	{
		private final ILayer bodyLayer;
		private final IRowDataProvider<BaseMetric> dataProvider;
		/***
		 * Constructor for metric label configuration
		 * @param bodyLayer the body layer, used to convert row position to row index
		 * @param dataProvider the data provider
		 * @param listMetrics the list 
		 */
		public MetricConfigLabelAccumulator(ILayer bodyLayer, IRowDataProvider<BaseMetric> dataProvider) {
			super(dataProvider);
			this.bodyLayer = bodyLayer;
			this.dataProvider = dataProvider;
		}
		
		@Override
		public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
			
			int rowIndex = bodyLayer.getRowIndexByPosition(rowPosition);
			BaseMetric metric = dataProvider.getRowObject(rowIndex);
			if (metric.isInvisible()) {
				configLabels.addLabel(LABEL_ROW_GRAY);
			}
			super.accumulateConfigLabels(configLabels, columnPosition, rowPosition);
		} 
	}
	
	/*******************************************************************************
	 * 
	 * Configuration for metric column
	 *
	 *******************************************************************************/
	private static class MetricConfiguration extends AbstractRegistryConfiguration 
	{
		
		public static Font getMetricFont() {
			Font font ;
			try {
				font = FontManager.getMetricFont();
			} catch (Exception e) {
				font = JFaceResources.getTextFont();
			}
			return font;
		}
		
		public static Point getWidth() {
			Display display = Display.getCurrent();
			GC gc = new GC(display);
			gc.setFont(getMetricFont());
			return gc.textExtent("-X.XX+eXX 99.9%-");
		}

		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			Font font = getMetricFont();

			Style style = new Style();
			style.setAttributeValue(CellStyleAttributes.FONT, font);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
												   style, 
												   DisplayMode.NORMAL, 
												   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + INDEX_VALUE);
		}
	}
	
	/**************************************************
	 * 
	 * Specific configuration for check box column
	 *
	 *************************************************/
	private static class CheckBoxConfiguration extends AbstractRegistryConfiguration
	{
		
		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, 
												   IEditableRule.ALWAYS_EDITABLE,
												   DisplayMode.NORMAL,
												   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + INDEX_VISIBILITY);

			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
												   new CheckBoxPainter(), 
												   DisplayMode.NORMAL, 
												   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + INDEX_VISIBILITY);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, 
												   new DefaultBooleanDisplayConverter(), 
												   DisplayMode.NORMAL, 
												   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + INDEX_VISIBILITY);
			
			configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, 
												   new CheckBoxCellEditor(), 
												   DisplayMode.EDIT,
												   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + INDEX_VISIBILITY);
		}
	}
	

	/*******************************
	 * 
	 *Basic metric data provider 
	 *
	 *******************************/
	public static class FilterDataProvider implements IRowDataProvider<BaseMetric> 
	{
		private static final String METRIC_DERIVED = "Derived metric"; //$NON-NLS-N$
		private static final String METRIC_EMPTY   = "empty";
		private final List<BaseMetric> list;
		private final RootScope root;
		
		public FilterDataProvider(List<BaseMetric> list, RootScope root) {
			this.list = list;
			this.root = root;
		}

		@Override
		public Object getDataValue(int columnIndex, int rowIndex) {
			BaseMetric metric = list.get(rowIndex);
			switch (columnIndex) {
			case INDEX_VISIBILITY: 	
				return metric.getDisplayed(); 
			case INDEX_NAME: 		
				return metric.getDisplayName();
			case INDEX_VALUE:
				MetricValue mv = root.getMetricValue(metric);
				if (mv == MetricValue.NONE)
					return METRIC_EMPTY;
				return metric.getMetricTextValue(mv);
				
			case INDEX_DESCRIPTION: 
				if (metric instanceof DerivedMetric) {
					return METRIC_DERIVED;
				}
				return metric.getDescription();
			}
			assert (false);
			return null;
		}

		@Override
		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
			BaseMetric data = list.get(rowIndex);
			if (data.isInvisible())
				return;

			switch(columnIndex) {
			case INDEX_VISIBILITY:
				VisibilityType visible = (Boolean)newValue ? VisibilityType.SHOW : VisibilityType.HIDE;
				data.setDisplayed(visible);
				break;
			case INDEX_NAME:
				data.setDisplayName((String) newValue);
				break;
			case INDEX_DESCRIPTION:
				data.setDescription((String) newValue);
				break;
			default:
				assert(false);
			}
		}

		@Override
		public int getColumnCount() {
			return COLUMN_LABELS.length;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public BaseMetric getRowObject(int rowIndex) {
			return list.get(rowIndex);
		}

		@Override
		public int indexOfRowObject(BaseMetric rowObject) {
			return list.indexOf(rowObject);
		}		
	}
	
	abstract protected void createAdditionalButton(Composite parent); 
}
