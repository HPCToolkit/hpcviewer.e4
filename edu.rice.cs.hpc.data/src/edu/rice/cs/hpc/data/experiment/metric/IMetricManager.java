package edu.rice.cs.hpc.data.experiment.metric;

import java.util.List;

/******************************************************************
 * 
 * Interface to handle metric descriptors
 *
 ******************************************************************/
public interface IMetricManager 
{
	/***
	 * Returns a metric descriptor given a metric id
	 * @param ID String metric id
	 * @return {@code BaseMetric}
	 */
	public BaseMetric   getMetric(String ID);
	
	/***
	 * Returns a metric descriptor given a metric index
	 * @param index int metric index
	 * @return {@code BaseMetric}
	 */
	public BaseMetric 	getMetric(int index);
	
	/***
	 * Returns a metric descriptor given a metric "order". 
	 * The order has to be the original order from hpcrun. 
	 * @param order int metric order
	 * @return {@code BaseMetric}
	 */
	public BaseMetric   getMetricFromOrder(int order);
	
	/***
	 * Returns the number of metric descriptors
	 * @return int
	 */
	public int 		    getMetricCount();
	
	/** get all metrics associated with this database*/
	public List<BaseMetric> getMetricList();
	
	/** get metrics that are NOT invisible (can be hidden, but not invisible) */
	public List<BaseMetric> getVisibleMetrics();

	/****
	 * Add a new user derived metrics
	 * @param objMetric {@code DerivedMetric} a new metric
	 */
	public void addDerivedMetric(DerivedMetric objMetric);
}
