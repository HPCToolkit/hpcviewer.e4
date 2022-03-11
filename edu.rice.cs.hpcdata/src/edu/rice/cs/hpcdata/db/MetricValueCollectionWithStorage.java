package edu.rice.cs.hpcdata.db;

import java.io.IOException;

import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricValueCollection;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


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
	private IntObjectHashMap<MetricValue> values;
	
	
	/***
	 * Constructor of the class. No parameter is needed.
	 */
	public MetricValueCollectionWithStorage() {
		values = new IntObjectHashMap<MetricValue>(2); //new HashMap<Integer, MetricValue>(2);
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
	public MetricValue getValue(Scope scope, BaseMetric metric) {
		MetricValue mv = values.get(metric.getIndex());
		if (mv == null) {
			if (metric instanceof DerivedMetric)
				mv = ((DerivedMetric) metric).getValue(scope);
			else
				mv = MetricValue.NONE;
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
		IntObjectMap<MetricValue> source = mvCollection.getValues();
		if (values == null) {
			values = new IntObjectHashMap<>(source.size()); //new HashMap<Integer, MetricValue>(source.size());
		}
		// tricky part: append the metric values and shift the index by an offset
		
		if (source == null ) {
			return;
		}
		source.forEachKeyValue( (index, mv) -> {
			values.put(index + offset, mv);
		});
	}

	@Override
	public IntObjectMap<MetricValue> getValues() {
		return values;
	}


	@Override
	public IMetricValueCollection duplicate() throws IOException {
		return new MetricValueCollectionWithStorage();
	}

}
