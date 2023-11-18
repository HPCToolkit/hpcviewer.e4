package edu.rice.cs.hpctraceviewer.filter.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import edu.rice.cs.hpcfilter.AbstractFilterPane;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterDataItemSortModel;
import edu.rice.cs.hpcfilter.FilterDataProvider;
import edu.rice.cs.hpcfilter.FilterInputData;
import edu.rice.cs.hpcfilter.IFilterChangeListener;

public class TraceFilterPane extends AbstractFilterPane<IExecutionContext> implements IFilterChangeListener
{
	private final IEnableButtonOk buttonEnabler;
	
	private FilterDataProvider<IExecutionContext> dataProvider;
	
	
	
	private ExecutionContextMatcherEditor matcherEditor;

	/****
	 * Create a filter pane just for trace lines.
	 * 
	 * @param parent
	 * 			The parent widget (or shell)
	 * @param style
	 * 			Either {@code STYLE_COMPOSITE} or {@code STYLE_INDEPENDENT}
	 * @param inputData
	 * 			The data to be filtered, has to be a type of {@code IExecutionContext}
	 * @param buttonEnabler
	 * 			A call back when to turn on or off the close (or ok) button.
	 */
	public TraceFilterPane(Composite parent, int style, TraceFilterInputData inputData, final IEnableButtonOk buttonEnabler) {
		super(parent, style, inputData);
		this.buttonEnabler = buttonEnabler;
		
		matcherEditor = new ExecutionContextMatcherEditor(inputData.getIdTupleType());
		var filterList = getFilterList();
		filterList.setMatcherEditor(matcherEditor);
	}
	
	
	/****
	 * Create the filter list for the table.
	 * This method will set the text matcher to the new filter list automatically.
	 * 
	 * @param eventList
	 * @return
	 */
	@Override
	protected FilterList<FilterDataItem<IExecutionContext>> createFilterList(EventList<FilterDataItem<IExecutionContext>> eventList) {
		return new FilterList<>(eventList);
	}
	
	
	@Override
	protected void setLayerConfiguration(DataLayer datalayer) {
		datalayer.setColumnPercentageSizing(true);
		datalayer.setColumnWidthPercentageByPosition(0, 10);
		datalayer.setColumnWidthPercentageByPosition(1, 70);
		datalayer.setColumnWidthPercentageByPosition(2, 20);
	}

	@Override
	protected String[] getColumnHeaderLabels() {
		return new String[] {"Visible", "Execution context", "Samples"};
	}

	@Override
	protected FilterDataProvider<IExecutionContext> getDataProvider(
			FilterList<FilterDataItem<IExecutionContext>> filterList) {
		if (dataProvider == null) {
			dataProvider = new FilterDataProvider<>(filterList, this) {
				@Override
				public int getColumnCount() {
					return 3;
				}
				
				@Override
				public Object getDataValue(int columnIndex, int rowIndex) {
					var item = filterList.get(rowIndex);
					IExecutionContext context = (IExecutionContext) item.getData();

					if (columnIndex == 2) {
						return context.getNumSamples();
					}
					
					return super.getDataValue(columnIndex, rowIndex);
				}
			};
		}
		return dataProvider;
	}

	
	@Override
	protected FilterDataItemSortModel<IExecutionContext> createSortModel(EventList<FilterDataItem<IExecutionContext>> eventList) {
		return new FilterDataItemSortModel<>(eventList) {
			@Override
			public int compare(FilterDataItem<IExecutionContext> o1, FilterDataItem<IExecutionContext> o2) {
				if (currentSortColumn == 2) {
					int factor = 1;
					if (currentSortDirect == SortDirectionEnum.DESC) {
						factor = -1;
					}
					return factor * Integer.compare(o1.data.getNumSamples(), o2.data.getNumSamples());
				}
				return super.compare(o1, o2);				
			}
		};
	}
	

	@Override
	public void changeEvent(Object data) {
		var list = getEventList();
		if (list == null || list.isEmpty())
			return;
		var enabled = (list.stream().anyMatch(item -> item.checked));
		buttonEnabler.enableOkButton(enabled);
	}

	
	@Override
	protected int createAdditionalButton(Composite parent, FilterInputData<IExecutionContext> inputData) {
		return 0;
	}

	@Override
	protected void selectionEvent(FilterDataItem<IExecutionContext> item, int click) {
		// no action is needed
	}

	@Override
	protected void addConfiguration(NatTable table) {
		// no action is needed
	}

	@Override
	protected int createAdditionalFiler(Composite parent, FilterInputData<IExecutionContext> inputData) {
		Label lblFilter = new Label(parent, SWT.NONE);
		lblFilter.setText("Minimum samples:");
		
		var comboSampleFilter = new Combo(parent, SWT.DROP_DOWN);
		comboSampleFilter.setSize(50, comboSampleFilter.getSize().y);
		comboSampleFilter.addModifyListener(event -> {
			var strSamples = comboSampleFilter.getText();
			int numSamples = 0;
			try {
				numSamples = Integer.parseInt(strSamples);
			} catch (NumberFormatException e) {
				numSamples = 0;
			}
			filterSamples(numSamples);
		});

		// give enough room for the samples 
		GridDataFactory.fillDefaults().hint(100, 20).applyTo(comboSampleFilter);

		return 2;
	}
	
	
	@Override
	protected void eventFilterText(String text) {
		if (matcherEditor == null)
			// matcher is not set yet
			return;
		
		matcherEditor.filterText(text);
		getNatTable().refresh();
	}
	
	private void filterSamples(final int minSamples) {
		if (matcherEditor == null)
			// matcher is not set yet
			return;
		
		matcherEditor.filterMinSamples(minSamples);
		getNatTable().refresh();
	}
}
