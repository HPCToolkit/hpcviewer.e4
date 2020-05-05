package edu.rice.cs.hpc.data.experiment.scope;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;

/***********************************************************
 * 
 * A generic interface to access Metrics 
 *
 ***********************************************************/
public interface IMetricScope 
{
	/**
	 * Returns the value of a given metric at this scope
	 * 
	 * @param index : the index of the metric
	 * @return
	 */
	public MetricValue getMetricValue(int index);
	
	/***
	 * Returns the value of a given metric at this scope.<br/>
	 * overload the method to take-in the index ---FMZ
	 * @param metric
	 * @return
	 */
	public MetricValue getMetricValue(BaseMetric metric);
	
	/***
	 * Sets the value of a given metric at this scope.
	 * 
	 * @param index
	 * @param value
	 */
	public void setMetricValue(int index, MetricValue value);
	
	/****
	 * retrieve the value of the root. <br/>
	 * This method is useful to compute the percentage
	 * 
	 * @param metric : the metric 
	 * 
	 * @return metric value
	 */
	public MetricValue getRootMetricValue(BaseMetric metric);
}
