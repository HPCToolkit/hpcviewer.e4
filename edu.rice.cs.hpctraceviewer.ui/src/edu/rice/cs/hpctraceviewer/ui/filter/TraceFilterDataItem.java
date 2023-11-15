package edu.rice.cs.hpctraceviewer.ui.filter;

import edu.rice.cs.hpcfilter.FilterDataItem;

public class TraceFilterDataItem extends FilterDataItem<IExecutionContext> 
{

	public TraceFilterDataItem(IExecutionContext data, boolean checked, boolean enabled) {
		super(data, checked, enabled);
	}

	@Override
	public int compareTo(FilterDataItem<IExecutionContext> o) {
		return Integer.compare(data.getNumSamples(), o.data.getNumSamples());
	}

	@Override
	public void setLabel(String name) {
		// immutable
	}

	@Override
	public String getLabel() {
		return data.getIdTuple().toString();
	}
}
