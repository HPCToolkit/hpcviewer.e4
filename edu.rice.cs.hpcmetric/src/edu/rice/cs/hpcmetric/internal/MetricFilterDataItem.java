package edu.rice.cs.hpcmetric.internal;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcfilter.FilterDataItem;

public class MetricFilterDataItem extends FilterDataItem implements Comparable<MetricFilterDataItem>
{
	private final int id;

	public MetricFilterDataItem(int id, Object data, boolean checked, boolean enabled) {
		super(data, checked, enabled);
		this.id = id;
	}

	@Override
	public int compareTo(MetricFilterDataItem o) {
		return id-o.id;
	}
	
	
	public void setLabel(String name) {
		BaseMetric metric = (BaseMetric) data;
		metric.setDisplayName(name);
	}
	
	
	public String getLabel() {
		BaseMetric metric = (BaseMetric) data;
		return metric.getDisplayName();
	}
	
	
	@Override
	public String toString() {
		BaseMetric metric = (BaseMetric) data;
		return metric.getDisplayName();
	}
}
