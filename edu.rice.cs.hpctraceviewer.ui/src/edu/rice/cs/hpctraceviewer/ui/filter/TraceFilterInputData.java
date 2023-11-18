package edu.rice.cs.hpctraceviewer.ui.filter;

import java.util.List;

import edu.rice.cs.hpcdata.db.IdTupleType;
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
