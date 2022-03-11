package edu.rice.cs.hpcdata.experiment.metric;

import java.io.IOException;

import org.eclipse.collections.api.map.primitive.IntObjectMap;

import edu.rice.cs.hpcdata.experiment.scope.Scope;

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
	 * copy metric values from a source {@code IMetricValueCollection} to this collection
	 * @param mvCollection 
	 * 			{@code IMetricValueCollection} source
	 * @param offset
	 * 			Starting index number 
	 */
	public void appendMetrics(IMetricValueCollection mvCollection, int offset);
	
	/****
	 * get a metric value of a given metric index. To keep compatibility, this index
	 * is not the ID of the metric, but the index of the dense metrics.
	 * The derived class needs to covert from metric index to ID manually.
	 * @param index of the metric (not the metric ID)
	 * @return
	 */
	public MetricValue getValue(Scope scope, int index);
	
	/****
	 * Get a metric value for a given metric object.
	 * 
	 * @param scope
	 * @param metric
	 * @return
	 */
	public MetricValue getValue(Scope scope, BaseMetric metric);
	
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
	 * get the annotation of a given metric index
	 * @param index
	 * @return
	 */
	public float getAnnotation(int index);

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
	
	public IntObjectMap<MetricValue> getValues();
	
	public IMetricValueCollection duplicate() throws IOException;
}
