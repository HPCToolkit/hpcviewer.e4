package edu.rice.cs.hpc.data.experiment.metric.version3;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import edu.rice.cs.hpc.data.db.DataSummary;
import edu.rice.cs.hpc.data.experiment.BaseExperimentWithMetrics;
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
	final private RootScope root;
	
	private Scope currentScope;
	private List<MetricValueSparse> sparseValues;
	private HashMap<Integer, MetricValue> values;
	
	public MetricValueCollection3(RootScope root, Scope scope) throws IOException
	{
		this.root	 = root;
		this.currentScope = scope;
	}
	
	@Override
	public MetricValue getValue(Scope scope, int index) 
	{
		if (values == null || currentScope != scope)
		{
			currentScope = scope;
			
			// create and initialize the first metric values instance
			final DataSummary data = root.getDataSummary();
			// initialize
			try {
				sparseValues = data.getMetrics(scope.getCCTIndex());
			} catch (IOException e1) {
				e1.printStackTrace();
				return MetricValue.NONE;
			}
			if (sparseValues != null && sparseValues.size()>0)
			{
				values = new HashMap<Integer, MetricValue>();
				
				for (MetricValueSparse mvs: sparseValues) {
					float value = mvs.getValue();
					float annotationValue = 1.0f;
					
					// compute the percent annotation
					if (!(scope instanceof RootScope)) {
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
				// metric values already exist, but the index is bigger than the standard values
				// this must be a derived metric
				BaseExperimentWithMetrics exp = (BaseExperimentWithMetrics) root.getExperiment();
				BaseMetric metric = exp.getMetric(index);
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
		BaseExperimentWithMetrics exp = (BaseExperimentWithMetrics) root.getExperiment();
		return exp.getMetricCount();
	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean hasMetrics(Scope scope) {
		// trigger initialization
		getValue(scope, 0);
		if (values != null)
		{
			return values.size()>0;
		}
		return false;
	}

}
