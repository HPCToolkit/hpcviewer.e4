package edu.rice.cs.hpcdata.merge;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;

public class DatabasesToMerge 
{
	public Experiment[] experiment;
	public BaseMetric[] metric;
	
	
	public DatabasesToMerge() {
		experiment = new Experiment[2];
		metric = new BaseMetric[2];
	}
}
