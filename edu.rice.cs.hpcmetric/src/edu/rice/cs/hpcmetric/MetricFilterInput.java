package edu.rice.cs.hpcmetric;

import java.util.List;

import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcfilter.dialog.FilterDataItem;

public class MetricFilterInput 
{
	public IMetricManager metricManager;
	public RootScope root;
	public List<FilterDataItem> listItems;
	public boolean affectAll;
}
