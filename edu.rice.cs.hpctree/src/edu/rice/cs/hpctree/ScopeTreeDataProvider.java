package edu.rice.cs.hpctree;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class ScopeTreeDataProvider implements IDataProvider 
{
	private final ITreeData<Scope> treeData;
	private final Experiment experiment;
	
	public ScopeTreeDataProvider(ITreeData<Scope> treeData, Experiment experiment) {
		this.treeData   = treeData;
		this.experiment = experiment;
	}

	
	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		Scope scope = treeData.getDataAtIndex(rowIndex);

		if (columnIndex == 0)
			return scope.getName();

		MetricValue mv = scope.getMetricValue(columnIndex-1);
		return mv.getValue();
	}

	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
	}

	
	@Override
	public int getColumnCount() {
		List<BaseMetric> metrics = experiment.getVisibleMetrics();
		
		return 1 + metrics.size();
	}

	@Override
	public int getRowCount() {
		return treeData.getElementCount();
	}

}
