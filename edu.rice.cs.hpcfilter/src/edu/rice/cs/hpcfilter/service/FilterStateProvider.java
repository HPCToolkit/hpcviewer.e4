package edu.rice.cs.hpcfilter.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;

/**************************************************************
 * 
 * Class to manage filter state, whether it needs to be refreshed
 * or there's a filter selection in the view
 *
 **************************************************************/

@Creatable
@Singleton
public class FilterStateProvider 
{
	final static public String FILTER_REFRESH_PROVIDER = "hpcfilter/update";
	final static public String FILTER_ENABLE_PROVIDER  = "hpcfilter/enable";
	
	@Inject IEventBroker eventBroker;
	
	
	public String getTopic() {
		return FILTER_REFRESH_PROVIDER;
	}
	
	
	/*****
	 * refresh the table as the filter pattern may change
	 * Usually called by FilterAdd and FilterDelete 
	 */
	public void broadcast(FilterMap filterMap)
	{
		if (eventBroker != null)
			eventBroker.post(FILTER_REFRESH_PROVIDER, filterMap);
	}
	
	
	/***
	 * filter an experiment database
	 * 
	 * @param experiment
	 * @return Experiment itself (if changed)
	 */
	static public Experiment filterExperiment(Experiment experiment) {
		// filter the experiment if it is not null and it is in original form
		// (it isn't a merged database)
		if (experiment != null && !experiment.isMergedDatabase()) 
		{
			long t0 = System.currentTimeMillis();

			try {
				// ---------------------------------------
				// conserve the added metrics
				// ---------------------------------------
				List<BaseMetric> metrics = new ArrayList<BaseMetric>(experiment.getMetricCount());

				for (BaseMetric metric : experiment.getMetricList()) {
					if (metric instanceof DerivedMetric && 
						metric.getMetricType()==MetricType.UNKNOWN) {
						
						// only add user derived metrics, not all derived metrics
						//  provided by hpcprof
						
						metrics.add((DerivedMetric) metric);
					} else {
						metrics.add(metric.duplicate());
					}
				}
				// ---------------------------------------
				// filtering 
				// ---------------------------------------
				experiment.reopen();
				experiment.filter(FilterMap.getInstance());
				
				// ---------------------------------------
				// put the original metrics and derived metrics back
				// ---------------------------------------
				experiment.setMetrics(metrics);
				experiment.resetThreadData();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			long t1 = System.currentTimeMillis();
			LoggerFactory.getLogger(FilterStateProvider.class).debug("Time to filter: " + (t1-t0) + " ms");
		}
		return experiment;
	}
}
