package edu.rice.cs.hpc.data.experiment.metric;

import java.util.Map;

import edu.rice.cs.hpc.data.experiment.BaseExperimentWithMetrics;

public interface IMetricMutable 
{
	public void renameExpression(Map<Integer, Integer> mapOldIndex, Map<Integer, Integer> mapOldOrder);
	public void setExperiment(BaseExperimentWithMetrics experiment);
}
