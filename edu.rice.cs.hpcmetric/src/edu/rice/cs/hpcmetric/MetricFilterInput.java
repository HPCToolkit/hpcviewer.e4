package edu.rice.cs.hpcmetric;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataItem;

public class MetricFilterInput 
{
	private final RootScope root;
	private final List<MetricFilterDataItem> listItems;
	private final boolean affectAll;
	
	public MetricFilterInput(RootScope root, TreeColumn []columns, boolean affectAll) {		
		Experiment experiment = (Experiment) root.getExperiment();
		this.listItems = createFilterList(experiment.getVisibleMetrics(), columns);
		this.root = root;
		this.affectAll = affectAll;
	}
	
	public List<MetricFilterDataItem> getFilterList() {
		return listItems;
	}
	
	public List<MetricFilterDataItem> createFilterList(List<BaseMetric> metrics, TreeColumn []columns) {
		List<MetricFilterDataItem> listItems = new ArrayList<MetricFilterDataItem>(metrics.size());
		
		for(BaseMetric metric: metrics) {
			
			MetricFilterDataItem item = new MetricFilterDataItem(metric.getIndex(), metric.getDisplayName(), false, false);
			item.setData(metric);
			
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
		return (IMetricManager) root.getExperiment();
	}


	public RootScope getRoot() {
		return root;
	}


	public boolean isAffectAll() {
		return affectAll;
	}
}
