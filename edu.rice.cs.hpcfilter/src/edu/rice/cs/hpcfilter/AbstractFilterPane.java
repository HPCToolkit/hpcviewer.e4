package edu.rice.cs.hpcfilter;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
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
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionBindings;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcfilter.internal.CheckBoxConfiguration;
import edu.rice.cs.hpcfilter.internal.FilterConfigLabelAccumulator;
import edu.rice.cs.hpcfilter.internal.FilterPainterConfiguration;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;



public abstract class AbstractFilterPane implements IFilterChangeListener, 
													IPropertyChangeListener, 
													DisposeListener 
{
	private final FilterInputData inputData;
	private final NatTable  natTable ;
	private final EventList<FilterDataItem> eventList ;
	private final DataLayer dataLayer ;
	private final RowSelectionProvider<FilterDataItem> rowSelectionProvider;
	private final FilterList<FilterDataItem> filterList;

	public AbstractFilterPane(Composite parent, int style, FilterInputData inputData) {
		this.inputData = inputData;
		
		
		Composite parentContainer = new Composite(parent, SWT.BORDER);
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
				getDataProvider().checkAll();
				natTable.refresh(false);
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
				getDataProvider().uncheckAll();
				natTable.refresh(false);
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

		// ------------------------------------------------------------
		// Start building the nat-table
		// ------------------------------------------------------------
		this.eventList = GlazedLists.eventList(inputData.getListItems());

		SortedList<FilterDataItem> sortedList = new SortedList<FilterDataItem>(eventList);
		filterList = new FilterList<FilterDataItem>(sortedList);

		// data layer
		FilterDataProvider dataProvider = getDataProvider();
		
		dataLayer = new DataLayer(dataProvider);
		GlazedListsEventLayer<FilterDataItem> listEventLayer = new GlazedListsEventLayer<FilterDataItem>(dataLayer, eventList);
		DefaultBodyLayerStack defaultLayerStack = new DefaultBodyLayerStack(listEventLayer);
		defaultLayerStack.getSelectionLayer().addConfiguration(new RowOnlySelectionConfiguration());

		//
		//
		setLayerConfiguration(dataLayer);
		
		// columns header
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(getColumnHeaderLabels());
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
		defaultLayerStack.setConfigLabelAccumulator(new FilterConfigLabelAccumulator(defaultLayerStack, dataProvider));
		
		// the table
		natTable = new NatTable(parentContainer, gridLayer, false); 

		// default style configuration
		DefaultNatTableStyleConfiguration styleConfig = new DefaultNatTableStyleConfiguration();
		
		// additional configuration
		natTable.addConfiguration(styleConfig);
		natTable.addConfiguration(new CheckBoxConfiguration(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + IConstants.INDEX_VISIBILITY));
		natTable.addConfiguration(new FilterPainterConfiguration());
		natTable.addConfiguration(new RowOnlySelectionBindings());
		
		// ------------------------------------------------------------
		// Customized configuration
		// ------------------------------------------------------------
		addConfiguration(natTable);
		
		natTable.configure();
 		
		final TextMatcherEditor<FilterDataItem> textMatcher = new TextMatcherEditor<>(new TextFilterator<FilterDataItem>() {

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
            Iterator<FilterDataItem> it = selection.iterator();
            
            if (!it.hasNext())
            	return;
            FilterDataItem item = (FilterDataItem) it.next();
            
            selectionEvent(item, SWT.MouseDown);
		});
		
		natTable.getUiBindingRegistry().registerDoubleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new IMouseAction() {
			
			@Override
			public void run(NatTable natTable, MouseEvent event) {
				int row = natTable.getRowPositionByY(event.y);
				int index = natTable.getRowIndexByPosition(row);
				FilterDataItem item = dataProvider.getRowObject(index);
				selectionEvent(item, SWT.MouseDoubleClick);
			}
		});

		final ThemeConfiguration modernTheme = new ModernNatTableThemeConfiguration();
		natTable.setTheme(modernTheme);
		
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
				natTable.refresh(false);
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
				natTable.refresh(false);
			}
		});
		// expand as much as possible both horizontally and vertically
		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		
		
		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		pref.addPropertyChangeListener((IPropertyChangeListener) this);

	}
	
	
	protected FilterInputData getInputData() {
		return inputData;
	}
	
	
	protected DataLayer getDataLayer() {
		return dataLayer;
	}
	
	
	public NatTable getNatTable() {
		return natTable;
	}

	
	public EventList<FilterDataItem> getEventList() {
		return eventList;
	}
	
	public FilterList<FilterDataItem> getFilterList() {
		return filterList;
	}


	/****
	 * Retrieve the selection provider
	 * @return {@code ISelectionProvider}
	 */
	protected ISelectionProvider getSelectionProvider() {
		return rowSelectionProvider;
	}

	abstract protected void setLayerConfiguration(DataLayer datalayer);
	abstract protected String[] getColumnHeaderLabels();
	abstract protected FilterDataProvider getDataProvider();
	abstract protected void createAdditionalButton(Composite parent); 	
	abstract protected void selectionEvent(FilterDataItem item, int click);
	abstract protected void addConfiguration(NatTable table);
}
