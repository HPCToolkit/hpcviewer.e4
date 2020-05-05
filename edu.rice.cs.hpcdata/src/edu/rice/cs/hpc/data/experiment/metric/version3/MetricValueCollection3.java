package edu.rice.cs.hpc.data.experiment.metric.version3;

import java.io.IOException;

import edu.rice.cs.hpc.data.db.DataSummary;
import edu.rice.cs.hpc.data.experiment.BaseExperimentWithMetrics;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricValueCollection;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
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
	private MetricValue []values;
	
	public MetricValueCollection3(RootScope root, Scope scope) throws IOException
	{
		this.root	 = root;
	}
	
	@Override
	public MetricValue getValue(Scope scope, int index) 
	{
		if (values == null)
		{
			// create and initialize the first metric values instance
			BaseExperimentWithMetrics exp = (BaseExperimentWithMetrics) root.getExperiment();
			int metric_size = exp.getMetricCount();
			final DataSummary data = root.getDataSummary();
			// initialize
			try {
				values = data.getMetrics(scope.getCCTIndex(), exp);
				if (values != null && values.length>0)
				{
					// compute the percent annotation
					for(int i=0; i<metric_size; i++)
					{
						if (values[i] != MetricValue.NONE)
						{
							float annotationValue = 1.0f;
							if (!(scope instanceof RootScope)) {
								MetricValue mv = root.getMetricValue(i);
								if (mv != MetricValue.NONE)
								{
									annotationValue = values[i].getValue()/mv.getValue();
								}
							}
							MetricValue.setAnnotationValue(values[i], annotationValue);
						}
					}
					return values[index];
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else 
		{
			// metric values already exist
			if (index < values.length) {
				return values[index];
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
		MetricValue mv = values[index];
		return MetricValue.getAnnotationValue(mv);
	}

	@Override
	public void setValue(int index, MetricValue value) {
		if (values != null) {
			// If the index is out of array bound, it means we want to add a new derived metric.
			// We will compute the derived value on the fly instead of storing it.
			if (index < values.length) {
				values[index]  = value;
			}
		}
	}

	@Override
	public void setAnnotation(int index, float ann) {
		MetricValue value = values[index];
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
			for(MetricValue value : values)
			{
				if (value != MetricValue.NONE)
					return true;
			}
		}
		return false;
	}

}
