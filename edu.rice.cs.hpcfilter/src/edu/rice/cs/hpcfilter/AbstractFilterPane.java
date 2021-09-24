package edu.rice.cs.hpcfilter;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
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
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.theme.DarkNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.style.theme.ThemeConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import edu.rice.cs.hpcfilter.internal.CheckBoxConfiguration;
import edu.rice.cs.hpcfilter.internal.FilterConfigLabelAccumulator;
import edu.rice.cs.hpcfilter.internal.FilterPainterConfiguration;
import edu.rice.cs.hpcfilter.internal.IConstants;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;


/***********************************************************************
 * 
 * Base class to have a filter pane which consists of:
 * <ul>
 *   <li>Action buttons area, which consists of:
 *     <ul>
 *       <li>Check all: to check all the enabled visible items</li>
 *       <li>Uncheck all: to clear the visible checked items</li>
 *       <li>Others (to be defined by the child</li>
 *     </ul>
 *   </li>
 *   <li>Text filter area</li>
 *   <li>Table to display list of items to be filtered. The table contains:
 *     <ul>
 *       <li>Column check/uncheck</li>
 *       <li>Label of the item</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 ***********************************************************************/
public abstract class AbstractFilterPane<T> implements IPropertyChangeListener, DisposeListener
{
	public final static int STYLE_COMPOSITE   = 0;
	public final static int STYLE_INDEPENDENT = 1;
	
	private final int style;
	private final FilterInputData<T> inputData;
	private final NatTable  natTable ;
	private final EventList<FilterDataItem<T>> eventList ;
	private final DataLayer dataLayer ;
	private final RowSelectionProvider<FilterDataItem<T>> rowSelectionProvider;
	private final FilterList<FilterDataItem<T>> filterList;

	/***
	 * Constructor to create the item and its composite widgets.
	 * 
	 * @param parent 
	 * @param style int
	 * @param inputData {@code FilterInputData}
	 * @see STYLE_COMPOSITE, STYLE_INDEPENDENT
	 */
	public AbstractFilterPane(Composite parent, int style, FilterInputData<T> inputData) {
		this.inputData = inputData;
		this.style     = style;
		
		Composite parentContainer = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parentContainer);

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
		
		// number of buttons: 3 (check, uncheck, regex) + additional buttons
		int numAddButtons = 3 + createAdditionalButton(groupButtons);
		
		GridLayoutFactory.fillDefaults().numColumns(numAddButtons).applyTo(groupButtons);

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

		filterList = new FilterList<FilterDataItem<T>>(eventList);

		// data layer
		IRowDataProvider<FilterDataItem<T>> dataProvider = getDataProvider();
		
		dataLayer = new DataLayer(dataProvider);
		GlazedListsEventLayer<FilterDataItem<T>> listEventLayer = new GlazedListsEventLayer<FilterDataItem<T>>(dataLayer, eventList);
		DefaultBodyLayerStack defaultLayerStack = new DefaultBodyLayerStack(listEventLayer);
		defaultLayerStack.getSelectionLayer().addConfiguration(new RowOnlySelectionConfiguration());

		// data layer configuration to be implemented by the child class
		// 
		setLayerConfiguration(dataLayer);
		
		// columns header
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(getColumnHeaderLabels());
		DataLayer columnDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		ColumnHeaderLayer colummnLayer = new ColumnHeaderLayer(columnDataLayer, defaultLayerStack, defaultLayerStack.getSelectionLayer());		
		SortHeaderLayer<FilterDataItem<T>> sortHeaderLayer = new SortHeaderLayer<FilterDataItem<T>>(colummnLayer, 
																									new FilterDataItemSortModel<T>(eventList));
		
		// row header
		DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(dataProvider);
		DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, defaultLayerStack, defaultLayerStack.getSelectionLayer());

		// corner layer
		DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		CornerLayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, sortHeaderLayer);
		
		// grid layer
		GridLayer gridLayer = new GridLayer(defaultLayerStack, sortHeaderLayer, rowHeaderLayer, cornerLayer);		
		defaultLayerStack.setConfigLabelAccumulator(new FilterConfigLabelAccumulator<T>(defaultLayerStack, dataProvider));
		
		// the table
		natTable = new NatTable(parentContainer, gridLayer, false); 

		// additional configuration
		natTable.addConfiguration(new CheckBoxConfiguration(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + IConstants.INDEX_VISIBILITY));
		natTable.addConfiguration(new FilterPainterConfiguration());
		natTable.addConfiguration(new RowOnlySelectionBindings());
		natTable.addConfiguration(new SingleClickSortConfiguration());
		
		// ------------------------------------------------------------
		// Customized configuration
		// ------------------------------------------------------------
		addConfiguration(natTable);
		
		natTable.configure();
 		
		final TextMatcherEditor<FilterDataItem<T>> textMatcher = new TextMatcherEditor<>(new TextFilterator<FilterDataItem<T>>() {

			@Override
			public void getFilterStrings(List<String> baseList, FilterDataItem<T> element) {
				baseList.add(element.getLabel());
			}
		});
		textMatcher.setMode(TextMatcherEditor.CONTAINS);
		filterList.setMatcherEditor(textMatcher);

		rowSelectionProvider = new RowSelectionProvider<FilterDataItem<T>>(defaultLayerStack.getSelectionLayer(), dataProvider);
		rowSelectionProvider.addSelectionChangedListener((event)-> {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            Iterator<FilterDataItem<T>> it = selection.iterator();
            
            if (!it.hasNext())
            	return;
            FilterDataItem<T> item = (FilterDataItem<T>) it.next();
            
            selectionEvent(item, SWT.MouseDown);
		});
		
		// this UI binding causes mouse click event to be extremely slow
		/*
		natTable.getUiBindingRegistry().registerDoubleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new IMouseAction() {
			
			@Override
			public void run(NatTable natTable, MouseEvent event) {
				int row = natTable.getRowPositionByY(event.y);
				int index = natTable.getRowIndexByPosition(row);
				FilterDataItem<T> item = dataProvider.getRowObject(index);
				selectionEvent(item, SWT.MouseDoubleClick);
			}
		}); */

		ThemeConfiguration modernTheme = new ModernNatTableThemeConfiguration();
		if (Display.isSystemDarkTheme())
			modernTheme = new DarkNatTableThemeConfiguration();
		
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
		GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(parentContainer);

		if (style == STYLE_COMPOSITE) {
			// Real application, not within a simple unit test
			PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
			pref.addPropertyChangeListener(this);
			natTable.addDisposeListener(this);
		}
	}
	

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		final String property = event.getProperty();
		if (property.equals(PreferenceConstants.ID_FONT_GENERIC)) {
			getNatTable().redraw();
		}
	}
	
	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (style == STYLE_COMPOSITE) {
			PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
			pref.removePropertyChangeListener(this);
		}
	}
	
	protected FilterInputData<T> getInputData() {
		return inputData;
	}
	
	
	protected DataLayer getDataLayer() {
		return dataLayer;
	}
	
	
	public NatTable getNatTable() {
		return natTable;
	}

	
	public EventList<FilterDataItem<T>> getEventList() {
		return eventList;
	}
	
	public FilterList<FilterDataItem<T>> getFilterList() {
		return filterList;
	}

	public int getStyle() {
		return style;
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
	abstract protected FilterDataProvider<T> getDataProvider();

	abstract protected int createAdditionalButton(Composite parent); 	
	abstract protected void selectionEvent(FilterDataItem<T> item, int click);
	abstract protected void addConfiguration(NatTable table);
}
