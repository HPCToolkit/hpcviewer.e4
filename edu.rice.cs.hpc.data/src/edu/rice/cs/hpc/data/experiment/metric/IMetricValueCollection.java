package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.scope.Scope;

/***********************************************************
 * 
 * The root interface to manage a collection of metric values.
 * The implementation can use either an array or a sparse 
 * array for storing a set of metric values.
 *
 ***********************************************************/
public interface IMetricValueCollection 
{
	/****
	 * get a metric value of a given metric index. To keep compatibility, this index
	 * is not the ID of the metric, but the index of the dense metrics.
	 * The derived class needs to covert from metric index to ID manually.
	 * @param index of the metric (not the metric ID)
	 * @return
	 */
	public MetricValue getValue(Scope scope, int index);
	
	/***
	 * get the annotation of a given metric index
	 * @param index
	 * @return
	 */
	public float getAnnotation(int index);
	
	/****
	 * set a metric value to a certain index
	 * 
	 * @param index
	 * @param value
	 */
	public void setValue(int index, MetricValue value);
	
	/*****
	 * add an additional annotation to the metric value
	 * @param index
	 * @param ann
	 */
	public void setAnnotation(int index, float ann);
	
	/***
	 * return if the current scope has at least a metric value
	 * @return true if a metric value exists, false otherwise.
	 */
	public boolean hasMetrics(Scope scope);
	
	/*****
	 * get the size of metric values
	 * @return
	 */
	public int size();
	
	/*****
	 * dispose the allocated resources
	 */
	public void dispose();
}
