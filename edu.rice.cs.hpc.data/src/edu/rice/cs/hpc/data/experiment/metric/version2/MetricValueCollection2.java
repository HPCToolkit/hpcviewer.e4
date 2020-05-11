package edu.rice.cs.hpc.data.experiment.metric.version2;

import edu.rice.cs.hpc.data.experiment.BaseExperimentWithMetrics;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricValueCollection;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/*********************************************************************
 * 
 * The implementation of {@link IMetricValueCollection} for database
 * version 2 format (uncompact format)
 *
 *********************************************************************/
public class MetricValueCollection2 implements IMetricValueCollection 
{
	final private MetricValue []values;

	public MetricValueCollection2(int size) {
		values = new MetricValue[size];
	}
	
	@Override
	public MetricValue getValue(Scope scope, int index) {
		if (values != null) {
			if (index < values.length)
			{
				final MetricValue mv = values[index];
				if (mv != null) {
					return mv;
				}
			} else {
				// index out of array bound: must be a derived metric
				BaseExperimentWithMetrics experiment = (BaseExperimentWithMetrics) scope.getExperiment();
				BaseMetric metric = experiment.getMetric(index);
				if (metric instanceof DerivedMetric)
				{
					return ((DerivedMetric)metric).getValue(scope);
				}
			}
		}
		return MetricValue.NONE;
	}

	@Override
	public float getAnnotation(int index) {
		if (values[index] != null) {
			return MetricValue.getAnnotationValue(values[index]);
		}
		return 0;
	}

	@Override
	public void setValue(int index, MetricValue value) 
	{
		if (index < values.length)
		{
			values[index] = value;
		}
	}

	@Override
	public void setAnnotation(int index, float annotation) {
		if (index < values.length) {
			MetricValue.setAnnotationValue(values[index], annotation);
		}
	}


	@Override
	public int size() {
		if (values != null)
			return values.length;
		else
			return 0;
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean hasMetrics(Scope scope) {
		if (values != null)
		{
			for (MetricValue mv : values)
			{
				if (mv != MetricValue.NONE)
					return true;
			}
		}
		return false;
	}
}
