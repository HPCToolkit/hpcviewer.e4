package edu.rice.cs.hpc.data.db.version3;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricValueCollection;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.metric.MetricValueSparse;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;


/******************************************************************
 * 
 * The implementation of {@link IMetricValueCollection} for 
 * database version 3 (the compact version).
 * 
 * This class is designed to read metric values when needed.
 * If the scope is never asked for metric value, no access to
 * the database file will occur.
 * 
 * The current version is a draft implementation, it is not well
 * optimized yet. 
 *
 ******************************************************************/
public class MetricValueCollection3 implements IMetricValueCollection 
{
	private DataSummary dataSummary;

	private HashMap<Integer, MetricValue> values;
	
	public MetricValueCollection3(DataSummary dataSummary, Scope scope) throws IOException
	{
		this.dataSummary = dataSummary;
	}
	
	@Override
	public MetricValue getValue(Scope scope, int index) 
	{
		if (values == null)
		{
			List<MetricValueSparse> sparseValues;
			
			// read the sparse metrics from the file (thread.db)
			// if the read is successful, we cache all the metrics into values
			// so that the next time we ask for another metric from the same scope,
			//  just need to look at the cache, instead of reading the file again.
			
			try {
				sparseValues = dataSummary.getMetrics(scope.getCCTIndex());
			} catch (IOException e1) {
				e1.printStackTrace();
				return MetricValue.NONE;
			}
			// the reading is successful
			// fill up the cache containing metrics of this scope for the next usage
			
			if (sparseValues != null && sparseValues.size()>0)
			{
				values = new HashMap<Integer, MetricValue>();
				
				for (MetricValueSparse mvs: sparseValues) {
					float value = mvs.getValue();
					float annotationValue = 1.0f;
					
					// compute the percent annotation
					if (!(scope instanceof RootScope)) {
						RootScope root = scope.getRootScope();
						MetricValue mv = root.getMetricValue(mvs.getIndex());
						if (mv != MetricValue.NONE)
						{
							annotationValue = value/mv.getValue();
						}
					}
					MetricValue mv = new MetricValue(value, annotationValue);
					values.put(mvs.getIndex(), mv);
				}
				
				MetricValue mv = values.get(index);
				if (mv != null)
					return mv;
			}

		} else 
		{
			// metric values already exist
			MetricValue mv = values.get(index);
			if (mv != null) {
				return mv;
			} else {
				// the cache of metric values already exist, but we cannot find the value of this metric
				// either the value is empty, or it's a derived metric which have to be computed here

				RootScope root = scope.getRootScope();
				Experiment experiment = (Experiment) root.getExperiment();
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
		MetricValue mv = values.get(index);
		if (mv != null)
			return MetricValue.getAnnotationValue(mv);
		
		return 0.0f;
	}

	@Override
	public void setValue(int index, MetricValue value) {
		if (values != null) {
			// If the index is out of array bound, it means we want to add a new derived metric.
			// We will compute the derived value on the fly instead of storing it.

			values.put(index, value);
		}
	}

	@Override
	public void setAnnotation(int index, float ann) {
		MetricValue value = values.get(index);
		if (value != null)
			MetricValue.setAnnotationValue(value, ann);
	}


	@Override
	public int size() {
		return values.size();
	}

	@Override
	public void dispose() {
		// old jvm may need to clear the variables to perform GC
		// we don't need this for newer jvm (AFAIK).
		
		values.clear();
		values = null;
	}

	@Override
	public boolean hasMetrics(Scope scope) {
		// trigger initialization
		Experiment exp = (Experiment) scope.getRootScope().getExperiment();
		List<BaseMetric> list = exp.getMetricList();
	
		// TODO: hack -- grab the first metric for initialization purpose
		// just to make sure we already initialized :-(
		
		if (values == null) {
			getValue(scope, list.get(0).getIndex());
		}

		if (values != null)
		{
			return values.size()>0;
		}
		return false;
	}

	
	/***
	 * Retrieve the object to access to thread-major sparse database
	 * @return
	 */
	public DataSummary getDataSummary() {
		return dataSummary;
	}

	@Override
	public void appendMetrics(IMetricValueCollection mvCollection, int offset) {
		AbstractMap<Integer, MetricValue> source = mvCollection.getValues();
		if (values == null) {
			values = new HashMap<Integer, MetricValue>(source.size());
		}
		// tricky part: append the metric values and shift the index by an offset
		
		AbstractMap<Integer, MetricValue> mapSource = mvCollection.getValues();
		mapSource.forEach( (index, mv) -> {
			values.put(index + offset, mv);
		});
	}

	@Override
	public AbstractMap<Integer, MetricValue> getValues() {
		return values;
	}
}
