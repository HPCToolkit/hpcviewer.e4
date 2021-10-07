package edu.rice.cs.hpcfilter.internal;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;

import edu.rice.cs.hpcfilter.FilterDataItem;

public class FilterConfigLabelAccumulator<T> extends ColumnLabelAccumulator 
{
	private final ILayer bodyLayer;
	private IRowDataProvider<FilterDataItem<T>> dataProvider;

	/***
	 * Constructor for metric label configuration
	 * @param bodyLayer the body layer, used to convert row position to row index
	 * @param dataProvider2 the data provider
	 * @param listMetrics the list 
	 */
	public FilterConfigLabelAccumulator(ILayer bodyLayer, IRowDataProvider<FilterDataItem<T>> dataProvider2) {
		super(dataProvider2);
		this.bodyLayer = bodyLayer;
		this.dataProvider = dataProvider2;
	}
	
	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		
		int rowIndex = bodyLayer.getRowIndexByPosition(rowPosition);
		FilterDataItem<T> item = dataProvider.getRowObject(rowIndex);
		if (!item.enabled) {
			configLabels.addLabel(IConstants.LABEL_ROW_GRAY);
		}
		super.accumulateConfigLabels(configLabels, columnPosition, rowPosition);
	} 
	
	public void setDataProvider(IRowDataProvider<FilterDataItem<T>> dataProvider) {
		this.dataProvider = dataProvider;
	}
}
