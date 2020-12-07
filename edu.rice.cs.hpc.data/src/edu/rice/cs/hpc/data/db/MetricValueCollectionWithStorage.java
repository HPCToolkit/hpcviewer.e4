package edu.rice.cs.hpc.data.db;

import java.util.AbstractMap;
import java.util.HashMap;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricValueCollection;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;


/*******************************************
 * 
 * Special simple class to store sparse metrics in database 4.0
 * <p>This class is designed to be used when the top-down view
 * has MetricValueCollection3 object, and the bottom-up and
 * flat views will have this class to store sparse metric values.
 *
 *******************************************/
public class MetricValueCollectionWithStorage implements IMetricValueCollection 
{
	private final float VALUE_ZERO = 0.0f;
	
	/** Sparse storage of metric values.
	 *  The key is the metric index, the value is a 
	 *  {@code MetricValue} object. Usually not {@code NONE} **/
	private AbstractMap<Integer, MetricValue> values;
	
	
	/***
	 * Constructor of the class. No parameter is needed.
	 */
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
		
		if (value == MetricValue.NONE)
			return;
		
		MetricValue mv = values.get(index);
		
		if (mv != null) {
			// replace the existing value
			
			mv.setValue(value.getValue());
			mv.setAnnotationValue(value.getAnnotationValue());
		} else {
			// add a new metric index
			values.put(index, value.duplicate());
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

	@Override
	public void appendMetrics(IMetricValueCollection mvCollection, int offset) {
		AbstractMap<Integer, MetricValue> source = mvCollection.getValues();
		if (values == null) {
			values = new HashMap<Integer, MetricValue>(source.size());
		}
		// tricky part: append the metric values and shift the index by an offset
		
		AbstractMap<Integer, MetricValue> mapSource = mvCollection.getValues();
		if (mapSource == null ) {
			return;
		}
		mapSource.forEach( (index, mv) -> {
			values.put(index + offset, mv);
		});
	}

	@Override
	public AbstractMap<Integer, MetricValue> getValues() {
		return values;
	}

}
