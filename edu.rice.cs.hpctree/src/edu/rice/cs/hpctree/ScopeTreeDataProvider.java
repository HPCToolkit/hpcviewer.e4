package edu.rice.cs.hpctree;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class ScopeTreeDataProvider implements IDataProvider, IRowDataProvider<Scope>
{
	private final ScopeTreeData treeData;
	
	public ScopeTreeDataProvider(IScopeTreeData treeData) {
		this.treeData   = (ScopeTreeData) treeData;
	}

	public BaseMetric getMetric(int columnIndex) {
		if (columnIndex == 0)
			return null;
		
		return treeData.getMetric(columnIndex-1);
	}
	
	
	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		Scope scope = treeData.getDataAtIndex(rowIndex);

		if (columnIndex == 0)
			return scope.getName();

		BaseMetric metric = treeData.getMetric(columnIndex-1);
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
		
		return 1 + treeData.getMetricCount();
	}

	@Override
	public int getRowCount() {
		return treeData.getElementCount();
	}

	@Override
	public Scope getRowObject(int rowIndex) {
		return treeData.getDataAtIndex(rowIndex);
	}

	@Override
	public int indexOfRowObject(Scope rowObject) {
		return treeData.indexOf(rowObject);
	}

}
