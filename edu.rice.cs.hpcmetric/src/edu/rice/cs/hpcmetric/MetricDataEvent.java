package edu.rice.cs.hpcmetric;

import java.util.List;

import edu.rice.cs.hpcfilter.FilterDataItem;

public class MetricDataEvent 
{
	final private boolean applyToAll;
	final private Object  data;
	final private List<FilterDataItem> list;
	
	public MetricDataEvent(Object data, List<FilterDataItem> list, boolean applyToAll) {
		this.applyToAll = applyToAll;
		this.data = data;
		this.list = list;
	}
	
	public boolean isApplyToAll() {
		return applyToAll;
	}

	public Object getData() {
		return data;
	}

	public List<FilterDataItem> getList() {
		return list;
	}

	@Override
	public String toString() {
		return "All: " + applyToAll + ", data: " + data + ", list size: " + list.size();
	}
}
