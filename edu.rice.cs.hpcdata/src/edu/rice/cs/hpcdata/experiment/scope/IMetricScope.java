package edu.rice.cs.hpcdata.experiment.scope;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;

/***********************************************************
 * 
 * A generic interface to access Metrics 
 *
 ***********************************************************/
public interface IMetricScope 
{
	/**
	 * Returns the "raw" value of a given metric at this scope.
	 * This method doesn't execute finalization phase of the metric,
	 * it just returns whatever the metric in the scope.
	 * 
	 * @param index 
	 * 			the index of the metric
	 * @return {@code MetricValue}
	 * 			The "raw" metric value without finalization.
	 */
	public MetricValue getDirectMetricValue(int index);
	
	/***
	 * Returns the value of a given metric at this scope.<br/>
	 * overload the method to take-in the index ---FMZ
	 * @param metric
	 * 			the metric
	 * @return {@code MetricValue}
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
	 * get the root scope of this scope
	 * 
	 * @return RootScope
	 */
	RootScope getRootScope();
}
