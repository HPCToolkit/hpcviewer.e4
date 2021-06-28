package edu.rice.cs.hpcmetric;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
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
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractFilterComposite<T> extends Composite 
{
	
	protected Text objSearchText;
	private final Composite parentContainer;
	
	public AbstractFilterComposite(Composite parent, int style, List<? extends Data> list, String []labels) {
		super(parent, style);
		
		parentContainer = new Composite(parent, SWT.BORDER);

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

		NatTable nattable = setLayer(list, labels);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(nattable);
		
		parentContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		parentContainer.setLayout(grid);		
	}

	
	protected NatTable setLayer(List<? extends Data> list, final String []labels) {

		IConfigRegistry configRegistry = new ConfigRegistry();

		EventList<Data> eventList   = GlazedLists.eventList(list);
		SortedList<Data> sortedList = new SortedList<Data>(eventList);
		FilterList<Data> filterList = new FilterList<AbstractFilterComposite.Data>(sortedList);

		IDataProvider dataProvider = new FilterDataProvider(filterList);

		// data layer
		DataLayer dataLayer = new DataLayer(dataProvider);
		GlazedListsEventLayer<Data> listEventLayer = new GlazedListsEventLayer<AbstractFilterComposite.Data>(dataLayer, eventList);
		DefaultBodyLayerStack defaultLayerStack = new DefaultBodyLayerStack(listEventLayer);

		// columns header
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(labels);
		DataLayer columnDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		ColumnHeaderLayer colummnLayer = new ColumnHeaderLayer(columnDataLayer, dataLayer, defaultLayerStack.getSelectionLayer());
		
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
		
		final ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(defaultLayerStack);
		defaultLayerStack.setConfigLabelAccumulator(columnLabelAccumulator);
		columnLabelAccumulator.registerColumnOverrides(0, labels[0]);
	        
		// the table
		NatTable natTable = new NatTable(parentContainer, gridLayer, false); 
		
		// additional configuration
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
		natTable.addConfiguration(new FilterConfiguration(labels));
		
		natTable.setConfigRegistry(configRegistry); 
		natTable.configure();
		
		return natTable;
	}
	
	
	private static class FilterConfiguration extends AbstractRegistryConfiguration
	{
		private final String []labels;
		
		public FilterConfiguration(String []labels) {
			this.labels = labels;
		}
		
		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {

			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
												   new CheckBoxPainter(), 
												   DisplayMode.NORMAL, 
												   labels[0]);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultBooleanDisplayConverter(), DisplayMode.NORMAL, labels[0]);
			configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new CheckBoxCellEditor(), DisplayMode.NORMAL, labels[0]);
		}
	}
	
	
	public static class Data implements Comparable<Data>{
		public List<Object> values;

		@Override
		public int compareTo(Data o) {
			String s1 = (String) values.get(1);
			String s2 = (String) o.values.get(1);
			return s1.compareTo(s2);
		}
	}

	
	public static class FilterDataProvider implements IDataProvider 
	{
		private final List<Data> list;
		
		public FilterDataProvider(List<Data> list) {
			this.list = list;
		}

		@Override
		public Object getDataValue(int columnIndex, int rowIndex) {
			return list.get(rowIndex).values.get(columnIndex);
		}

		@Override
		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
			Data data = list.get(rowIndex);
			data.values.set(columnIndex-1, newValue);
		}

		@Override
		public int getColumnCount() {
			return list.get(0).values.size();
		}

		@Override
		public int getRowCount() {
			return list.size();
		}
		
	}
	
	
	abstract protected void createAdditionalButton(Composite parent); 

}
