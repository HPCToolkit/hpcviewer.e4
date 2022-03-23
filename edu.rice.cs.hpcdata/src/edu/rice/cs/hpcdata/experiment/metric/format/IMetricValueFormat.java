package edu.rice.cs.hpcdata.experiment.metric.format;

import edu.rice.cs.hpcdata.experiment.metric.MetricValue;

public interface IMetricValueFormat {

	public String format(MetricValue value, MetricValue rootValue);
}
