package edu.rice.cs.hpcdata.experiment.metric;

import java.util.List;

import ca.odell.glazedlists.event.ListEventListener;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

/******************************************************************
 * 
 * Interface to handle metric descriptors
 *
 ******************************************************************/
public interface IMetricManager 
{
	/****
	 * Return the unique ID of this metric manager (or experiment database).
	 * Usually it's the database directory.
	 * <p>The goal is to differentiate between different databases.
	 * 
	 * @return String
	 */
	public String getID();
	
	/***
	 * Get the correlated raw metric version of the given base metric.
	 * <br/>
	 * Database version 2.x or older has no correlation between the base
	 * metric and the raw metric. This method will return if the base 
	 * metric has its correspondent raw metric.
	 * 
	 * @param metric
	 * 			The base metric
	 * @return {@code MetricRae}
	 * 			The correspondent raw metric
	 */
	public BaseMetric getCorrespondentMetricRaw(BaseMetric metric);
	
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

	public List<Integer> getNonEmptyMetricIDs(Scope scope);
	
	/****
	 * get the list of "raw" metrics used for plotting graphs
	 * @return
	 */
	public List<BaseMetric> getRawMetrics();
	
	/****
	 * Add a new user derived metrics
	 * @param objMetric {@code DerivedMetric} a new metric
	 */
	public void addDerivedMetric(DerivedMetric objMetric);
	
	/****
	 * Add a listener for any change in list of metrics
	 * @param listener
	 */
	public void addMetricListener(ListEventListener<BaseMetric> listener);
	
	
	/****
	 * Remove the existing listener
	 * @param listener
	 */
	public void removeMetricListener(ListEventListener<BaseMetric> listener);
}
