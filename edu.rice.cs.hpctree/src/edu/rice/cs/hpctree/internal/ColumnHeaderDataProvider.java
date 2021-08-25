package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpctree.ScopeTreeDataProvider;

public class ColumnHeaderDataProvider implements IDataProvider 
{
	private final ScopeTreeDataProvider treeDataProvider;
	
	public ColumnHeaderDataProvider(ScopeTreeDataProvider treeDataProvider) {
		this.treeDataProvider = treeDataProvider;
	}
	
	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		if (columnIndex < 0)
			return null;
		
		if (columnIndex == 0)
			return "Scope";
		
		BaseMetric metric = treeDataProvider.getMetric(columnIndex);
		return metric.getDisplayName();
	}

	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {}

	@Override
	public int getColumnCount() {
		return treeDataProvider.getColumnCount();
	}

	@Override
	public int getRowCount() {
		return 1;
	}
}
