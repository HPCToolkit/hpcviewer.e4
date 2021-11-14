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
