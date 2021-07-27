package edu.rice.cs.hpcfilter.internal;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;

import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.IConstants;

public class FilterConfigLabelAccumulator extends ColumnLabelAccumulator 
{
	private final ILayer bodyLayer;
	private final IRowDataProvider<FilterDataItem> dataProvider;
	/***
	 * Constructor for metric label configuration
	 * @param bodyLayer the body layer, used to convert row position to row index
	 * @param dataProvider the data provider
	 * @param listMetrics the list 
	 */
	public FilterConfigLabelAccumulator(ILayer bodyLayer, IRowDataProvider<FilterDataItem> dataProvider) {
		super(dataProvider);
		this.bodyLayer = bodyLayer;
		this.dataProvider = dataProvider;
	}
	
	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		
		int rowIndex = bodyLayer.getRowIndexByPosition(rowPosition);
		FilterDataItem item = dataProvider.getRowObject(rowIndex);
		if (!item.enabled) {
			configLabels.addLabel(IConstants.LABEL_ROW_GRAY);
		}
		super.accumulateConfigLabels(configLabels, columnPosition, rowPosition);
	} 
}
