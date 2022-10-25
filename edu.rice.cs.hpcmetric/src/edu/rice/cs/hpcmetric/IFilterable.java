package edu.rice.cs.hpcmetric;

import java.util.List;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcbase.BaseConstants.ViewType;

public interface IFilterable 
{
	public abstract List<FilterDataItem<BaseMetric>> getFilterDataItems();
	public abstract ViewType getViewType();
}
