// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.filter.internal;

import java.util.List;

import org.hpctoolkit.db.local.db.IdTupleType;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterDataProvider;
import edu.rice.cs.hpcfilter.IFilterChangeListener;

public class TraceFilterDataProvider extends FilterDataProvider<IExecutionContext> 
{
	private final IdTupleType idTupleType;
	
	public TraceFilterDataProvider(List<FilterDataItem<IExecutionContext>> list,
								   IFilterChangeListener changeListener,
								   IdTupleType idTupleType) {
		super(list, changeListener);
		this.idTupleType = idTupleType;
	}

	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		var item = getList().get(rowIndex);
		
		switch (columnIndex) {
		case 1:
			return item.data.getIdTuple().toString(idTupleType);
		case 2:
			return item.data.getNumSamples();
		default:
			return super.getDataValue(columnIndex, rowIndex);
		}
	}
	
	
	@Override
	public int getColumnCount() {
		return 3;
	}
}
