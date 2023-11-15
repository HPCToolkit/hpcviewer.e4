package edu.rice.cs.hpctraceviewer.ui.filter;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.swt.widgets.Composite;

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

	protected TraceFilterPane(Composite parent, int style, FilterInputData<IExecutionContext> inputData, final IEnableButtonOk buttonEnabler) {
		super(parent, style, inputData);
		this.buttonEnabler = buttonEnabler;
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
		
	}

	@Override
	protected void addConfiguration(NatTable table) {
		
	}		
}
