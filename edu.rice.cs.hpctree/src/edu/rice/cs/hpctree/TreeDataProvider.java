package edu.rice.cs.hpctree;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class TreeDataProvider implements IDataProvider 
{
	private final RootScope root;
	private int columnSorted;
	
	public TreeDataProvider(RootScope root) {
		this.root = root;
		columnSorted = 0;
	}

	public void setSortColumn(int column) {
		columnSorted = column;
	}
	
	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		Scope scope = rowIndex == 0 ? root : (Scope) root.getChildAt(rowIndex-1);
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
		Experiment experiment = (Experiment) root.getExperiment();
		List<BaseMetric> metrics = experiment.getVisibleMetrics();
		
		return 1 + metrics.size();
	}

	@Override
	public int getRowCount() {
		return 1 + root.getChildCount();
	}

}
