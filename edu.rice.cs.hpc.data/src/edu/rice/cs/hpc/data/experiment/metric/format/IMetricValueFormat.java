package edu.rice.cs.hpc.data.experiment.metric.format;

import edu.rice.cs.hpc.data.experiment.metric.MetricValue;

public interface IMetricValueFormat {

	public String format(MetricValue value);
}
