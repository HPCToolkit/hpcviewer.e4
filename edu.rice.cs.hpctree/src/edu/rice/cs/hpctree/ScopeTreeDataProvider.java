package edu.rice.cs.hpctree;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class ScopeTreeDataProvider implements IDataProvider 
{
	private final ITreeData<Scope> treeData;
	private final IMetricManager metricManager;
	
	public ScopeTreeDataProvider(ITreeData<Scope> treeData, IMetricManager metricManager) {
		this.treeData   = treeData;
		this.metricManager = metricManager;
	}

	
	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		Scope scope = treeData.getDataAtIndex(rowIndex);

		if (columnIndex == 0)
			return scope.getName();

		List<BaseMetric> metrics = metricManager.getVisibleMetrics();
		BaseMetric metric = metrics.get(columnIndex-1);
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
		List<BaseMetric> metrics = metricManager.getVisibleMetrics();
		
		return 1 + metrics.size();
	}

	@Override
	public int getRowCount() {
		return treeData.getElementCount();
	}

}
