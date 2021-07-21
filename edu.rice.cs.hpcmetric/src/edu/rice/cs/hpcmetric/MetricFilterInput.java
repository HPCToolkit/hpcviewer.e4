package edu.rice.cs.hpcmetric;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataItem;

public class MetricFilterInput 
{
	private final RootScope root;
	private final List<MetricFilterDataItem> listItems;
	private final boolean affectAll;
	private final IMetricManager metricManager;

	
	public MetricFilterInput(RootScope root, IMetricManager metricManager, TreeViewer treeViewer, boolean affectAll) {		

		this.root = root;
		this.metricManager = metricManager;

		this.listItems = createFilterList(metricManager.getVisibleMetrics(), treeViewer);
		this.affectAll = affectAll;
	}
	
	
	public List<MetricFilterDataItem> getFilterList() {
		return listItems;
	}
	
	
	private List<MetricFilterDataItem> createFilterList(List<BaseMetric> metrics, TreeViewer treeViewer) {
		List<MetricFilterDataItem> listItems = new ArrayList<MetricFilterDataItem>(metrics.size());
		TreeColumn []columns = treeViewer.getTree().getColumns();
		
		for(BaseMetric metric: metrics) {
			
			MetricFilterDataItem item = new MetricFilterDataItem(metric.getIndex(), metric, false, false);
			
			// looking for associated metric in the column
			// a metric may not exit in table viewer because
			// it has no metric value (empty metric)
			
			for(TreeColumn column: columns) {
				Object data = column.getData();
				
				if (data != null) {
					BaseMetric m = (BaseMetric) data;
					if (m.equalIndex(metric)) {
						item.enabled = true;
						item.checked = column.getWidth() > 1;
						break;
					}
				}
			}
			listItems.add(item);
		}
		return listItems;
	}


	public IMetricManager getMetricManager() {
		return metricManager;
	}


	public RootScope getRoot() {
		return root;
	}


	public boolean isAffectAll() {
		return affectAll;
	}

	
}
