// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;

public class ColumnHeaderDataProvider implements IDataProvider, ListEventListener<BaseMetric> 
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
		if (metric == null)
			return "No metric";
		
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

	@Override
	public void listChanged(ListEvent<BaseMetric> listChanges) {
	}
}
