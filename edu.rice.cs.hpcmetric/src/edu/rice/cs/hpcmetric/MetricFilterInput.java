package edu.rice.cs.hpcmetric;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcfilter.dialog.FilterDataItem;

public class MetricFilterInput 
{
	private IMetricManager metricManager;
	private RootScope root;
	private List<FilterDataItem> listItems;
	private boolean affectAll;
	
	public List<FilterDataItem> getFilterList() {
		return listItems;
	}
	
	public void setFilterList(List<BaseMetric> metrics, TreeColumn []columns) {
		listItems = new ArrayList<FilterDataItem>(metrics.size());
		
		for(BaseMetric metric: metrics) {
			
			FilterDataItem item = new FilterDataItem(metric.getDisplayName(), false, false);
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
	}


	public IMetricManager getMetricManager() {
		return metricManager;
	}


	public void setMetricManager(IMetricManager metricManager) {
		this.metricManager = metricManager;
	}


	public RootScope getRoot() {
		return root;
	}


	public void setRoot(RootScope root) {
		this.root = root;
	}


	public boolean isAffectAll() {
		return affectAll;
	}


	public void setAffectAll(boolean affectAll) {
		this.affectAll = affectAll;
	}
}
