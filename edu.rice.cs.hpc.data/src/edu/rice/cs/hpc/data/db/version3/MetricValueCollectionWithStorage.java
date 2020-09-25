package edu.rice.cs.hpc.data.db.version3;

import java.util.AbstractMap;
import java.util.HashMap;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricValueCollection;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class MetricValueCollectionWithStorage implements IMetricValueCollection 
{
	private final float VALUE_ZERO = 0.0f;
	
	private AbstractMap<Integer, MetricValue> values;
	
	
	public MetricValueCollectionWithStorage() {
		values = new HashMap<Integer, MetricValue>();
	}
	
	@Override
	public MetricValue getValue(Scope scope, int index) {
		MetricValue mv = values.get(index);
		
		if (mv == null) {
			// the cache of metric values already exist, but we cannot find the value of this metric
			// either the value is empty, or it's a derived metric which have to be computed here

			RootScope root = scope.getRootScope();
			Experiment experiment = (Experiment) root.getExperiment();
			BaseMetric metric = experiment.getMetric(index);
			
			if (metric instanceof DerivedMetric)
			{
				mv = ((DerivedMetric)metric).getValue(scope);
			} else {
				mv = MetricValue.NONE;
			}
		}
		
		return mv;
	}

	@Override
	public float getAnnotation(int index) {
		MetricValue mv = values.get(index);
		
		if (mv == null) {
			return VALUE_ZERO;
		}
		
		return mv.getAnnotationValue();
	}

	@Override
	public void setValue(int index, MetricValue value) {
		MetricValue mv = values.get(index);
		
		if (mv != null && value != MetricValue.NONE) {
			mv.setValue(value.getValue());
			mv.setAnnotationValue(value.getAnnotationValue());
		} else {
			values.put(index, value);
		}
	}

	@Override
	public void setAnnotation(int index, float ann) {
		MetricValue mv = values.get(index);
		if (mv != null) {
			mv.setAnnotationValue(ann);
		} else {
			throw new RuntimeException("Metric index unknown: " + index);
		}
	}

	@Override
	public boolean hasMetrics(Scope scope) {
		
		return values != null && values.size()>0;
	}

	@Override
	public int size() {

		return values.size();
	}

	@Override
	public void dispose() {
		values = null;
	}

}
