package edu.rice.cs.hpc.data.experiment.metric;

import java.util.Comparator;

public class MetricComparator implements Comparator<BaseMetric> 
{

	@Override
	public int compare(BaseMetric o1, BaseMetric o2) {

		return o1.getIndex() - o2.getIndex();
	}

}
