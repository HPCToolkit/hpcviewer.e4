package edu.rice.cs.hpcmetric;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcfilter.dialog.FilterDataItem;
import edu.rice.cs.hpcmetric.internal.IFilterChangeListener;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataItem;
import edu.rice.cs.hpcsetting.fonts.FontManager;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
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
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionBindings;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
public abstract class AbstractFilterPane implements IFilterChangeListener
{	
	private static final int INDEX_VISIBILITY  = 0;
	private static final int INDEX_NAME        = 1;
	private static final int INDEX_DESCRIPTION = 2;
	private static final int INDEX_VALUE       = 3;
	
	private static final String []COLUMN_LABELS = {"Visible", "Name", "Description", "Aggregate value"};
	
	private static final String LABEL_ROW_GRAY = "row.gray";
	
	private final Composite parentContainer;
	private final NatTable  nattable ;
	private TextMatcherEditor<MetricFilterDataItem> textMatcher;
	private FilterDataProvider dataProvider;
	private EventList<MetricFilterDataItem> eventList ;
	private RowSelectionProvider<MetricFilterDataItem> rowSelectionProvider ;
	
	/***
	 * 
	 * @param parent
	 * @param style
	 * @param metricManager
	 * @param root
	 */
	public AbstractFilterPane(Composite parent, int style, MetricFilterInput input) {
		
		this.parentContainer = new Composite(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parentContainer);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parentContainer);

		// prepare the buttons: check and uncheck

