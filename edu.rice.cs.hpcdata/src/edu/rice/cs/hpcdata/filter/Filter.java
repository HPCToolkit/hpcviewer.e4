package edu.rice.cs.hpcdata.filter;

import java.util.ArrayList;
import java.util.List;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.visitors.FilterScopeVisitor;
import edu.rice.cs.hpcdata.experiment.scope.visitors.TraceScopeVisitor;


public class Filter 
{
	
	/***
	 * filter an experiment database
	 * 
	 * @param experiment
	 * @return Experiment itself (if changed)
	 */
	public int filterExperiment(Experiment experiment, IFilterData filter) {
		int numFilteredNodes = 0;
		
		// filter the experiment if it is not null and it is in original form
		// (it isn't a merged database)
		if (experiment != null && !experiment.isMergedDatabase()) 
		{
			try {
				// ---------------------------------------
				// conserve the added metrics
				// ---------------------------------------
				List<BaseMetric> metrics = new ArrayList<>(experiment.getMetricCount());

				for (BaseMetric metric : experiment.getMetricList()) {
					if (metric instanceof DerivedMetric && 
						metric.getMetricType()==MetricType.UNKNOWN) {
						
						// only add user derived metrics, not all derived metrics
						//  provided by hpcprof
						
						metrics.add(metric);
					} else {
						metrics.add(metric.duplicate());
					}
				}
				// ---------------------------------------
				// reopening the database 
				// ---------------------------------------
				experiment.reopen();

				// ---------------------------------------
				// filtering 
				// ---------------------------------------
				numFilteredNodes = filter(experiment, filter);
				
				// ---------------------------------------
				// put the original metrics and derived metrics back
				// ---------------------------------------
				experiment.setMetrics(metrics);
				experiment.resetThreadData();
				
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		return numFilteredNodes;
	}


	/*************************************************************************
	 * Filter the cct 
	 * <p>caller needs to call post-process to ensure the callers tree and flat
	 * tree are also filtered </p>
	 * 
	 * @param filter
	 * 			a filter set 
	 * @return
	 * 			the number of filtered nodes
	 *************************************************************************/
	private int filter(Experiment experiment, IFilterData filter)
	{
		if (experiment.getRootScope() == null)
			// case of corrupt database
			return 0;
		
		final RootScope rootCCT = experiment.getRootScope(RootScopeType.CallingContextTree);

		if (experiment.getTraceDataVersion() > 0) {
			
			// needs to gather info about cct id and its depth
			// this is needed for traces
			TraceScopeVisitor visitor = new TraceScopeVisitor();
			rootCCT.dfsVisitScopeTree(visitor);
			
			experiment.setMaxDepth(visitor.getMaxDepth());
			experiment.setScopeMap(visitor.getCallPath());
		}
		// duplicate and filter the cct
		FilterScopeVisitor visitor = new FilterScopeVisitor(rootCCT, filter);
		rootCCT.dfsVisitFilterScopeTree(visitor);
		
		int filterNumScopes = 0;


		var listToRemove = visitor.getScopeToRemove();
		if (listToRemove != null && !listToRemove.isEmpty()) {
			filterNumScopes = listToRemove.size();
			listToRemove.stream().forEach( scope -> {
				scope.getParentScope().remove(scope);
				scope.dispose(); 
			});
		}
		var listTreeToRemove = visitor.getTreeToRemove();
		if (listTreeToRemove != null && !listTreeToRemove.isEmpty()) {
			filterNumScopes += listTreeToRemove.size();
			for(var tree: listTreeToRemove) {
				tree.getParentScope().remove(tree);
				tree.disposeSelfAndChildren();
			}
		}

		experiment.setMaxDepth(visitor.getMaxDepth());
		
		return filterNumScopes;
	}
}
