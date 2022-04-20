package edu.rice.cs.hpcdata.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import edu.rice.cs.hpcdata.experiment.metric.AbstractMetricWithFormula;
import edu.rice.cs.hpcdata.experiment.metric.AggregateMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.FinalMetric;
import edu.rice.cs.hpcdata.experiment.metric.HierarchicalMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.Metric;
import edu.rice.cs.hpcdata.experiment.metric.MetricComparator;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.VisibilityType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.filters.MetricValuePropagationFilter;
import edu.rice.cs.hpcdata.util.Constants;


/****************************************************************************
 * 
 * abstract base experiment that contains metrics. <br/>
 * This class just load metrics without generating callers view and flat view
 *
 ****************************************************************************/
public abstract class BaseExperimentWithMetrics extends BaseExperiment 
implements IMetricManager, ListEventListener<BaseMetric>
{
	
	/** A list of metric descriptor   
	 ** the list is sorted based on the metric ID 
	 **/
	private EventList<BaseMetric> metrics;
	
	/** A list of metric descriptors sorted based on the hpcrun order
	 ** 
	 */
	private Map<Integer, BaseMetric> metricsWithOrder;
	
	/**
	 * map ID to the metric descriptor
	 */
	//private Map<Integer, BaseMetric> mapIndexToMetric;	
	private MutableIntObjectMap<BaseMetric> mapIndexToMetric;

	
	//////////////////////////////////////////////////////////////////////////
	//ACCESS TO METRICS													    //
	//////////////////////////////////////////////////////////////////////////


	/*****
	 * Set the list of metric descriptors. Ideally this method has to be called
	 * at most once for every creation of experiment
	 * 
	 * @param metricList : a list of metric descriptors
	 */
	public void setMetrics(List<BaseMetric> metricList) {
		metrics =  GlazedLists.eventList(metricList);
		mapIndexToMetric = new IntObjectHashMap<>(metricList.size());// HashMap<Integer, BaseMetric>(metricList.size());
		metricsWithOrder = new HashMap<>(metricList.size()/2);
		
		// the simple index has to start from 1 since the zero index
		// is reserved for tree column
		
		for(BaseMetric metric:metrics) {
			assert mapIndexToMetric.get(metric.getIndex()) == null : 
				   "Duplicate metric-id " + metric.getIndex();

			if (metric.getOrder() >= 0) {
				metricsWithOrder.put(metric.getOrder(), metric);
			}
			var m = mapIndexToMetric.put(metric.getIndex(), metric);
			if (m != null)
				throw new RuntimeException("Non-unique index metric " + metric.getDisplayName() + " with index: " + metric.getIndex());
		}
		
		if (getMajorVersion() >= Constants.EXPERIMENT_SPARSE_VERSION) {
			// reorder the metric since hpcprof2 will output not in order fashion
			Collections.sort(metrics, new MetricComparator());
		}
		metrics.addListEventListener(this);
	}

	
	protected void copyMetric(Scope target, Scope source, int src_i, int targ_i, MetricValuePropagationFilter filter) {
		if (filter.doPropagation(source, target, src_i, targ_i)) {
			MetricValue mv = source.getMetricValue(src_i);
			if (mv != MetricValue.NONE && Float.compare(MetricValue.getValue(mv), 0.0f)!=0) {
				target.setMetricValue(targ_i, mv);
			}
		}
	}


	
	protected void copyMetricsToPartner(Scope scope, MetricType sourceType, MetricValuePropagationFilter filter) {
		ArrayList<BaseMetric> listDerivedMetrics = new ArrayList<>();
		
		for(BaseMetric metric: metrics) {
			if (metric instanceof Metric || metric instanceof HierarchicalMetric) {
				if (metric.getMetricType() == sourceType) {
					// get the partner index (if the metric exclusive, its partner is inclusive)
					
					int partner 			 = metric.getPartner(); 	 // get the partner ID
					BaseMetric partnerMetric = getMetric(partner);   // get the partner metric
					int partnerIndex		 = partnerMetric.getIndex(); // get the index of partner metric
					
					copyMetric(scope, scope, metric.getIndex(), partnerIndex, filter);
				}
			} else if (metric instanceof AggregateMetric) {
				if (metric.getMetricType() == MetricType.EXCLUSIVE ) {
					int partner = ((AggregateMetric)metric).getPartner();
					String partner_id = String.valueOf(partner);
					BaseMetric partner_metric = getMetric( partner_id );

					// case for old database: no partner information
					if (partner_metric != null) {
						MetricValue partner_value = scope.getMetricValue( partner );
						scope.setMetricValue(metric.getIndex(), partner_value);
					}
				}
			} else if (metric instanceof DerivedMetric) {
				listDerivedMetrics.add(metric);
			}
		}

		// compute the root value of derived metric at the end
		// some times, hpcrun derived metrics require the value of "future" metrics. 
		// This causes the value of derived metrics to be empty.
		// If we compute derived metrics at the end, we are more guaranteed that the value
		// is not empty.
		// FIXME: unless a derived metric requires a value of "future" derived metric. 
		//        In this case, we are doomed.
		
		for (BaseMetric metric: listDerivedMetrics) {
			// compute the metric value
			MetricValue mv = metric.getValue(scope);
			scope.setMetricValue(metric.getIndex(), mv);
		}
	}

	
	/****
	 * Hide empty metrics, but still visible for users.
	 * Should we remove instead?
	 * 
	 * @param root
	 */
	protected void hideEmptyMetrics(Scope root) {		
		// hide columns if the metric is to be shown but has no value
		for(BaseMetric metric: metrics) {
			if (metric.getVisibility() == VisibilityType.SHOW &&
				root.getMetricValue(metric) == MetricValue.NONE) {				
				metric.setDisplayed(BaseMetric.VisibilityType.HIDE);
			}
		}
	}
	
	/**
	 * Check if an inclusive computation is needed or not
	 * <br/>we need to compute inclusive metrics if the metric is a raw metric (or its kinds)
	 * @return true if inclusive computation is needed
	 */
	protected boolean inclusiveNeeded() {		
		for (BaseMetric m: metrics) {
			boolean isNeeded = !(   (m instanceof FinalMetric) 
					     		 || (m instanceof AbstractMetricWithFormula));
			if (isNeeded)
				return true;
		}
		return false;
	}


	@Override
	public void listChanged(ListEvent<BaseMetric> listChanges) {
		while(listChanges.next()) {
			if (listChanges.getType() == ListEvent.INSERT) {
				int index = listChanges.getIndex();
				BaseMetric metric = metrics.get(index);
				this.mapIndexToMetric.put(metric.getIndex(), metric);
			}
		}
	}
	
	
	@Override
	public void addMetricListener(ListEventListener<BaseMetric> listener) {
		metrics.addListEventListener(listener);
	}
	
	
	@Override
	public void removeMetricListener(ListEventListener<BaseMetric> listener) {
		metrics.removeListEventListener(listener);
	}
	

	/***
	 * Retrieve the list of metrics.
	 * @apiNote Do not call this method if you don't have to! 
	 * 		Please use {@link getMetricCount} and
	 * 				   {@link getMetric} methods instead.
	 * @return {@code List<BaseMetric>}
	 */
	@Override
	public List<BaseMetric> getMetrics() 
	{
		return metrics;
	}
	
	
	/*****
	 * Return the list of "visible" metrics. <br/>
	 * Note: visible doesn't mean "SHOW". 
	 * Visible here means that the metrics can be displayed on metric property window
	 * or on list of metrics. 
	 * Perhaps we should change the term to be "displayable" metrics.
	 */
	@Override
	public List<BaseMetric> getVisibleMetrics() 
	{
		ArrayList<BaseMetric> listMetrics = new ArrayList<>(metrics.size());
		
		for(BaseMetric metric : metrics) {
			switch (metric.getVisibility()) {
			
			case SHOW:
			case HIDE:
				listMetrics.add(metric);
				break;
				
			case SHOW_INCLUSIVE:
				if (metric.getMetricType() == MetricType.INCLUSIVE)
					listMetrics.add(metric);
				break;
				
			case SHOW_EXCLUSIVE:
				if (metric.getMetricType() == MetricType.EXCLUSIVE)
					listMetrics.add(metric);
				break;
			
			default:
				break;
			}
		}
		return listMetrics;
	}

	

	@Override
	public List<Integer> getNonEmptyMetricIDs(Scope scope) {
		List<BaseMetric> list = getVisibleMetrics();
		List<Integer> listIDs = new ArrayList<>(list.size());
		
		// a non-efficient way to collect metric index into a list
		// we should use Java's stream, but not sure how to do that
		// any volunteer?
		for(int i=0; i<list.size(); i++) {
			BaseMetric m = list.get(i);
			if (scope.getMetricValue(m) != MetricValue.NONE)
				listIDs.add(m.getIndex());
		}
		return listIDs;
	}

	
	/*************************************************************************
	 *	Returns the number of metrics in the experiment.
	 ************************************************************************/
	public int getMetricCount()
	{
		return this.metrics.size();
	}

	/*************************************************************************
	 *	Returns the metric with a given index.
	 ************************************************************************/
	public BaseMetric getMetric(int index)
	{	
		BaseMetric metric = mapIndexToMetric.get(index);
		if (metric != null)
			return metric;

		// not found
		throw new RuntimeException("Invalid metric index: " + index);
	}
	
	/*************************************************************************
	 *	Returns the metric with a given internal name.
	 *   
	 *  @param shortName the short name (ID) of the metric 
	 ************************************************************************/
	public BaseMetric getMetric(String shortName)
	{
		return this.getMetric(Integer.valueOf(shortName));	
	}
	
	@Override
	public BaseMetric getMetricFromOrder(int order)
	{
		return metricsWithOrder.get(order);
	}
	
	
	
	//////////////////////////////////////////////////////////////////////////
	//Compute Derived Metrics												//
	//////////////////////////////////////////////////////////////////////////

	/**
	 * Create a derived metric based on formula expression
	 * @param objMetric : a new derived metric
	 * @return 
	 */
	@Override
	public void addDerivedMetric(DerivedMetric objMetric) {

		metrics.add(0, objMetric);
		mapIndexToMetric.put(objMetric.getIndex(), objMetric);
	}



	//////////////////////////////////////////////////////////////////////////
	//ACCESS TO SEARCH PATH												    //
	//////////////////////////////////////////////////////////////////////////

	/*************************************************************************
	 *	Returns the number of search paths in the experiment.
	 ************************************************************************/
	public int getSearchPathCount()
	{
		return this.configuration.getSearchPathCount();
	}

	/*************************************************************************
	 *	Returns the search path with a given index.
	 ************************************************************************/

	public File getSearchPath(int index)
	{
		return this.configuration.getSearchPath(index);
	}
}
