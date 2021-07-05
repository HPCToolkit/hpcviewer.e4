package edu.rice.cs.hpcmetric;

import java.util.List;

import edu.rice.cs.hpcfilter.dialog.FilterDataItem;

public interface IMetricFilterEvent 
{
	public void filterMetrics(List<FilterDataItem> listItems);
}