		Composite groupButtons = new Composite(parentContainer, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(groupButtons);

		// check button
		Button btnCheckAll = new Button(groupButtons, SWT.NONE);
		btnCheckAll.setText("Check all"); 
		btnCheckAll.setToolTipText("Select all the current listed items");
		btnCheckAll.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnCheckAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dataProvider.checkAll();
				nattable.refresh(false);
			}
		});

		// uncheck button
		Button btnUnCheckAll = new Button(groupButtons, SWT.NONE);
		btnUnCheckAll.setText("Uncheck all");
		btnUnCheckAll.setToolTipText("Remove the selection of the current listed items");
		btnUnCheckAll.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnUnCheckAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dataProvider.uncheckAll();
				nattable.refresh(false);
			}
		});

		// regular expression option
		final Button btnRegExpression = new Button(groupButtons, SWT.CHECK);
		btnRegExpression.setText("Regular expression");
		btnRegExpression.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnRegExpression.setSelection(false);	
		btnRegExpression.setToolTipText("Option to enable that the text to filter is a regular expression");
		
		createAdditionalButton(groupButtons);
		
		GridLayoutFactory.fillDefaults().numColumns(6).applyTo(groupButtons);

		// set the layout for group filter
		Composite groupFilter = new Composite(parentContainer, SWT.NONE);
		groupFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(groupFilter);
		
		// string to match
		Label lblFilter = new Label (groupFilter, SWT.FLAT);
		lblFilter.setText("Filter:");
		
		Text objSearchText = new Text (groupFilter, SWT.BORDER);
		objSearchText.setToolTipText("Type text to filter the list");
		
		// expand as much as possible horizontally
		GridDataFactory.fillDefaults().grab(true, false).applyTo(objSearchText);

		nattable = setLayer(input);
		final Color defaultBgColor = objSearchText.getBackground();
		
		objSearchText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String text = objSearchText.getText();
				if (text == null) {
					textMatcher.setFilterText(new String [] {});
				} else {
					if (btnRegExpression.getSelection()) {
						// check if the regular expression is correct
						try {
							Pattern.compile(text);
						} catch(Exception err) {
							Color c = e.display.getSystemColor(SWT.COLOR_YELLOW);
							objSearchText.setBackground(c);
							return;
						}
						objSearchText.setBackground(defaultBgColor);
					}
					textMatcher.setFilterText(new String [] {text});
				}
				nattable.refresh(false);
			}
		});

		btnRegExpression.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean regExp = btnRegExpression.getSelection();
				if (regExp) {
					try {
						textMatcher.setMode(TextMatcherEditor.REGULAR_EXPRESSION);
					} catch(Exception err) {
						Color c = e.display.getSystemColor(SWT.COLOR_YELLOW);
						objSearchText.setBackground(c);
						return;
					}
					objSearchText.setBackground(defaultBgColor);
				} else {
					textMatcher.setMode(TextMatcherEditor.CONTAINS);
				}
				nattable.refresh(false);
			}
		});
		// expand as much as possible both horizontally and vertically
		GridDataFactory.fillDefaults().grab(true, true).applyTo(nattable);
	}

	
	
	
	/****
	 * Set the layers of the table
	 * 
	 * @param metricManager the experiment or metric manager
	 * @param root the root scope
	 * 
	 * @return an instance of nat table
	 */
	protected NatTable setLayer(MetricFilterInput input) {

		this.eventList = GlazedLists.eventList(input.getFilterList());

		SortedList<MetricFilterDataItem> sortedList = new SortedList<MetricFilterDataItem>(eventList);
		FilterList<MetricFilterDataItem> filterList = new FilterList<MetricFilterDataItem>(sortedList);

		this.dataProvider = new FilterDataProvider(input.getRoot(), filterList, this);

		// data layer
		DataLayer dataLayer = new DataLayer(dataProvider);
		GlazedListsEventLayer<MetricFilterDataItem> listEventLayer = new GlazedListsEventLayer<MetricFilterDataItem>(dataLayer, eventList);
		DefaultBodyLayerStack defaultLayerStack = new DefaultBodyLayerStack(listEventLayer);
		defaultLayerStack.getSelectionLayer().addConfiguration(new RowOnlySelectionConfiguration());
		
		dataLayer.setColumnWidthPercentageByPosition(INDEX_VISIBILITY, 10);
		dataLayer.setColumnWidthPercentageByPosition(INDEX_NAME, 30);
		dataLayer.setColumnWidthPercentageByPosition(INDEX_DESCRIPTION, 40);
		dataLayer.setColumnWidthPercentageByPosition(INDEX_VALUE, 20);
		dataLayer.setColumnsResizableByDefault(true);

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
		
		defaultLayerStack.setConfigLabelAccumulator(new MetricConfigLabelAccumulator(defaultLayerStack, dataProvider, input.getRoot()));
		
		// the table
		NatTable natTable = new NatTable(parentContainer, gridLayer, false); 

		// additional configuration
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new CheckBoxConfiguration());
		natTable.addConfiguration(new PainterConfiguration());
		natTable.addConfiguration(new RowOnlySelectionBindings());
		
		natTable.configure();
 		
		textMatcher = new TextMatcherEditor<>(new TextFilterator<FilterDataItem>() {

			@Override
			public void getFilterStrings(List<String> baseList, FilterDataItem element) {
				baseList.add(element.getLabel());
				if (element.getData() != null) {
					BaseMetric metric = (BaseMetric) element.getData();
					baseList.add(metric.getDescription());
				}
			}
		});
		textMatcher.setMode(TextMatcherEditor.CONTAINS);
		filterList.setMatcherEditor(textMatcher);

		rowSelectionProvider = new RowSelectionProvider<>(defaultLayerStack.getSelectionLayer(), dataProvider);
		rowSelectionProvider.addSelectionChangedListener((event)-> {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            Iterator<MetricFilterDataItem> it = selection.iterator();
            
            if (!it.hasNext())
            	return;
            MetricFilterDataItem item = (MetricFilterDataItem) it.next();
            
            selectionEvent(item, SWT.MouseDown);
		});
		
		natTable.getUiBindingRegistry().registerDoubleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new IMouseAction() {
			
			@Override
			public void run(NatTable natTable, MouseEvent event) {
				int row = natTable.getRowPositionByY(event.y);
				int index = natTable.getRowIndexByPosition(row);
				MetricFilterDataItem item = dataProvider.getRowObject(index);
				selectionEvent(item, SWT.MouseDoubleClick);
			}
		});
		
		return natTable;
	}
	
	
	/****
	 * Retrieve the list of the modified items
	 * @return {@code List<MetricFilterDataItem>}
	 */
	public List<MetricFilterDataItem> getList() {
		return eventList;
	}
	

	/****
	 * Change the current item
	 * @param metric
	 */
	public void update(BaseMetric metric) {
		dataProvider.update(metric);
		nattable.redraw();
	}


	/****
	 * Retrieve the selection provider
	 * @return {@code ISelectionProvider}
	 */
	protected ISelectionProvider getSelectionProvider() {
		return rowSelectionProvider;
	}
	
	/****************************************************************************
	 * 
	 * Configuration to paint or render a cell.
	 * It wraps texts in description column
	 *
	 *******************************************************************************/
	private static class PainterConfiguration extends AbstractRegistryConfiguration
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

		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			
			// wrap long texts for description column
			
			TextPainter tp = new TextPainter(true, true, true);			
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
												   tp, 
												   DisplayMode.NORMAL, 
												   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + INDEX_DESCRIPTION);	
			
			// gray colors for disabled metrics
			
			Style styleGray = new Style();
			styleGray.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_DARK_GRAY);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
												   styleGray, 
												   DisplayMode.NORMAL, 
												   LABEL_ROW_GRAY);
			
			// left justified for label columns
			
			Style styleLeft = new Style();
			styleLeft.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
												   styleLeft, 
												   DisplayMode.NORMAL, 
												   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + INDEX_NAME);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
					   							   styleLeft, 
					   							   DisplayMode.NORMAL, 
					   							   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + INDEX_DESCRIPTION);
			
			// right justify for metric columns
			Font font = getMetricFont();

			Style style = new Style();
			style.setAttributeValue(CellStyleAttributes.FONT, font);
			style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);

			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
												   style, 
												   DisplayMode.NORMAL, 
												   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + INDEX_VALUE);
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
		private final IRowDataProvider<MetricFilterDataItem> dataProvider;
		/***
		 * Constructor for metric label configuration
		 * @param bodyLayer the body layer, used to convert row position to row index
		 * @param dataProvider the data provider
		 * @param listMetrics the list 
		 */
		public MetricConfigLabelAccumulator(ILayer bodyLayer, IRowDataProvider<MetricFilterDataItem> dataProvider, RootScope root) {
			super(dataProvider);
			this.bodyLayer = bodyLayer;
			this.dataProvider = dataProvider;
		}
		
		@Override
		public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
			
			int rowIndex = bodyLayer.getRowIndexByPosition(rowPosition);
			FilterDataItem item = dataProvider.getRowObject(rowIndex);
			if (!item.enabled) {
				configLabels.addLabel(LABEL_ROW_GRAY);
			}
			super.accumulateConfigLabels(configLabels, columnPosition, rowPosition);
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
	public static class FilterDataProvider implements IRowDataProvider<MetricFilterDataItem> 
	{
		private static final String METRIC_DERIVED = "Derived metric"; //$NON-NLS-N$
		private static final String METRIC_EMPTY   = "empty";
		
		private final IFilterChangeListener changeListener;
		private final RootScope root;
		private final List<MetricFilterDataItem> list;
		
		public FilterDataProvider(RootScope root, List<MetricFilterDataItem> list, IFilterChangeListener changeListener) {
			this.root = root;
			this.list = list;
			this.changeListener = changeListener;
		}

		public void checkAll() {
			list.stream().filter(item-> item.data != null && item.enabled)
						 .forEach(item-> {
							 item.setChecked(true);
						 });

			changeListener.changeEvent(list);
		}

		public void uncheckAll() {
			list.stream().filter(item-> item.data != null && item.enabled)
			 		     .forEach(item-> { 
			 		    	 item.setChecked(false);
			 		       });

			changeListener.changeEvent(list);
		}
		

		public void update(BaseMetric metric) {
			Optional<MetricFilterDataItem> mfdi = list.stream()
													.filter( item -> ((BaseMetric)item.data).getIndex() == metric.getIndex() )
													.findFirst();
			if (mfdi.isEmpty())
				return;
			
			MetricFilterDataItem item = mfdi.get();
			item.data = metric;
			item.setLabel(metric.getDisplayName());
		}
		
		
		@Override
		public Object getDataValue(int columnIndex, int rowIndex) {
			FilterDataItem item = list.get(rowIndex);
			Object data = item.getData();
			
			switch (columnIndex) {
			case INDEX_VISIBILITY: 	
				return item.isChecked(); 
			case INDEX_NAME: 		
				return item.getLabel();
			case INDEX_VALUE:
				if (data == null)
					return METRIC_EMPTY;
				BaseMetric metric = (BaseMetric) data;
				return metric.getMetricTextValue(root);
				
			case INDEX_DESCRIPTION: 
				if (data == null)
					return METRIC_EMPTY;

				metric = (BaseMetric) data;
				if (metric instanceof DerivedMetric) {
					String desc = metric.getDescription().isEmpty() ? METRIC_DERIVED :
									metric.getDescription();
					return desc;
				}
				
				return metric.getDescription();
			}
			assert (false);
			return null;
		}

		@Override
		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
			MetricFilterDataItem item = list.get(rowIndex);
			Object data = item.getData();

			if (data == null || !item.enabled)
				return;

			switch(columnIndex) {
			case INDEX_VISIBILITY:
				boolean newCheck = (boolean) newValue;
				if (newCheck != item.checked) {
					item.setChecked((boolean) newValue);
					changeListener.changeEvent(item);
				}
				break;
				
			case INDEX_NAME:
				item.setLabel((String) newValue);;				
				BaseMetric metric = (BaseMetric) data;
				metric.setDisplayName((String) newValue);
				break;
				
			case INDEX_DESCRIPTION:
				metric = (BaseMetric) data;
				metric.setDescription((String) newValue);
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
		public MetricFilterDataItem getRowObject(int rowIndex) {
			return list.get(rowIndex);
		}

		@Override
		public int indexOfRowObject(MetricFilterDataItem rowObject) {
			return list.indexOf(rowObject);
		}		
	}
	
	abstract protected void createAdditionalButton(Composite parent); 	
	abstract protected void selectionEvent(MetricFilterDataItem item, int click);
}
