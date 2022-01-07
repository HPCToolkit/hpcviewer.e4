package edu.rice.cs.hpcdata.merge;

import java.util.List;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;

/********************************************************************
 * 
 * Record containing two databases to be merged and its metrics to compare.
 * If the metrics are not defined, it's recommended to find the metrics
 * via {@link getMetricsToCompare}
 *
 ********************************************************************/
public class DatabasesToMerge 
{
	public Experiment[] experiment;
	public BaseMetric[] metric;
	public RootScopeType type;
	
	/****
	 * Create automatically arrays of experiment and metric.
	 * The caller is responsible to initialize them.
	 */
	public DatabasesToMerge() {
		experiment = new Experiment[2];
		metric = new BaseMetric[2];
	}
	
	
	/****
	 * Search for metrics to be compared. 
	 * <p>Ideally the metrics are inclusive metrics and they have the same name.
	 * For instance, we want to compare Cycles with Cycles. We don't want to 
	 * compare Cycles with Instructions.  
	 * </p>
	 * @param experiments
	 * @return An array of two elements of metrics
	 */
	public static BaseMetric[] getMetricsToCompare(Experiment[] experiments) {
		
		List<BaseMetric> metrics1 = experiments[0].getVisibleMetrics();
		List<BaseMetric> metrics2 = experiments[1].getVisibleMetrics();
		
		BaseMetric []metrics = new BaseMetric[2];
		
		for(BaseMetric m1: metrics1) {
			if (m1.getMetricType() == MetricType.INCLUSIVE) {
				for(BaseMetric m2: metrics2) {
					if (m1.getDisplayName().equals(m2.getDisplayName())) {
						metrics[0] = m1;
						metrics[1] = m2;
						
						return metrics;
					}
				}
			}
		}
		// no metric matches by name. 
		// let's use the first metric. Hopefully it works :-(
		
		BaseMetric metricSourceCompare = metrics1.get(0);
		for (BaseMetric m: metrics1) {
			if (m.getMetricType() == MetricType.INCLUSIVE) {
				metricSourceCompare = m;
				break;
			}
		}
		
		BaseMetric metricTargetCompare = metrics2.get(0);
		for (BaseMetric m: metrics2) {
			if (m.getMetricType() == MetricType.INCLUSIVE) {
				metricTargetCompare = m;
				break;
			}
		}
		metrics[0] = metricTargetCompare;
		metrics[1] = metricSourceCompare;
		
		return metrics;
	}
}
