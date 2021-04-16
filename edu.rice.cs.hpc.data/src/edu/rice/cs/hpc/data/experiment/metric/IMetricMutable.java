package edu.rice.cs.hpc.data.experiment.metric;

import java.util.Map;

public interface IMetricMutable 
{
	public void renameExpression(Map<Integer, Integer> mapOldIndex);
}
