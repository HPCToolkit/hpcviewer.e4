// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcmetric;

import java.util.List;

import org.hpctoolkit.db.local.experiment.metric.BaseMetric;
import edu.rice.cs.hpcfilter.FilterDataItem;

public class MetricDataEvent 
{
	final private boolean applyToAll;
	final private Object  data;
	final private List<FilterDataItem<BaseMetric>> list;
	
	public MetricDataEvent(Object data, List<FilterDataItem<BaseMetric>> copyList, boolean applyToAll) {
		this.applyToAll = applyToAll;
		this.data = data;
		this.list = copyList;
	}
	
	public boolean isApplyToAll() {
		return applyToAll;
	}

	public Object getData() {
		return data;
	}

	public List<FilterDataItem<BaseMetric>> getList() {
		return list;
	}

	@Override
	public String toString() {
		return "All: " + applyToAll + ", data: " + data + ", list size: " + list.size();
	}
}
