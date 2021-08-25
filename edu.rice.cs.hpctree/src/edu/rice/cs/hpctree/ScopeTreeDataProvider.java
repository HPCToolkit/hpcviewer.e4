package edu.rice.cs.hpctree;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class ScopeTreeDataProvider implements IDataProvider 
{
	private final EventList<BaseMetric> listMetrics;
	private final IScopeTreeData treeData;
	
	public ScopeTreeDataProvider(IScopeTreeData treeData) {
		this.treeData   = treeData;
		
		listMetrics = new BasicEventList<>();
		IMetricManager metricManager = treeData.getMetricManager();
		List<BaseMetric> listVisibleMetrics = metricManager.getVisibleMetrics();
		
		for(BaseMetric metric: listVisibleMetrics) {
			if (treeData.getRoot().getMetricValue(metric) != MetricValue.NONE) {
				listMetrics.add(metric);
			}
		}
	}

	public BaseMetric getMetric(int columnIndex) {
		if (columnIndex == 0)
			return null;
		
		return listMetrics.get(columnIndex-1);
	}
	
	
	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		Scope scope = treeData.getDataAtIndex(rowIndex);

		if (columnIndex == 0)
			return scope.getName();

		BaseMetric metric = listMetrics.get(columnIndex-1);
		return metric.getMetricTextValue(scope);
	}

	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		Scope scope = treeData.getDataAtIndex(rowIndex);

		if (columnIndex == 0) {
			return;
		}
		scope.setMetricValue(columnIndex-1, (MetricValue) newValue);
	}

	
	@Override
	public int getColumnCount() {
		
		return 1 + listMetrics.size();
	}

	@Override
	public int getRowCount() {
		return treeData.getElementCount();
	}

}
