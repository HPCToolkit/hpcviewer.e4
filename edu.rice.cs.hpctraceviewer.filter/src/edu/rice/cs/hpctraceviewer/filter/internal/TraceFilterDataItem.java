// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.filter.internal;

import edu.rice.cs.hpcfilter.FilterDataItem;

public class TraceFilterDataItem extends FilterDataItem<IExecutionContext> 
{
	private final int index;
	
	public TraceFilterDataItem(int index, IExecutionContext data, boolean checked, boolean enabled) {
		super(data, checked, enabled);
		this.index = index;
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
	
	public int getIndex() {
		return index;
	}
}
