package edu.rice.cs.hpcdata.experiment.extdata;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricRaw;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


public abstract class AbstractThreadDataCollection implements
		IThreadDataCollection {


	@Override
	public double getMetric(Scope scope, BaseMetric metric, IdTuple idtuple, int numMetrics) throws IOException {
		int metricIndex = getMetricIndex(scope, metric);
		return getMetric(scope.getCCTIndex(), metricIndex, idtuple, numMetrics);
	}

	@Override
	public double[] getMetrics(Scope scope, BaseMetric metric, int numMetrics) throws Exception {
		int metricIndex = getMetricIndex(scope, metric);
		return getMetrics(scope.getCCTIndex(), metricIndex, numMetrics);
	}

	private int getMetricIndex(Scope scope, BaseMetric metric) {
		int metricIndex = metric.getIndex();
		if ( scope instanceof RootScope  && 
			 metric instanceof MetricRaw && 
			 metric.getMetricType().isExclusive()) {
			
			var partner = ((MetricRaw) metric).getMetricPartner();
			if (partner != null)
				metricIndex = partner.getRawID();
		}
		return metricIndex;
	}
	
	public double[] getEvenlySparseRankLabels() throws IOException {
		double []values = getRankLabels();
		int parallelism = getParallelismLevel();
		
		// we only spread the values if the parallelism is more than 1 
		
		if (parallelism>1) {
			int i = 0;
			while(i<values.length) {
				int numSiblings = 0;
				int rankFirst 	= (int) Math.floor(values[i]);
				
				int j = i+1;
				for(; j<values.length; j++) {
					int nextRank =  (int) Math.floor(values[j]);
					numSiblings++;
										
					if (nextRank > rankFirst) {
						break;
					} else if (j==values.length-1) {
						numSiblings++;
					}
				}
				for (int k=0; k<numSiblings; k++) {
					values[i+k] = rankFirst + ((double)k/numSiblings);
				}
				i = j;
			}
		}
		return values;
	}

	
	@Override
	public List<IdTuple> getIdTupleListWithoutGPU(IdTupleType idtype) {
		var idtuples = getIdTuples();			

		return idtuples.stream()
	 			 	   .filter(idt -> !idt.isGPU(idtype))
	 			 	   .collect(Collectors.toList());
	}
	
	
	@Override
	public Object[] getIdTupleLabelWithoutGPU(IdTupleType idtype) {
		var idtuples = getIdTupleListWithoutGPU(idtype);
		
		return idtuples.stream()
					   .map(idt -> idt.toString(idtype))
					   .toArray();
	}
} 
