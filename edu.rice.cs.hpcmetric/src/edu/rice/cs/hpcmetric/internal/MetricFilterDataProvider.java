package edu.rice.cs.hpcmetric.internal;

import java.util.List;
import java.util.Optional;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterDataProvider;
import edu.rice.cs.hpcfilter.IFilterChangeListener;

public class MetricFilterDataProvider extends FilterDataProvider<BaseMetric>
{
	private static final String METRIC_DERIVED = "Derived metric"; //$NON-NLS-N$
	private static final String METRIC_EMPTY   = "empty";

	private final RootScope root;

	public MetricFilterDataProvider(RootScope root, List<FilterDataItem<BaseMetric>> filterList, IFilterChangeListener changeListener) {
		super(filterList, changeListener);
		this.root = root;
	}

	
	public Object getDataValue(int columnIndex, int rowIndex) {
		FilterDataItem<BaseMetric> item = getList().get(rowIndex);
		Object data = item.getData();
		
		switch (columnIndex) {
		case IConstants.INDEX_METRIC_VAL:
			if (data == null)
				return METRIC_EMPTY;
			BaseMetric metric = (BaseMetric) data;
			return metric.getMetricTextValue(root);
			
		case IConstants.INDEX_DESCRIPTION: 
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
		return super.getDataValue(columnIndex, rowIndex);
	}
	

	public void update(BaseMetric metric) {
		Optional<FilterDataItem<BaseMetric>> mfdi = getList().stream()
												.filter( item -> ((BaseMetric)item.data).getIndex() == metric.getIndex() )
												.findFirst();
		if (mfdi.isEmpty())
			return;
		
		FilterDataItem<BaseMetric> item = mfdi.get();
		item.data = metric;
		item.setLabel(metric.getDisplayName());
	}

	
	@Override
	public int getColumnCount() {
		return IConstants.COLUMN_LABELS.length;
	}

	
	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		FilterDataItem<BaseMetric> item = getList().get(rowIndex);
		Object data = item.getData();

		if (data == null || !item.enabled)
			return;

		BaseMetric metric = (BaseMetric) data;

		switch(columnIndex) {

		case IConstants.INDEX_DESCRIPTION:
			metric.setDescription((String) newValue);
			break;
		case IConstants.INDEX_METRIC_VAL:
			break;
		default: 
			super.setDataValue(columnIndex, rowIndex, newValue);
		}
	}
}
