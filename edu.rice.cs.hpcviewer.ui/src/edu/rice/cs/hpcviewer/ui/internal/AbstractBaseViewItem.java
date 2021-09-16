package edu.rice.cs.hpcviewer.ui.internal;

import java.util.List;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcmetric.MetricFilterInput;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataItem;

public abstract class AbstractBaseViewItem extends AbstractView {

	public AbstractBaseViewItem(CTabFolder parent, int style) {
		super(parent, style);
	}
	
	
	
	
	protected static void updateColumnHideOrShowStatus(ScopeTreeViewer treeViewer, Object data) {
		if (data instanceof List<?>) {
			@SuppressWarnings("unchecked")
			List<MetricFilterDataItem> list = (List<MetricFilterDataItem>) data;
			list.stream().forEach(item -> {
				setColumnHideOrShow(treeViewer, item);
			});
			
		} else if (data instanceof MetricFilterDataItem) {
			setColumnHideOrShow(treeViewer, (MetricFilterDataItem) data);
		}
	}
	
	
	protected static void setColumnHideOrShow(ScopeTreeViewer treeViewer, MetricFilterDataItem filterItem) {

		BaseMetric metric = (BaseMetric) filterItem.data;
		TreeColumn []columns = treeViewer.getTree().getColumns();
		TreeColumn column = null;
		for (int i=0; i<columns.length; i++) {
			if (columns[i].getData() == metric) {
				column = columns[i];
				break;
			}
		}
		if (column != null)
			treeViewer.setColumnsStatus(column, filterItem.checked);
	}

	@Override
	public List<FilterDataItem<BaseMetric>> getFilterDataItems() {
		IMetricManager metricManager = getMetricManager();
		List<BaseMetric> metrics = metricManager.getVisibleMetrics();

		return MetricFilterInput.createFilterList(metrics, getScopeTreeViewer());
	}
	
	
	public abstract ScopeTreeViewer getScopeTreeViewer();
}
