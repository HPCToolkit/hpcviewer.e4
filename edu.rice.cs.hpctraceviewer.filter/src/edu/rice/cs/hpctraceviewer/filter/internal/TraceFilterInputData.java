// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.filter.internal;

import java.util.List;

import org.hpctoolkit.db.local.db.IdTupleType;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterInputData;

public class TraceFilterInputData extends FilterInputData<IExecutionContext> 
{
	private final IdTupleType idTupleType;
	
	public TraceFilterInputData(List<FilterDataItem<IExecutionContext>> list, IdTupleType idTupleType) {
		super(list);
		this.idTupleType = idTupleType;
	}

	public IdTupleType getIdTupleType() {
		return idTupleType;
	}
}
